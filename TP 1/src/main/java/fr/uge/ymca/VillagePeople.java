package fr.uge.ymca;

import java.util.Objects;

public record VillagePeople(String name, Kind kind) implements Inmate {
  public VillagePeople {
    Objects.requireNonNull(name);
    Objects.requireNonNull(kind);
  }

//  @Override
//  public int price() {
//    return 100;
//  }

  @Override
  public String toString() {
    return name + " (" + kind + ")";
  }
}
