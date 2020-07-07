# data-ingestion-lib
Data ingestion lib creates reusable framework library with Spring boot and Apache Camel Integration to read CSV files from Azure Blob
and transform/converts and stores it in spring microservice project database.

This library has been published in bin tray with git push actions on new release (https://bintray.com/hmcts/hmcts-maven/data-ingestion-lib).

# To build the project in local execute the following command
./gradlew build 

# How to use library
Common library properties like email settings configured in library and customized properties should be configured with specific 
microservices eg. rd-judicial-data-load (https://github.com/hmcts/rd-judicial-data-load)





