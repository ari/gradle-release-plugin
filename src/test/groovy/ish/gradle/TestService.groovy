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

 /* This is a mock service used only for testing */

package ish.gradle

import ish.gradle.SCMService
import org.gradle.api.Project

class TestService extends SCMService {
    Project project

    def TestService(project) {
        this.project = project
    }

    def boolean localIsAheadOfRemote() {
        return false
    }

    def boolean hasLocalModifications() {
        return false
    }

    def boolean remoteIsAheadOfLocal() {
        return false
    }

    def String getLatestReleaseTag(String currentBranch) {
        return "test-RELEASE-11"
    }

    String getSCMVersion() {
        return "abc"
    }

    def boolean onTag() {
        return false
    }

    def String getBranchName() {
        return "xyz"
    }

    def performTagging(String tag, String message) {
        // nothing needs to happen here
    }
}