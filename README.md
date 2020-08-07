# Build Pipeline Plugin

* [Wiki][wiki]
* [Issue Tracking][issues]
* [How to Contribute][contributing]

## Summary

This plugin provides a *Build Pipeline View* of upstream and downstream connected jobs that typically
form a build pipeline.  In addition, it offers the ability to define manual triggers for jobs that
require intervention prior to execution (e.g., an approval process outisde of Jenkins).

*WARNING*: The current version of this plugin may not be safe to use.  Please review the following
warnings before use:

- [Stored XSS vulnerability](https://jenkins.io/security/advisory/2019-08-07/#SECURITY-879)

## Overview

Continuous Integration has become a widely adopted practice in modern software development.  Jenkins
and Hudson are great tools for supporting Continuous Integration.

**Taking it to the next level**: Continous Integration can become the centerpeice of your
[deployment pipeline](http://www.informit.com/articles/article.aspx?p=1621865), orchestrating the promotion
of a version of software through quality gates and into production.  By extending the concepts of CI
you can create a chain of jobs, each one subjecting your build to quality assurance steps.  These
QA steps may be a combination of manual and automated steps.  Once a build has passed all of these,
it can be automatically deployed into production.

In order to better support this process, we have developed the Build Pipeline Plugin.  This gives
the ability to form a chain of jobs based on their upstream and downstream dependencies.  Downstream
jobs may, as per the default behaviors, be triggered automatically, or by a suitable authorized user
manually triggering it.

You can also see a history of pipelines in a view, the current status, and where each version got to
in the chain based on it revision number in VCS.

### Screenshots

#### The Pipeline View

![](https://wiki.jenkins.io/download/attachments/54723106/bpp1.png?version=2&modificationDate=1340695983000&api=v2){width="900"}

## Configuration

### View Configuration

1. Install the plugin using the Jenkins Plugin Manager and restart.
2. Create a view of the new type, *Build Pipeline View*.  You will then be redirected directly to the
configuration page.
3. The table below outlines each interesting parameters controls:

![](https://wiki.jenkins.io/download/attachments/54723106/config.png?version=1&modificationDate=1340758239000&api=v2)

### Job Configuration

1. Navigate to the Job Configuration page.
2. Scroll down to the *Post-build Actions* section.
	1. For an **Automated** downstream build step: To add a build step that will trigger automatically upon
	the successful completion of the previous one:
		1. Select the *Build other projects* check-box.
		2. Enter the name(s) of the downstream projects in the *Projects to build* field. (n.b. Multiple
		projects can be specified by using comma, like "abc, def".).
	2. For a *Manually Triggered* downstream build step: To add a build step that will wait for a manual
	trigger:
		1. Select the *Build Pipeline Plugin -\> Manually Execution Downstream Project* check-box.
		2. Enter the name(s) of the downstream projects in the *Downstream Project Names* field.  (n.b.
		Multiple projects can be specified by using comma, like "abc, def".).
3. Click *Save*.

### Automatic & Manual Downstream Build Steps

The **Build Pipeline Plugin** handles teh creation of multiple automatic or manually triggered downstream
build steps on the same project.

![](https://wiki.jenkins.io/download/attachments/54723106/JobConfig.PNG?version=2&modificationDate=1346302165000&api=v2)

### Upgrading from Release 1.0.0

When upgrading from 1.0.0 to 1.1.x some of the previous view and job configuration fields have been
removed.  You may notice some errors appearing in the Jenkins log:

> WARNING: Skipping a non-existen field downstreamProjectName com.thoughtworks.xstream.converters.reflection.NonExistentFieldException:
No such field
au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger.downstreamProjectName

This is because the configuration files refer to old fields that may no longer exist.  In order to
correct these issues, go to the Job Configuration page, confirm that all of the details are correct,
and click on the *Save* button.

## More on Pipelines

The canonical reference for pipelines in the book [Continuous Delivery](http://www.amazon.com/gp/product/B003YMNVC0/).

[Chapter 5]([here](http://www.informit.com/articles/article.aspx?p=1621865)) of the book, which
describes how deployment pipelines work, is available for free.

## Building the Project

### Dependencies

* [Apache Maven][maven] 3.0.4 or later

### Targets

```shell
  $ mvn clean install
```

## Installing Plugin Locally

1. Build the project to produce `target/build-pipeline-plugin.hpi`
2. Remove any installation of the build-pipeline-plugin in `$user.home/.jenkins/plugins/`
3. Copy `target/build-pipeline-plugin.hpi` to `$user.home/.jenkins/plugins/`
4. Start/Restart Jenkins

## Continuous Integration

After a pull request is accepted, it is run through a [Jenkins job][job] hosted on CloudBees.

[wiki]: https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin
[issues]: http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27build-pipeline-plugin%27
[contributing]: https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin+-+How+to+Contribute
[maven]: https://maven.apache.org/
[job]: https://jenkins.ci.cloudbees.com/job/plugins/job/build-pipeline-plugin/

