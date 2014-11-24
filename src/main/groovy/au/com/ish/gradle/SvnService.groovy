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
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*

class SvnService extends SCMService {

    def private Project project
    def private SVNStatus wcStatus;
    def private svnClientManager

    private SVNRepository svnRepo;
    
    SvnService() {
        //private constructor only used for tests
    }

    SvnService(Project project) {
        this.project = project;
        project.logger.info("Creating SvnService for $project")

        // Don't let svnkit try to upgrade the working copy version unless it tries to create a tag
        System.setProperty("svnkit.upgradeWC", "false");

        // do some basic setup
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        DAVRepositoryFactory.setup();

        //set an auth manager which will provide user credentials
        project.logger.info("SvnService authentication manager setup")
        def ISVNAuthenticationManager authManager
        if (scmCredentialsProvided()) {
            project.logger.lifecycle("Release plugin is using subversion scm with provided authentication details (user "+project.extensions.release.username+")")
            authManager= SVNWCUtil.createDefaultAuthenticationManager(project.extensions.release.username, project.extensions.release.password);
        } else {
            project.logger.lifecycle("Release plugin is using subversion scm with authentication details from svn config")
            authManager= SVNWCUtil.createDefaultAuthenticationManager();
        }

        // svn client manager has to be defined with authManager, otherwise it will use the default one (important for the performTagging)
        project.logger.info("Creating SvnClient")
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        svnClientManager = SVNClientManager.newInstance(options, authManager);

        project.logger.info("Getting working copy status")
        wcStatus = svnClientManager.getStatusClient().doStatus(project.projectDir,false)
        project.logger.info("Got svn version: "+getSCMVersion())

        //not sure if the code below is required
        def svnRepo = SVNRepositoryFactory.create(wcStatus.getURL())
        svnRepo.setAuthenticationManager(authManager);
    }

    def private boolean scmCredentialsProvided() {
        def boolean userDefined = project.extensions.release.username != null && project.extensions.release.username.length() > 0
        def boolean passwordDefined = project.extensions.release.password != null && project.extensions.release.password.length() > 0
        return userDefined && passwordDefined
    }
    
    def boolean localIsAheadOfRemote() {
        return false
    }

    def boolean hasLocalModifications() {
        def repoStatus = new UpToDateChecker()
        svnClientManager.getStatusClient().doStatus(project.rootDir, true, false, false, false, repoStatus)
        return repoStatus.hasModifications()
    }

    def boolean remoteIsAheadOfLocal() {
        return (getSCMVersion() != svnRepo.getLatestRevision())
    }

    def SVNURL getSCMRemoteURL() {
        return wcStatus.getURL()
    }

    // calculates the svn root URL as string
    def String getSvnRootURL() {
        String svnRootUrlString = getSCMRemoteURL().toString()

        // if svn URL contains "/tags/", then find the name of the branch
        if (svnRootUrlString.contains("/tags/")) {
            return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/tags/"))
        }

        if (svnRootUrlString.contains("/branches/")) {
            return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/branches/"))
        }
        
        return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/trunk"))
    }
  
    def String getSCMVersion() {
        return wcStatus.getRevision().getNumber().toString()
    }

    def boolean onTag() {
        return getSCMRemoteURL().getPath().contains("/tags/")
    }

    def boolean onBranch() {
        return getSCMRemoteURL().getPath().contains("/branches/")
    }
    
    def String getBranchName() {
        List splitPath = Arrays.asList(getSCMRemoteURL().getPath().split("/"))

        // if svn URL contains "/tags/", then find the name of the branch
        if (onTag()) {
            assert(splitPath.indexOf("tags") > 0)
            assert(splitPath.indexOf("tags") < splitPath.size()-1)
            return splitPath.get(splitPath.size()-1)
        }

        // if svn URL contains "/branches/", then find the name of the branch
        if (onBranch()) {
            assert(splitPath.indexOf("branches") > 0)
            assert(splitPath.indexOf("branches") < splitPath.size()-1)
            return splitPath.get(splitPath.size()-1)
        }
        
        return "trunk"
    }

    def String getLatestReleaseTag(String currentBranch) {
        def entries = svnRepo.getDir( "tags", -1 , null , (Collection) null );
        SVNDirEntry max = entries.max{it2->
            def matcher = releaseTagPattern.matcher(it2.name);
            if (matcher.matches() && branchName.equals(matcher.group(1))) {
              return Integer.valueOf(matcher.group(2))
            } else {
              return null
            }
        }
        return max.name
    }  

    /**
    * creates a url for the new tag
    */
    def SVNURL createTagsUrl(String tag) {
        if (project != null) {
            project.logger.info("$project, Crafting new tag: $tag")
        }
        // root url to use for tagging
        def SVNURL rootURL = SVNURL.parseURIDecoded(getSvnRootURL())

        def SVNURL tagsURL = rootURL.appendPath("tags",false)

        // need to preseve the path elements after 'branches'/'tags'/'trunk' and the branch name
        String urlTail = getSCMRemoteURL().toString().replace(getSvnRootURL(),"").replace("branches", "").replace("tags", "").replace(getBranchName(), "")

        List splitUrlTail = Arrays.asList(urlTail.split("/"))
        for (String pathElement : splitUrlTail) {
            if (pathElement.length() > 0) {
                tagsURL = tagsURL.appendPath(pathElement, false);
            }
        }
        tagsURL = tagsURL.appendPath(tag,false)

        return tagsURL
    }

    def performTagging(String tag, String message) {
        project.logger.info("$project, Tagging release: $tag")
        
        def tagsURL = createTagsUrl(tag)
        
        //javadoc helper :
        //doCopy(SVNCopySource[] sources, SVNURL dst, 
        //                          boolean isMove, boolean makeParents, boolean failWhenDstExists, 
        //                          java.lang.String commitMessage, SVNProperties revisionProperties) 
        svnClientManager.getCopyClient().doCopy(
            [new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, wcStatus.URL)] as SVNCopySource[],
            tagsURL, 
            false, 
            true, 
            true, 
            message,
            null)
    }
}