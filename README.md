#Gradle release plugin (Git and Subversion) [![Build Status](http://travis-ci.org/ari/gradle-release-plugin.png?branch=develop)](http://travis-ci.org/ari/gradle-release-plugin)

Gradle releases made easy. Other release processes make you store your versioning information inside the project; this plugin keeps versions where they belong, in your version control system. We currently support subversion or git. Additional SCM options are easy to add.

## Version numbering

Use this plugin to give your project a version number aligned to the branch or tag you are building from. In the case of a normal gradle build, the plugin generates a version name based on the current branch name like this:

    ${branchName}-SNAPSHOT

If you are building on a tag, this plugin will automatically detect that and use the tag name as the version.


## Release management

Just run this to have the plugin automatically determine the next available version number, and create a tag.

     gradle release

Or this if you want to set the release version yourself.

     gradle release -PreleaseVersion=4.1.2



### Usage

Add the following to your build.gradle file:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'ish.gradle:gradle-release-plugin:2.0'
  }
}

apply plugin: 'release'
release {
  failOnSnapshotDependencies = true
  scm = 'git'
}

version = release.projectVersion
````

Most of the above is self-explanatory. Include the buildscript section to pull this plugin into your project. Apply the plugin, and then configure it by defining a handful of properties:

* failOnSnapshotDependencies: default is true. Will fail the release task if any dependency is currently pointing to a SNAPSHOT
* scm: your choices here are 'git' or 'svn' and it defaults to 'svn'

## Properties

You can access two properties from this plugin once you have configured it:

  release.projectVersion
    Read only property for getting the version of this project.
    The version is derived from the following rules:
    1. If the releaseVersion property was passed to gradle via the -P command line option, then use that.
    2. If the version control system is currently pointing to a tag, then use a version derived from the name of the tag
    3. Use the name of the branch (or trunk/head) as the version appended with "-SNAPHOT"
  
  release.scmVersion
    Read only property for getting the version from the source control system.
    This will return the svn commit number or the git hash for the current state of the local repository. This value may be useful for putting into the manifest file.


## Tasks

Many people will want to call their build task like this in order to upload their release artifacts.

    gradle clean build release uploadArchives