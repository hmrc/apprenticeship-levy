# Employment Check
## Resource
	GET /apprenticeship-levy/epaye/empref/{empref}/employed/{nino}?fromDate={date}&toDate={date}
	
## Path Parameters

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The Employer Reference for the PAYE scheme. Because this value contains a `/` character it needs to be escaped when used in the url, e.g. `123%02FAB12345` |
| nino | String | AB1234567 | The National Insurance number for the individual to check |

## Query Parameters
| Name | Type | Example | Default | Description |
|:-|:-|:-|:-|:-|
| fromDate | Date | 2016-03-06 | none | The start date for which the check should be done. Mandatory. |
| toDate | Date | 2016-04-05 | none | The end date for which the check should be done. Mandatory |

## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

Departmental Access with TOTP (time-based one-time password) in cases where the application needs to carry out a check for a scheme where the OAuth access token has expired.

## Accepted Media Types
    application/vnd.hmrc.1.0+json

## Response
```
{
    “empref” : “123/AB12345”,
    “nino” : “AB123456A”,
    “fromDate” : “2016-03-06”,
    “toDate” : “2016-04-05”,
    “employed” : true
}
```

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The empref of the scheme the declarations relate to. This will be the same as the empref provided in the URL |
| nino | String | AB123456A | The nino of the individual that is being checked. This will be the same as the nino provided in the url |
| fromDate | Date | 2016-03-06 | The start date for which the check is being carried out. This will be the same value as the query parameter that was passed in.|
| endDate | Date | 2016-03-06 | The end date for which the check is being carried out. This will be the same value as the query parameter that was passed in. |
| employed | boolean | true | Whether the individual was employed in the payroll scheme at any time during the given date range |

## Error Scenarios

| Error Scenario | HTTP Status | Code |
|:-|:-|:-|
| Invalid EMRPEF | 400 (Bad Request) | EPAYE_EMPREF_INVALID |
| Invalid NINO | 400 (Bad Request) | EPAYE_NINO_INVALID |
| Unknown EMPREF | 404 (Not Found) | EPAYE_EMPREF_UNKNOWN |
| Unknown EMPREF | 404 (Not Found) | EPAYE_NINO_UNKNOWN |