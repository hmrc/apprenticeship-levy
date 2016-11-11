
## General

- [x] Connectors failing backend tests
- [ ] Why is live responding differently with same requests as sandbox e.g. 401 v 404?
- [x] Prune unused code
- [ ] Add caching to documentation controller
- [x] Consider converting config to be updatable on the fly (AppContext)
- [x] :question: Confirm useage of headercarrier
- [v] Write tests for bindable local date
- [x] Consider consolidating data/domain classes into single package hierarchy
- [ ] Unit tests where integration tests aren't in place
- [ ] Add date filtering to sandbox data endpoint
- [ ] Update configuration for service manager to retire apprenticeship-levy-stub
- [x] scalastyle
- [x] possibly remove ala stub and serve data from a sandbox data endpoint in case api platform requests stub to be removed

## :bangbang: Backend endpoints
- [x] Add wiremock stub responses for each endpoint with same data as stub
- Integration tests (both happy/unhappy paths and both live/sandbox)
  - [x] Auth
  - [x] EDH
  - [x] EPAYE
  - [x] EPS
  - [x] RTI
  - [x] Service Locator
  - [x] Update all tests once backend endpoint specs are in place
  - [x] Employment check for not employed
  - [x] Employment check for not known
  - [] Declarations for different date ranges

## :bangbang: API endpoints
- Integration tests (both happy/unhappy paths)
  - [x] root
  - [x] empref
  - [x] declarations
  - [x] fractions
  - [x] fraction dates
  - [x] employment check
  - [x] documentation
  - [x] api definition

## Documentation to RAML
- [ ] Convert xmls to single xml source
- [ ] XSLT to produce definition.json
- [ ] XSLT to produce endpoint xmls
- [ ] XSLT to produce RAML

## Metrics
- [ ] Add tests for audit events
- [ ] Add tests for metric events
