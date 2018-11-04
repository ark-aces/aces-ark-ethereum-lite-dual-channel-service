# ACES Ark-Ethereum Lite Dual Channel Service

ACES Ark to Ethereum lite dual channel web service using Geth light sync client 
to provide ARK-ETH and ETH-ARK transfer channels. 

## Set Ethereum Client

### Install Go Ethereum client (geth)

```
sudo add-apt-repository -y ppa:ethereum/ethereum
sudo apt-get update

sudo apt-get install software-properties-common build-essential
sudo apt-get install ethereum
```

### Start geth (testnet)

```
geth --testnet --syncmode "light" --datadir "/data/ethereum-testnet/" \
--rpc --rpcapi eth,web3,personal --cache=1024  --rpcport 8545
```

### Connect to geth ipc command line (testnet)

```
geth --testnet attach ipc:/data/ethereum-testnet/geth.ipc
```

### Create service ethereum account

Connect to geth ipc command line and run the following to create a new account:

```
var password = "change-me";
personal.newAccount(password);
```


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
curl http://localhost:9190/ethereumArkChannel
```


Create a new Service Contract:

```
curl -X POST http://localhost:9190/ethereumArkChannel/contracts \
-H 'Content-type: application/json' \
-d '{
  "arguments": {
    "recipientArkAddress": "..."
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
    "recipientArkAddress": "...",
    "depositEthAddress": "...",
    "transfers": []
}
```

Get Contract information after sending ARK funds to `depositEthAddress`:

```
curl -X GET http://localhost:9190/ethereumArkChannel/contracts/{id}
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientArkAddress": "...",
    "depositEthAddress": "...",
    "transfers" : [ {
      "id" : "uDui0F8PIjldKyGm0rdd",
      "status" : "new",
      "createdAt" : "2018-01-21T20:24:52.057Z",
      "ethTransactionId" : "...",
      ...
    } ]
  }
}
```