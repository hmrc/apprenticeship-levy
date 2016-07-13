# Employment Check
## Resource
	GET /employment-check/epaye/empref/{empref}/employed/{nino}?atDate={date}
	
## Path Parameters

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The Employer Reference for the PAYE scheme |
| nino | String | AB1234567 | The National Insurance number for the individual to check |

## Query Parameters
| Name | Type | Example | Default | Description |
|:-|:-|:-|:-|:-|
| atDate | Date | 2016-03-15 | the current date | The date for which the check should be done. Optional. If not present then the current date will be used. |

## Authorisation
OAuth 2.0 Bearer Token with the `read:employment-check` scope.

Departmental Access with TOTP (time-based one-time password) in cases where the application needs to carry out a check for a scheme where the OAuth access token has expired.

## Accepted Media Types
    application/vnd.hmrc.1.0+json

## Response
```
{
    “empref” : “123/AB12345”,
    “nino” : “AB123456A”,
    “date” : “2016-03-15”,
    “employed” : true
}
```

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The empref of the scheme the declarations relate to. This will be the same as the empref provided in the URL |
| nino | String | AB123456A | The nino of the individual that is being checked. This will be the same as the nino provided in the url |
| date | date | 2016-03-15 | The date for which the check is being carried out. Will match the date from the query parameter if that was supplied. |
| employed | boolean | true | Whether the individual was employed in the payroll scheme on the given date |

## Error Scenarios

| Error Scenario | HTTP Status | Code |
|:-|:-|:-|
| Invalid EMRPEF | 400 (Bad Request) | EPAYE_EMPREF_INVALID |
| Invalid NINO | 400 (Bad Request) | EPAYE_NINO_INVALID |
| Unknown EMPREF | 404 (Not Found) | EPAYE_EMPREF_UNKNOWN |