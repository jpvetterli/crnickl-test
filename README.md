crnickl-test : common test code for CrNiCKL drivers 
===================================================

	Copyright 2012-2017 Hauser Olsson GmbH.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
    	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*** 

This provides common test code for tests included in CrNiCKL
drivers. Currently, drivers are available for JDBC (crnickl-jdbc)
and MongoDB (crnickl-mongodb). Note that CrNiCKL itself (crnickl) 
cannot be tested without using a driver.

Version 2
---------

Some tests have been upgraded to the latest version of dependencies.

For Maven users
---------------

The software is available from the <a 
href="http://repo.maven.apache.org/maven2/ch/agent/crnickl-test/">Maven central 
repository</a>. To use version `x.y.z`, insert the following dependency into your 
`pom.xml` file:

    <dependency>
      <groupId>ch.agent</groupId>
      <artifactId>crnickl-test</artifactId>
      <version>x.y.z</version>
      <scope>test</scope>
    </dependency>

Building the software
---------------------

The recommended way is to use [git](http://git-scm.com) for accessing the
source and [maven](<http://maven.apache.org/>) for building. The procedure 
is easy, as maven takes care of locating and downloading dependencies:

	$ git clone https://github.com/jpvetterli/crnickl-test.git
	$ cd crnickl-test
	$ mvn install

Browsing the source code
------------------------

The source is available on [GitHub](http://github.com/jpvetterli/crnickl-test.git).

Finding more information
------------------------

More information on CrNiCKL is available at the [project web 
site](http://agent.ch/timeseries/crnickl/).

<small>Updated: 2017-10-06/jpv</small>

<link rel="stylesheet" type="text/css" href="README.css"/>

