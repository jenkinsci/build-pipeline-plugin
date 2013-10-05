Build Pipeline Plugin
=====================
* [Wiki][wiki]
* [Issue Tracking][issues]
* [How to Contribute][contributing]

Building the Project
--------------------

### Dependencies
* [Apache Maven][maven] 3.0.4 or later

### Targets
```shell
  $ mvn clean install
```

Installing Plugin Locally
-------------------------
1. Build the project to produce `target/build-pipeline-plugin.hpi`
2. Remove any installation of the build-pipeline-plugin in `$user.home/.jenkins/plugins/`
3. Copy `target/build-pipeline-plugin.hpi` to `$user.home/.jenkins/plugins/`
4. Start/Restart Jenkins


Continuous Integration
----------------------
After a pull request is accepted, it is run through a [Jenkins job][job] hosted on CloudBees.


[wiki]: https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin
[issues]: http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27build-pipeline%27
[contributing]: https://wiki.jenkins-ci.org/display/JENKINS/Build+Pipeline+Plugin+-+How+to+Contribute
[maven]: https://maven.apache.org/
[job]: https://jenkins.ci.cloudbees.com/job/plugins/job/build-pipeline-plugin/

