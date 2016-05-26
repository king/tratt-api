# Overview

The purpose of the "tratt-api" library is to help verifying that a **tracking** system is working as expected on an end-to-end level.

When a user interacts with a system, e.g. makes a purchase in an app or clicks a button on a webpage, one or multiple events are logged to a server. These logs on the server can later be used for data analysis. This logging mechanism is referred by King as tracking. The "tratt-api" is a regular java library API and can for example be used with any test automation framework (e.g. JUnit, TestNG) or serve as a backend for a frontend GUI.

For additional information See [User Guide](../../wiki).


#### How to build from command line
From the project root folder execute the following command:
```
> gradlew build
```
Make sure JAVA_HOME environment variable is set and pointing to a JDK (Java 8 or higher) and that `gradlew` has execution rights.
