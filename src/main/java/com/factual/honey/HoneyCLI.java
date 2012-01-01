package com.factual.honey;

import java.io.File;
import java.io.IOException;

import jline.ConsoleReader;

import org.apache.commons.io.FileUtils;

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
      key = consoleReader.readLine("API Key: ");
      secret = consoleReader.readLine("API Secret: ");
      if(userWantsAuthSaved()) {
        saveAuth(key, secret);
      }
    }
    factual = new Factual(key, secret);
  }

  private boolean userWantsAuthSaved() {
    try {
      System.out.print("Save auth in " + honeyDir() + "? [Y/n]");
      return consoleReader.readCharacter(new char[]{'Y', 'n'}) == 'Y';
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
        String line = consoleReader.readLine(">");
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
      System.out.println(pe.getFirstLineOfMessage());
    }
  }

  private void evaluateQuery(String sql) {
    HoneyStatement stmt = new HoneyStatement(sql);

    //String sql = "SELECT * FROM PLACES WHERE (name = 'Starbucks' or name= 'Icbm') AND (locality = 'Joplin' OR locality = 'Malone')  LIMIT 10";
    //String sql = "SELECT * FROM PLACES WHERE name = 'Starbucks' or name= 'Icbm' AND locality = 'Joplin' OR locality = 'Malone' LIMIT 10";
    //String sql = "SELECT * FROM PLACES WHERE (name = 'Starbucks' or name = 'Icbm') LIMIT 10";
    //String sql = "SELECT name, tel FROM PLACES WHERE name = 'Starbucks' LIMIT 10";

    if(stmt.isExplain()) {
      System.out.println(stmt.getExplanation());
    } else {
      ResponseFormatter formatter = new ResponseFormatter();
      if(stmt.hasSelectFields()) {
        formatter.setColumns(stmt.getSelectFields());
      }
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
