package com.factual.honey;

import java.io.File;
import java.io.IOException;

import jline.ConsoleReader;
import jline.History;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.factual.driver.Factual;
import com.factual.driver.Tabular;
import com.factual.honey.parse.ParseException;
import com.google.common.collect.Lists;


public class HoneyCLI {
  private final ConsoleReader consoleReader;
  private Factual factual;
  private History commandHistory;


  public HoneyCLI() {
    try {
      ensureHoneyDir();
      consoleReader = new ConsoleReader();
      controlCmdHistory();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void controlCmdHistory() {
    try {
      consoleReader.setUseHistory(false);
      commandHistory = new History (new File(honeyDir(), "history"));
      consoleReader.setHistory(commandHistory);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    HoneyCLI cli = new HoneyCLI();
    cli.auth();
    cli.repl();
  }

  private void auth() throws IOException {
    if(savedAuth()) {
      loadAuthFromFiles();
    } else {
      getAuthFromUser();
    }
  }

  protected void loadAuthFromFiles() {
    String key = readKeyFile();
    String secret = readSecretFile();
    factual = new Factual(key, secret);
  }

  private void getAuthFromUser() throws IOException {
    String key = consoleReader.readLine("Your Factual API Key: ");
    String secret = consoleReader.readLine("Your Factual API Secret: ");
    if(userWantsAuthSaved()) {
      saveAuth(key, secret);
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
      FileUtils.writeStringToFile(keyFile(), key);
      FileUtils.writeStringToFile(secretFile(), secret);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void ensureHoneyDir() {
    try {
      FileUtils.forceMkdir(honeyDir());
    } catch (IOException e) {
      //NOOP: Honey should work even w/o write access to local disk!
    }
  }

  private String readSecretFile() {
    return read(secretFile());
  }

  private String readKeyFile() {
    return read(keyFile());
  }

  private File keyFile() {
    return honeyUserFile("key");
  }

  private File secretFile() {
    return honeyUserFile("secret");
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
    StringBuilder cmdbuf = new StringBuilder();
    while (true) {
      try {
        String line = consoleReader.readLine("> ").trim();
        if(!StringUtils.isBlank(line)) {
          if(line.endsWith("\\")) {
            cmdbuf.append(StringUtils.chop(line));
          } else {
            cmdbuf.append(line);
            String cmd = cmdbuf.toString();
            cmdbuf = new StringBuilder();
            commandHistory.addToHistory(cmd);
            evaluateLine(cmd);
          }
        }
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
      evaluateSql(line);
    } catch (ParseException pe) {
      String niceMsg = pe.getFirstLineOfMessage();
      if(StringUtils.isBlank(niceMsg)) {
        pe.printStackTrace();
      } else {
        System.out.println(niceMsg);
      }
    }
  }

  protected void evaluateSql(String sql) {
    HoneyStatement stmt = new HoneyStatement(sql);
    if(stmt.isExplain()) {
      // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
      System.out.println(stmt.getExplanation());
    } else if(stmt.hasCountFn()) {
      TabularFormatter formatter = new TabularFormatter();
      System.out.println(formatter.formatCount(stmt.execute(factual)));
    } else {
      Tabular table = stmt.execute(factual);
      TabularFormatter formatter = new TabularFormatter();

      if(stmt.isDescribe()) {
        formatter.setFirstColumn("name");
      } else if(stmt.hasSelectFields()) {
        formatter.setColumns(Lists.newArrayList(stmt.getSelectFields()));
      }
      System.out.println();
      System.out.println(formatter.format(table));
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
