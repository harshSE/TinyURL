![HLD](HLD.png)
##Services:

###**Random Generation services**
Random generation services creates random number and pushed to distributed set tobe utilised by creation services.
* Random is min 8 digit value contains A-Z,0-9.
* Random services instance communicated with zookeeper server and listen for events.
* On Start up, each random service picks digit/alphabets from which it starts generating value.
Ex: let's say service start picks value 'A'. So it will start generating value starting for A incrementally. A, AA, AB, AZ,...,A9,.....,A999999999. This service periodically push the value to set in batch.
* To avoid conflict in picking starting number or tracking generated numbers, 
all service instance connected with zookeeper. From zookeepr, all service keep in sync.
* Each service periodically writes last generated sequence in zookeeper before pushing values into distributed set.

###**Conversion Service**
Conversion service convert actual url into tiny url.
* On Receiving request, Conversion service pop from the distributed set and map to tiny url. 
* Created tiny URL put into distributed queue tobe utilized by key-value store NoSQL DB. 
* Once service push the value into queue, service add the entry into distributed cache backed by key-value NoSQL DB, 
where key is actual URL and value is tiny URL.
* This cache will be used to avoid regeneration of tiny URL for received actual URL.


###**QueryService**
Query service provides actual URL for given tiny service.
* Query service uses cache backed by key-value NoSQL DB to fetch actual URL to tiny url.

## Storage:
###Tiny URL -> Actual URL (LRU Cache + Key-Value NoSQL DB)
Query service uses this storage to fetch actual ulr by providing tiny url. 

* Creation service uses this storage in read-only mode only.
* On Cache miss, Cache query Key-Value NoSQL DB to fetch actual url. 
* It stores tiny URL created by conversion service to actual URL. 
* It uses queue to load data, so there is eventual consistency in storing data  
* Cache is LRU such that most frequently used key stored in cache.
 

###Actual URL -> Tiny URL (LRU Cache + Key-Value NoSQL DB)
Conversion service uses this DB to avoid regeneration of large ulr for which tiny url is already generated.

* DB instance store actual url received in request to tiny URL.
* It uses queue to load data, so there is eventual consistency in storing data
* We assume that recently created ULR can be queried again, so that FIFO queue can be useful here. 

### Distributed Set(Can we use queue over here, how can we ensure duplication then?)
Set is used by conversion service to pop generated randoms by random generation service in batch.

* Set will ensure that one random can not be pop by multiple conversation service.

##Other Component

### Load Balancer
Load balancer has multiple responsibility.

* Route request to appropriate service.
* Load Balance requests between service instances.
* Timeout the request is response is not received within time limit.
* Does resend request required???

### Queue
Queue works as single source of truth. Both Key-Value NoSQL DB uses queue to populate data.

###  Zookeeper Cluster
Zookeeper cluster uses to communicate between cluster and store the state. 

* This cluster used by Queue if we use kafka
* Random Generation service uses kafka to 
  * store last generated random and use it during restart
  * If multiple service run, it will be used to pick different series of random 
  to avoid duplication of random









