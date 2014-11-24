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

import org.junit.Test

class GitServiceTest {    

    // tests parsing the url with 'tags'
    @Test
    public void testOnTag() {
        TestGitService service = new TestGitService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0")
        assert service.onTag() == true
        assert service.getBranchName() == "1.8.0"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0/doc")
        assert service.onTag() == true
        assert service.getBranchName() == "doc"
    }

    // tests parsing the url with 'branches'
    @Test
    public void testOnBranch() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x/doc")
        assert service.onTag() == false
        assert service.getBranchName() == "doc"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x")
        assert service.onTag() == false
        assert service.getBranchName() == "1.7.x"
    }

    // tests parsing the url with 'trunk'
    @Test
    public void testOnTrunk() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk/doc")
        assert service.onTag() == false
        assert service.getBranchName() == "trunk"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk")
        assert service.onTag() == false
        assert service.getBranchName() == "trunk"
    }

    // tests parsing the root url with 'tags'
    @Test
    public void testRootURLonTags() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0/doc/")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"
    }

    // tests parsing the root url with 'branches'
    @Test
    public void testRootURLonBranches() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x/doc")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"
    }

    // tests parsing the root url with 'trunk'
    @Test
    public void testRootURLonTrunk() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk/doc")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk")
        assert service.getSvnRootURL() == "http://svn.apache.org/repos/asf/subversion"
    }


    // tests creating tag url from url with 'trunk'
    @Test
    public void testCreateTagsUrlonTrunk() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk/doc")
        assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/doc/10.0"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/trunk")
         assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/10.0"
    }

    // tests creating tag url from url with 'branches'
    @Test
    public void testCreateTagsUrlonBranch() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x/doc")
        assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/1.7.x/10.0"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/branches/1.7.x")
         assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/10.0"
    }

    // tests creating tag url from url with 'tags'
    @Test
    public void testCreateTagsUrlonTags() {
        TestSvnService service = new TestSvnService()
        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0/doc")
        assert service.createTagsUrl("doc2").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/1.8.0/doc2"

        service.setSCMRemoteURL("http://svn.apache.org/repos/asf/subversion/tags/1.8.0")
         assert service.createTagsUrl("1.8.1").toDecodedString() == "http://svn.apache.org/repos/asf/subversion/tags/1.8.1"
    }
}