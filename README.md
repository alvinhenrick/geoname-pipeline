## Geo names ingest pipeline


## Prerequisite
JAVA Version 1.8
SBT Version 1.1.5
SCALA Version 2.11.12
SPARK Version 2.2.1
SOLR Version 7.2.1
SPARK Solr Connector Version 3.4.0

## Description
1. The purpose of this project is to do the POC to ingest and index data for easy search.
2. It has support for geo spatial search [SpatialSearch] **nearest neighbors** or **full-text by name**.
3. Apache Solr can be configured in cloud mode (Multiple Solr server servers) can be easily scaled up by increasing server nodes.
4. The collection can be configured with **shards (no of partitions)** and **replicas (fault tolerance)**
5. The requirement to handle schema evolution can be done by Solr [Managed Schema Configuration]
6. The `id` attribute which is derived from `geonameid` will take care of updating the collection for future updates and schema evolution as describe above.
7. We can store binary data [Binary Data Store] such as Shape Files into Solr Document.
8. We can also convert shape file into GeoJSON format and then ingest it into Solr for future processing and updates.


## Setup
1. Download the specified Apache Solr Version mentioned in prerequisite section.
2. Unzip the folder and copy it to some location on the disk.
3. Change to Solr Home Directory
    ```bash
       cd solr-7.2.1 
    ```
4. Start Solr Server in cloud mode
    ```bash
       bin/solr start -cloud
    ```
5. Create collection for storage and indexing
    ```bash
       bin/solr create -c test_collect_2
    ```
6. Create schema
    ```bash
       curl -X POST -H 'Content-type:application/json' --data-binary '{
         "add-field":[
            {
             "name":"administrativeLevel1",
             "type":"string",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"administrativeLevel2",
             "type":"string",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"countryCode",
             "type":"string",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"latitude",
             "type":"pfloat",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"location",
             "type":"location",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"longitude",
             "type":"pfloat",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true},
           {
             "name":"name",
             "type":"string",
             "docValues":true,
             "multiValued":false,
             "indexed":true,
             "stored":true
             }]
       }' http://localhost:8983/solr/test_collect_2/schema
    ```

7. Build the project
    ```bash
       sbt clean assembly
    ```
8. Index geo locations
    ```bash
       spark-submit \
       --master "local[*]" \
       --class com.geoname.IndexGeoData \
       --driver-memory "1g" \
       target/geoname-pipeline-assembly-0.1.jar \
       /Users/shona/IdeaProjects/geoname-pipeline/data/cities1000.txt
    ```
9. Search By Name
   ```bash
       curl "http://localhost:8983/solr/test_collect_2/select?q=name:Saint-*"
   ```
10. Search Nearest Neighbors By Great Circle Distance Box **geofilt** and Filter By Radius 10.
    ```bash
       curl "http://localhost:8983/solr/test_collect_2/select?d=10&fq=\{!geofilt%20sfield=location\}&pt=47.10247,5.26556&q=*:*&sfield=location"
     ```
12. Search Nearest Neighbors By Bounding Box Distance **bbox** and Filter By Radius 5.
    ```bash
       curl "http://localhost:8983/solr/test_collect_2/select?d=5&fq=\{\!bbox%20sfield=location\}&pt=47.10247,5.26556&q=*:*&sfield=location"
     ```

**NOTE**: Considering its a POC I am ignoring shape file which can be easily ingested into Solr document as is Binary form or converted to GeoJSON.

[Managed Schema Configuration]:https://lucene.apache.org/solr/guide/7_2/schema-factory-definition-in-solrconfig.html#solr-uses-managed-schema-by-default
[SpatialSearch]: https://lucene.apache.org/solr/guide/7_2/spatial-search.html#SpatialSearch-RPT
[Binary Data Store]: https://lucene.apache.org/solr/guide/6_6/field-types-included-with-solr.html
