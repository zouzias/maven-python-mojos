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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Installs a Python module using distribute
 *
 * @goal test
 * @phase test
 */
public class TestMojo extends AbstractMojo {

  private static final String PYTEST_COMMAND = "pytest";
  private static final String PYTHON_FLAG = "-m";

  /**
   * @parameter default-value="${project.basedir}/src/main/python/tests"
   * @required
   */
  private String testDirectory;

  /**
   * @parameter default-value="python"
   * @required
   */
  private String pythonExecutable;

  /**
   * @parameter default-value="-v -s"
   * @required
   */
  private String extraParams;

  /* (non-Javadoc)
   * @see org.apache.maven.plugin.AbstractMojo#execute()
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    final String command = String.format("'%s %s %s %s %s'",
        pythonExecutable,
        PYTHON_FLAG,
        PYTEST_COMMAND,
        extraParams,
        testDirectory);

    final File testDirectoryFile = new File(testDirectory);

    //Verify input of ProcessBuilder to prevent command injection
    if (!Utils.verifyPython(pythonExecutable)) {
      throw new MojoExecutionException(String.format("%s is not a valid python executable",
          pythonExecutable));
    }
    if (!Utils.verifyPath(testDirectory)) {
      throw new MojoExecutionException(String.format("%s is not a valid directory",
          testDirectory));
    }
    if (Utils.verifyContains(extraParams, Utils.SEPARATOR)) {
      throw new MojoExecutionException(String.format(
          "Extra parameters for pytest '%s' contain a separator",
          extraParams));
    }

    try {

      getLog().info(String.format("Running %s", command));
      List<String> args = new ArrayList<String>();
      args.add(pythonExecutable);
      args.add(PYTHON_FLAG);
      args.add(PYTEST_COMMAND);
      for (String arg : extraParams.split(" ")) {
        args.add(arg);
      }
      args.add(testDirectory);
      ProcessBuilder processBuilder = new ProcessBuilder(args);
      processBuilder.directory(testDirectoryFile);
      processBuilder.redirectErrorStream(true);
      Process pr = processBuilder.start();
      int exitCode = pr.waitFor();
      BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      String line = "";
      while ((line = buf.readLine()) != null) {
        getLog().info(line);
      }

      // See https://docs.pytest.org/en/latest/usage.html#possible-exit-codes
      switch (exitCode) {
        case 1:
          throw new MojoFailureException("Some test(s) failed");

        case 2:
          throw new MojoExecutionException("Tests execution was interrupted");

        case 3:
          throw new MojoFailureException("Pytest internal error");

        case 4:
          throw new MojoFailureException("Pytest command line usage error");

        case 5:
          getLog().info("No tests were collected");
          break;

        default:
          getLog().info("All tests passed successfully");
      }
    } catch (IOException | InterruptedException e) {
      throw new MojoExecutionException(String.format("Unable to execute %s",
          command), e);
    }
  }
}