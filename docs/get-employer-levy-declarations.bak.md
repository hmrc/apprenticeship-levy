# Get employer levy declarations
Returns a list of levy declarations for a given employer reference.



## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/epaye/{empref}/declarations?fromDate={from}&toDate={to}

### Request
#### Headers
| Name | Value |
| --- | --- |
|Accept|application/vnd.hmrc.1.0+json|
|Authorization|Bearer [token here]|


#### Path Parameters
| Name | Type | Value | Example |
| --- | --- | --- | --- |
|empref|String|A valid (URL encoded) employer reference for the PAYE scheme.|`123/AB12345`|



#### Query Parameters
| Name | Type | Value | Example |
| --- | --- | --- | --- |
|fromDate|Date|The date of the earliest record to return. If no date is supplied the default date of current month/year - 6 years will be used.|`2015-08-09`|
|toDate|Date|The date of the latest record to return. If no date is supplied the default date of 'now'/'today' will be used.|`2015-08-09`|


### Response


#### Successful Response

```json
{
            "empref" : "123/AB12345",
            "declarations" : [
                {
                    "id" : 12345684,
                    "submissionTime" : "2016-10-15T16:05:23.000",
                    "dateCeased" : "2016-09-05"
                },
                {
                    "id" : 12345683,
                    "submissionTime" : "2016-07-15T16:05:23.000",
                    "inactiveFrom" : "2016-06-06",
                    "inactiveTo" : "2016-09-05"
                },
                {
                    "id" : 12345682,
                    "submissionTime" : "2016-06-15T16:05:23.000",
                    "payrollPeriod" : {
                        "year" : "16-17",
                        "month" : 2
                    },
                    "levyDueYTD" : 200,
                    "levyAllowanceForFullYear" : 15000
                },
                {
                    "id" : 12345681,
                    "submissionTime" : "2016-06-07T16:05:23.000",
                    "payrollPeriod" : {
                        "year" : "16-17",
                        "month" : 2
                    },
                    "levyDueYTD" : 1000,
                    "levyAllowanceForFullYear" : 15000
                },
                {
                    "id" : 12345680,
                    "submissionTime" : "2016-05-07T16:05:23.000",
                    "payrollPeriod" : {
                        "year" : "16-17",
                        "month" : 1
                    },
                    "levyDueYTD" : 500,
                    "levyAllowanceForFullYear" : 15000
                },
                {
                    "id" : 12345679,
                    "submissionTime" : "2015-04-07T16:05:23.000",
                    "payrollPeriod" : {
                        "year" : "15-16",
                        "month" : 12
                    },
                    "noPaymentForPeriod" : true
                    },
                {
                    "id" : 12345678,
                    "submissionTime" : "2016-03-07T16:05:23.000",
                    "payrollPeriod" : {
                        "year" : "15-16",
                        "month" : 11
                    },
                    "levyDueYTD" : 600,
                    "levyAllowanceForFullYear" : 15000
                }
            ]
    }
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|empref|String|The PAYE Reference for the employer. This will be the same as provided in the URL|`123/AB12345`|
|declarations[].id|Number|A unique identifier for the declaration. This will remain consistent from one call to the API to the next so that the client can identify declarations they’ve already retrieved. Dividing this identifier by 10 (ignoring the remainder) gives the identifier assigned by the RTI system to the EPS return, so it is possible to cross-reference with HMRC if needed. Taking this identifier modulo 10 gives the type of entry: 0, no entry; 1, inactive; 2, levy declaration; 3, ceased.|`12345684`|
|declarations[].dateCeased|Date|If present, indicates the date that the payroll scheme was ceased|`2016–03–17`|
|declarations[].inactiveFrom|Date|The date after which the payroll scheme will be active again. Should always be the 5th of the month of the last inactive payroll period|`2016–09–05`|
|declarations[].inactiveTo|Date|The date after which the payroll scheme will be active again. Should always be the 5th of the month of the last inactive payroll period|`2016–09–05`|
|declarations[].noPaymentForPeriod|Boolean|If present, will always have the value true and indicates that no declaration was necessary for this period. This can be interpreted to mean that the YTD levy balance is unchanged from the previous submitted value|`[true`|
|declarations[].submissionTime|DateTime|The time at which the EPS submission that this declaration relates to was received by HMRC. If the backend systems return a bad date that can not be handled this will be set to 1970-01-01T01:00:00.000.|`2016–02–21T16:05:23.000.000`|
|declarations[].payrollPeriod.year|String|The tax year of the payroll period against which the declaration was made|`15-16`|
|declarations[].payrollPeriod.month|Number|The tax month of the payroll period against which the declaration was made. Month 1 is April.|`1`|
|declarations[].levyDueYTD|Number|The amount of apprenticeship levy that was declared in the payroll month.|`600.20`|
|declarations[].allowance|Number|The annual amount of apprenticeship levy allowance that has been allocated to this payroll scheme. If absent then the value can be taken as 0. The maximum value in the 2017/18 will be 15,000|`15000`|


#### Error Responses
<table>
  <thead>
    <tr>
      <td>Code &amp; Name</td>
      <td>Value</td>
      <td>Description</td>
      <td>Example</td>
    </tr>
  </thead>
  <tbody>
<tr><td>400 Bad Request</td>
    <td>EMPREF_INVALID</td>
    <td>Employer reference (EMPREF) request parameter is incorrect.</td>
    <td><code>{
  "statusCode": "400",
  "message": "EMPREF_INVALID: '...' is in the wrong format. Should be ^\\d{3}/[0-9A-Z]{1,10}$ and url encoded."
}</code></td></tr><tr><td>400 Bad Request</td>
    <td>DATE_INVALID</td>
    <td>Date(s) request parameter(s) is incorrect.</td>
    <td><code>{
  "statusCode": "400",
  "message": "DATE_INVALID: '.....' date parameter is in the wrong format. Should be '^(\\d{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$' where date is yyyy-MM-dd and year is 2000 or later."
}</code></td></tr><tr><td>400 Bad Request</td>
    <td>EPAYE_EMPREF_INVALID</td>
    <td>Employer reference (EMPREF) request parameter is incorrect.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth unauthorised error: GET of '....' returned 401. Response body: ''"
}</code></td></tr><tr><td>401 Unauthorized</td>
    <td>INVALID_CREDENTIALS</td>
    <td>The request requires correct authentication headers with valid token.</td>
    <td><code>{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid Authentication information provided"
}</code></td></tr><tr><td>401 Unauthorized</td>
    <td>DES_ERROR</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth unauthorised error: GET of '...' returned 401. Response body: ''"
}</code></td></tr><tr><td>403 Forbidden</td>
    <td>DES_ERROR</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth forbidden error: GET of '...' returned 403. Response body: ''"
}</code></td></tr><tr><td>404 Not Found</td>
    <td>EPAYE_EMPREF_UNKNOWN</td>
    <td>Employer reference is unknown or endpoint not found.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth endpoint not found: GET of '....' returned 404 (Not Found). Response body: ''"
}</code></td></tr><tr><td>408 Request Time-out</td>
    <td>DES_ERROR</td>
    <td>Endpoint or internal system has become unresponsive.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth not responding error: GET of '...' timed out with message 'Request timeout to localhost/127.0.0.1:8080 after 500 ms'"
}</code></td></tr><tr><td>429 Too many requests</td>
    <td>DES_ERROR</td>
    <td>Too many requests have been made to this endpoint</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Too many requests"
}</code></td></tr><tr><td>500 Internal Server Error</td>
    <td>DES_ERROR_JSON_FAILURE</td>
    <td>DES or backend system has returned invalid json which can not be relayed to API caller.</td>
    <td><code>{
  "code": "DES_ERROR_JSON_FAILURE",
  "message": "DES and/or BACKEND server returned bad json."
}</code></td></tr><tr><td>500 Internal Server Error</td>
    <td>DES_ERROR_BACKEND_FAILURE</td>
    <td>DES returned unexpected Json error response type on a HTTP 200 response.</td>
    <td><code>{
  "code": "DES_ERROR_BACKEND_FAILURE",
  "message": "DES backend error: DES returned error code object on HTTP 200 response (treating as error). DES error reason: '<message from RTI here>'."
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth 5xx error: GET of '....' returned 500. Response body: ''"
}</code></td></tr></table>