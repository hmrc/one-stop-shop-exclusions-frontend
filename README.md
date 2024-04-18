
# one-stop-shop-exclusions-frontend

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application

To update from Nexus and start all services from the RELEASE version instead of snapshot
```
sm --start ONE_STOP_SHOP_ALL -r
```

### To run the application locally execute the following:

The service needs to run in testOnly mode in order to access the testOnly get-passcodes endpoint which will generate a passcode for email verification.
```
sm --stop ONE_STOP_SHOP_EXCLUSIONS_FRONTEND
```
and
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").



