
# trusts-store

This service is used to store claimed trusts for use within the **claim a trust** journey

# Endpoints 

## `GET /claim`

### Successful

#### Retrieval Flow `200 OK`

* A call is made to the `get` endpoint with valid authentication headers
* A `TrustClaim` is found for the request
> This is dependent on the `internalId` within the authenticated request
* The `TrustClaim` is returned in the following form:
```json
{
  "internalId": "some-authenticated-internal-id",
  "id": "1234567890",
  "managedByAgent": true
}
```
> the `id` is the 10 digit UTR, or the 15 digit URN associated with the trust
and `managedByAgent` is derived from answers in the **claim a trust** journey

### Unsuccessful

* All error responses will be returned in the following form:
```json
{
  "status": "integer representing the http response code",
  "message": "description of the error that has occurred",
  "errors": "(optional) mixed type holding information regarding the errors"
}
```
#### Failed Retrieval Flow `404 Not Found`
* A call is made to the `get` endpoint with valid authentication headers
* No `TrustClaim` is found for the request
> This is dependent on the `internalId` within the authenticated request
* A `Not Found` response is returned

#### Unauthenticated Flow `401 Unauthorized`

* A call is made to the `get` endpoint with invalid or no authentication headers
* An empty `Unauthorized` response is returned

##  `POST /claim`

### Successful
* Successful requests will contain a body in the format as follows:
```json
{
  "id": "a string representing the tax reference to associate with this internalId",
  "managedByAgent": "boolean derived from answers in the claim a trust journey"
}
```
#### Stored Flow `201 CREATED`
* A call is made to the `store` endpoint with valid authentication header and a valid request body
* The `TrustClaim` is successfully stored within the `trusts-store`
> This trust claim will now be associated with the `internalId` within the authenticated request
* The `201 Created` response will return a representation of what has been stored in the following form:
```json
{
  "internalId": "some-authenticated-internal-id",
  "id": "1234567890",
  "managedByAgent": true
}
```

### Unsuccessful
* All error responses will be returned in the following form:
```json
{
  "status": "integer representing the http response code",
  "message": "description of the error that has occurred",
  "errors": "(optional) mixed type holding information regarding the errors"
}
```

#### Bad Request Flow `400 Bad Request`
* A call is made to the `store` endpoint with valid authentication headers and an invalid request body
* A `TrustClaim` is unable to be parsed
* A `400 Bad Request` response is returned


#### Write Errors Flow `500 Internal Server Error`
* A call is made to the `store` endpoint with valid authentication headers and an invalid request body
* An error occurs when writing to `trusts-store`
* A `500 Internal Server Error` is returned with additional write error information as follows:
```json
[
  {
    "index 0": [{ "code": 50, "message": "some other mongo write error" }]
  },
  {
    "index 1": [
      { "code": 50, "message": "some mongo write error" },
      { "code": 120, "message": "another mongo write error" }
    ]
  }
]
```

##  `GET /register/tasks/{identifier}` and `GET /maintain/tasks/{identifier}`

### Successful
Successful requests will contain the status of the tasks relevant to a given identifier. In registration this is the draft ID, and in maintain this is the identifier of the trust (i.e. the UTR or URN). The status can be any of the following:
```scala
Completed // the task is completed
InProgress // the task has been started but not finished
NotStarted // the task has not been started
CannotStartYet // the task cannot be started yet as other tasks must be finished first
NoActionNeeded // the task does not require completion in order for the user to be able to submit a declaration
```
If a document does not exist for the given identifier, a set of tasks with a default state of `NotStartedYet` are returned.

### Unsuccessful
A `401 Unauthorised` response is returned for a request with invalid authentication.

##  `POST /register/tasks/{identifier}` and `POST /maintain/tasks/{identifier}`

### Successful
Successful requests will contain the status of the updated tasks relevant to a given identifier. In registration this is the draft ID, and in maintain this is the identifier of the trust (i.e. the UTR or URN). The status can be any of the following:
```scala
Completed // the task is completed
InProgress // the task has been started but not finished
NotStarted // the task has not been started
CannotStartYet // the task cannot be started yet as other tasks must be finished first
NoActionNeeded // the task does not require completion in order for the user to be able to submit a declaration
```

### Unsuccessful
* A `400 Bad Request` response is returned for a request with a body that cannot be validated as an instance of `Tasks`.
* A `401 Unauthorised` response is returned for a request with invalid authentication.

##  `DELETE /register/tasks/{identifier}` and `DELETE /maintain/tasks/{identifier}`

### Successful
Successful requests will reset the status of all tasks to `NotStartedYet` relevant to a given identifier. In registration this is the draft ID, and in maintain this is the identifier of the trust (i.e. the UTR or URN).

### Unsuccessful
A `401 Unauthorised` response is returned for a request with invalid authentication.

##  `POST /register/tasks/update-{task}/{identifier}` and `POST /maintain/tasks/update-{task}/{identifier}`

### Successful
Successful requests will contain the status of the updated task relevant to a given identifier. In registration this is the draft ID, and in maintain this is the identifier of the trust (i.e. the UTR or URN). The status can be any of the following:
```scala
Completed // the task is completed
InProgress // the task has been started but not finished
NotStarted // the task has not been started
CannotStartYet // the task cannot be started yet as other tasks must be finished first
NoActionNeeded // the task does not require completion in order for the user to be able to submit a declaration
```

### Unsuccessful
* A `400 Bad Request` response is returned for a request with a body that cannot be validated as an instance of `TaskStatus`.
* A `401 Unauthorised` response is returned for a request with invalid authentication.

# Running the Service
* Start the service locally with `sbt run` in the root directory
* The service can be started via service manager with `sm2 --start TRUSTS_STORE` or as part of the `TRUSTS_ALL` profile

# Testing the Service

To test the service locally run use the following script, this will run both the unit and integration tests (requires `MongoDB`) as well as check for dependency updates and check the coverage of the tests.

`./run_all_tests.sh`

* Run the unit tests by running `sbt test` in the root directory
* Run the integration tests (requires `MongoDB`) by running `it:test` in the root directory

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
