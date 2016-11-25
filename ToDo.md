
## General

- [ ] Check pence values in declarations
- [ ] Update error codes
- [ ] Unit tests where integration tests aren't in place
- [ ] Add date filtering to sandbox data endpoint
- [ ] Update configuration for service manager to retire apprenticeship-levy-stub
- [ ] Add caching to documentation controller
- [ ] Remove play 2.5 warnings
- [x] Connectors failing backend tests
- [x] Prune unused code
- [x] Consider converting config to be updatable on the fly (AppContext)
- [x] :question: Confirm useage of headercarrier
- [x] Write tests for bindable local date
- [x] Consider consolidating data/domain classes into single package hierarchy
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
- [x] Convert xmls to single xml source
- [ ] XSLT to produce definition.json
- [x] XSLT to produce endpoint xmls
- [ ] XSLT to produce RAML files
- [ ] XSLT to produce MD files

## Metrics
- [x] Add tests for audit events
- [x] Add tests for metric events

