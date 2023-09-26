package fr.uge.ymca;

import java.util.Objects;

public record Minion(String name) implements Inmate {
  public Minion {
    Objects.requireNonNull(name);
  }

//  @Override
//  public int price() {
//    return 1;
//  }

  @Override
  public String toString() {
    return name + " (MINION)";
  }
}
