/*
 * Copyright 2012 ish group pty ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.com.ish.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class ReleasePluginTest {

  private Project project
  private Project childProject

  @Before
  public void setUp() {
    project = ProjectBuilder.builder().build()
    project.task("clean") // a clean task is needed for the plugin to work
    project.task("build") // also build

    childProject = ProjectBuilder.builder().withParent(project).build()
  }

  @Test
  public void releasePluginAddsReleaseTaskToProject() {
    initializePlugin()
    assert project.tasks.release != null
  }

  @Test
  public void releaseVersionIsValid() {
    initializePlugin()
    assert project.version == "xyz-SNAPSHOT"
  }

  @Test
  public void childReleaseVersionIsValid() {
    initializePlugin()
    assert childProject.version == "xyz-SNAPSHOT"
  }

  @Test
  public void checkSCMversion() {
    initializePlugin()
    assert project.release.scmVersion == "abc"
  }

  //verifies if the exec env is available
  @Test
  public void testExec() {
    initializePlugin()
    def stdout = new ByteArrayOutputStream()
    project.exec {
      executable = 'env'
    }
    if (stdout.toByteArray().length > 0) {
      println stdout.toString()
    }
  }

  @Test
  public void testNoTag() {
    initializePlugin()
    runAllTasksForRelease()
    assert 'xyz-RELEASE-xyz-SNAPSHOT-#0000 Release xyz-SNAPSHOT from branch xyz' == findTagInService()

  }

  @Test
  public void testTagAsString() {
    initializePlugin([scm     : "test",
                      tagName     : "release TAG"
    ])
    runAllTasksForRelease()
    assert 'release TAG-#0000 Release xyz-SNAPSHOT from branch xyz' == findTagInService()
  }

  @Test
  public void testTagAsClosure() {

    project.version = "not a real version, should get the right one from context"
    initializePlugin([scm     : "test",
                      tagName     : {branch, version ->
                        "do something with $version"
                      }
    ])

    runAllTasksForRelease()
    assert 'do something with xyz-SNAPSHOT-#0000 Release xyz-SNAPSHOT from branch xyz' == findTagInService()
  }

  private runAllTasksForRelease() {
    project.tasks.find({ it.name == 'release' }).actions.each({ it.execute(project.tasks.release) })

  }

  private findTagInService() {
    //groovy magic to get at fields within private properties
    project.plugins.getPlugin('release').properties.get('SCMService').tag

  }

  private initializePlugin(Map releaseOptions = [scm: "test"]) {
    project.allprojects {
      project.apply plugin: 'release'
      release {
        releaseOptions.each { key, value ->
          release[key] = value
        }
      }
      version = release.projectVersion
    }

  }


}