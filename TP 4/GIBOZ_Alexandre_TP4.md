# TP4 - Hacher menu
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td04.php)
***

## Exercice 2 - HashTableSet

1. **Quels doivent être les champs de la classe Entry correspondant à une case d'une des listes chaînées utilisées par table de hachage**

Les champs seront les suivants:
- `key` : La chaîne de caractère que l'on souhaite stocker
- `next` : La référence vers l'Entry suivante dans la liste chainée (ou null si c'est la dernière)

**Rappeler quelle est l'intérêt de déclarer Entry comme membre de la classe HashTableSet plutôt que comme une classe à coté dans le même package que HashTableSet ?**

L'avantage de passer par une classe interne est qu'il devient possible de mettre en place une classe Entry qui sera uniquement visible et accessible par la classe HashTableSet (si la classe est privée).
Par ailleurs, Entry pourra, si besoin, accéder aux champs de HashTableSet.

**Ne pourrait-on pas utiliser un record plutôt qu'une classe, ici ? Si oui, pourquoi ? Si non, pourquoi ?**

Si l'on insère de la tête à la queue, chaque entrée contiendra une référence vers la suivante, qui sera, au départ, null.
Cette référence pouvant changer en fonction des ajouts et suppressions, il est nécessaire de pouvoir la modifier, ce qu'un Record ne permet pas.

Cependant, dans notre cas, on insère la queue d'abord (qui aura une référence nulle indiquant la fin de la liste chaînée), puis les autres entrées.
Dans ce mode d'insertion précis, et puisqu'il n'est pas prévu que l'on puisse modifier/supprimer des chainons, alors l'usage d'un Record est préférable. 

**Écrire la classe HashTableSet dans le package fr.uge.set et ajouter Entry en tant que classe interne.**

Classe `HashTableSet`:
```java
package fr.uge.set;

public final class HashTableSet {
  private record Entry(Object key, Entry next) { }
}

```

2. **On souhaite maintenant ajouter un constructeur sans paramètre, une méthode add qui permet d'ajouter un élément non null et une méthode size qui renvoie le nombre d'éléments insérés (avec une complexité en O(1)).**

On modifie la classe `HashTableSet` comme suit:
```java
package fr.uge.set;

import java.util.Objects;

public final class HashTableSet {
  private final static int INIT_SIZE = 16;
  private final Entry[] entries = new Entry[INIT_SIZE];
  private int size; // 0 by default

  public int size() {
    return size;
  }

  public void add(Object key) {
    Objects.requireNonNull(key);
    int index = key.hashCode() & entries.length - 1;

    if (!existsAtIndex(key, index)) {
      entries[index] = new Entry(key, entries[index]);
      size++;
    }
  }

  private boolean existsAtIndex(Object key, int index) {
    for (var entry = entries[index]; entry != null; entry = entry.next) {
      if (entry.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  private record Entry(Object key, Entry next) { }
}
}
```

3. **On cherche maintenant à implanter une méthode forEach qui prend en paramètre une fonction. 
     La méthode forEach parcourt tous les éléments insérés et pour chaque élément, appelle la fonction prise en paramètre avec l'élément courant.
     Quelle doit être la signature de la functional interface prise en paramètre de la méthode forEach ?**

La functional interface devra prendre un Object en paramètre, et ne renverra rien.

**Quel est le nom de la classe du package java.util.function qui a une méthode ayant la même signature ?**

Le nom de cette interface fonctionnelle est `Consumer`.

**Écrire la méthode forEach.**

On ajoute la méthode suivante à la classe `HashTableSet`:
```java
public void forEach(Consumer<Object> consumer) {
  Objects.requireNonNull(consumer);
  for (var entry : entries) {
    for (var e = entry; e != null; e = e.next) {
      consumer.accept(e.key);
    }
  }
}
```

4. **On souhaite maintenant ajouter une méthode contains qui renvoie si un objet pris en paramètre est un élément de l'ensemble ou pas, sous forme d'un booléen.
     Expliquer pourquoi nous n'allons pas utiliser forEach pour implanter contains (Il y a deux raisons, une algorithmique et une spécifique à Java).**

Raison spécifique à Java:
Notre méthode forEach ne peut pas être utilisée car elle ne renvoi aucune valeur (consumer) et, de plus, ne s'arrète pas si jamais l'élément est trouvé.
Le forEach va continuer a boucler jusqu'a la fin même si on a déja trouvé l'élément, ce qui est inutile.

Raison algorithmique:
La méthode forEach va parcourir chaque index du tableau. Il serait préférable que la méthode calcule l'index a partir du hash de l'objet, puis
parcours uniquement la liste chainée correspondante en s'arrêtant dès que l'élément est trouvé.

**Écrire la méthode contains.**

On obtient la classe `HashTableSet` suivante:
```java
public final class HashTableSet {
  private final static int INIT_SIZE = 16;
  private final Entry[] entries = new Entry[INIT_SIZE];
  private int size; // 0 by default

  public int size() {
    return size;
  }

  public void add(Object key) {
    Objects.requireNonNull(key);
    int index = getIndex(key);

    if (!containsAtIndex(key, index)) {
      entries[index] = new Entry(key, entries[index]);
      size++;
    }
  }

  public void forEach(Consumer<Object> consumer) {
    Objects.requireNonNull(consumer);
    for (var entry : entries) {
      for (var e = entry; e != null; e = e.next) {
        consumer.accept(e.key);
      }
    }
  }

  public boolean contains(Object key) {
    Objects.requireNonNull(key);
    return containsAtIndex(key, getIndex(key));
  }

  private boolean containsAtIndex(Object key, int index) {
    Objects.requireNonNull(key);
    for (var entry = entries[index]; entry != null; entry = entry.next) {
      if (entry.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  private int getIndex(Object key) {
    Objects.requireNonNull(key);
    return key.hashCode() & entries.length - 1;
  }

  private record Entry(Object key, Entry next) { }
}
```

5. **On veut maintenant faire en sorte que la table de hachage se redimensionne toute seule. 
     Pour cela, lors de l'ajout d'un élément, on peut avoir à agrandir la table pour garder comme invariant que la taille du tableau est au moins 2 fois plus grande que le nombre d'éléments.
     Pour agrandir la table, on va créer un nouveau tableau deux fois plus grand et recopier touts les éléments dans ce nouveau tableau à la bonne place. 
     Ensuite, il suffit de remplacer l'ancien tableau par le nouveau.
     Expliquer pourquoi, en plus d'être plus lisible, en termes de performance, l'agrandissement doit se faire dans sa propre méthode.**

Il est toujours préférable d'avoir une méthode dédiée à chaque tâche précise, et ce afin qu'elle puisse être appelée à n'importe quelle moment ou cela s'avère
être une nécessité. Le code est ainsi plus simple à relire et à maintenir.

**Modifier votre implantation pour que la table s'agrandisse dynamiquement.**

Classe `HashTableSet`:
```java
package fr.uge.set;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class HashTableSet {
  private final static int INIT_SIZE = 16;
  private Entry[] entries = new Entry[INIT_SIZE];
  private int size; // 0 by default

  public int size() {
    return size;
  }

  public void add(Object key) {
    Objects.requireNonNull(key);
    int index = getIndex(key);

    if (!containsAtIndex(key, index)) {
      entries[index] = new Entry(key, entries[index]);
      size++;

      if (size >= entries.length / 2) {
        enlargeEntries();
      }
    }
  }

  public void forEach(Consumer<Object> consumer) {
    Objects.requireNonNull(consumer);
    for (var entry : entries) {
      for (var e = entry; e != null; e = e.next) {
        consumer.accept(e.key);
      }
    }
  }

  public boolean contains(Object key) {
    Objects.requireNonNull(key);
    return containsAtIndex(key, getIndex(key));
  }

  private boolean containsAtIndex(Object key, int index) {
    Objects.requireNonNull(key);
    for (var entry = entries[index]; entry != null; entry = entry.next) {
      if (entry.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  private int getIndex(Object key) {
    Objects.requireNonNull(key);
    return key.hashCode() & entries.length - 1;
  }

  private void enlargeEntries() {
    var newEntries = new Entry[entries.length * 2];

    forEach(key -> {
      int index = getIndex(key);
      newEntries[index] = new Entry(key, newEntries[index]);
    });

//    for (var entry : entries) {
//      for (var e = entry; e != null; e = e.next) {
//        int index = getIndex(e.key);
//        newEntries[index] = new Entry(e.key, newEntries[index]);
//      }
//    }

    entries = newEntries;
  }
  
  // ...
}
```

Entries est devenu non-final afin de pouvoir être élargi. La nouvelle méthode privée enlargeEntries est appelée lorsqu'on ajoute un élément et que la taille égalise ou dépasse la moitié
de la taille du tableau. 
La méthode utilise le forEach codé précédemment et la méthode getIndex, et ce dans le but d'optimiser le code et de le réutiliser le plus possible.

6. **L'implantation actuelle a un problème : même si on n'ajoute que des String lorsque l'on utilise forEach, l'utilisateur va probablement devoir faire des cast parce que les éléments envoyés par forEach sont typés Object.
   Par exemple, si on veut vérifier que toutes les chaînes de caractères contenues dans un ensemble commencent par "f", le code ci-dessous ne fonctionne pas**
```java
var set = new HashTableSet();
set.add("foo");
set.add("five");
set.add("fallout");
set.forEach(element -> assertTrue(element.startsWith("f")));
```

**Pour que ce code fonctionne, il faut dire que le type des éléments que l'on ajoute avec add et le type des éléments que l'on reçoit dans la lambda du forEach est le même. 
Pour cela, il faut déclarer HashTableSet comme un type paramétré.
Rappeler pourquoi en Java, il n'est pas possible de créer un tableau de type paramétré ?**

Du à l'erasure, les types paramétrés sont présents à la compilation mais absents à l'exécution. A l'exécution, les types paramétrés
sont remplacés par des raw types, des bornes ou des casts.
Les tableaux sont covariants. Les types arguments sont supprimés à l'exécution par l'erasure, empechant la VM de fonctionner correctement et de lever
des erreurs comme `ArrayStoreException`.

**Quel est le work around ?**

Il est possible de créer des tableaux de variable de type grâce à un cast non-safe. On créé un tableau d'Object qui sera casté en tableau de E.

**Pourquoi celui-ci génère-t-il un warning ?**

Le cast est "non-safe", ce qui déplait au compilateur qui nous avertit.

**Dans quel cas et comment peut on supprimer ce warning ?**

On supprime ce warning grâce à l'annotation `@SuppressWarnings("unchecked")` qui indique au compilateur de ne pas générer de warning pour cette ligne.
On indique ainsi au compilateur qu'il peut nous faire confiance sur le fait que ce cast est sûr.

**Mettez en commentaire votre ancien code, puis dupliquez-le pour faire les changements qui permettent d'avoir un ensemble paramétré par le type des ses éléments.**

On obtient la classe `HashTableSet` suivante:
```java
public final class HashTableSet<E> {
     private final static int INIT_SIZE = 16;
     
     @SuppressWarnings("unchecked")
     private Entry<E>[] entries = new Entry[INIT_SIZE];
     private int size; // 0 by default

     public int size() {
          return size;
     }

     public void add(E key) {
          Objects.requireNonNull(key);
          int index = getIndex(key);

          if (!containsAtIndex(key, index)) {
               entries[index] = new Entry<>(key, entries[index]);
               size++;

               if (size >= entries.length / 2) {
                    enlargeEntries();
               }
          }
     }

     public void forEach(Consumer<? super E> consumer) {
          Objects.requireNonNull(consumer);
          for (var entry : entries) {
               for (var e = entry; e != null; e = e.next) {
                    consumer.accept((E) e.key);
               }
          }
     }

     public boolean contains(E key) {
          Objects.requireNonNull(key);
          return containsAtIndex(key, getIndex(key));
     }

     private boolean containsAtIndex(E key, int index) {
          Objects.requireNonNull(key);
          for (var entry = entries[index]; entry != null; entry = entry.next) {
               if (entry.key.equals(key)) {
                    return true;
               }
          }
          return false;
     }

     private int getIndex(E key) {
          Objects.requireNonNull(key);
          return key.hashCode() & entries.length - 1;
     }

      @SuppressWarnings("unchecked")
      private void enlargeEntries() {
          var newEntries = new Entry[entries.length * 2];

          forEach(key -> {
               int index = getIndex(key);
               newEntries[index] = new Entry<E>(key, newEntries[index]);
          });

          entries = newEntries;
      }

     private record Entry<E>(E key, Entry<E> next) { }
}
```

7. **En fait, la signature de la méthode forEach que vous avez écrite n'est pas la bonne. 
     En effet, forEach appelle la lambda avec des éléments de type E, donc la fonction peut prendre en paramètre des valeurs qui sont des super-types de E.
     Regarder comme on déclare un super-type dans un type paramétré en regardant la javadoc de la méthode forEach de ArrayList.**

On procède de la façon suivante pour déclarer un super-type:
```java
public void forEach(Consumer<? super E> consumer) {
  // ...
}
```

**Modifier votre code en conséquence.**

On obtient la méthode suivante:
```java
public void forEach(Consumer<? super E> consumer) {
  Objects.requireNonNull(consumer);
  for (var entry : entries) {
    for (var e = entry; e != null; e = e.next) {
      consumer.accept((E) e.key);
    }
  }
}
```

8. **On souhaite maintenant écrire une méthode addAll qui permet d'ajouter tous les éléments d'un HashTableSet dans le HashTableSet courant.
   Note : les éléments du HashTableSet pris en paramètre peuvent être des sous-types du type du HashTableSet courant. Si vous ne savez pas comment déclarer un sous-type dans un type paramétré, vous pouvez regarder la méthode Collection.addAll() des API du JDK.
   Écrire la méthode addAll.**

On obtient la méthode suivante:
```java
public void addAll(HashTableSet<? extends E> set) {
  Objects.requireNonNull(set);
  set.forEach(this::add);
}
```

9. **Enfin, pour les plus balèzes, on souhaite pouvoir tester si deux HashTableSet sont égaux avec la méthode equals.
   Modifier votre code en conséquence.**