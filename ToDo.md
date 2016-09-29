
## General

- [] Add caching to documentation controller
- [] Consider converting config to be updatable on the fly (AppContext)
- [] :question: Confirm useage of headercarrier
- [] Write tests for bindable local date
- [] Consider consolidating data/domain classes into single package hierarchy
- [] Unit tests where integration tests aren't in place
- [] scalastyle

## :bangbang: Backend endpoints
  - [] Add wiremock stub responses for each endpoint with same data as stub
  - Integration tests (both happy/unhappy paths and both live/sandbox)
    - [] Auth
    - [] EDH
    - [] EPAYE
    - [] EPS
    - [] RTI
    - [] Service Locator
    - [] Update all tests once backend enpoint specs are in place

## :bangbang: API endpoints
  - Integration tests (both happy/unhappy paths)
    - [] root
    - [] empref
    - [] declarations
    - [] fractions
    - [] fraction dates
    - [] employment check
    - [x] documentation
    - [x] api definition

## Documentation to RAML
  - [] Convert xmls to single xml source
  - [] XSLT to produce definition.json
  - [] XSLT to produce endpoint xmls
  - [] XSLT to produce RAML

## Metrics
  - [] Add tests for audit events
  - [] Add tests for metric events
