# apprenticeship-levy

[![Build Status](https://travis-ci.org/hmrc/apprenticeship-levy.svg?branch=master)](https://travis-ci.org/hmrc/apprenticeship-levy)
[![Download](https://api.bintray.com/packages/hmrc/releases/apprenticeship-levy/images/download.svg) ](https://bintray.com/hmrc/releases/apprenticeship-levy/_latestVersion)
[![Download](https://img.shields.io/badge/Download-Production%20Preview-orange.svg)](https://github.com/hmrc/apprenticeship-levy/releases/download/2.31.1/apprenticeship-levy-2.31.1.zip)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## Background

The Skills Funding Agency (**SFA**) is a sub-department of the Department for Business, Innovation and Skills (**BIS**) responsible for, amongst other things, providing funding to employers for approved apprenticeship training schemes. From April 2017 a new Apprenticeship Levy will be applied to organisations with large payrolls (broadly speaking, payrolls in excess of £3 million). HMRC will be collecting the levy, but the SFA has responsibility for ensuring that the employers who paid the levy have access to those funds to spend on apprentices. The purpose of this api is to allow the Digital Apprenticeship Service (**DAS**), being built by the SFA, to access information about the levy that an organisation has paid. This document describes the API that is being built on the Multi-Channel Digital Tax Platform (**MDTP**) to serve DAS.

An employer makes levy declarations as part of an Employer Payment Summary (**EPS**) submission to the PAYE RTI system.  From April 2017 the schema for the EPS will be extended to include fields relating to the levy. These submissions are made in respect of a payroll scheme identified by an Employer Reference (**empref**) and will form the primary source of data for this API. See the [HMRC page about EPS](https://www.gov.uk/guidance/what-payroll-information-to-report-to-hmrc#eps-what-to-report) for details about what data appears on an EPS submission. For even more detail, including the specific XML schema and business rules that are applied to EPS submission, see the [RTI information pages for software developers](https://www.gov.uk/government/collections/real-time-information-online-internet-submissions-support-for-software-developers). The technical specifications sections for each tax year contain links to a `.zip` file that includes the XML Schema file and Schematron rules for the tax year. As of the date of writing this document the 2017/18 rules have not been defined so no specific information is available yet about the fields relating to the apprenticeship levy.

Most submissions will include a year-to-date (**YTD**) figure for the total levy declared in the current tax year. However, there may be times that no levy or values for other fields are relevant, in which case the employer might file an EPS with an indication of a Period of Inactivity or No Payment for Period. The levy api needs to be able to reflect these situations out to DAS.

## Using this Service

### Local Development
For local development either download the preview release or clone this repository and run using the instructions below as a guide.

The endpoints are:

* [/](./docs/get-all-employers.bak.md) which returns a list of valid 'empref' values used in the remaining 5 endpoints
* [/epaye/{empref}](./docs/get-employer-details.bak.md) which returns a list of endpoints for the empref and employer contact details
* [/epaye/{empref}/declarations](./docs/get-employer-levy-declarations.bak.md) which returns a list of Apprenticeship Levy amounts
* [/epaye/{empref}/fractions](./docs/get-employer-fraction-calculations.bak.md) which returns a list of employee location distributions
* [/epaye/{empref}/employed/<nino>](./docs/get-employment-status.bak.md) which returns true if employee is employed with the employer and false otherwise
* [/fraction-calculation-date](./docs/get-latest-fraction-calculation-date.bak.md) which returns the date the last time HMRC systems were updated with new fraction values

## Running

### Running Preview Release

The preview release is configured to fetch sandbox data from itself on port 9470 but additional sandbox data can be added to test various scenarios. To run the API:

* create a new directory to hold additional sandbox test data files
* run using `./bin/apprenticeship-levy -Dextra.sandbox-data.dir=<path-to-your-extra-test-data-here> -Dhttp.port=9470`
* add additional test data as required following examples in `https://github.com/hmrc/apprenticeship-levy/tree/master/public/sandbox-data`

### Building & Running in Development
#### Pre-requisites

You will require [SBT 1.10.2](http://www.scala-sbt.org/download.html) to be installed on your machine. At present there are no other dependencies required by this service.

#### Build

On a command line simply use `sbt clean compile` to compile

#### Run

On a command line use `sbt run`

#### Testing

##### Coverage Report
To run with coverage `sbt clean coverage test it/test` and an HTML report will be available in the target/scala-2.11/scoverage-report/index.html
directory.

##### Acceptance Testing (Separate from internal A/C testing)

Run in staging using `sbt '; set javaOptions ++= Seq("-Denvironment=staging", "-Dbearer.token.staging=<bearer token>"); ac:test'`

Run in qa using `sbt '; set javaOptions ++= Seq("-Denvironment=qa", "-Dbearer.token.qa=<bearer token>"); ac:test'`

Run in local using `sbt '; set javaOptions ++= Seq("-Denvironment=local", "-Dbearer.token.local=<bearer token>"); ac:test'`

##### Integration Testing

Run using `sbt it/test`

##### Unit Testing

Run using `sbt test`


