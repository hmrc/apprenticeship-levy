# Apprenticeship Levy Declarations API

## Introduction
The Skills Funding Agency (**SFA**) is a sub-department of the Department for Business, Innovation and Skills (**BIS**) responsible for, amongst other things, providing funding to employers for approved apprenticeship training schemes. From April 2017 a new Apprenticeship Levy will be applied to organisations with large payrolls (broadly speaking, payrolls in excess of £3 million). HMRC will be collecting the levy, but the SFA has responsibility for ensuring that the employers who paid the levy have access to those funds to spend on apprentices. The purpose of this api is to allow the Digital Apprenticeship Service (**DAS**), being built by the SFA, to access information about the levy that an organisation has paid. This document describes the API that is being built on the Multi-Channel Digital Tax Platform (**MDTP**) to serve DAS.

An employer makes levy declarations as part of an Employer Payment Summary (**EPS**) submission to the PAYE RTI system.  From April 2017 the schema for the EPS will be extended to include fields relating to the levy. These submissions are made in respect of a payroll scheme identified by an Employer Reference (**empref**) and will form the primary source of data for this API. See the [HMRC page about EPS](https://www.gov.uk/guidance/what-payroll-information-to-report-to-hmrc#eps-what-to-report) for details about what data appears on an EPS submission. For even more detail, including the specific XML schema and business rules that are applied to EPS submission, see the [RTI information pages for software developers](https://www.gov.uk/government/collections/real-time-information-online-internet-submissions-support-for-software-developers). The technical specifications sections for each tax year contain links to a `.zip` file that includes the XML Schema file and Schematron rules for the tax year. As of the date of writing this document the 2017/18 rules have not been defined so no specific information is available yet about the fields relating to the apprenticeship levy.

Most submissions will include a year-to-date (**YTD**) figure for the total levy declared in the current tax year. However, there may be times that no levy or values for other fields are relevant, in which case the employer might file an EPS with an indication of a Period of Inactivity or No Payment for Period. The levy api needs to be able to reflect these situations out to DAS.

## API Endpoint
    GET /apprenticeship-levy/epaye/{empref}/declarations[?months=n]

## Path Parameters
| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The Employer Reference for the PAYE scheme. Because this value contains a `/` character it needs to be escaped when used in the url, e.g. `123%02FAB12345` |
## Query Parameters
| Name | Type | Example | Default | Description |
|:-|:-|:-|:-|:-|
| months | Number | 12 | 72 | Restrict the number of months of data returned. If the value is greater than 72 then the api will use a value of 72. |
## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

Departmental Access with a Time-based One-Time Password (**TOTP**) in cases where the application needs to track amendments for schemes where the OAuth access token has expired.

## Accepted Media Types
    application/vnd.hmrc.1.0+json

## Response
```
{
    "empref" : "123/AB12345",
    "declarations" : [
        {
            "id" : 12345684,
            "submissionTime" : "2016-10-15T16:05:23",
            "dateCeased" : "2016-09-05"            
        },
        {
            "id" : 12345683,
            "submissionTime" : "2016-07-15T16:05:23",
            "inactiveFrom" : "2016-06-06",
            "inactiveTo" : "2016-09-05"
        },    
        {
            "id" : 12345682,
            "submissionTime" : "2016-06-15T16:05:23",
            "payrollPeriod" : {
                "year" : "16-17",
                "month" : 2
            },
            "amount" : 200,
            "allowance" : 15000
        },
        {
            "id" : 12345681,
            "submissionTime" : "2016-06-07T16:05:23",
            "payrollPeriod" : {
                "year" : "16-17",
                "month" : 2
            },
            "amount" : 1000,
            "allowance" : 15000
        },
        {
            "id" : 12345680,
            "submissionTime" : "2016-05-07T16:05:23",
            "payrollPeriod" : {
                "year" : "16-17",
                "month" : 1
            },
            "amount" : 500,
            "allowance" : 15000
        },
        {
            "id" : 12345679,
            "submissionTime" : "2015-04-07T16:05:23",
            "payrollPeriod" : {
                "year" : "15-16",
                "month" : 12
            },
            "noPaymentForPeriod" : true
        },
        {
            "id" : 12345678,
            "submissionTime" : "2016-03-07T16:05:23",
            "payrollPeriod" : {
                "year" : "15-16",
                "month" : 11
            },
            "amount" : 600,
            "allowance" : 15000
        }
    ]
}
```

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The empref of the scheme the declarations relate to. This will be the same as the empref provided in the URL |
| id | Number | 12345684 | A unique identifier for the declaration. This will remain consistent from one call to the API to the next so that the client can identify declarations they’ve already retrieved. It is the identifier assigned by the RTI system to the EPS return, so it is possible to cross-reference with HMRC if needed |
| dateCeased | Date | 2016-03-17 | If present, indicates the date that the payroll scheme was ceased |
| inactiveFrom | Date | 2016-06-06 | Indicates the the payroll scheme will be inactive starting from this date. Should always be the 6th of the month of the first inactive payroll period |
| inactiveTo | Date | 2016-09-05 | The date after which the payroll scheme will be active again. Should always be the 5th of the month of the last inactive payroll period |
| noPaymentForPeriod | Boolean | true | Will always have the value `true` and indicates that no declaration was necessary for this period. This can be interpreted to mean that the YTD levy balance is unchanged from the previous submitted value |
| submissionTime | DateTime | 2016-02-21Z16:05:23 | the time at which the EPS submission that this declaration relates to was received by HMRC |
| payrollPeriod.year | Number | 15-16 | The tax year of the payroll period against which the declaration was made |
| payrollPeriod.month | Number | 1 | The tax month of the payroll period against which the declaration was made. Month 1 is April. |
| amount | Number | 600.20 | The amount of apprenticeship levy that was declared in the payroll month. |
| allowance | Number | 15000 | The annual amount of apprenticeship levy allowance that has been allocated to this payroll scheme. If absent then the value can be taken as 0. The maximum value in the 2017/18 will be 15,000 |

## Error Scenarios

| Error Scenario | HTTP Status | Code |
|:-|:-|:-|
| Invalid EMRPEF | 400 (Bad Request) | EPAYE_EMPREF_INVALID |
| Unknown EMPREF | 404 (Not Found) | EPAYE_EMPREF_UNKNOWN |

## Response details
There are some points to note about the response:
* Entries will be sorted in reverse chronological order of the submission time. In other words, the entry with the most recent timestamp will be first in the list.
* Each entry has a unique id that will be consistent between calls to the API. This is the id allocated to the EPS submission by the RTI system.

The sample response shown above covers a number of different scenarios that can result from EPS submissions, which I’ll cover here:
### Regular payment declaration
```
{
    "id" : 12345678,
    "submissionTime" : "2016-03-07Z16:05:23",
    "payrollPeriod" : {
        "year" : "15-16",
        "month" : 11
    },
    "amount" : 600,
    "allowance" : 15000
}
```

This entry is the typical case that references a payroll month and an amount. The amount represents the total levy liability for the scheme year-to-date. In general this amount will increase month-to-month by the additional amount due each month. It is possible for the amount to decrease from a previous month for a number of reasons, for example:

* The employer realised they made an error on an earlier EPS for the year and adjusts the YTD figure on the most recent EPS to correct the error
* For some reason (such as a reduction in staff) the amount of levy due for the month has reduced from the previous month and the levy allowance allocated for the month more than covers the liability for the month.
* The employer has taken unused levy allowance that had been allocated to a different scheme and re-allocated it to this scheme. This would normally only happen in month 12.

The `allowance` figure is the annual amount of apprenticeship levy allowance that has been allocated to this payroll scheme. It should remain consistent throughout the tax year, but may change in month 12 to indicate that the employer has reallocated their allowance to or from different schemes (though note that there is no enforced business rule that would prevent them from re-allocating in-year, only HMRC guidance). The employer is supposed to use 1/12th of this value to offset their levy liability for any given month.

### Two (or more) submissions in a tax month
```
[
    {
        "id" : 12345682,
        "submissionTime" : "2016-06-15Z16:05:23",
        "payrollPeriod" : {
            "year" : "16-17",
            "month" : 2
        },
        "amount" : 200,
        "allowance" : 15000
    },
    {
        "id" : 12345681,
        "submissionTime" : "2016-06-07Z16:05:23",
        "payrollPeriod" : {
            "year" : "16-17",
            "month" : 2
        },
        "amount" : 1000,
        "allowance" : 15000
    }
]
```

In this case the employer has submitted an EPS on June 7th and then submitted another one on June 15th with a different amount. The second submission is consider to replace the first one completely. The amount will have been adjusted for similar reasons to the previous scenario.
### Employer declares no liability for a period
```
{
    "id" : 12345679,
    "submissionTime" : "2015-04-07Z16:05:23",
    "payrollPeriod" : {
        "year" : "15-16",
        "month" : 12
    },
    "noPaymentForPeriod" : true
}
```

In this case the employer has indicated that no payments are due for the period. In other words, the YTD figure has not changed from the previous EPS submission.
### Employer declares a period of inactivity
```
{
    "id" : 12345683,
    "submissionTime" : "2016-07-15Z16:05:23",
    "inactiveFrom" : "2016-06-06",
    "inactiveTo" : "2016-09-05"
}
```

Here, the employer has indicated that they will not be filing any EPS submissions in the period defined by the given dates. The `inactiveFrom` date will be the start of the next tax month, i.e. the 6th of the next month, and the `inactiveTo` will be up to 12 months after the `inactiveFrom` date.

### The payroll scheme has been terminated
```
{
    "id" : 12345684,
    "submissionTime" : "2016-10-15Z16:05:23",
    "dateCeased" : "2016-09-05"            
}
```

There are various reasons why the payroll scheme may be terminated, most commonly because the employer has chosen to end it. There should never be any EPS submissions after the `dateCeased`