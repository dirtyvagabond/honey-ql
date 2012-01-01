package com.factual.honey.preprocess;

public class Snip {
  private final String first;
  private final String last;
  private final String middle;

  public Snip(String first, String middle, String last) {
    this.first = first;
    this.middle = middle;
    this.last = last;
  }

  public String middle() {
    return middle;
  }

  public String splice() {
    return first + last;
  }

  @Override
  public String toString() {
    return "[SNIP: |" + first + "|, |" + middle + "|, |" + last + "|]";
  }

}
