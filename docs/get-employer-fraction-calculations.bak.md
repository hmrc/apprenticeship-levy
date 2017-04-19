# Get employer fraction calculations
Returns a list of fraction calculations for a given employer reference.

HMRC will calculate the English Fraction values for all PAYE schemes on a regular, but infrequent, basis, most likely quarterly. The estimate for the number of schemes that are connected to DAS accounts in the first year is placed at 33,000, rising to 60,000 after three years, so refreshing the English Fraction values will be costly, and not something we’d want to do on a daily basis given how infrequently the values are updated.

This API endpoint will let DAS ask the HMRC system for the date that the most recent English Fraction batch calculation was done. DAS can call this endpoint on a frequent basis, perhaps daily, store the date and only refresh the scheme values when it changes.
      



## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/epaye/{empref}/fractions?fromDate={from}&toDate={to}

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
|fromDate|Date|The date of the earliest calculation to return. Defaults to 72 months prior to current date.|`2015-08-09`|
|toDate|Date|The date of the latest calculation to return. Defaults to current date.|`2015-08-09`|


### Response


#### Successful Response

```json
{
        "empref": "123/AB12345",
        "fractionCalculations": [
            {
                "calculatedAt": "2016-03-15",
                "fractions": [
                    {
                        "region": "England",
                        "value": 0.83
                    }
                ]
            },
            {
                "calculatedAt": "2015-11-18",
                "fractions": [
                    {
                        "region": "England",
                        "value": 0.78
                    }
                ]
            }
          ]
}
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|empref|String|The PAYE Reference for the employer. This will be the same as provided in the URL.|`123/AB12345`|
|fractionCalculations[].calculatedAt|Date|The date that the fractions were calculated|`2015–11–18`|
|fractionCalculations[].fractions[].region|String|The region the specific fraction applies to. Will always be England for the forseeable future.|`England`|
|fractionCalculations[].fractions[].value|Number|The fraction calculated for the region. Will be a decimal in the range 0 to 1.|`0.83`|


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
    <td>BAD_REQUEST</td>
    <td>A request parameter is incorrect or unknown, or from date is after to date.</td>
    <td><code>{
  "code": "BAD_REQUEST",
  "message": "From date was after to date"
}</code></td></tr><tr><td>400 Bad Request</td>
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
    <td>Employer reference (EMPREF) request parameter is unknown.</td>
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
    <td>Endpoint or internal system has become unavailable.</td>
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
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth 5xx error: GET of '....' returned 500. Response body: ''"
}</code></td></tr></table>