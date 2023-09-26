package fr.uge.ymca;

public class Main {
  public static void main(String[] args) {
    var lee = new VillagePeople("Lee", Kind.BIKER);
    System.out.println(lee);  // Lee (BIKER)

    var house = new House();
    System.out.println(house);  // Empty House
    var david = new VillagePeople("David", Kind.COWBOY);
    var victor = new VillagePeople("Victor", Kind.COP);
    house.add(david);
    house.add(victor);
    System.out.println(house);  // House with David, Victor
  }
}