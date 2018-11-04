# ACES Ark-Ethereum Lite Dual Channel Service

ACES Ark to Ethereum lite dual channel web service using Geth light sync client 
to provide ARK-ETH and ETH-ARK transfer channels. 

https://electrumx.readthedocs.io/en/latest/protocol.html

## Set up local database

```
docker run -d -p 5432:5432 \
--name aces_ark_ethereum_lite_dual_channel_service_db \
-e POSTGRES_PASSWORD=password \
-e POSTGRES_USER=postgres \
-e POSTGRES_DB=aces_ark_ethereum_lite_dual_channel_service_db \
postgres:9.6.1
```


### Configuration

Copy [src/main/resources/application.yml](src/main/resources/application.yml) into an external file on your system
(for example: `/etc/opt/{service-name}/application.yml`) and replace configuration properties to match your
local setup. For example, you would need to change the service address and passphrase to an actual account.


### Run Channel Service

```
mvn clean spring-boot:run --spring.config.location=file:/etc/{service-name}/application.yml
```

### Run Channel Service (production)


To run the application in a live environment, you can build a jar package using `mvn package` and then
run the jar app generated under `/target` build directory with you custom configuration:

```
java -jar {jar-name}.jar --spring.config.location=file:/etc/{service-name}/application.yml
```


## Using the Service

Get service info:

```
curl http://localhost:9190/
```
```
{
  "name" : "ACES ARK-BTC Lite Channel Service",
  "description" : "ACES ARK to BTC Channel service for transferring ARK to BTC",
  "version" : "1.0.0",
  "websiteUrl" : "https://arkaces.com",
  "instructions" : "After this contract is executed, any ARK sent to depositArkAddress will be exchanged for BTC and sent directly to the given recipientBtcAddress less service fees.\n",
  "flatFee" : "0.0001",
  "flatFeeUnit": "ARK",
  "percentFee" : "1.00",
  "capacities": [{
    "value": "50.00",
    "unit": "BTC"
  }],
  "inputSchema" : {
    "type" : "object",
    "properties" : {
      "recipientBtcAddress" : {
        "type" : "string"
      }
    },
    "required" : [ "recipientBtcAddress" ]
  },
  "outputSchema" : {
    "type" : "object",
    "properties" : {
      "depositArkAddress" : {
        "type" : "string"
      },
      "recipientBtcAddress" : {
        "type" : "string"
      },
      "transfers" : {
        "type" : "array",
        "properties" : {
          "arkAmount" : {
            "type" : "string"
          },
          "arkToBtcRate" : {
            "type" : "string"
          },
          "arkFlatFee" : {
            "type" : "string"
          },
          "arkPercentFee" : {
            "type" : "string"
          },
          "arkTotalFee" : {
            "type" : "string"
          },
          "btcSendAmount" : {
            "type" : "string"
          },
          "btcTransactionId" : {
            "type" : "string"
          },
          "createdAt" : {
            "type" : "string"
          }
        }
      }
    }
  }
}
```

Create a new Service Contract:

```
curl -X POST http://localhost:9190/contracts \
-H 'Content-type: application/json' \
-d '{
  "arguments": {
    "recipientBtcAddress": "mu7gjSBLssPhKYuYU4qqBGFzjbh7ZTA6uY"
  }
}' 
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientBtcAddress": "mu7gjSBLssPhKYuYU4qqBGFzjbh7ZTA6uY",
    "depositArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers": []
}
```

Get Contract information after sending ARK funds to `depositArkAddress`:

```
curl -X GET http://localhost:9190/contracts/{id}
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientBtcAddress": "mu7gjSBLssPhKYuYU4qqBGFzjbh7ZTA6uY",
    "depositArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers" : [ {
      "id" : "uDui0F8PIjldKyGm0rdd",
      "status" : "new",
      "createdAt" : "2018-01-21T20:24:52.057Z",
      "arkTransactionId" : "78b6c99c40451d7e46f2eb41cdb831d087fecd759b01e00fd69e34959b5bee25",
      "arkAmount" : "1.96545690",
      "arkToBtcRate" : "1985.31000000",
      "arkFlatFee" : "0.00000000",
      "arkPercentFee" : "1.00000000",
      "arkTotalFee" : "0.00001000",
      "btcSendAmount" : "0.00100000"
    } ]
  }
}
```