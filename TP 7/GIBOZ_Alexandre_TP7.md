# TP7 - Liste persistante (fonctionnelle)

## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td07.php)
***

## Exercice 2 - Seq

1. **Dans un premier temps, on va définir une classe SeqImpl qui est une implantation de l'interface Seq dans le même package que Seq.
   Écrire le constructeur dans la classe SeqImpl ainsi que la méthode from(list) dans l'interface Seq sachant que, comme indiqué ci-dessus, SeqImpl contient une liste non mutable.
   Expliquer pourquoi le constructeur ne doit pas être public ?
   Puis déclarer les méthodes size et get() dans l'interface Seq et implanter ces méthodes dans la classe SeqImpl.**

Interface `Seq`:
```java
public sealed interface Seq<T> permits SeqImpl {
  static <T> Seq<T> from(List<? extends T> list) {
    Objects.requireNonNull(list);
    return new SeqImpl<>(List.copyOf(list));
  }

  int size();

  T get(int index);
}
```

Classe `SeqImpl`:
```java
package fr.uge.seq;

import java.util.List;
import java.util.Objects;

final class SeqImpl<T> implements Seq<T> {
  private final List<T> elements;

  SeqImpl(List<T> elements) {
    // Objects.requireNonNull(elements); Not mandatory as it is an implementation
    // this.elements = List.copyOf(elements); Not mandatory as it is an implementation and we always make a defensive copy
    this.elements = elements;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public T get(int index) {
    Objects.checkIndex(index, size());
    return elements.get(index);
  }
}
```

2. **On souhaite écrire une méthode d'affichage permettant d'afficher les valeurs d'un Seq séparées par des virgules (suivies d'un espace), l'ensemble des valeurs étant encadré par des chevrons ('<' et '>').**

On redéfinit la méthode `toString` dans l'implémentation:
```java
@Override
public String toString() {
  //    var stringJoiner = new StringJoiner(", ", "<", ">");
  //    for (var element : elements) {
  //      stringJoiner.add(element.toString());
  //    }
  //    return stringJoiner.toString();

  return elements.stream()
          .map(Object::toString)
          .collect(Collectors.joining(", ", "<", ">"));
}
```

3. **On souhaite écrire une méthode map qui prend en paramètre une fonction à appliquer à chaque élément d'un Seq pour créer un nouveau Seq. On souhaite avoir une implantation paresseuse, c'est-à-dire une implantation qui ne fait pas de calcul si ce n'est pas nécessaire. Par exemple, tant que personne n'accède à un élément du nouveau Seq il n'est pas nécessaire d'appliquer la fonction. L'idée est de stoker les anciens éléments ainsi que la fonction et de l'appliquer seulement si c'est nécessaire.
   Bien sûr, cela va nous obliger à changer l'implantation déjà existante de SeqImpl car maintenant tous les Seq vont stocker une liste d'éléments ainsi qu'une fonction de transformation (de mapping).
   Avant de se lancer dans l'implantation de map, quelle doit être sa signature ?**

On ajoute la méthode abstraite à l'interface `Seq`:
```java
public sealed interface Seq<T> permits SeqImpl {
  static <T> Seq<T> from(List<? extends T> list) {
    Objects.requireNonNull(list);
    // return new SeqImpl<T, T>(list, Function.identity());
    return new SeqImpl<>(list, Function.identity());
  }
   
  // ...

  <U> Seq<U> map(Function<? super T, ? extends U> function);
}
```

On modifie ensuite l'implémentation:
```java
final class SeqImpl<T, U> implements Seq<T> {
  private final List<U> elements;
  private final Function<? super U, ? extends T> mapper;

  SeqImpl(List<? extends U> elements, Function<? super U, ? extends T> mapper) {
    this.elements = List.copyOf(elements);
    this.mapper = mapper;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public T get(int index) {
    Objects.checkIndex(index, size());
    return mapper.apply(elements.get(index));
  }

  @Override
  public <E> Seq<E> map(Function<? super T, ? extends E> mapper) {
    Objects.requireNonNull(mapper);

    // return new SeqImpl<E, U>(elements, this.mapper.andThen(mapper));
    return new SeqImpl<>(elements, this.mapper.andThen(mapper));
  }

  @Override
  public String toString() {
    return elements.stream()
            .map(mapper)
            .map(Objects::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
```

4. **On souhaite avoir une méthode findFirst qui renvoie le premier élément du Seq si celui-ci existe.
   Quel doit être le type de retour ?**

La méthode devra renvoyer un Optional<T>.

**Déclarer la méthode findFirst dans l'interface et implanter celle-ci dans la classe SeqImpl.**

Interface `Seq`:
```java
public sealed interface Seq<T> permits SeqImpl {
  // ...

  Optional<T> findFirst();
}
```

Classe `SeqImpl`
```java
final class SeqImpl<T, U> implements Seq<T> {
  // ...

  @Override
  public Optional<T> findFirst() {
    if (size() == 0) {
      return Optional.empty();
    }

    return Optional.of(get(0)); // Will map automatically
  }
  
  // ...
}
```