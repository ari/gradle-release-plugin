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
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project

import static org.eclipse.jgit.lib.Constants.HEAD

class GitService extends SCMService {

    def private Project project
    def private Git git
    def private Repository repository

    GitService() {
        //private constructor only used for tests
    }
    
    def GitService(Project project) {
        this.project = project;
        project.logger.info("Creating GitService for $project")

        repository = new FileRepositoryBuilder().setGitDir(project.projectDir)
            .readEnvironment()
            .findGitDir()
            .build();
        git = new Git(repository);
    }

    def boolean localIsAheadOfRemote() {
        // this is git, so we don't really care about the remote (and there could be several remotes)
        return false
    }

    def boolean hasLocalModifications() {
        return git.status().getPaths().size() > 0
    }

    def boolean remoteIsAheadOfLocal() {
        // this is git, so we don't really care about the remote (and there could be several remotes)
        return false
    }

    def String getLatestReleaseTag(String currentBranch) {
        // this just gets the last commit and something cleverer might be needed to match tags with the right naming
        List<Ref> tagList = git.tagList().call()
        return tagList[0].getName()
    }

    String getSCMVersion() {
        return repository.getRef(HEAD)
    }

    def boolean onTag() {
        try {
            if (releaseTagPattern.matcher(tagNameOnCurrentRevision()).matches()) {
                return true
            }
        } catch (Exception ignored) {}
        return false
    }

    def String getBranchName() {
        if (onTag()) {
            return tagNameOnCurrentRevision()
        }

        return repository.getBranch()
    }

    def performTagging(String tag, String message) {
        git.tag().setName(tag).setAnnotated(true).setMessage(message).call()
        git.push().call();
    }

    /*
        Get the name of the most recent tag related to the branch we are on.
        It will have a strange name like v1.0.4-14-g2414721 if we aren't actually
        on the tag right now.
    */
    private String tagNameOnCurrentRevision() {
        gitExec(['describe', '--exact-match', 'HEAD']).replaceAll("\\n", "")
    }
}