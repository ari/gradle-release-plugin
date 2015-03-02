/*
 *  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
 *
 *  OCLC proprietary information: the enclosed materials contain proprietary information of OCLC Online Computer
 *  Library Center, Inc. and shall not be disclosed in whole or in any part to any third party or used by any person
 *  for any purpose, without written consent of OCLC, Inc.  Duplication of any portion of these materials shall include
 *  this notice.
 */
package au.com.ish.gradle

import org.gradle.api.Project

class TestGitService extends GitService {
  String tag

  TestGitService(Project project) {
    super(project)
  }

  protected gitExec(List gitArgs) {
    'refs/heads/BRANCH'
  }

  protected String tagNameOnCurrentRevision() {
    'TAG_VERSION'
  }

  def performTagging(String tag, String message) {
    this.tag = tag
  }
  
}
