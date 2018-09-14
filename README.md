# Maven Python Mojos

A collection of Maven plugins that allows to integrate useful tools written in Python
into the Java build lifecycle.

## Plugins

The following plugins are offered:
	
* maven-python-plugin: https://github.com/Sqooba/maven-python-mojos/tree/master/maven-python-plugin

	Allows to package and distribute Python modules during Maven builds


## Usage


To use the plugin, add the following in your `pom.xml`

```xml

<build>
        <plugins>
        ...
            <plugin>
                <groupId>io.sqooba</groupId>
                <artifactId>maven-python-plugin</artifactId>
                <version>1.1.2</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>test</goal>
                            <goal>package</goal>
                            <goal>install</goal>
                        </goals>
                    </execution>
                </executions>
                  <configuration>
                      <pythonbuild>bdist_wheels</pythonbuild> <!-- select your packaging approach -->
                  </configuration>
            </plugin>
            ...
        </plugins>
    </build>

```