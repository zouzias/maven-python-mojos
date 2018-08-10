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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Packages a Python module using distribute
 *
 * @goal package
 * @phase package
 */
public class PackageMojo extends AbstractMojo {

  private static final String VERSION = "${VERSION}";

  /**
   * @parameter default-value="${project.version}"
   * @required
   */
  private String packageVersion;

  /**
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * @parameter default-value="${project.basedir}/src/main/python"
   * @required
   */
  private String sourceDirectory;

  /**
   * @parameter default-value="python"
   * @required
   */
  private String pythonExecutable;

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    //Copy sourceDirectory
    final File sourceDirectoryFile = new File(sourceDirectory);
    final File buildDirectory = Paths.get(project.getBuild().getDirectory(), "py").toFile();
    try {
      FileUtils.copyDirectory(sourceDirectoryFile, buildDirectory);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to copy source", e);
    }

    //Try to find setup.py
    final Path setupPath = Paths.get(buildDirectory.getPath(), "setup.py");
    try {
      if (!setupPath.toFile().exists()) {
        throw new FileNotFoundException();
      }

      //Replace ${VERSION} placeholder with artifact's version
      Charset charset = StandardCharsets.UTF_8;
      String content = new String(Files.readAllBytes(setupPath), charset);
      packageVersion = packageVersion.replaceAll("-?SNAPSHOT", "");
      if (content.matches("version\\s*=\\s*")) {
        content = content.replace(VERSION, packageVersion);
      } else {
        content = content.replace("setup(", "setup(version='${VERSION}',");
      }
      content = content.replace(VERSION, packageVersion);
      Files.write(setupPath, content.getBytes(charset));

      //Execute setup script
      ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable,
          setupPath.toFile().getAbsolutePath(),
          "bdist_egg");
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
                pythonExecutable,
                setupPath.toFile().getAbsolutePath(),
                exitCode));
      }

    } catch (FileNotFoundException e) {
      throw new MojoExecutionException(String.format("Unable to find %s", setupPath), e);
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Unable to read %s", setupPath), e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException(String.format("Unable to execute %s %s",
          pythonExecutable,
          setupPath), e);
    }

  }
}