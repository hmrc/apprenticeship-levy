# Fraction Calculation Batch Date API
## Introduction
HMRC will calculate the English Fraction values for all PAYE schemes on a regular, but infrequent, basis, most likely quarterly. The estimate for the number of schemes that are connected to DAS accounts in the first year is placed at 33,000, rising to 60,000 after three years, so refreshing the English Fraction values will be costly, and not something we’d want to do on a daily basis given how infrequently the values are updated.

This API endpoint will let DAS ask the HMRC system for the date that the most recent English Fraction batch calculation was done. DAS can call this endpoint on a frequent basis, perhaps daily, store the date and only refresh the scheme values when it changes.

## API Endpoint
    GET /apprenticeship-levy/fraction-calculation-date
    
## Authorisation
None

## Accepted Media Types
    application/vnd.hmrc.1.0+json

## Response
```2017-04-05```

The response will take the form of a date, in the standard `YYYY-MM-DD` format used by MTDP APIs. The HMRC system will ensure that the fraction values for all schemes have been recalculated before updating the calculation date.