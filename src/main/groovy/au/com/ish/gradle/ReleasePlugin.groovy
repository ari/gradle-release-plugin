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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency

class ReleasePlugin implements Plugin<Project> {
  private final String TASK_RELEASE = 'release'
  private SCMService scmService
  private Project project
  def void apply(Project project) {
    this.project = project
    project.logger.info("Applying ReleasePlugin to project: " + project.name)
    ReleasePluginExtension extension = project.getExtensions().create("release", ReleasePluginExtension.class, this);
    Task releaseTask = project.task(TASK_RELEASE) {
      doFirst {
        if (project.release.failOnSnapshotDependencies) {

          def snapshotDependencies = [] as Set

          project.allprojects.each { currentProject ->
            currentProject.configurations.each { configuration ->
              project.logger.info("Checking for snapshot dependencies in $currentProject.path -> $configuration.name")
              configuration.allDependencies.each { Dependency dependency ->
                if (dependency.version?.contains('SNAPSHOT') && !(dependency instanceof ProjectDependency)) {
                  snapshotDependencies.add("${dependency.group}:${dependency.name}:${dependency.version}")
                }
              }
            }
          }

          if (!snapshotDependencies.isEmpty()) {
            throw new GradleException("Project contains SNAPSHOT dependencies: ${snapshotDependencies}")
          }
        }

        if (getSCMService().hasLocalModifications() && !extension.getAllowLocalModifications()) {
          throw new GradleException('Uncommited changes found in the source tree')
        }

        if (getSCMService().localIsAheadOfRemote()) {
          throw new GradleException('Project contains changed which are not pushed to the remote repository.');
        }

        if (!getSCMService().onTag()) {
          def msg = "#0000 Release ${project.version} from branch ${getSCMService().getBranchName()}"
          if (extension.getReleaseDryRun()) {
            project.logger.lifecycle("Scm would be tagged now, but releaseDryRun=true was specified.");
          } else {
            def tagName = extension.tagIt()
            project.logger.lifecycle("Tag! you are it! Release plugin will create a new branch $tagName for project ${project.name}");
            getSCMService().performTagging(tagName, msg)
          }
        }
      }
    }

    releaseTask.description = "Release the project by setting a final non-snapshot version, building and creating a tag."
  }

  def String getProjectVersion() {
    project.logger.debug("release version specified? " + hasProperty('releaseVersion'))

    if (project.hasProperty('releaseVersion')) {
      project.logger.info("release version specified: " + project.releaseVersion + " won't attempt to use scm service to establish project version")
      return project.releaseVersion
    }

    if (getSCMService().onTag()) {
      project.logger.info("build based on branch, using branch name as project version")
      return getSCMService().getBranchName()
    }

    project.logger.info("build based on trunk, using SNAPSHOT project version")
    return getSCMService().getBranchName() + "-SNAPSHOT"
  }

  def String getSCMVersion() {
    return getSCMService().getSCMVersion()
  }

  /*
      Lazily instantiate the SCMService when it is needed, or return the existing service if we've already created it before
  */

  def SCMService getSCMService() {
    if (scmService) return scmService

    Class c
    try {
      // capitalise the first letter
      def className = project.release.scm[0].toUpperCase() + project.release.scm[1..-1] + "Service"
      c = this.getClass().classLoader.loadClass("au.com.ish.gradle." + className)

    } catch (all) {
      throw new GradleException("Your value of scm = '${project.release.scm}' is invalid. Possible options are 'git' or 'svn'")
    }
    scmService = c.newInstance(project)
    return scmService
  }

}