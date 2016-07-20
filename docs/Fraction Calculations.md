# Regional Fraction Calculations

## Resource
    GET /apprenticeship-levy/epaye/{empref}/fractions[?months=n]

## Path Parameters
| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The Employer References for the PAYE scheme |

## Query Parameters
| Name | Type | Example | Default | Description |
|:-|:-|:-|:-|:-|
| months | Number | 12 | 48 | Restrict the number of months of data returned. If the value is greater than 48 then it will be ignored. |

## Authorisation
OAuth 2.0 Bearer Token with the `read:apprenticeship-levy` scope.

Departmental Access with TOTP (time-based one-time password) in cases where the application needs to track amendments for schemes where the OAuth access token has expired.

## Accepted Media Types
    application/vnd.hmrc.1.0+json

## Response
```
{
	“empref”: “123/AB12345”,
	“fractionCalculations”: [
		{
			“calculatedAt”: “2016-03-15”,
			“fractions”: [
				{
					“region”: “England”,
					“value”: “0.83”
				}
			]
		},
		{
			“calculatedAt”: “2015-11-18”,
			“fractions”: [
				{
					“region”: “England”,
					“value”: “0.78”
				}
			]
		}
	]	
}
```

| Name | Type | Example | Description |
|:-|:-|:-|:-|
| empref | String | 123/AB12345 | The empref of the scheme the declarations relate to. This will be the same as the empref provided in the URL |
| fractionCalculations.calculatedAt | Date | 2015-11-18 | The date that the fractions were calculated |
| fractions.region | String | England | The region the specific fraction applies to. For the present this value will always be England. In the future the API may be extended to support reporting of the fraction values for Wales, Scotland and Northern Ireland |
| fractions.value | Number | 0.83 | The fraction calculated for the region. Will be a decimal in the range 0 to 1. |
 
 