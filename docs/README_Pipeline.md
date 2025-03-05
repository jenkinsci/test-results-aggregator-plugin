# Test Results Aggregator Pipeline Syntax

Example 1 : Minimum
	
	stage ("Report"){
		testResultsAggregator jobs:[[jobName: 'My CI Job1'], [jobName: 'My CI Job2'], [jobName: 'My CI Job3']]
	}
	

Example 2 : Report and **publish via html publisher plugin**.

    testResultsAggregator columns: 'Job, Build, Status, Percentage, Total, Pass, Fail',
                          recipientsList: 'nick@some.com,mairy@some.com',
                          outOfDateResults: '10', 
                          sortresults: 'Job Name',
                          subject: 'Test Results',
                        	 jobs: [
                                // Group with 2 Jobs
                                [jobName: 'My CI Job1', jobFriendlyName: 'Job 1', groupName: 'TeamA'],
                                [jobName: 'My CI Job2', jobFriendlyName: 'Job 2', groupName: 'TeamA'],
                                // jobFriendlyName is optional
                                [jobName: 'My CI Job3', groupName: 'TeamB'],
                                [jobName: 'My CI Job4', groupName: 'TeamB'],
                                // No Groups, groupName is optional
                                [jobName: 'My CI Job6'],
                                [jobName: 'My CI Job7']
                            ]
		
	publishHTML(target: [allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: "html", reportFiles: 'index.html', reportName: "Results"])
	
Example 3 : Override ** Global Configuration **.

    testResultsAggregator testResultsAggregator ignoreDisabledJobs: true,
							ignoreNotFoundJobs: true,
							ignoreRunningJobs: false,
							compareWithPreviousRun: true,
							overrideJenkinsBaseURL: 'https://newjenkinsurl.com',
							overrideAPIAccountUsername: 'myname',
							overrideAPIAccountPassword: 'mypassword', 
                        	jobs: [
                                // Group with 2 Jobs
                                [jobName: 'My CI Job1', jobFriendlyName: 'Job 1', groupName: 'TeamA'],
                                [jobName: 'My CI Job2', jobFriendlyName: 'Job 2', groupName: 'TeamA']
                            ]
							
							
Example 4 : Post to ** Grafana **

    testResultsAggregator testResultsAggregator ignoreDisabledJobs: true,
							ignoreNotFoundJobs: true,
							ignoreRunningJobs: false,
							compareWithPreviousRun: true,
							influxdbUrl: 'http://infuxdburl:8086',
							influxdbToken: 'l9IfvjWabjBBzaVjzEOBhupcLMVx05pl1LKXoMKjUNhwsmiL-CNPw16yNyv9Om2TRQfsy4IUeyfjlI3ncTc9sA==',
							influxdbBucket: 'TestResultsAggregatorBucket',
							influxdbOrg: 'MyOrg',
                        	 jobs: [
                                // Group with 2 Jobs
                                [jobName: 'My CI Job1', jobFriendlyName: 'Job 1', groupName: 'TeamA'],
                                [jobName: 'My CI Job2', jobFriendlyName: 'Job 2', groupName: 'TeamA'],
                                // jobFriendlyName is optional
                                [jobName: 'My CI Job3', groupName: 'TeamB'],
                                [jobName: 'My CI Job4', groupName: 'TeamB'],
                                // No Groups, groupName is optional
                                [jobName: 'My CI Job6'],
                                [jobName: 'My CI Job7']
                            ]
														
### Parameters & values :
 
| Argument | Description | 
| --- | ----------- |
| columns | Comma separated with possible values : Health, Job, Status, Percentage, Total, Pass, Fail, Skip, Commits, LastRun, Duration, Description, Packages, Files, Classes, Methods, Lines, Conditions, Sonar, Build | 
| jobs | <p>List of 3 items: jobName, jobFriendlyName and groupName.<br><br>jobName is mandatory, it's the exact Jenkins job name to get results. In case of a job inside a 'folder' use: folderName/jobName, for multi-folders use folder path for example folder1/folder2/jobName.<br><br>jobFriendlyName is optional, and is in use only for reporting purposes, if null or empty then "Job Name" will be used in the report.<br><br>groupName is optional, it's used in reports to group Jenkins jobs. For example teams, products, or testing types.<p> |  
| recipientsList |Comma separated recipients list, ex : 'nick@some.com,mairy@some.com' If empty or blank no email will be triggered. Supports job variables, for example '${my_parameter_for_mail}'. | 
| recipientsListCC |Comma separated recipients list CC, ex : 'nick@some.com,mairy@some.com' If empty or blank no email will be triggered. Supports job variables. | 
| recipientsListBCC |Comma separated recipients list Bcc, ex : 'nick@some.com,mairy@some.com' If empty or blank no email will be triggered. Supports job variables. | 
| recipientsListIgnored|Comma separated recipients list for ignored Jobs, ex : 'nick@some.com,mairy@some.com' If empty or blank no email will be triggered. |
| subject |Mail Subject prefix. Supports job & env variables. |
| beforebody|Text before mail body. Static text or HTML code. Supports also job & env variables , for example ${WORKSPACE} or ${myVariable} |
| afterbody|Text after mail body. Static text or HTML code. Supports also job & env variables , for example ${WORKSPACE} or ${myVariable} |
| theme|Mail theme , possible values are: light, dark |
| sortresults|Sort Results using one of the following available options: Job Name, Job Status, Total Tests, Pass Tests, Failed Tests, Skipped Tests, Percentage, Commits, Time Stamp, Duration, Build Number |
| outOfDateResults|Completed Jenkins Jobs with results more than X hours ago will be marked with 'red' color under 'Last Run' column report. Otherwise if blank or empty then column 'Last Run' will just have the timestamp of job completion. |
| compareWithPreviousRun|Compare next run with the previous regarding job statuses, tests results and code coverage metrics. If false then no differences are displayed in report , no signs + - ,options true/false |
| ignoreAbortedJobs|Ignore from report jobs with status ABORTED. Options true/false |
| ignoreDisabledJobs|Ignore from report jobs with status DISABLED. Options true/false |
| ignoreNotFoundJobs|Ignore from report jobs with status NOT_FOUND. Options true/false |
| ignoreRunningJobs|Ignore from report jobs with status RUNNING. Options true/false. If false then jobs are reported with status RUNNING. If true then jobs are reported with the previous status and results. Moreover an asterisk at the side of status declares that the job is still running. |
