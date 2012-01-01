package com.factual.honey;

import java.io.File;
import java.io.IOException;

import jline.ConsoleReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.factual.Factual;
import com.factual.honey.parse.ParseException;


public class HoneyCLI {
  private final ConsoleReader consoleReader;
  private Factual factual;


  public HoneyCLI() throws IOException {
    consoleReader = new ConsoleReader();
  }

  public static void main(String[] args) throws IOException {
    HoneyCLI cli = new HoneyCLI();
    cli.auth();
    cli.repl();
  }

  private void auth() throws IOException {
    String key;
    String secret;

    if(savedAuth()) {
      key = readKeyFile();
      secret = readSecretFile();
    } else {
      key = consoleReader.readLine("Your Factual API Key: ");
      secret = consoleReader.readLine("Your Factual API Secret: ");
      if(userWantsAuthSaved()) {
        saveAuth(key, secret);
      }
    }
    factual = new Factual(key, secret);
  }

  private boolean userWantsAuthSaved() {
    try {
      System.out.print("Save auth in " + honeyDir() + "? [Y/n]");
      boolean wants = consoleReader.readCharacter(new char[]{'Y', 'n'}) == 'Y';
      System.out.println();
      return wants;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveAuth(String key, String secret) {
    try {
      FileUtils.forceMkdir(honeyDir());
      FileUtils.writeStringToFile(keyFile(), key);
      FileUtils.writeStringToFile(secretFile(), secret);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String readSecretFile() {
    return read(secretFile());
  }

  private String readKeyFile() {
    return read(keyFile());
  }

  private File keyFile() {
    return honeyUserFile("key.txt");
  }

  private File secretFile() {
    return honeyUserFile("secret.txt");
  }

  private File honeyUserFile(String filename) {
    return new File(honeyDir(), filename);
  }

  private File honeyDir() {
    return new File(System.getProperty("user.home"), ".honey");
  }

  private boolean savedAuth() {
    return keyFile().exists();
  }

  private void repl() {
    while (true) {
      try {
        String line = consoleReader.readLine("> ");
        evaluateLine(line);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void evaluateLine(String line) {
    if (line.equals("quit") || line.equals("exit")) {
      System.exit(0);
    }

    try {
      evaluateQuery(line);
    } catch (ParseException pe) {
      String niceMsg = pe.getFirstLineOfMessage();
      if(StringUtils.isBlank(niceMsg)) {
        pe.printStackTrace();
      } else {
        System.out.println(niceMsg);
      }
    }
  }

  private void evaluateQuery(String sql) {
    HoneyStatement stmt = new HoneyStatement(sql);

    if(stmt.isExplain()) {
      // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
      System.out.println(stmt.getExplanation());
    } else {
      ResponseFormatter formatter = new ResponseFormatter();
      if(stmt.hasSelectFields()) {
        formatter.setColumns(stmt.getSelectFields());
      }
      System.out.println();
      System.out.println(formatter.format(stmt.execute(factual)));
    }

  }

  /**
   * Reads value from named file in src/test/resources
   */
  private String read(File file) {
    try {
      return FileUtils.readFileToString(file).trim();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
