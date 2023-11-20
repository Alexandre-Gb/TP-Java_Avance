# TP9 - Structure de données spécialisée pour les types primitifs

## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td11.php)
***

## Exercice 2 - NumericVec

1. **Dans la classe fr.uge.numeric.NumericVec, on souhaite écrire une méthode longs qui prend en paramètre des entiers longs séparés par des virgules qui permet de créer un NumericVec vide contenant les valeurs prises en paramètre. Cela doit être la seule façon de pouvoir créer un NumericVec.
   Écrire la méthode longs puis ajouter les méthodes, get(index) et size.**

On créé la classe `NumericVec`:
```java
public class NumericVec<T> {
  private final long[] values;

  private NumericVec(long[] values) {
    this.values = values;
  }

  public static NumericVec<Long> longs(long... values) {
    return new NumericVec<>(Arrays.copyOf(values, values.length));
  }

  public long get(int index) {
    Objects.checkIndex(index, size());
    return values[index];
  }

  public int size() {
    return values.length;
  }
}
```

2. **On souhaite ajouter une méthode add(element) qui permet d'ajouter un élément. 
     Le tableau utilisé par NumericVec doit s'agrandir dynamiquement pour permettre d'ajouter un nombre arbitraire d'éléments.**

On modifie la logique de la méthode afin qu'elle resize de manière plus souple. On procède ainsi:
```java
public class NumericVec<T> {
  private long[] values;
  private int size;

  private NumericVec(long[] values) {
    this.values = values;
    this.size = values.length;
  }

  public static NumericVec<Long> longs(long... values) {
    return new NumericVec<>(Arrays.copyOf(values, values.length));
  }

  public void add(Long value) {
    Objects.requireNonNull(value);

    if (size == 0) {
      values = new long[1];
    } else if (size >= values.length) {
      values = Arrays.copyOf(values, size * 2);
    }

    values[size] = value;
    size++;
  }

  public long get(int index) {
    Objects.checkIndex(index, size());
    return values[index];
  }

  public int size() {
    return size;
  }
}
```

3. **On veut maintenant ajouter les 2 méthodes ints, doubles qui permettent respectivement de créer des NumericVec d'int ou de double en prenant en paramètre des valeurs séparées par des virgules.
     En termes d'implantation, l'idée est de convertir les int ou les double en long avant de les insérer dans le tableau. Et dans l'autre sens, lorsque l'on veut lire une valeur, c'est à dire quand on prend un long dans le tableau, on le convertit en le type numérique attendu. 
     Pour cela, l'idée est de stocker dans chaque NumericVec une fonction into qui sait convertir un élément en long, et une fonction from qui sait convertir un long vers un élément.**

Classe `NumericVer`:
```java
public class NumericVec<T> {
  private long[] values;
  private final LongFunction<T> from;
  private final ToLongFunction<T> into;
  private int size;

  private NumericVec(long[] values, LongFunction<T> from, ToLongFunction<T> into) {
    this.values = values;
    this.size = values.length;
    this.from = from;
    this.into = into;
  }

  public static NumericVec<Long> longs(long... values) {
    Objects.requireNonNull(values);
    return new NumericVec<>(Arrays.copyOf(values, values.length), e -> e, e -> e);
  }

  public static NumericVec<Integer> ints(int... values) {
    Objects.requireNonNull(values);
    var array = Arrays.stream(values).mapToLong(e -> e).toArray();
    return new NumericVec<>(array, e -> (int) e, Integer::longValue);
  }

  public static NumericVec<Double> doubles(double... values) {
    Objects.requireNonNull(values);

    // var array = new long[values.length];
    // for (int i = 0; i < values.length; i++) {
    //   array[i] = Double.doubleToRawLongBits(values[i]);
    // }
    var array = Arrays.stream(values).mapToLong(Double::doubleToRawLongBits).toArray();
    return new NumericVec<>(array, Double::longBitsToDouble, Double::doubleToRawLongBits);
  }

  public void add(T value) {
    Objects.requireNonNull(value);

    if (size == 0) {
      values = new long[1];
    } else if (size >= values.length) {
      values = Arrays.copyOf(values, size * 2);
    }

    values[size] = into.applyAsLong(value);
    size++;
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return from.apply(values[index]);
  }

  public int size() {
    return size;
  }
}
```