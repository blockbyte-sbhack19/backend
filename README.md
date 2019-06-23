# LendIt backend

3 corda nodes:
* Lender
* Leaser
* Notar
Note Corda is working on offering B2C capabilities while still keeping the number of node low.

2 webserver controller
* com.blockbyte.web.controllers.LeaserController
  * `api/leaser/soils`         return all parcels
  * `api/leaser/soil/filter`   used in frontend for searching parcels matching criteria
  * `api/leaser/soil/lease`    used in frontend for renting parcel
  * `api/leaser/soil/free`     used in frontend for freeing parcel
  
* com.blockbyte.web.controllers.LenderController
  * `api/lender/soil`         used in frontend for offering a new parcel for renting