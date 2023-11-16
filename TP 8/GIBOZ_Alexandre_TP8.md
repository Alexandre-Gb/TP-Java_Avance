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

4. **On souhaite ajouter une méthode toLazyList qui renvoie une liste non-modifiable dont les éléments sont calculés dans une liste modifiable sous-jacente uniquement si on demande la taille et/ou les éléments de la liste.
   Note : il existe une classe java.util.AbstractList qui peut vous servir de base pour implanter la liste paresseuse demandée.
   Attention : vous veillerez à ne pas demander plusieurs fois si un même élément est présent, une seule fois devrait suffire.
   Écrire la méthode toLazyList.**

On définit la métode `toLazyList`:
```java
@Override
public List<U> toLazyList() {
  return new AbstractList<>() {
    private final Iterator<T> iterator = elements.iterator();
    private final List<U> cache = new ArrayList<>();
    
    @Override
    public U get(int index) {
      Objects.checkIndex(index, elements.size());
      if (index < cache.size()) {
        return cache.get(index);
      }
      
      while (iterator.hasNext()) {
        var optional = mapper.apply(iterator.next());
        if (optional.isPresent()) {
          cache.add(optional.get());
          if (index == cache.size() - 1) {
            return optional.get();
          }
        }
      }
      
      throw new ArrayIndexOutOfBoundsException();
    }
    
    @Override
    public int size() {
      while (iterator.hasNext()) {
        var optional = mapper.apply(iterator.next());
        optional.ifPresent(cache::add);
      }
      
      return cache.size();
    }
  };
}
```

5. **On souhaite pouvoir créer une Query en utilisant une nouvelle méthode fromIterable qui prend un Iterable en paramètre. Dans ce cas, tous les éléments de l'Iterable sont considérés comme présents.
   Note : une java.util.List est un Iterable et Iterable possède une méthode spliterator().
   Écrire la méthode fromIterable et modifier le code des méthodes existantes si nécessaire.**

On obtient le résultat suivant:
```java
public sealed interface Query<T> permits Query.QueryImpl {
  // ...

  static <T> Query<T> fromIterable(Iterable<? extends T> iterable) {
    Objects.requireNonNull(iterable);
    return new QueryImpl<>(iterable, Optional::of);
  }

  final class QueryImpl<T, U> implements Query<U> {
     private final Iterable<? extends T> elements;
     private final Function<? super T,? extends Optional<? extends U>> mapper;

     QueryImpl(Iterable<? extends T> elements, Function<? super T,? extends Optional<? extends U>> mapper) {
        this.elements = elements;
        this.mapper = mapper;
     }
     
     // ...

     @Override
     public List<U> toLazyList() {
        return new AbstractList<>() {
           private final Iterator<? extends T> iterator = elements.iterator();
           private final List<U> cache = new ArrayList<>();
           @Override
           public U get(int index) {
              if (index < cache.size()) {
                 return cache.get(index);
              }

              while (iterator.hasNext()) {
                 var optional = mapper.apply(iterator.next());
                 if (optional.isPresent()) {
                    cache.add(optional.get());
                    if (index == cache.size() - 1) {
                       return optional.get();
                    }
                 }
              }

              throw new ArrayIndexOutOfBoundsException();
           }

           @Override
           public int size() {
              while (iterator.hasNext()) {
                 var optional = mapper.apply(iterator.next());
                 optional.ifPresent(cache::add);
              }

              return cache.size();
           }
        };
     }

     @Override
     public Stream<U> toStream() {
        return StreamSupport.stream(elements.spliterator(), false)
                .flatMap(e -> mapper.apply(e).stream());
     }
     
     // ...
  }
}
```

6. **On souhaite écrire une méthode filter qui permet de sélectionner uniquement les éléments pour lesquels un appel à la fonction prise en paramètre de filter renvoie vrai.
   Écrire la méthode filter.**

On créé la méthode `filter`:
```java
public sealed interface Query<T> permits Query.QueryImpl {
  // ...
  
  <U> Query<U> map(Function<? super T,? extends U> function);

  final class QueryImpl<T, U> implements Query<U> {
    // ...

    @Override
    public Query<U> filter(Predicate<? super U> predicate) {
       Objects.requireNonNull(predicate);
       return new QueryImpl<>(elements, mapper.andThen(o -> o.filter(predicate)));
    }
  }
}
```

7. **On souhaite écrire une méthode map qui renvoie une Query telle que chaque élément est obtenu en appelant la fonction prise en paramètre de la méthode map sur un élément d'une Query d'origine.
   Écrire la méthode map.**

On créé la méthode `map`:
```java
public sealed interface Query<T> permits Query.QueryImpl {
  // ...
  
  <U> U reduce(U identity, BiFunction<U, ? super T, U> biFunction);

  final class QueryImpl<T, U> implements Query<U> {
    // ...
     
    @Override
    public <V> Query<V> map(Function<? super U, ? extends V> function) {
      Objects.requireNonNull(function);
      return new QueryImpl<>(elements, this.mapper.andThen(o -> o.map(function)));
    }
  }
}
```

8. **Enfin, on souhaite écrire une méthode reduce sur une Query qui marche de la même façon que la méthode reduce à trois paramètres sur un Stream et sachant que comme notre Query n'a pas d'implantation parallel, le troisième paramètre est superflu.
   Écrire la méthode reduce.**


On créé la méthode `reduce`:
```java
public sealed interface Query<T> permits Query.QueryImpl {
  // ...

  <U> Query<U> map(Function<? super T, ? extends U> function);

  final class QueryImpl<T, U> implements Query<U> {
    // ...

    @Override
    public <V> V reduce(V identity, BiFunction<V, ? super U, V> biFunction) {
       Objects.requireNonNull(biFunction);

       var result = identity;
       for (var element : elements) {
          var optional = mapper.apply(element);
          if (optional.isPresent()) {
             result = biFunction.apply(result, optional.get());
          }
       }

       return result;
    }
  }
}
```