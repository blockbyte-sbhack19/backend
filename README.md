# LendIt backend

The project Lend-It uses private DLT capabilities to connect lenders and borrowers on P2P basis. 
The private network allows to keep high level of anonymity, and at the same time it creates trusted infrastructure for 
business connections. 

Lend-It is built on top of DLT R3 Corda.

#####  Anonymity & Privacy
R3 Corda networks use point-to-point messaging instead of a global broadcast. The messaging system calls flow.
The coordinating a ledger update requires network participants to specify exactly 
what information needs to be sent, to which counterparties, and in what order. A flow is a sequence of steps that tells 
a node how to achieve a specific ledger update, such as offering the land to the platform or lease it.

R3 Corda is a privacy-preserving platform and it provides security on all layers, anonymity on communication, 
immutability and double spending guarantees, cryptography keys randomisation, SGX supports and others. 

#####  Protocol driven design 
R3 Corda is oriented to build comprehensive protocols by design. The protocol may include any number of 
independent and untrusted participants. This functionality guarantees quick adoption of technology
as for enterprise players as for small local farmers. 

The project Lend-It can be easily put on top of already existing infrastructure as overlay without 
destructing any ongoing business processes. 
 
#####  Scalability 
R3 Corda uses Quasar, ArtemisMQ and other technology which makes possible to run unlimited number of applications (cordApps)
simultaneously. The consensus is reached by dedicated consortium, or participants are allowed to run it own. 

##### Digital Identity 
Both borrowing farmer and lending farmer are digitially identified once before onboarding on the network 
and using the application. Currently it's done by PKI and X500 certificates, but the next milestone of the project is to
combine it with Self-Sovereing Identity (SSI), specially Hyperledger Indy. Combination of these technologies brings plenty of 
use cases based on Zero Knowledge Proofs and verifiable credentials. 

The integration can be easily done by using Cordentity (https://github.com/hyperledger-labs/cordentity). 

##### Industrial potential
The permission DLT network allows to grow to other use cases, where data isolation requirements are higher 
and security need to be stronger. The architecture design of Lend-It focuses on potential grows to Truck & Trace solution, 
digital payment and loyalty programs and credit of letter scenarios.
 
##### Pluggable design
The nature of the selected technology gives efficient way to mix Lend-It with already existing solutions, 
such payments rails (SWIFT, XRP, etc.), trans-boarding transactions, insurance platfroms, etc. 

The platforms provides Oracles facility out of the box and can be be integrated with data services very quick.
 
##### The Corda 
Each business role obtain individual Cortda node and mobile application. There are following roles supported:
* Borrowing Farmer
* Lending Farmer
* Service Provider
* Notary

The future statement is to have:
* Financial institution
* IoT-Management service and IoT-Devices

As a PoC we integrated with Insurance network on top of Ethereum.

There are 4 flows:

For Lender:
1. RequestForListingFlow - it initiates the land on-boarding procedure. 
Point to improvement: during this procedure the SSI check will be implemented to illuminate any manual step which exists now.

For Borrowing:
2. LookupLandsFlow - it runs a query to get available lands offered by lenders 
3. RequestForLeaseFlow - it initiates the deal between Lender and Borrower on P2P basis
4. CompleteLeaseFlow - it breaks the lease. 
 
There are following endpoints: 
* `com.blockbyte.web.controllers.LeaserController`
  * `GET  api/leaser/soils`         return all parcels
  * `POST api/leaser/soil/filter`   for searching parcels matching criteria
  * `POST api/leaser/soil/lease`    for renting parcel
  * `POST api/leaser/soil/free`     for freeing parcel
  
* `com.blockbyte.web.controllers.LenderController`
  * `POST api/lender/soil`         for offering a new parcel for renting
  
##### Network configuration
The network is served with 3 indepened business Corda Nodes (Borrower, Lender, Provider) and 1 infrastructure node (Notary).
The infrastructure one solves double spending and other issues. In future use cases it will be adjusted to run BFT algorithms. 
Each Corda Node has individual web-server application on Spring. 


##### Code running (examples)
There are following endpoints can be riched out via Postman or our mobile application
 
* Offer the Land: post / http://52.209.35.41:8081/api/lender/soil 
* Search the Lands: post / http://52.209.35.41:8081/api/leaser/soil/filter 
* Lease the Land: post / http://52.209.35.41:8081/api/leaser/soil 