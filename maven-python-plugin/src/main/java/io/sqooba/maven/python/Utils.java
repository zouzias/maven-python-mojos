package io.sqooba.maven.python;

import org.apache.maven.project.MavenProject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class Utils {

  private static final String PYTHON_NAME = "python";
  private static final String PIP_NAME = "pip";
  private static final String PYTHON_DIST_DIRECTORY = "py";
  public static final String SEPARATOR = ";";

  public static Boolean verifyContains(String command, String expectedExecName) {

    Path path = Paths.get(command);
    return path.getFileName().toString().contains(expectedExecName);
  }

  public static Boolean verifyPython(String command) {

    return !verifyContains(command, SEPARATOR) &&
           (verifyContains(command, PYTHON_NAME) || verifyPath(command));
  }

  public static Boolean verifyPip(String command) {

    return !verifyContains(command, SEPARATOR) &&
           (verifyContains(command, PIP_NAME) || verifyPath(command));
  }

  public static Boolean verifyPath(String command) {

    Path path = Paths.get(command);
    return Files.exists(path);
  }

  public static Path getBuildDirectory(MavenProject project){
    return Paths.get(project.getBuild().getDirectory(), PYTHON_DIST_DIRECTORY);
  }
}
