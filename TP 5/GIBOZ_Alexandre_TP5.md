# TP5 - Faites la queue
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td05.php)
***

## Exercice 2 - Fifo

1. **On souhaite écrire une classe Fifo générique (avec une variable de type E) dans le package fr.uge.fifo prenant en paramètre une capacité (un entier), le nombre d’éléments maximal que peut stocker la structure de données.
   On souhaite de plus, écrire la méthode offer qui permet d'ajouter des éléments à la fin (tail) du tableau circulaire sachant que pour l'instant on ne se préoccupera pas du cas où le tableau est plein. Et une méthode size qui renvoie le nombre d'éléments ajoutés.
   Écrire le code correspondant.**

Classe `Fifo`:
```java
package fr.uge.fifo;

import java.util.Objects;

public class Fifo<E> {
  private static final int DEFAULT_CAPACITY = 16;
  private final E[] fifo;
  private final int capacity;
  private int head;
  private int tail;
  private int size;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException();
    }

    this.capacity = capacity;
    fifo = (E[]) new Object[capacity];
  }

  public Fifo() {
    this(DEFAULT_CAPACITY);
  }

  public void offer(E value) {
    Objects.requireNonNull(value);

    if (size < fifo.length) {
      fifo[tail] = value;
      size++;
      tail = reindex(tail + 1);
    }
  }

  public int size() {
    return size;
  }
}
```

2. **Avez-vous pensé aux préconditions ?**

Oui j'y ai pensé :)

3. **On souhaite écrire une méthode poll qui retire un élément du tableau circulaire. Que faire si la file est vide ?
   Écrire le code de la méthode poll.**

Si la file est vide, on renvoi null.

Méthode `poll`, `peek` et `reindex`:
```java
public E peek() {
  return fifo[head];
}

public E poll() {
  if (fifo[head] == null) {
    return null;
  }

  var value = fifo[head];
  fifo[head] = null;
  size--;
  head = reindex(head + 1);
  return value;
}

private int reindex(int index) {
  return index % capacity;
}
```

4. **Rappelez ce qu'est un memory leak en Java**

Un memory leak est une fuite de mémoire, c'est à dire que la mémoire allouée n'est pas libérée.
Le garbage collector est l'entité devant s'occuper de la libération de mémoire allouée par les objets qui ne sont plus référencés.

La classe ne permet pas de memory leak, car chaque élément est remis à null dans la file lors de l'extraction.

5. **On souhaite agrandir le tableau circulaire dynamiquement en doublant sa taille quand le tableau est plein. 
     Attention, il faut penser au cas où le début de la liste (head) a un indice qui est supérieur à l'indice indiquant la fin de la file (tail).
     Modifier votre implantation pour que le tableau s'agrandisse dynamiquement en ajoutant une méthode resize.**

Classe `Fifo`:
```java
public class Fifo<E> {
  private static final int DEFAULT_CAPACITY = 16;
  private E[] fifo;
  private int capacity;
  private int head;
  private int tail;
  private int size;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException();
    }

    this.capacity = capacity;
    fifo = (E[]) new Object[capacity];
  }

  public Fifo() {
    this(DEFAULT_CAPACITY);
  }

  public void offer(E value) {
    Objects.requireNonNull(value);

    if (size == capacity) {
      resize();
    }

    fifo[tail] = value;
    tail = reindex(tail + 1);
    size++;
  }

  public E peek() {
    return fifo[head];
  }

  public E poll() {
    if (fifo[head] == null) {
      return null;
    }

    var value = fifo[head];
    fifo[head] = null;
    size--;
    head = reindex(head + 1);
    return value;
  }

  private int reindex(int index) {
    return index % capacity;
  }

  public void resize() {
    var newcapacity = capacity * 2;

    @SuppressWarnings("unchecked")
    var newFifo = (E[]) new Object[newcapacity];

    if (head < tail) {
      System.arraycopy(fifo, head, newFifo, 0, size);
    } else {
      System.arraycopy(fifo, head, newFifo, 0, fifo.length - head);
      System.arraycopy(fifo, 0, newFifo, fifo.length - head, tail + 1);
    }

    fifo = newFifo;
    head = 0;
    tail = capacity;
    capacity = newcapacity;
  }

  public int size() {
    return size;
  }
}
```

6. **On souhaite ajouter une méthode d'affichage qui affiche les éléments dans l'ordre dans lequel ils seraient sortis en utilisant poll. L'ensemble des éléments devra être affiché entre crochets ('[' et ']') avec les éléments séparés par des virgules (suivies d'un espace).
   Écrire la méthode d'affichage.**

Méthode `toString`:
```java
@Override
public String toString() {
  //  Useless since the stringJoiner will take care of that behaviour if no elements is present
  //  if (size == 0) {
  //    return "[]";
  //  }
  
  var stringJoiner = new StringJoiner(", ", "[", "]");
  var index = head;
  for (var i = 0; i < size; i++) {
    stringJoiner.add(fifo[index].toString());
    index = reindex(index + 1);
  }
  
  return stringJoiner.toString();
}
```

7. **En fait, le code que vous avez écrit est peut-être faux (oui, les testent passent, et alors ?)... Le calcul sur les entiers n'est pas sûr/sécurisé en Java (ou en C, car Java à copié le modèle de calcul du C). 
    En effet, une opération '+' sur deux nombres suffisamment grand devient négatif.**

Le test passe, aucune modification n'est nécessaire.
![test7](src/main/resources/screen-test7.png)

8. **Rappelez quel est le principe d'un itérateur.**

Un itérateur est un objet permettant de parcourir une collection d'objets en ayant la garantie de le faire sous une complexité O(n).
Un itérateur possède deux méthodes: `hasNext` (qui retourne un booléen indiquant si l'itérateur possède un élément suivant) et `next` (qui retourne l'élément suivant).

**Quel doit être le type de retour de la méthode iterator() ?**

La méthode `iterator` doit retourner un objet implémentant l'interface `Iterator`.

**Implanter la méthode iterator().**

On ajoute la méthode suivante à la classe `Fifo`:
```java
public Iterator<E> iterator() {
  return new Iterator<>() {
    private int index = head;
    private int count = 0;
    
    @Override
    public boolean hasNext() {
      return count < size;
    }
    
    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      
      var value = fifo[index];
      index = reindex(index + 1);
      count++;
      return value;
    }
  };
}
```

9. **On souhaite que le tableau circulaire soit parcourable en utilisant une boucle for-each-in, comme ceci :**
```java
var fifo = ...
for(var value: fifo) {
    // ...
}
```

**Quelle interface doit implanter la classe Fifo ?**

La classe `Fifo` doit implémenter l'interface `Iterable` (qu'implémente déjà la plupart des collections en dehors des Maps).

**Implanter cette interface**

On implante l'interface ainsi:
```java
public class Fifo<E> implements Iterable<E> {
  // ...
}
```