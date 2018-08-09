package com.github.mojos.distribute;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.util.List;

/**
 * Packages a Python module using distribute
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

  private static final String VERSION = "${VERSION}";

  /**
   * @parameter expression="${project.version}"
   * @required
   */
  private String projectVersion;

  /**
   * Allows overriding the default version
   */
  @Getter
  @Setter
  private String overridenVersion = null;

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    File setup = new File("src/main/python/setup.py");

    try {

      if (overridenVersion != null) {
        overridenVersion = projectVersion;
      }


      //update VERSION to latest version
      List<String> lines = IOUtils.readLines(new BufferedInputStream(new FileInputStream(setup)));
      for (String line : lines) {
        if (line.contains(VERSION)) {
          line = line.replace(VERSION, overridenVersion);
        }
      }
      IOUtils.writeLines(lines, "\n", new BufferedOutputStream(new FileOutputStream(setup)));

      //execute setup script
      ProcessBuilder t = new ProcessBuilder("python", "setup.py", "bdist_egg");
      t.directory(new File("src/test/python"));
      t.redirectErrorStream(true);

      Process pr = t.start();
      int exitCode = pr.waitFor();
      BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      String line = "";
      while ((line = buf.readLine()) != null) {
        getLog().info(line);
      }

      if (exitCode != 0) {
        throw new MojoExecutionException("python setup.py returned error code " + exitCode);
      }

    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Unable to find " + setup.getPath(), e);
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to read " + setup.getPath(), e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Unable to execute python " + setup.getPath(), e);
    }

  }
}
