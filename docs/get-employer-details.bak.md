# Get employer details
Returns more details about an empref including details about the employer and a list of available endpoints that apply to the empref.

Departmental Access with a Time-based One-Time Password (**TOTP**) in cases where the application needs to track amendments for schemes where the OAuth access token has expired is *NOT* applicable to this endpoint.

## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/epaye/{empref}

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


### Response


#### Successful Response

```json
{
        "_links": {
            "self": {
                "href": "/epaye/123%2FAB12345"
            },
            "declarations": {
                "href": "/epaye/123%2FAB12345/declarations"
            },
            "fractions": {
                "href": "/epaye/123%2FAB12345/fractions"
            },
            "employment-check": {
                "href": "/epaye/123%2FAB12345/employed"
            }
        },
        "empref" : "123/AB12345",
        "employer": {
            "name": {
                "nameLine1": "Foo Bar Ltd."
            }
        }
    }
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|_links.self|URL|The link to this endpoint|`/epaye/123%2FAB12345`|
|_links.declarations|URL|The link to the endpoint that returns information on levy declarations for the empref|`/epaye/123%2FAB12345/declarations`|
|_links.fractions|URL|The link to the endpoint that returns information on fraction calculations for the empref|`[/epaye/123%2FAB12345/fractions]`|
|_links.employment-check|URL|The link to the endpoint that returns information whether an employee was employed at a specific time by the empref|`/epaye/123%2FAB12345/employed]`|
|employer.name.nameLine1|String|The name associated to this empref|`Foo Bar Ltd.`|


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
    <td>A request parameter is incorrect.</td>
    <td><code>{
  "statusCode": "400",
  "message": "EMPREF_INVALID: '...' is in the wrong format. Should be ^\\d{3}/[0-9A-Z]{1,10}$ and url encoded."
}</code></td></tr><tr><td>400 Bad Request</td>
    <td>EPAYE_EMPREF_INVALID</td>
    <td>A request parameter is incorrect.</td>
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
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>DES_ERROR</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "DES_ERROR",
  "message": "Auth 5xx error: GET of '....' returned 500. Response body: ''"
}</code></td></tr></table>