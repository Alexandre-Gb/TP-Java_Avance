package fr.uge.ymca;

public sealed interface Inmate permits Minion, VillagePeople {
  String name();
//  int price();
}
