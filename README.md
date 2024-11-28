
# one-stop-shop-exclusions-frontend

This is the repository for One Stop Shop Exclusions Frontend

Exclusions Service
------------

The main function of this service is to allow traders who are registered with the One Stop Shop Registration
service to leave the One Stop Shop service, if they are no longer eligible to use it.

Traders are presented with three options to choose from in the self-exclusion service:
- Moving the business to an EU country
- The business has stopped selling eligible goods to customers in the EU or Northern Ireland
- Voluntary self-exclusion

Traders can also be excluded by HMRC, however this service is only for self-exclusions.

Summary of APIs
------------

ETMP:
Submitting the exclusion details triggers an amend registration API call to ETMP

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application locally via Service Manager

```
sm2 --start ONE_STOP_SHOP_ALL
```

### To run the application locally from the repository, execute the following:


```
sm2 --stop ONE_STOP_SHOP_EXCLUSIONS_FRONTEND
```
and
```
sbt run
```

### Running correct version of mongo
Mongo 6 with a replica set is required to run the service. Please refer to the MDTP Handbook for instructions on how to run this

### Using the application

Access the Authority Wizard to log in:
http://localhost:9949/auth-login-stub/gg-sign-in

Enter the following details on this page and submit:
- Redirect URL: http://localhost:10196/pay-vat-on-goods-sold-to-eu/leave-one-stop-shop
- Affinity Group: Organisation
- Enrolments (there are two rows this time):
- Enrolment Key: HMRC-MTD-VAT
- Identifier Name: VRN
- Identifier Value: 100000002
- Enrolment Key: HMRC-OSS-ORG
- Identifier Name: VRN
- Identifier Value: 100000002

It is recommended to use VRN 100000002 for a regular exclusions journey, however alternatives can be found in the
one-stop-shop-registration-stub which holds scenarios for registered traders and any existing exclusions.

### Unit Tests

To run all tests, run the following command in your sbt session:
```
test
```

To run a single test, run the following command in your sbt session:
```
testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
testOnly *ApplicationCompleteControllerSpec
```

### Integration Tests

To run all tests, run the following command in your sbt session:
```
it:test
```

To run a single test, run the following command in your sbt session:
```
it:testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
it:testOnly *SessionRepositorySpec
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").



