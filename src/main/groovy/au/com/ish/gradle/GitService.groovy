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
import org.gradle.api.Project
import org.gradle.process.internal.ExecException

class GitService extends SCMService {

  def private Project project

  def GitService(Project project) {
    this.project = project;
  }

  def boolean localIsAheadOfRemote() {
    gitExec(['status']).contains('Your branch is ahead')
  }

  def boolean hasLocalModifications() {
    gitExec(['status', '--porcelain']) == true
  }

  def boolean remoteIsAheadOfLocal() {
    return false
    //TODO requires implementation
  }

  String getSCMVersion() {
    return "abc" //TODO: implement
  }

  def boolean onTag() {
    try {
      if (releaseTagPattern.matcher(tagNameOnCurrentRevision()).matches()) {
        return true
      }
    } catch (Exception e) {
    }
    return false
  }

  def String getBranchName() {
    if (onTag()) {
      return tagNameOnCurrentRevision()
    }

    def refName = gitExec(['symbolic-ref', '-q', 'HEAD']).replaceAll("\\n", "")

    if (!refName) {
      throw new GradleException('Could not determine the current branch name.');
    } else if (!refName.startsWith('refs/heads/')) {
      throw new GradleException('Checkout the branch to release from.');
    }

    def prefixLength = 'refs/heads/'.length()
    def branchName = refName[prefixLength..-1]

    return branchName.replaceAll('[^\\w\\.\\-\\_]', '_')
  }

  def performTagging(String tag, String message) {
    try {
      gitExec(['tag', '-a', tag, '-m', message])
      gitExec(['push', '--tags'])
    } catch (ExecException e) {
      throw new GradleException("Failed to create or push the git tag ${tag}")
    }
  }

  /*
      Get the name of the most recent tag related to the branch we are on.
      It will have a strange name like v1.0.4-14-g2414721 if we aren't actually
      on the tag right now.
  */

  protected String tagNameOnCurrentRevision() {
    gitExec(['describe', '--exact-match', 'HEAD']).replaceAll("\\n", "")
  }

  def protected gitExec(List gitArgs) {
    def stdout = new ByteArrayOutputStream()

    project.exec {
      executable = 'git'
      args = gitArgs
      standardOutput = stdout
    }

    if (stdout.toByteArray().length > 0) {
      return stdout.toString()
    } else {
      return null
    }
  }
}