package io.sqooba.maven.python;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Installs a Python module using distribute
 *
 * @goal install
 * @phase install
 */
public class InstallMojo extends AbstractMojo {

  /**
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * @parameter default-value="pip"
   * @required
   */
  private String pipExecutable;

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "py").toFile();
    //Try to find setup.py
    final Path setupPath = Paths.get(buildDirectory.getPath(), "setup.py").getParent();
    try {

      if (!setupPath.toFile().exists()) {
        throw new FileNotFoundException();
      }

      // Execute setup script
      ProcessBuilder processBuilder = new ProcessBuilder(pipExecutable,
          "install",
          setupPath.toFile().getAbsolutePath()
          );
      processBuilder.directory(buildDirectory);
      processBuilder.redirectErrorStream(true);
      Process pr = processBuilder.start();
      int exitCode = pr.waitFor();
      BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      String line = "";
      while ((line = buf.readLine()) != null) {
        getLog().info(line);
      }

      if (exitCode != 0) {
        throw new MojoExecutionException(
            String.format("'%s install %s' returned error code %s",
                pipExecutable,
                setupPath.toFile().getAbsolutePath(),
                exitCode));
      }

    } catch (FileNotFoundException e) {
      throw new MojoExecutionException(String.format("Unable to find %s", setupPath), e);
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Unable to read %s", setupPath), e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException(String.format("Unable to execute %s %s",
          pipExecutable,
          setupPath), e);
    }
  }
}