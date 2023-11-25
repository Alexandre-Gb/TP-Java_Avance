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
    Objects.requireNonNull(values);
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

On modifie la logique de la méthode afin qu'elle resize de manière plus souple. 
On procède ainsi :
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
    } else if (size >= values.length) { // "==" works as well, im just paranoid
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

4. **On souhaite écrire une méthode stream() qui renvoie un stream des éléments du NumericVec dans l'ordre d'insertion. Pour cela, on va créer une classe implantant l'interface Spliterator. Puis on utilisera StreamSupport.stream() pour créer le Stream à partir du Spliterator.
   Note : s'l y a moins de 1024 éléments, on n'essaiera pas de couper le Spliterator.
   Écrire la méthode stream qui renvoie un Stream parallélisable.**

Spliterator:
```java
public Stream<T> stream() {
  return StreamSupport.stream(spliterator(0, size), false);
}

private Spliterator<T> spliterator(int start, int end) {
  return new Spliterator<T>() {
    private int i = start;

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      if (i < end) {
        action.accept(from.apply(values[i++]));
        return true;
      }
      return false;
    }

    @Override
    public Spliterator<T> trySplit() {
      if (size < 1024) {
        return null;
      }

      var middle = (i + end) >>> 1;
      if (middle == i) {
        return null;
      }

      var split = spliterator(i, middle);
      i = middle;
      return split;
    }

    @Override
    public long estimateSize() {
      return end - i;
    }

    @Override
    public int characteristics() {
      return NONNULL | ORDERED | IMMUTABLE | SIZED;
    }
  };
}
```

5. **On souhaite qu'un NumericVec implante l'interface java.util.List.
   Modifier le code pour que NumericVec soit une liste.**

On fait en sorte que `NumericVec` extends `AbstractList`.
À présent, la méthode `add` doit renvoyer une valeur booléenne, on renvoie donc `true` à chaque ajout d'un élément.
De plus, on définit une méthode publique spliterator sans paramètres qui va fournir un spliterator avec les bonnes valeurs par défaut grâce à la méthode privée.

```java
public class NumericVec<T> extends AbstractList<T> {
  // ...
  
  public boolean add(T value) {
    Objects.requireNonNull(value);

    if (size == 0) {
      values = new long[1];
    } else if (size >= values.length) {
      values = Arrays.copyOf(values, size * 2);
    }

    values[size] = into.applyAsLong(value);
    size++;
    return true;
  }

  @Override
  public Spliterator<T> spliterator() {
    return spliterator(0, size);
  }
  
  // ...
}
```

6. **On souhaite ajouter une méthode addAll qui permet d'ajouter une collection à un NumericVec déjà existant. 
     Techniquement, l'implantation de addAll que l'on reçoit de AbstractList marche déjà, mais ici, on va faire une implantation plus efficace dans le cas où le paramètre est aussi un NumericVec.
     Écrire le code de la méthode addAll qui optimise le cas où le paramètre est aussi un NumericVec.**

On obtient le code suivant : 
```java
public boolean addAll(Collection<? extends T> collection) {
  Objects.requireNonNull(collection);
  if (collection.isEmpty()) {
    return false;
  }
  
  if (collection instanceof NumericVec<?> numericVec) {
    if (values.length < size + numericVec.size) {
      values = Arrays.copyOf(values, size + numericVec.size);
    }
    
    System.arraycopy(numericVec.values, 0, values, size, numericVec.size);
    size += numericVec.size;
  } else {
    collection.forEach(this::add);
  }
  
  return true;
}
```

7. **On souhaite écrire une méthode toNumericVec(factory) qui prend en paramètre une référence de méthode permettant de créer un NumericVec et renvoie un Collector qui peut être utilisé pour collecter des valeurs numériques dans un des NumericVec créés par la référence de méthode.
    Écrire la méthode toNumericVec.**

On définit la méthode statique :
```java
public static <T> Collector<T, ?, NumericVec<T>> toNumericVec(Supplier<NumericVec<T>> factory) {
  Objects.requireNonNull(factory);
  
  return Collectors.toCollection(factory);
}
```