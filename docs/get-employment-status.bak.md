# Get employment status
Checks the employment status of an individual in a payroll scheme.



## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/epaye/{empref}/employed/{nino}?fromDate={date}& toDate={date}

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
|nino|String|A valid National Insurance Number (nino) for the individual being checked.|`XY654321Z`|



#### Query Parameters
| Name | Type | Value | Example |
| --- | --- | --- | --- |
|fromDate|Date|Starting date of the period to check employment for.|`2016-01-31`|
|toDate|Date|Ending date of the period to check employment for.|`2016-01-31`|


### Response


#### Successful Response

```json
{
                "empref" : "123/AB12345",
                "nino" : "XY654321Z",
                "fromDate" : "2016-03-06",
                "toDate" : "2016-04-05",
                "employed" : true
}
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|empref|String|The PAYE Reference for the employer. This will be the same as provided in the URL.|`123/AB12345`|
|nino|String|The NINO of the individual being checked. This will be the same as provided in the URL.|`XY654321Z`|
|fromDate|Date|The start date of the range the check should be made for.|`[2016-03-06]`|
|toDate|Date|The end date of the range the check should be made for.|`[2016-04-05]`|
|employed|Boolean|Whether or not the individual was employed in the scheme at any time with the date range.|`[true]`|


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
    <td>EPAYE_EMPREF_INVALID</td>
    <td>A request parameter is incorrect or unknown, or from date is after to date.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth unauthorised error: GET of '....' returned 401. Response body: ''"
}</code></td></tr><tr><td>401 Unauthorized</td>
    <td>AUTH_ERROR</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth unauthorised error: GET of '...' returned 401. Response body: ''"
}</code></td></tr><tr><td>403 Forbidden</td>
    <td>AUTH_ERROR</td>
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
    <td>AUTH_ERROR</td>
    <td>Endpoint or internal system has become unresponsive.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth not responding error: GET of '...' timed out with message 'Request timeout to localhost/127.0.0.1:8080 after 500 ms'"
}</code></td></tr><tr><td>429 Too many requests</td>
    <td>AUTH_ERROR</td>
    <td>Too many requests have been made to this endpoint</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Too many requests"
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>AUTH_ERROR</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth 5xx error: GET of '....' returned 500. Response body: ''"
}</code></td></tr></table>