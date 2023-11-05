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
  var stringJoiner = new StringJoiner(", ", "<", ">");
  for (var element : elements) {
    stringJoiner.add(element.toString());
  }
  
  return stringJoiner.toString();
}
```