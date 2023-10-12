# TP3 - Slices of bread
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td03.php)
***

## Exercice 2 - The Slice and The furious

1. **Écrire l'interface Slice puis implanter la classe SliceArray et ses méthodes array, size et get(index).**

Code du fichier Slice.java:
```java
package fr.uge.slice;

import java.util.Objects;

public sealed interface Slice <T> permits Slice.ArraySlice {
  static <T> Slice<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }
  T get(int index);
  int size();

  final class ArraySlice<T> implements Slice<T> {
    private final T[] array;

    private ArraySlice(T[] array) {
      this.array = array;
    }

    @Override
    public T get(int index) {
      Objects.checkIndex(index, size());
      return array[index];
    }

    @Override
    public int size() {
      return array.length;
    }
  }
}
```

2. **On souhaite que l'affichage d'un slice affiche les valeurs séparées par des virgules avec un '[' et un ']' comme préfixe et suffixe.**

On override la méthode toString() de la classe ArraySlice:
```java
@Override
public String toString() {
  return Arrays.stream(array)
  .map(Objects::toString)
  .collect(Collectors.joining(", ", "[", "]"));
}
```

Au lieu d'utiliser `T::toString`, on utilise `Objects::toString` pour éviter les NullPointerException.

3. **On souhaite ajouter une surcharge à la méthode array qui, en plus de prendre le tableau en paramètre, prend deux indices from et to et montre les éléments du tableau entre from inclus et to exclus.**

On modifie l'interface Slice en surchargant la méthode array et en ajoutant SubArraySlice:
```java
public sealed interface Slice<T> permits Slice.ArraySlice, Slice.SubArraySlice {
  static <T> Slice<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  static <T> Slice<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    return new SubArraySlice<>(array, from, to);
  }

  // ...

  final class SubArraySlice<T> implements Slice<T> {
    private final T[] array;
    private final int from;
    private final int to;

    private SubArraySlice(T[] array, int from, int to) {
      this.array = array;
      this.from = from;
      this.to = to;
    }

    @Override
    public T get(int index) {
      Objects.checkIndex(index, size());
      return array[from + index];
    }

    @Override
    public int size() {
      return to - from;
    }

    @Override
    public String toString() {
      return Arrays.stream(array, from, to)
      .map(Objects::toString)
      .collect(Collectors.joining(", ", "[", "]"));
    }
  }
}
```

Etant donné que l'on vérifie la validité des indexes dans la méthode statique array, et que
le constructeur de SubArraySlice est privé, il ne peut donc être appelé que de cette façon, et donc, il est
innutile de dupliquer la vérification d'indexes dans le constructeur.

4. **On souhaite enfin ajouter une méthode subSlice(from, to) à l'interface Slice qui renvoie un sous-slice restreint aux valeurs entre from inclus et to exclu.**

On ajoute une nouvelle méthode abstraite dans l'interface Slice, afin de pouvoir l'implanter dans les deux classes:
```java
public sealed interface Slice<T> permits Slice.ArraySlice, Slice.SubArraySlice {
  static <T> Slice<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  static <T> Slice<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    return new SubArraySlice<>(array, from, to);
  }

  T get(int index);

  int size();

  Slice<T> subSlice(int from, int to);
}
```

On implante la méthode subSlice dans la classe ArraySlice:
```java
@Override
public Slice<T> subSlice(int from, int to) {
  Objects.checkFromToIndex(from, to, size());
  return new SubArraySlice<>(array, from, to);
}
```

On implante ensuite la méthode subSlice dans la classe SubArraySlice:
```java
@Override
public Slice<T> subSlice(int from, int to) {
  Objects.checkFromToIndex(from, to, size());
  return new SubArraySlice<>(array, this.from + from, this.from + to);
}
```

<br>

## Exercice 3 - 2 Slice 2 Furious

1. **Recopier l'interface Slice de l'exercice précédent dans une interface Slice2. 
    Vous pouvez faire un copier-coller de Slice dans même package, votre IDE devrait vous proposer de renommer la copie. 
    Puis supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la ré-implanter aussi, mais plus tard.**

Après modifications, on obtient le code suivant:
```java
package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice2<T> permits Slice2.ArraySlice {
  static <T> Slice2<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  T get(int index);

  int size();

//  Slice2<T> subSlice(int from, int to);


  final class ArraySlice<T> implements Slice2<T> {
    private final T[] array;

    private ArraySlice(T[] array) {
      this.array = array;
    }

    @Override
    public T get(int index) {
      Objects.checkIndex(index, size());
      return array[index];
    }

    @Override
    public int size() {
      return array.length;
    }

//    @Override
//    public Slice2<T> subSlice(int from, int to) {
//      Objects.checkFromToIndex(from, to, size());
//      return new SubArraySlice<>(array, from, to);
//    }

    @Override
    public String toString() {
      return Arrays.stream(array)
      .map(Objects::toString)
      .collect(Collectors.joining(", ", "[", "]"));
    }
  }
}
```

2. **Déclarer une classe SubArraySlice à l'intérieur de la classe ArraySlice comme une inner class donc pas comme une classe statique et implanter cette classe et la méthode array(array, from, to).**

On ajoute la inner class SubArraySlice dans la classe ArraySlice:
```java
package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice2<T> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {

  // ...

  final class ArraySlice<T> implements Slice2<T> {
    
    // ...

    public final class SubArraySlice implements Slice2<T> {
      private final int from;
      private final int to;

      private SubArraySlice(int from, int to) {
        Objects.requireNonNull(array);
        Objects.checkFromToIndex(from, to, array.length);
        this.from = from;
        this.to = to;
      }

      @Override
      public T get(int index) {
        Objects.checkIndex(index, size());
        return array[from + index];
      }

      @Override
      public int size() {
        return to - from;
      }

      @Override
      public String toString() {
        return Arrays.stream(array, from, to)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    }
  }
}
```

On définit ensuite la méthode static array dans l'interface Slice2. Cette méthode va instancier un ArraySlice avant de retourner un SubArraySlice, la classe parent devant
être instanciée avant la inner class.
```java
static <T> Slice2<T> array(T[] array, int from, int to) {
  Objects.requireNonNull(array);
  Objects.checkFromToIndex(from, to, array.length);
  return new ArraySlice<>(array).new SubArraySlice(from, to);
}
```

3. **Dé-commenter la méthode subSlice(from, to) de l'interface et fournissez une implantation de cette méthode dans les classes ArraySlice et SubArraySlice.**

On implante la méthode subSlice dans la classe ArraySlice et dans la inner class SubArraySlice:
```java
public sealed interface Slice2<T> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {
  
  // ...

  Slice2<T> subSlice(int from, int to);
  
  final class ArraySlice<T> implements Slice2<T> {
    
    // ...

    @Override
    public Slice2<T> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, size());
      return new SubArraySlice(from, to);
    }

    public final class SubArraySlice implements Slice2<T> {
      
      // ...

      @Override
      public Slice2<T> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new SubArraySlice(this.from + from, this.from + to);
      }
    }
  }
}
```

4. **Dans quel cas va-t-on utiliser une inner class plutôt qu'une classe interne ?**

On préfèrera l'utilisation d'une inner class dans la situation ou il peut exister une possible relation d'interdépendance des objets entre la classe parent et la inner class.
Cette relation se manifeste par le fait que la inner class a besoin d'accéder aux attributs de la classe parent.

<br>

## Exercice 4 - The Slice and The Furious: Tokyo Drift

1. **Recopier l'interface Slice du premier exercice dans une interface Slice3. 
    Supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la réimplanter plus tard.
    Puis déplacer la classe ArraySlice à l'intérieur de la méthode array(array) et transformer celle-ci en classe anonyme.**

On obtient le code suivant:
```java
public interface Slice3<T> {
  static <T> Slice3<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new Slice3<T>() {
      @Override
      public T get(int index) {
        return array[index];
      }

      @Override
      public int size() {
        return array.length;
      }

      @Override
      public String toString() {
        return Arrays.stream(array)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    };
  }

  T get(int index);

  int size();

  // Slice3<T> subSlice(int from, int to);
}
```

2. **On va maintenant chercher à implanter la méthode subSlice(from, to) directement dans l'interface Slice3. Ainsi, l'implantation sera partagée.
   Écrire la méthode subSlice(from, to) en utilisant là encore une classe anonyme.
   Comme l'implantation est dans l'interface, on n'a pas accès au tableau qui n'existe que dans l'implantation donnée dans la méthode array(array)... mais ce n'est pas grave, car on peut utiliser les méthodes de l'interface.**

On obtient le code suivant:
```java
package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Slice3<T> {
  
  // ...
  
  Slice3<T> subSlice(int form, int to);

  static <T> Slice3<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new Slice3<T>() {
      
      // ...

      @Override
      public Slice3<T> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return Slice3.array(array, from, to);
      }
    };
  }

  static <T> Slice3<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);
    var sliceArray = Slice3.array(array);

    return new Slice3<T>() {
      @Override
      public T get(int index) {
        Objects.checkIndex(index, size());
        return sliceArray.get(from + index);
      }

      @Override
      public int size() {
        return to - from;
      }

      @Override
      public Slice3<T> subSlice(int arrayFrom, int arrayTo) {
        Objects.checkFromToIndex(arrayFrom, arrayTo, size());
        return Slice3.array(array, from + arrayFrom, from + arrayTo);
      }

      @Override
      public String toString() {
        return Arrays.stream(array, from, to)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    };
  }
}
```
