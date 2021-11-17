# Get latest fraction calculation date
Returns the date of the most recent fraction calculation batch run.

HMRC will calculate the English Fraction values for all PAYE schemes on a regular, but infrequent, basis, most likely quarterly. The estimate for the number of schemes that are connected to DAS accounts in the first year is placed at 33,000, rising to 60,000 after three years, so refreshing the English Fraction values will be costly, and not something we’d want to do on a daily basis given how infrequently the values are updated.

This API endpoint will let consumers of this API (namely DAS) to ask the HMRC system for the date that the most recent English Fraction batch calculation was completed. DAS can call this endpoint on a frequent basis, perhaps daily, store the date and only refresh the scheme values when it changes.

The response will take the form of a date, in the standard `YYYY-MM-DD` format used by MTDP APIs. The HMRC system will ensure that the fraction values for all schemes have been recalculated before updating the calculation date.

## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/fraction-calculation-date

### Request
#### Headers
| Name | Value |
| --- | --- |
|Accept|application/vnd.hmrc.1.0+json|
|Authorization|Bearer [token here]|

### Response


#### Successful Response

```json
"2016-04-05"
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|Date|String|Year-Month-Day date of last calculation of fraction values.|`"2017-04-06"`|


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
<tr><td>401 Unauthorized</td>
    <td>INVALID_CREDENTIALS</td>
    <td>The request requires correct authentication headers with valid token.</td>
    <td><code>{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid Authentication information provided"
}</code></td></tr><tr><td>401 Unauthorized</td>
    <td>DES_ERROR_UNAUTHORIZED</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "DES_ERROR_UNAUTHORIZED",
  "message": "Auth unauthorised error"
}</code></td></tr><tr><td>403 Forbidden</td>
    <td>DES_ERROR_FORBIDDEN</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "DES_ERROR_FORBIDDEN",
  "message": "Auth forbidden error"
}</code></td></tr><tr><td>404 Not Found</td>
    <td>DES_ERROR_NOT_FOUND</td>
    <td>Endpoint or internal system has become unavailable.</td>
    <td><code>{
              "code":"DES_ERROR_NOT_FOUND",
              "message":"DES endpoint not found"
            }</code></td></tr><tr><td>408 Request Time-out</td>
    <td>DES_ERROR_REQUEST_TIMEOUT</td>
    <td>Endpoint or internal system has become unresponsive.</td>
    <td><code>{
  "code": "DES_ERROR_REQUEST_TIMEOUT",
  "message": "DES not responding error: Not responding"
}</code></td></tr><tr><td>429 Too Many Requests</td>
    <td>DES_ERROR_TOO_MANY_REQUESTS</td>
    <td>Too many requests have been made to the back-end systems.</td>
    <td><code>{
  "code": "DES_ERROR_TOO_MANY_REQUESTS",
  "message": "DES too many requests"
}</code></td></tr><tr><td>500 Internal server error</td>
    <td>DES_ERROR_API</td>
    <td>API has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR_API",
  "message": "Auth 5xx erro"
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR_IO</td>
    <td>Internal system has become unresponsive causing an IO error.</td>
    <td><code>{
  "code": "DES_ERROR_IO",
  "message": "DES not responding error: Not responding"
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR_OTHER</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR_OTHER",
  "message": "Auth 5xx error"
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR_BACKEND_FAILURE</td>
    <td>Backend has experiences a server 5xx error.</td>
    <td><code>{
  "code": "DES_ERROR_BACKEND_FAILURE",
  "message": "Auth 5xx error"
}</code></td></tr></table>