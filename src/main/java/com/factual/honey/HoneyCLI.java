package com.factual.honey;

import java.io.IOException;

import jline.ConsoleReader;

import com.factual.Factual;


public class HoneyCLI {
  private final ConsoleReader consoleReader;
  private StringBuilder commandBuffer;
  private Honey honey;


  public HoneyCLI() throws IOException {
    consoleReader = new ConsoleReader();
  }

  public static void main(String[] args) throws IOException {
    HoneyCLI cli = new HoneyCLI();
    cli.auth();
    cli.repl();
  }

  private void auth() throws IOException {
    String key = consoleReader.readLine("API Key: ");
    String secret = consoleReader.readLine("API Secret: ");
    honey = new Honey(new Factual(key, secret));
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

    if (commandBuffer == null) {
      commandBuffer = new StringBuilder();
    }

    commandBuffer.append(line);

    if (line.trim().endsWith(";")) {
      String command = commandBuffer.toString();
      commandBuffer = null;
      honey.execute(command);
    }
  }

}
