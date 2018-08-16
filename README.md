# apprenticeship-levy

[![Build Status](https://travis-ci.org/hmrc/apprenticeship-levy.svg?branch=master)](https://travis-ci.org/hmrc/apprenticeship-levy)
[![Download](https://api.bintray.com/packages/hmrc/releases/apprenticeship-levy/images/download.svg) ](https://bintray.com/hmrc/releases/apprenticeship-levy/_latestVersion)
[![Download](https://img.shields.io/badge/Download-Production%20Preview-orange.svg)](https://github.com/hmrc/apprenticeship-levy/releases/download/2.31.1/apprenticeship-levy-2.31.1.zip)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

## Background

The Skills Funding Agency (**SFA**) is a sub-department of the Department for Business, Innovation and Skills (**BIS**) responsible for, amongst other things, providing funding to employers for approved apprenticeship training schemes. From April 2017 a new Apprenticeship Levy will be applied to organisations with large payrolls (broadly speaking, payrolls in excess of £3 million). HMRC will be collecting the levy, but the SFA has responsibility for ensuring that the employers who paid the levy have access to those funds to spend on apprentices. The purpose of this api is to allow the Digital Apprenticeship Service (**DAS**), being built by the SFA, to access information about the levy that an organisation has paid. This document describes the API that is being built on the Multi-Channel Digital Tax Platform (**MDTP**) to serve DAS.

An employer makes levy declarations as part of an Employer Payment Summary (**EPS**) submission to the PAYE RTI system.  From April 2017 the schema for the EPS will be extended to include fields relating to the levy. These submissions are made in respect of a payroll scheme identified by an Employer Reference (**empref**) and will form the primary source of data for this API. See the [HMRC page about EPS](https://www.gov.uk/guidance/what-payroll-information-to-report-to-hmrc#eps-what-to-report) for details about what data appears on an EPS submission. For even more detail, including the specific XML schema and business rules that are applied to EPS submission, see the [RTI information pages for software developers](https://www.gov.uk/government/collections/real-time-information-online-internet-submissions-support-for-software-developers). The technical specifications sections for each tax year contain links to a `.zip` file that includes the XML Schema file and Schematron rules for the tax year. As of the date of writing this document the 2017/18 rules have not been defined so no specific information is available yet about the fields relating to the apprenticeship levy.

Most submissions will include a year-to-date (**YTD**) figure for the total levy declared in the current tax year. However, there may be times that no levy or values for other fields are relevant, in which case the employer might file an EPS with an indication of a Period of Inactivity or No Payment for Period. The levy api needs to be able to reflect these situations out to DAS.

## Using this Service

### Production
For production usage register and log in on [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation). Create an application and notify HMRC of new application ID.

### Local Development
For local development either download the preview release or clone this repository and run using the instructions below as a guide.

You can use [curl](https://curl.haxx.se/) to send http requests directly to the service. There are six REST endpoints support GET requests on on http://127.0.0.1:9470/sandbox and http://127.0.0.1:9470/ which will return a JSON response object. Each endpoint will be documented in fully shortly here but full documentation is available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation).

In brief however the endpoints are:

* [/](./docs/get-all-employers.bak.md) which returns a list of valid 'empref' values used in the remaining 5 endpoints
* [/epaye/{empref}](./docs/get-employer-details.bak.md) which returns a list of endpoints for the empref and employer contact details
* [/epaye/{empref}/declarations](./docs/get-employer-levy-declarations.bak.md) which returns a list of Apprenticeship Levy amounts
* [/epaye/{empref}/fractions](./docs/get-employer-fraction-calculations.bak.md) which returns a list of employee location distributions
* [/epaye/{empref}/employed/<nino>](./docs/get-employment-status.bak.md) which returns true if employee is employed with the employer and false otherwise
* [/fraction-calculation-date](./docs/get-latest-fraction-calculation-date.bak.md) which returns the date the last time HMRC systems were updated with new fraction values

#### Examples
* `curl -vvv -H "Accept: application/vnd.hmrc.1.0+json" http://localhost:9470`
* `curl -vvv -H "Accept: application/vnd.hmrc.1.0+json" http://localhost:9470/epaye/840%2FMODES17`
* `curl -vvv -H "Accept: application/vnd.hmrc.1.0+json" http://localhost:9470/epaye/123%2FAB12345/declarations`
* `curl -vvv -H "Accept: application/vnd.hmrc.1.0+json" http://localhost:9470/epaye/123%2FAB12345/fractions`
* `curl -vvv -H "Accept: application/vnd.hmrc.1.0+json" http://localhost:9470/fraction-calculation-date`

## Running

### Running Preview Release

The preview release is configured to fetch sandbox data from itself on port 9470 but additional sandbox data can be added to test various scenarios. To run the API:

* create a new directory to hold additional sandbox test data files
* run using `./bin/apprenticeship-levy -Dextra.sandbox-data.dir=<path-to-your-extra-test-data-here> -Dhttp.port=9470`
* add additional test data as required following examples in `https://github.com/hmrc/apprenticeship-levy/tree/master/public/sandbox-data`

### Building & Running in Development
#### Pre-requisites

You will require [SBT 0.13.11](http://www.scala-sbt.org/download.html) to be installed on your machine. At present there are no other dependencies required by this service.

#### Build

On a command line simply use `sbt clean dist` to create a distribution or `sbt clean compile` to compile

#### Run

On a command line use `sbt run`

A different port can be specified as follows `sbt "run 9010"` update [application.conf](https://github.com/hmrc/apprenticeship-levy/blob/master/conf/application.conf#L238) ports for various services defined where appropriate.

### Running Demo Files

* Host the two demo html files e.g. `python -m SimpleHTTPServer 9000`
* Run web browser temporarily without cross site scripting security e.g. `chrome --disable-web-security --user-data-dir`
* Update `demo.html` file with client id and secret
* Open page in browser and follow on screen steps

##### Debugging

Debugging requires two steps as follows:

1. On a command line pass in the -jvm-debug <port> to sbt like so: `sbt -jvm-debug 5005 run`
2. In an editor that supports remote debugging start the remote debug on port 5005 with listening to socket.
    - For IntelliJ this is accomplished by selecting "Edit Configurations..." in the toolbar
    - Click '+' button to add new 'Remote' configuration
    - Ensure 'socket', and 'attach' are selected
    - Set host to either localhost or to 0.0.0.0 and port to 5005

#### Testing

##### Coverage Report
To run with coverage `sbt clean coverage test it:test` and an HTML report will be available in the target/scala-2.11/scoverage-report/index.html
directory.

##### Acceptance Testing (Separate from internal A/C testing)

Run using `sbt -Denvironment=qa -Dbearer.token.qa=<your token here> ac:test`

##### Integration Testing

Run using `sbt it:test`

##### Unit Testing

Run using `sbt test`

#### Turning on Full Stack Traces

Turn on full stacktrace in sbt console using `set testOptions in "apprenticeship-levy" += Tests.Argument("-oF")`

#### Other

Scalastyle is enabled for this project. To run use `sbt scalastyle` on the command line.
