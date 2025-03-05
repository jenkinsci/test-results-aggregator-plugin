# Test Results Aggregator

### Description

It's a Jenkins plugin that collects jobs and test results and reports them in :

* html,
* email, 
* graphs
* [grafana](https://grafana.com)

Test Results Aggregator can collect test results for : 

* [Junit](https://plugins.jenkins.io/junit)
* [TestNG](https://plugins.jenkins.io/testng-plugin) 
* [Nunit](https://plugins.jenkins.io/nunit)

and code coverage results for : 

* [JaCoCo](https://plugins.jenkins.io/jacoco)

![Latest Version](https://img.shields.io/jenkins/plugin/v/test-results-aggregator.svg)
![Version](https://ci.jenkins.io/buildStatus/icon?job=Plugins/test-results-aggregator-plugin/master)
![Build](https://github.com/jenkinsci/test-results-aggregator-plugin/workflows/Java_CI/badge.svg)
![Installs](https://img.shields.io/jenkins/plugin/i/test-results-aggregator.svg?color=blue)


### Samples:

|  |  | 
| --- | ----------- |
| <img src="docs/screenshots/htmlView2.png" alt="Email report" style="float: center; margin-right: 10px; width: 400" /> | <img src="docs/screenshots/MainView.png" alt="Job results history" style="float: center; margin-right: 10px; width: 400" /> |
| <img src="docs/screenshots/Untitled1.png" alt="Job results history main view" style="float: center; margin-right: 10px; width: 400" /> | <img src="docs/screenshots/Untitled2.png" alt="Job results per build" style="float: center; margin-right: 10px; width: 400" /> |
| <img src="docs/screenshots/Grafana1.png" alt="GrafanaMain" style="float: center; margin-right: 10px; width: 400" /> | <img src="docs/screenshots/Grafana2.png" alt="Grafana Jobs" style="float: center; margin-right: 10px; width: 400" /> |


**Important** 

It is advisable to be used as the last step in a CI/CD pipeline.

Supports '[free style project](docs/README_FreeStyle.md)' and '[pipeline](docs/README_Pipeline.md)'

Requires both **global** and **job configuration** .
 
**Global Configuration**

After installing this plugin, navigate to Global Configuration. Scroll down for Test Result Aggregator :

<img src="docs/screenshots/Global_Configuration.png" alt="Global Configuration" style="float: center; margin-right: 10px; width: 600" />
     

In this section you can define : 


| Argument | Description |
| --- | --- |
| Jenkins Base Url | The HTTP address of the Jenkins installation, such as http://yourhost.yourdomain/jenkins/ .This is used to access Jenkins API. |
| Jenkins Account Username | username of the account that will be used to access Jenkins API and fetch job results. |
| Jenkins Account password/token | password or token of the account that will be used to access Jenkins API and fetch job results. |
| Mail Notification From | sender for the mail Notification. Default is "Jenkins". |


Extra Notes : 

- Mail notification requires also the Jenkins Global Configuration setting for [SMTP server](https://plugins.jenkins.io/mailer)
- All Jobs that running tests should publish their own test results (Junit, testNG, Nunit). The same stands for coverage results (Jacoco).

**Job Configuration**

 * [Free Style Job Configuration](docs/README_FreeStyle.md)
 * [Pipeline Syntax](docs/README_Pipeline.md)

**Release Notes**

See the [Github releases page](https://github.com/jenkinsci/test-results-aggregator-plugin/releases).

* 1.1.x Requires Jenkins < 2.277
* 1.2.x Requires Jenkins >= 2.277
* 2.x Integration with jenkins client library
* 2.1.x Java 11
* 3.x Supports Grafana Integration

  
**Misc**

[Grafana Integration and Dashboards Samples](docs/README_GrafanaSamples.md)

[Jenkins.io](https://ci.jenkins.io/job/Plugins/job/test-results-aggregator-plugin/)
