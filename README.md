# Gradle release plugin

[![Build Status](http://travis-ci.org/ari/gradle-release-plugin.png)](http://travis-ci.org/ari/gradle-release-plugin)

Gradle releases made easy. Other release processes force you to store your versioning information inside the project files such as build.gradle; this plugin keeps versions where they belong, in your version control system. We currently support subversion or git. Additional SCM choices are easy to add.

This plugin doesn't try to take over your release management process. Maven's release plugin will create two or three commits, alter the pom twice and have a number of unusual side effects. Instead, this plugin does two simple and clear things:


## What this plugin does

### 1. Version numbering

Use this plugin to give your project a version number aligned to the branch or tag you are building from. The plugin generates a version name based on the current branch name.

So let's say you are building a project in subversion and your working copy was checked out from https://svn.acme.com/project/trunk. In this case, the gradle property release.projectVersion will be set to 'trunk-SNAPSHOT'.

If you are building code checked out from https://svn.acme.com/project/tags/1.2, release.projectVersion will be '1.2'. And if you are building from https://svn.acme.com/project/branches/big-refactor, the you'll get 'big-refactor-SNAPSHOT'.

Git is just the same. The version will automatically be set to the tag name or branch name. No longer will you be changing the version in build.gradle or properties as you make new branches or tag your code. No longer will some plugin be guessing what the next release version will be.

The main effect of this plugin in regular use is to set the variable "release.projectVersion". What you do with it is up to you, but typically you would have a line:

    version = release.projectVersion

At any time you can override the version number which this plugin creates by adding a property on the command line:

     gradle assemble -PreleaseVersion=2.0-SNAPSHOT


### 2. Release tagging

When you release, the plugin will not try to guess the release version of the product. Instead you pass it on the command line (or add a property to your CI system such as Jenkins).

     gradle release -PreleaseVersion=4.1.2

Once the build is complete, the release plugin will create a tag in your SCM.


## Usage

Add the following to your build.gradle file:

```groovy
buildscript {
  repositories {
    mavenCentral()
    maven {
      url 'http://maven.tmatesoft.com/content/repositories/releases/'
    }
  }

  dependencies {
    classpath 'au.com.ish.gradle:release:2.2.2'
  }
}

apply plugin: 'release'

release {
  failOnSnapshotDependencies = true
  allowLocalModifications = false
  releaseDryRun = false
  scm = 'svn'
  username = 'fred'
  password = 'secret01'
}
version = release.projectVersion
````

Most of the above is self-explanatory. Include the buildscript section to pull this plugin into your project. Apply the plugin, and set your project version from the output of this plugin. Make sure the above configuration is at the top build.gradle file.

The options avilable are:

* failOnSnapshotDependencies: default is true. Will fail the release task if any dependency is currently pointing to a SNAPSHOT
* allowLocalModifications: defaults to false. Will fail the release task if any uncommitted changes remain in your local version control. This prevents you from releasing a build which you cannot later reproduce because you don't have the complete set of source which went into the build.
* releaseDryRun: this skips the commit of the tag to your version control system
* scm: your choices here are 'git' or 'svn'
* username: a username for your version control system. This is mostly useful for running releases from a continuous integration server like Jenkins. If you don't pass this, the release plugin will take credentials from any cached on your system or prompt you for them.
* password: a password to match the username
* scmRoot: The path to your version control repository. This is not needed if you have a simple checkout of the trunk path and only one gradle project beneath that. Some people have different layouts and this hint is needed so that the release plugin knows where to make tags and look for branches. Only used for subversion.

### Properties

You can access two properties from this plugin once you have configured it:

  release.projectVersion
    Read only property for getting the version of this project. The version is derived from the following rules:

    1. If the releaseVersion property was passed to gradle via the -P command line option, then use that.
    2. If the version control system is currently pointing to a tag, then use a version derived from the name of the tag
    3. Use the name of the branch (or trunk/head) as the version appended with "-SNAPHOT"
  
  release.scmVersion
    Read only property for getting the version from the source control system.
    This will return the svn commit number or the git hash for the current state of the local repository. This value may be useful for putting into the manifest file of a Java project or since it can be more reliable (but not as pretty) as the public-facing version numbering.


### Tasks

Many people will want to call their build task like this to build, test, tag and upload their release artifacts.

    gradle clean test release uploadArchives


## Release notes

### 2.2.2 (12 January 2014)
Oops. Typo in the previous commit... can't set a Java property as a boolean, only as a string.

### 2.2.1 (4 January 2014)

* Don't let SVNkit try to upgrade the working copy to 1.8 when it creates a tag. This plays better with other tools (eg. Jenkins) which are still limited to running a 1.7 working copy.

### 2.2 (29 November 2013)

* Upgrade to svnkit 1.8 final release. No functional change.

### 2.2b1 (25 August 2013)

* Upgrade to svnkit alpha release so that we can support subversion 1.8 repositories

### 2.1.3 (29 March 2013)

* Fix a problem where release tags were not created with the right name if the gradle build file was executed from a subproject folder, in a multi-project build where each subproject is released separately.

### 2.1.2 (18 February 2013)

* Fix problem introduced in 2.1 with releasing from the trunk branch using svn.

### 2.1.1 (30 January 2013)

* Update svnkit for a whole bunch of bug fixes against subversion repositories.

### 2.1 (3 December 2012)

* Fixed some issues in the git execution commands which could prevent git from working properly
* Now supports projects with subprojects and where you want to release just one of those subprojects

### 2.0 (November 2012)

* Our first public release of the plugin and a complete rewrite of the earlier 1.x series which we used internally.
* Includes unit tests


## Releasing to maven central

These notes are for the developers of this plugin only.

1. # ./gradlew test
2. Make sure all changes are committed
3. # ./gradlew release -PreleaseVersion=x.x.x uploadArchives
4. Go to https://oss.sonatype.org and log in. Choose 'staging repositories'
5. Find the repository which starts aucomish- and "close"
6. Test the staged archives by clicking on the link and downloading the jars
7. Press the "release" button