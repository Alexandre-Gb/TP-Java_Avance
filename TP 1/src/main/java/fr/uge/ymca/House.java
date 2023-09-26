package fr.uge.ymca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public final class House {
  private final ArrayList<Inmate> people = new ArrayList<>();
  private final HashMap<Kind, Integer> discount = new HashMap<>();

//  public House() {
//    people = new ArrayList<>();
//    discount = new HashMap<>();
//  }

  public void add(Inmate person) {
    Objects.requireNonNull(person);
    people.add(person);
  }

  private double price(Inmate person) {
    return switch (person) {
      case Minion minion -> 1;
      case VillagePeople villagePeople -> {
        var kind = villagePeople.kind();
        if (discount.containsKey(kind)) {
          yield 100 * (1 - discount.get(kind) / 100.0);
        } else {
          yield 100;
        }
      }
    };
  }

  public void addDiscount(Kind kind) {
    Objects.requireNonNull(kind);

    if (!discount.containsKey(kind)) {
      discount.put(kind, 80);
    }
  }

  public void removeDiscount(Kind kind) {
    Objects.requireNonNull(kind);

    if (discount.containsKey(kind)) {
      discount.remove(kind);
    } else {
      throw new IllegalStateException("No discount for " + kind);
    }
  }

  public double averagePrice() {
    return people.stream()
            .mapToDouble(this::price)
            .average()
            .orElse(Double.NaN);
  }

//  public double averagePrice() {
//    return people.stream()
//            .mapToInt(Inmate::price)
//            .average()
//            .orElse(Double.NaN);
//  }

  @Override
  public String toString() {
    if (people.isEmpty()) {
      return "Empty House";
    }

    return "House with ".concat(
            people.stream()
                    .map(Inmate::name)
                    .sorted()
                    .collect(Collectors.joining(", "))
    );
  }

//  @Override
//  public String toString() {
//    if (people.isEmpty()) {
//      return "Empty House";
//    }
//
//    return "House with ".concat(
//            people.stream()
//                    .map(VillagePeople::name)
//                    .collect(Collectors.joining(", "))
//    );
//  }

//  @Override
//  public String toString() {
//    if (this.people.isEmpty()) {
//      return "Empty House";
//    }
//
//    var builder = new StringBuilder("House with ");
//    for (var person : this.people) {
//      builder.append(person).append(", ");
//    }
//
//    return builder.toString();
//  }
}
