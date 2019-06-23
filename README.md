# LendIt backend

## 3 Corda nodes:
* Lender
* Leaser
* Notar
Note Corda is working on offering B2C capabilities while still keeping the number of node low.

## 3 webserver controller
Used by our frontend 
* `com.blockbyte.web.controllers.LeaserController`
  * `api/leaser/soils`         return all parcels
  * `api/leaser/soil/filter`   for searching parcels matching criteria
  * `api/leaser/soil/lease`    for renting parcel
  * `api/leaser/soil/free`     for freeing parcel
  
* `com.blockbyte.web.controllers.LenderController`
  * `api/lender/soil`         for offering a new parcel for renting
  
* `com.blockbyte.web.controllers.InsuranceController` (started not completed)
  * `api/insurance/payPremium`   by farmer to pay insurance premium (1 ETH)
  * `api/insurance/collectInsuredPremium` by farmer to collect insured premium (2 ETH)
  * `api/insurance/numberOfDayWithoutRain` to display number of days without rain
  
## Code running (examples) 
* Offer the Land: post / http://52.209.35.41:8081/api/lender/soil 
* Search the Lands: post / http://52.209.35.41:8081/api/leaser/soil/filter 
* Lease the Land: post / http://52.209.35.41:8081/api/leaser/soil 
* Start insurance for 60 days: post / http://52.209.35.41:8081/api/insurance/payPremium