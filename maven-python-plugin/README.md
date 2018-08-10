# Maven python plugin

This maven plugin allows to build python egg distribution and install the package locally

## Prerequisites

* Maven 3
* Python 2.7 or 3.X
* pip
* Java 1.8
* setup.py in your python source directory

## Get plugin from sqooba's Artifactory

Add the repo to your `pom.xml` or `settings.xml`. For example in your pom.xml:

    <distributionManagement>
        <repository>
          <id>libs-release</id>
          <name>libs-release</name>
          <url>https://artifactory-v2.sqooba.io/artifactory/libs-release</url>
        </repository>
    </distributionManagement>

Make sure that have the rights to read from this maven repo

## Build and install manually

    mvn clean install
    cp ./maven-python-plugin/target/maven-python-plugin-1.0-SNAPSHOT.jar ~/.m2/repository/io/sqooba/maven-python-plugin/1.0-SNAPSHOT/

## Use in your pom.xml

    <plugin>
        <groupId>io.sqooba.maven</groupId>
        <artifactId>maven-python-plugin</artifactId>
        <version>1.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>package</goal>
                    <goal>install</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

## Build your python code with maven

    mvn package

to build an egg distribution of your python package in `./target/py/`

    mvn install

to install the python package in `site-packages` using pip

## Versioning

If no `version` attribute is found in `setup.py`, the project version present in the pom.xml
will be used as python package version.

If the placeholder `version = ${VERSION}` is used in the setup.py the project version present in the pom.xml
will also be used as python package version.

If a version number is specified in the setup.py it will be used to version the
python package.

## Configuration and defaults

`package` phase:

* Python interpreter (to build the dist): `<pythonExecutable>python</pythonExecutable>`
* Python source directory: `<sourceDirectory>${project.basedir}/src/main/python</sourceDirectory>`
* Python package version: `<packageVersion>${project.version}</packageVersion>`

`install` phase:

* Pip executable (to install the package): `<pipExecutable>pip</pipExecutable>`