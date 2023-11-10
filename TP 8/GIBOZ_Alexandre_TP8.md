# TP8 - Query (fonctionnelle)

## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td10.php)
***

## Exercice 2 - Query

1. **On souhaite écrire une interface Query ainsi qu'une classe QueryImpl qui est une classe interne de l'interface Query et qui va contenir l'implantation de l'interface. Cette classe doit être la seule implantation possible.
   Ce n'est pas très beau comme design, mais cela fait un seul fichier ce qui est plus pratique pour la correction.
   L'interface Query doit posséder une méthode fromList qui permet de créer une Query comme expliqué ci-dessus. De plus, il doit être possible d'afficher les éléments d'une Query avec la méthode toString() qui effectue le calcul des éléments et les affiche. L'affichage contient tous les éléments présents (ceux pour qui la fonction prise en second paramètre renvoie un élément présent) séparés par le symbole " |> ".
   Attention : il ne faut pas faire le calcul des éléments (savoir si ils sont présent ou non) à la création du Query, mais uniquement lorsque l'affichage est demandé.**

Interface `Query` :
```java
package fr.uge.query;

import java.util.*;
import java.util.function.Function;

public sealed interface Query<T> permits Query.QueryImpl {
  static <T, U> Query<U> fromList(List<T> list, Function<? super T, Optional<? extends U>> mapper) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(mapper);

    return new QueryImpl<>(Collections.unmodifiableList(list), mapper); // UnmodifiableList for initial null values
  }

  final class QueryImpl<T, U> implements Query<U> {
    private final List<T> elements;
    private final Function<? super T, Optional<? extends U>> mapper;

    QueryImpl(List<T> elements, Function<? super T, Optional<? extends U>> mapper) {
      this.elements = elements;
      this.mapper = mapper;
    }

    @Override
    public String toString() {
      var stringJoiner = new StringJoiner(" |> ");
//      for (var element : elements) {
//        mapper.apply(element).ifPresent(e -> stringJoiner.add(e.toString()));
//      }
      elements.forEach(e -> mapper.apply(e)
              .ifPresent(u -> stringJoiner.add(u.toString())));

      return stringJoiner.toString();
    }
  }
}
```

2. **On souhaite ajouter une méthode toList à l'interface Query dont le but est de renvoyer dans une liste non-modifiable les éléments présents.
   Écrire la méthode toList.**

On modifie l'interface `Query` :
```java
public sealed interface Query<T> permits Query.QueryImpl {
  // ...
  
  List<T> toList();
  
  final class QueryImpl<T, U> implements Query<U> {
    // ...
    
    @Override
    public List<U> toList() {
      var list = new ArrayList<U>();
      elements.forEach(e -> mapper.apply(e).ifPresent(list::add));
      
      return List.copyOf(list);
    }
  }
}
```

3. **On souhaite maintenant ajouter une méthode toStream qui renvoie un Stream des éléments présents dans une Query.
   Note : ici, on ne vous demande pas de créer un Spliterator, il existe déjà une méthode stream() sur l'interface List.
   Écrire la méthode toStream.**

On définit la méthode `toStream`:
```java
@Override
public Stream<U> toStream() {
  return elements.stream()
        .flatMap(e -> mapper.apply(e).stream());
}
```
