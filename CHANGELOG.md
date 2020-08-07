# Change Log

## Release Notes

### 1.5.8

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg) Address
    boundary case of
    [JENKINS-23532](https://issues.jenkins-ci.org/browse/JENKINS-23532) where
    relative paths would not retrigger

### [1.5.7.1](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.7.1)

-   (-) [JENKINS-45137](https://issues.jenkins-ci.org/browse/JENKINS-45137)
    Fix stack trace for pipelines created / edited in v 1.5.6

### [1.5.7](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.7)

-   (-) [JENKINS-23532](https://issues.jenkins-ci.org/browse/JENKINS-23532) Manual
    trigger execution causes TriggerException
-   (-) [JENKINS-44324](https://issues.jenkins-ci.org/browse/JENKINS-44324) NullPointerException upgrading
    from very old versions of plugin  
-   (-) Fix issue with dashboard view not loading
-   (+)Added support for extensible BuildCard

### [1.5.6](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.6)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Issues \#34722](https://issues.jenkins-ci.org/browse/JENKINS-34722)
    ([Pull
    \#88](https://github.com/jenkinsci/build-pipeline-plugin/pull/88))
    Performance fix when determining view permissions
-   ![(plus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/add.svg)
    [Issues \#14591](https://issues.jenkins-ci.org/browse/JENKINS-14591)
    ([Pull
    \#96](https://github.com/jenkinsci/build-pipeline-plugin/pull/96)
    and [Pull
    \#106](https://github.com/jenkinsci/build-pipeline-plugin/pull/106))
    Add the ability to choose which parameters build will be displayed

 ### [1.5.5](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.5)

 -   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    Eaten by
    [INFRA-588](https://issues.jenkins-ci.org/browse/JENKINS-31088)

### [1.5.4](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.4)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Pull
    \#105](https://github.com/jenkinsci/build-pipeline-plugin/pull/105)
    Keep git-plugin RevisionParameterAction on rerun of a build
-   ![(plus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/add.svg)
    [Issues \#31088](https://issues.jenkins-ci.org/browse/JENKINS-31088)
    show displayName by default

### [1.5.3.1](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.3.1)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Issue \#33935](https://issues.jenkins-ci.org/browse/JENKINS-33935)
    Fix re-running a build - broken in Jenkins \> 1.653, LTS \> 1.651.1
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Issue \#34722](https://issues.jenkins-ci.org/browse/JENKINS-34722)
    Performance fix - limit the number of downstream projects iterated
    over

### [1.5.2](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.2)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Issue \#33591](https://issues.jenkins-ci.org/browse/JENKINS-33591)
    Fix manual trigger breaking in Jenkins \>1.653

### [1.5.1](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.5.1)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    [Issue \#31746](https://issues.jenkins-ci.org/browse/JENKINS-31746)
    Fix issues in layout, UI updates

### [1.4.9](https://github.com/jenkinsci/build-pipeline-plugin/releases/tag/build-pipeline-plugin-1.4.9)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([issue
    \#30801](https://issues.jenkins-ci.org/browse/JENKINS-30801))
    Re-triggering a failed build copies the Actions from previous builds
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([issue
    \#28068](https://issues.jenkins-ci.org/browse/JENKINS-28068)) Build
    Pipeline Dashboard View destroys layout of jenkins
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([issue
    \#29477](https://issues.jenkins-ci.org/browse/JENKINS-29477)) View
    bad for Build Pipeline Plugin

### [1.4.8](https://github.com/jenkinsci/build-pipeline-plugin/compare/build-pipeline-plugin-1.4.7...build-pipeline-plugin-1.4.8)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
     ([issue
    \#28180](https://issues.jenkins-ci.org/browse/JENKINS-28180)) Build
    Pipeline background layout does not extend full width of pipeline

### [1.4.7](https://github.com/jenkinsci/build-pipeline-plugin/compare/build-pipeline-plugin-1.4.6...build-pipeline-plugin-1.4.7)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([JENKINS-25666](https://issues.jenkins-ci.org/browse/JENKINS-25666))
    Fixed left-side indentation for new jenkins versions

### [1.4.6](https://github.com/jenkinsci/build-pipeline-plugin/compare/build-pipeline-plugin-1.4.5...build-pipeline-plugin-1.4.6)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([JENKINS-20499](https://issues.jenkins-ci.org/browse/JENKINS-20499))
    Null cause brokes Cause entry and job
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    ([JENKINS-22665](https://issues.jenkins-ci.org/browse/JENKINS-22665))
    ([JENKINS-19755](https://issues.jenkins-ci.org/browse/JENKINS-19755))
    Don't store whole user object for cause
-   ![(plus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/add.svg)
    Fixed UI tests
-   ![(plus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/add.svg)
    Removed test libs from final package

### [1.4.5](https://github.com/jenkinsci/build-pipeline-plugin/compare/build-pipeline-plugin-1.4.4...build-pipeline-plugin-1.4.5)

-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    Support cloudbees-folder-plugin
    ([JENKINS-14565](https://issues.jenkins-ci.org/browse/JENKINS-14565))
    ([JENKINS-20841](https://issues.jenkins-ci.org/browse/JENKINS-20841))
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    Start build with parameters for parametrized builds
    ([JENKINS-25427](https://issues.jenkins-ci.org/browse/JENKINS-25427))
    ([JENKINS-19121](https://issues.jenkins-ci.org/browse/JENKINS-19121))
-   ![(minus)](https://wiki.jenkins.io/s/en_GB/8100/5084f018d64a97dc638ca9a178856f851ea353ff/_/images/icons/emoticons/forbidden.svg)
    Clicking on "console" icon doesn't work
    ([JENKINS-25430](https://issues.jenkins-ci.org/browse/JENKINS-25430))

### [1.4.4](https://github.com/jenkinsci/build-pipeline-plugin/compare/build-pipeline-plugin-1.4.3...build-pipeline-plugin-1.4.4)

### [1.3.3](http://code.google.com/p/build-pipeline-plugin/issues/list?can=1&q=label%3AMilestone-Release1.3.3&colspec=ID+Type+Status+Priority+Milestone+Owner+Summary&cells=tiles)  

### [1.3.1](http://code.google.com/p/build-pipeline-plugin/issues/list?can=1&q=label%3AMilestone-Release1.3.1&colspec=ID+Type+Status+Priority+Milestone+Owner+Summary&cells=tiles)

### [1.3.0](http://www.centrumsystems.com.au/2012/07/build-pipeline-plugin-1-3-0-release/)

- Also see the
[roadmap](https://wiki.jenkins.io/display/JENKINS/Build+Pipeline+Plugin+-+Roadmap)
for details.

### [1.2.4](http://code.google.com/p/build-pipeline-plugin/issues/list?can=1&q=label%3AMilestone-Release1.2.4&colspec=ID+Type+Status+Priority+Milestone+Owner+Summary&cells=tiles)

- Also see the
[roadmap](https://wiki.jenkins.io/display/JENKINS/Build+Pipeline+Plugin+-+Roadmap)
for details.

### [1.2.2](http://www.centrumsystems.com.au/blog/?p=325)

### [1.2.1](http://www.centrumsystems.com.au/blog/?p=287)

### [1.2](http://www.centrumsystems.com.au/blog/?p=281)

### [1.1.2](http://www.centrumsystems.com.au/blog/?p=200)

### [1.1.1](http://www.centrumsystems.com.au/blog/?p=165)

### [1.0.0](http://www.centrumsystems.com.au/blog/?p=121)