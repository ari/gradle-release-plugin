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

Most of the above is self-explanatory. Include the buildscript section to pull this plugin into your project. Apply the plugin, and then configure it by defining a handful of properties. The complete set of options looks like this:

````
release {
  failOnSnapshotDependencies = true
  allowLocalModifications = false
  releaseDryRun = false
  scm = 'svn'
  username = 'fred'
  password = 'secret01'
}
````

* failOnSnapshotDependencies: default is true. Will fail the release task if any dependency is currently pointing to a SNAPSHOT
* allowLocalModifications: defaults to false. Will fail the release task if any uncommitted changes remain in your local version control. This prevents you from releasing a build which you cannot later reproduce because you don't have the complete set of source which went into the build.
* releaseDryRun: this skips the commit of the tag to your version control system
* scm: your choices here are 'git' or 'svn' and it defaults to 'svn'
* username: a username for your version control system. This is mostly useful for running releases from a continuous integration server like Jenkins. If you don't pass this, the release plugin will take credentials from any cached on your system or prompt you for them.
* password: a password to match the username
* scmRoot: The path to your version control repository. This is not needed if you have a simple checkout of the trunk path and only one gradle project beneath that. Some people have different layouts and this hint is needed so that the release plugin knows where to make tags and look for branches. Only used for subversion.

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

Many people will want to call their build task like this to upload their release artifacts.

    gradle clean test release uploadArchives