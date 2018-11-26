REST server for testing a client implementation of the [suv-service-interface](https://github.com/electrumpayments/suv-service-interface).

## Testing a Server(TODO)
Testing a server implementation can be achieved using [this](https://github.com/electrumpayments/suv-service-test-pack/tree/master/test/postman) Postman (Collection v2) REST test pack.
These tests consist of correctly formatted JSON messages that validate server responses. Tests may also consist of a message flow in which multiple related messages are sent sequentially to the server to test handling of state-full interactions (such as requests and confirmations).  

The test pack is comprised of JSON files: `Suv.postman_collection.json`, `heroku.postman_environment.json` and `localhost.postman_environment.json`.
The postman_collection files are a collection of JSON tests that will be run. They contain JSON request messages and response validation scripts. These tests are dependant on variables contained in the the associated postman_environment files.

### Running tests

There are two possible ways to run this test pack: either via the Postman desktop client or via Newman, the command line interface for Postman.

#### Postman
1. Download Postman at: https://www.getpostman.com/apps
2. Import the test collection and environments via the Import option
3. Open the Collection Runner and select the Runs tab
4. Select a test collection and environment and hit Start Test. Note that individual test subsections may be selected.

Note that that tests may be run individually from the main Postman view where test conditions and structures may be modified.

#### Newman
1. Install newman (make sure Node Package Manager is installed first):
	`npm install newman -g`
2. Run the tests:
	`newman run Airtime.postman_collection.json -e localhost.postman_environment.json`
3. This will run all tests against an Airtime Service server implementation hosted on localhost:8080 and provide a basic breakdown of which tests passed and failed.

##Testing a Client (Not yet, assumptions made)
To test a client implementation an instance of this test server is provided at: https://suv-service-test-pack.herokuapp.com. 
Messages sent to this server via the urls described in the [suv-service-interface](https://github.com/electrumpayments/suv-service-interface) will be
validated and responded to with mocked up voucher data.

### Testing message correctness
Messages will be validated for correctness against the service interface and in the event that a field is missing something similar to the following can be expected:

```json
{
  "errorType": "FORMAT_ERROR",
  "errorMessage": "Bad formatting",
  "detailMessage": {
    "formatErrors": [
      {
        "field": "product",
        "msg": "may not be null",
        "value": "null"
      }
    ]
  }
}
```

An errorType of `FORMAT_ERROR` is returned followed by an explanation of the format errors as follows:

* The "field"  attribute containing the field that has been formatted incorrectly
* The "msg" field contains information on what violation has occurred
* The "value" field contains the incorrectly formatted value that was used
