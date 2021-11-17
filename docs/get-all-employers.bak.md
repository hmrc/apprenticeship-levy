# Get all employers
Returns a list of valid links indexed by empref in HAL format

Departmental Access with a Time-based One-Time Password (**TOTP**) in cases where the application needs to track amendments for schemes where the OAuth access token has expired is *NOT* applicable to this endpoint.

## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

## API
GET https://api.service.hmrc.gov.uk/apprenticeship-levy/

### Request
#### Headers
| Name | Value |
| --- | --- |
|Accept|application/vnd.hmrc.1.0+json|
|Authorization|Bearer [token here]|

### Response


#### Successful Response

```json
{
    "_links": {
        "self": {
            "href": "/"
        },
        "123/AB12345": {
            "href": "/epaye/123%2FAB12345"
        }
    },
    "emprefs": [
        "123/AB12345"
    ]
}
```

| Name | Type | Description | Example |
| ---  | ---  | ---         | ---     |
|_links.self|URL|The url to this endpoint|`/`|
|_links.{empref}|URL|The url to an endpoint that provides more information about the empref|`/epaye/123%2FAB12345`|
|emprefs[]|String[]|A list of emprefs for which links are provided|`["123/AB12345"]`|


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
    <td>AUTH_ERROR_UNAUTHORIZED</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "AUTH_ERROR_UNAUTHORIZED",
  "message": "Auth unauthorised error"
}</code></td></tr><tr><td>403 Forbidden</td>
    <td>AUTH_ERROR_FORBIDDEN</td>
    <td>The request requires user authentication. Please ensure Grant authority has been given and bearer token is supplied with the request headers.</td>
    <td><code>{
  "code": "AUTH_ERROR_FORBIDDEN",
  "message": "Auth forbidden error"
}</code></td></tr><tr><td>404 Not Found</td>
    <td>AUTH_ERROR_NOT_FOUND</td>
    <td>Endpoint or internal system has become unavailable.</td>
    <td><code>{
  "code": "AUTH_ERROR_NOT_FOUND",
  "message": "Auth endpoint not found"
}</code></td></tr><tr><td>408 Request Time-out</td>
    <td>AUTH_ERROR_REQUEST_TIMEOUT</td>
    <td>Endpoint or internal system has become unresponsive.</td>
    <td><code>{
  "code": "AUTH_ERROR_REQUEST_TIMEOUT",
  "message": "Auth not responding error"
}</code></td></tr><tr><td>408 Gateway Request Time-out</td>
    <td>AUTH_ERROR_GATEWAY_TIMEOUT</td>
    <td>Endpoint or internal system has become unresponsive.</td>
    <td><code>{
  "code": "AUTH_ERROR_GATEWAY_TIMEOUT",
  "message": "Auth not responding error"
}</code></td></tr><tr><td>409 Internal API error</td>
    <td>AUTH_ERROR_TOO_MANY_REQUESTS</td>
    <td>Endpoint API has experienced too many requests.</td>
    <td><code>{
  "code": "AUTH_ERROR_TOO_MANY_REQUESTS",
  "message": "Auth too many requests"
}</code></td></tr><tr><td>498 Wrong token supplied.</td>
    <td>AUTH_ERROR_WRONG_TOKEN</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "AUTH_ERROR_WRONG_TOKEN",
  "message": "Auth unauthorised error: OAUTH 2 User Token Required not TOTP"
}</code></td></tr><tr><td>500 Internal API error</td>
    <td>AUTH_ERROR_INTERNAL_SERVER_ERROR</td>
    <td>Endpoint API has experienced an internal error.</td>
    <td><code>{
  "code": "AUTH_ERROR_INTERNAL_SERVER_ERROR",
  "message": "API or Auth internal server error: ...."
}</code></td></tr><tr><td>503 Service Unavailable</td>
    <td>AUTH_ERROR_BACKEND_FAILURE</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "AUTH_ERROR_BACKEND_FAILURE",
  "message": "Auth 5xx error"
}</code></td></tr><tr><td>503 Other backend system error with 4xx reponse.</td>
    <td>AUTH_ERROR_OTHER</td>
    <td>Endpoint or internal system has experienced an internal error.</td>
    <td><code>{
  "code": "AUTH_ERROR_OTHER",
  "message": "Auth 5xx error"
}</code></td></tr><tr><td>503 Backend system IO failure</td>
    <td>AUTH_ERROR_IO</td>
    <td>Endpoint API has experienced IO failure</td>
    <td><code>{
  "code": "AUTH_ERROR_IO",
  "message": "Auth connection error"
}</code></td></tr></table>