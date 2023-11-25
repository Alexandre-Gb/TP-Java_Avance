# TP7 - Liste persistante (fonctionnelle)

## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td09.php)
***

## Exercice 2 - Seq

1. **Dans un premier temps, on va définir une classe SeqImpl qui est une implantation de l'interface Seq dans le même package que Seq.
   Écrire le constructeur dans la classe SeqImpl ainsi que la méthode from(list) dans l'interface Seq sachant que, comme indiqué ci-dessus, SeqImpl contient une liste non mutable.
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
final class SeqImpl<T> implements Seq<T> {
  private final List<T> elements;

  SeqImpl(List<T> elements) {
    // Objects.requireNonNull(elements); Not mandatory as it is an implementation, and we already checked if the provided List was null or not
    // this.elements = List.copyOf(elements); Not mandatory as it is an implementation, and we already have made a defensive copy
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

**Expliquer pourquoi le constructeur ne doit pas être public ?**

L'implémentation, à savoir `SeqImpl` ne sera pas visible de l'extérieur, ainsi, une entité externe ne doit pouvoir utiliser SeqImpl uniquement au travers de l'interface `Seq`.
Mettre le constructeur en visibilité package permet d'atteindre cet objectif.
L'instanciation d'un `SeqImpl` ne pourra ainsi se faire que par `Seq` (ou autre entité ajoutée au package ultérieurement) au travers de la factory method.

2. **On souhaite écrire une méthode d'affichage permettant d'afficher les valeurs d'un Seq séparées par des virgules (suivies d'un espace), l'ensemble des valeurs étant encadré par des chevrons ('<' et '>').**

On redéfinit la méthode `toString` dans l'implémentation :
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

On modifie l'implémentation :
```java
final class SeqImpl<T, U> implements Seq<T> {
  private final List<U> elements;
  private final Function<? super U, ? extends T> mapper;

  SeqImpl(List<? extends U> elements, Function<? super U, ? extends T> mapper) {
    this.elements = List.copyOf(elements);
    this.mapper = mapper;
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

  // ...
}
```

On ajoute ensuite la méthode abstraite à l'interface `Seq`.
On donne également une `Function.identity()` en tant que second paramètre du constructeur étant donné la modification structurelle effectuée ci-dessus :
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

    return Optional.of(get(0)); // Will map automatically via get
  }
  
  // ...
}
```

5. **On souhaite implanter la méthode stream() qui renvoie un Stream des éléments du Seq. 
     Pour cela, on va commencer par implanter un Spliterator. 
     Ici, on a deux façon d'implanter le Spliterator : soit on utilise le Spliterator de la liste sous-jacente, soit on utilise des indices. 
     Expliquer dans quel cas on utilise l'un ou l'autre, sachant que nos données sont stockées dans une List.**

On utilise le Spliterator de la liste sous-jacente lorsque celle-ci est déjà implémentée et que l'on souhaite utiliser ses fonctionnalités, et on utilise des indices lorsque l'on souhaite implémenter nous-même le Spliterator.

**Ensuite, on peut créer la classe correspondant au Spliterator à deux endroits : soit comme une classe interne de la classe SeqImpl, soit comme une classe anonyme d'une méthode spliterator(start, end), quelle est à votre avis le meilleur endroit ?
Écrire les 4 méthodes du Spliterator.**

La meilleure option semble être la classe anonyme dans la méthode spliterator(start, end) car elle n'est utilisée que dans cette méthode.

**Écrire les 4 méthodes du Spliterator.**

```java
private Spliterator<T> spliterator(int start, int end) {
  return new Spliterator<>() {
    private int i = start;
    
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
      Objects.requireNonNull(action);
      
      if (i < end) {
        action.accept(get(i++));
        return true;
      }
      return false;
    }
    
    @Override
    public Spliterator<T> trySplit() {
      var middle = (i + end) >>> 1;
      
      if (middle == i) {
        return null;
      }
      
      var spliterator = spliterator(i, middle);
      i = middle;
      return spliterator;
    }
    
    @Override
    public long estimateSize() {
      return end - i;
    }
    
    @Override
    public int characteristics() {
      return IMMUTABLE | ORDERED | SIZED;
    }
  };
}
```

**Puis déclarer la méthode stream dans l'interface et implanter celle-ci dans SeqImpl sachant qu'il existe la méthode StreamSupport.stream qui permet de créer un Stream à partir de ce Spliterator.**

Interface `Seq`:
```java
public sealed interface Seq<T> permits SeqImpl {
  // ...

  Stream<T> stream();
}
```

Classe `SeqImpl`:
```java
final class SeqImpl<T, U> implements Seq<T> {
  // ...

  @Override
  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(0, size()), false);
  }

  // ...
}
```

6. **On souhaite ajouter une méthode of à l'interface Seq permettant d'initialiser un Seq à partir de valeurs séparées par des virgules.**

On ajoute la méthode statique dans l'interface:
```java
@SafeVarargs
static <T> Seq<T> of(T... elements) {
  Objects.requireNonNull(elements);
  return from(Arrays.asList(elements));
}
```

7. **On souhaite faire en sorte que l'on puisse utiliser la boucle for-each-in sur un Seq.**

On modifie l'interface pour qu'elle étende Iterable, puis on implante la méthode par défaut "iterator()":
```java
public interface Seq<T> extends Iterable<T> {
  // ...

  default Iterator<T> iterator() {
    return new Iterator<>() {
      private int i;
      
      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        return get(i++);
      }
    };
  }

  // ...
}
```

8. **À la question 5, on a vu comment cacher la déclaration d'une classe à l'intérieur d'une méthode, on peut faire la même chose avec la classe SeqImpl et déclarer l'implantation sous forme d'une classe anonyme à l'intérieur d'une méthode dans l'interface.
   Quelle doit être la visibilité de la méthode contenant la déclaration de la classe anonyme d'implantation ?**

La visibilité de la méthode doit être publique afin qu'un acteur externe au package puisse tout de même l'instancier de l'extérieur.
Cependant, l'implantation doit rester privée afin qu'on ne puisse fournir un accès direct.
