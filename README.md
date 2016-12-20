# apprenticeship-levy

[![Build Status](https://travis-ci.org/hmrc/apprenticeship-levy.svg?branch=master)](https://travis-ci.org/hmrc/apprenticeship-levy)
[![Download](https://api.bintray.com/packages/hmrc/releases/apprenticeship-levy/images/download.svg) ](https://bintray.com/hmrc/releases/apprenticeship-levy/_latestVersion)
[![Download](https://img.shields.io/badge/Download-Production%20Preview-orange.svg)](https://github.com/hmrc/apprenticeship-levy/releases/download/2.31.1/apprenticeship-levy-2.31.1.zip)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

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

##### Integration Testing

Run using `sbt it:test`

##### Unit Testing

Run using `sbt test`

#### Turning on Full Stack Traces

Turn on full stacktrace in sbt console using `set testOptions in "apprenticeship-levy" += Tests.Argument("-oF")`

#### Other

Scalastyle is enabled for this project. To run use `sbt scalastyle` on the command line.
