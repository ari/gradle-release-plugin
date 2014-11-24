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

package au.com.ish.gradle

import org.gradle.api.Project

class TestGitService extends GitService {

    String remoteURL

    def TestGitService(Project project) {
        super(project)
    }

    def setSCMRemoteURL(String remoteURL) {
        this.remoteURL = remoteURL
    }
}