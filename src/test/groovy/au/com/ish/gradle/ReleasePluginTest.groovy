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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec;

import org.junit.Test
import org.junit.Before
import static org.junit.Assert.*

import org.gradle.testfixtures.ProjectBuilder

import au.com.ish.gradle.*

import groovy.mock.interceptor.*
import org.tmatesoft.svn.core.SVNException

class ReleasePluginTest {

	private Project project
	private Project childProject
	
	@Before
	public void setUp() {
		project = ProjectBuilder.builder().build()
		project.task("clean") // a clean task is needed for the plugin to work
		project.task("build") // also build
		
		childProject = ProjectBuilder.builder().withParent(project).build()

		project.allprojects {
			project.apply plugin: 'release'
			release {
				scm = "test"
			}
			version = release.projectVersion
		}
	}

	@Test
	public void releasePluginAddsReleaseTaskToProject() {
		assert project.tasks.release != null
	}

	@Test
	public void releaseVersionIsValid() {
		assert project.version == "xyz-SNAPSHOT"
	}

	@Test
	public void childReleaseVersionIsValid() {
		assert childProject.version == "xyz-SNAPSHOT"
	}

	@Test
	public void checkSCMversion() {
		assert project.release.scmVersion == "abc"
	}

}