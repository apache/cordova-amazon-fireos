<!--
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
-->
Cordova Amazon Fire OS
===

Cordova Amazon Fire OS is an application library that allows for Cordova-based
projects to be built for the Amazon Fire OS Platform version 4.0 and earlier. It uses Amazon's web app runtime that is built on open-source Chromium project. With the web app runtime, your web apps can achieve fluidity and speed approaching that of native apps.

[Apache Cordova](http://cordova.io) is a project at The Apache Software Foundation (ASF).

***

DEPRECATION NOTICE
---

***Cordova Amazon Fire OS platform support is deprecated for Amazon Fire OS 5.0 and higher (2015 devices and later); for these devices, please use the Android platform target only.***

***


Requires
---

- Java JDK 1.5 or greater
- Apache ANT 1.8.0 or greater
- Android SDK [http://developer.android.com](http://developer.android.com)
- Amazon WebView SDK [https://developer.amazon.com/sdk/fire/IntegratingAWV.html#installawv](https://developer.amazon.com/sdk/fire/IntegratingAWV.html#installawv)

Cordova Amazon Fire OS Developer Tools
---

The Cordova developer tooling is split between general tooling and project level tooling.

General Commands

    ./bin/create [path package activity] ... create the ./example app or a cordova-amazon-fireos project
    ./bin/check_reqs ....................... checks that your environment is set up for cordova-amazon-fireos development
    ./bin/update [path] .................... updates an existing cordova-amazon-fireos project to the version of the framework

Project Commands

These commands live in a generated Cordova Amazon Fire OS project. Emulator support is currently not available.

    ./cordova/clean ........................ cleans the project
    ./cordova/build ........................ calls `clean` then compiles the project
    ./cordova/log   ........................ stream device logs to stdout
    ./cordova/run   ........................ calls `build` then deploys to a connected Amazon device.
    ./cordova/version ...................... returns the cordova-amazon-fireos version of the current project

Importing a Cordova Amazon Fire OS Project into Eclipse
----

1. File > New > Project...
2. Android > Android Project
3. Create project from existing source (point to the generated app found in platforms/amazon-fireos)
4. Right click on libs/cordova.jar and add to build path
5  Right click on libs/awv_interface.jar and add to build path
6. Right click on the project root: Run as > Run Configurations
7. Click on the Target tab and select Manual (this way you can choose the device to build to)

Building without the Tooling
---
Note: The Developer Tools handle this.  This is only to be done if the tooling fails, or if
you are developing directly against the framework.


To create your `cordova.jar` file, run in the framework directory:

    android update project -p . -t android-19
    ant jar

Further Reading
---
- [https://developer.amazon.com/sdk/fire.html] (https://developer.amazon.com/sdk/fire.html)
- [http://developer.android.com](http://developer.android.com)
- [http://cordova.apache.org/](http://cordova.apache.org)
- [http://wiki.apache.org/cordova/](http://wiki.apache.org/cordova/)
