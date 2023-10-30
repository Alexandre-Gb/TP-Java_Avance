# TP6 - Trop Graph
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td06.php)
***

## Exercice 2 - MatrixGraph

1. **On souhaite créer la classe paramétrée (par le type des valeurs des arcs) MatrixGraph comme seule implantation possible de l'interface Graph définie par le fichier Graph.java
   La classe MatrixGraph contient
   Un champ array, un tableau des valeurs des arcs comme expliqué ci-dessus,
   Un constructeur qui prend en paramètre le nombre de nœuds du graphe,
   Une méthode nodeCount qui renvoie le nombre de nœuds du graphe.**

**Pour l'implantation du constructeur, rappeler pourquoi, en Java, il n'est pas possible de créer des tableaux de variables de type.**

Du au mécanisme d'erasure mis en place à la compilation, il n'est pas possible de conserver le type argument d'une variable de type lors de l'exécution.
Ce comportement a pour conséquences de rendre la création d'un tableau de variables de type impossible.

**Écrire la classe MatrixGraph, ses champs, son constructeur et la méthode nodeCount.**

Classe `MatrixGraph`:
```java
package fr.uge.graph;

public final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int nodeCount;

   public MatrixGraph(int nodeCount) {
      if (nodeCount < 0) {
         throw new IllegalArgumentException();
      }

      @SuppressWarnings("unchecked") // Réduction de porté à la variable locale 
      T[] graph = (T[]) new Object[nodeCount * nodeCount];
      
      this.graph = graph;
      this.nodeCount = nodeCount;
   }
   
//  @SuppressWarnings("unchecked")
//  public MatrixGraph(int capacity) {
//    if (capacity < 0) {
//      throw new IllegalArgumentException();
//    }
//
//    this.capacity = capacity;
//    this.graph = (T[]) new Object[capacity * capacity];
//  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }
}
```

On spécifie un `@SuppressWarnings("unchecked")` au desuss du constructeur pour affirmer au compilateur que le cast est safe et qu'il peut faire confiance à
notre encapsulation pour que le cast soit systématiquement safe.

On modifie également l'interface `Graph` pour qu'elle devienne une interface scellée ne permettant son implantation uniquement 
par la classe `MatrixGraph`. On décommente aussi la méthode `nodeCount` en y ajoutant son type de retour:
```java
package fr.uge.graph;

/**
 * An oriented graph with values on edges and not on nodes.
 */
public sealed interface Graph<T> permits MatrixGraph {
  /**
   * Returns the number of nodes of this graph.
   * @return the number of nodes of this graph.
   */
  int nodeCount();

  // ...
}
```

2. **On peut remarquer que la classe MatrixGraph n'apporte pas de nouvelles méthodes par rapport aux méthodes de l'interface Graph donc il n'est pas nécessaire que la classe MatrixGraph soit publique.
   Ajouter une méthode factory nommée createMatrixGraph dans l'interface Graph et déclarer la classe MatrixGraph non publique.**

On ajoute la méthode statique à l'interface:
```java
public sealed interface Graph<T> permits MatrixGraph {
  // ...

  /**
   * Create a graph implementation based on a matrix.
   *
   * @param <T> type of the edge weight.
   * @param nodeCount the number of nodes.
   * @return a new implementation of Graph.
   */
  static <T> Graph<T> createMatrixGraph(int nodeCount) {
    if (nodeCount < 0) {
      throw new IllegalArgumentException();
    }

    return new MatrixGraph<>(nodeCount);
  }

  // ...
}
```

A noter que l'on fait maintenant le check de nodeCount ici, on a plus besoin de le faire dans le constructeur de `MatrixGraph` car cette nouvelle
méthode statique est à présent l'unique point d'accès permettant d'obtenir une MatrixGraph. On conserve cette comparaison dans le constructeur uniquement pour passer le test Q1.

On modifie la classe en conséquence en faisant de cette dernière une classe à la visibilité `package`:
```java
final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int nodeCount;

  public MatrixGraph(int nodeCount) {
//    if (nodeCount < 0) {
//      throw new IllegalArgumentException();
//    }
    
    @SuppressWarnings("unchecked")
    T[] graph = (T[]) new Object[nodeCount * nodeCount];
    
    this.graph = graph;
    this.nodeCount = nodeCount;
  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }
}
```

3. **Implanter la méthode addEdge en utilisant la javadoc pour savoir quelle est la sémantique exacte.
   Implanter la méthode getWeight en utilisant la javadoc pour savoir quelle est la sémantique exacte.**

On implante la méthode `addEdge` et `getWeight`:
```java
@Override
public void addEdge(int src, int dst, T weight) {
  Objects.requireNonNull(weight);
  Objects.checkIndex(src, nodeCount);
  Objects.checkIndex(dst, nodeCount);
  graph[src * nodeCount + dst] = weight;
}

@Override
public Optional<T> getWeight(int src, int dst) {
  Objects.checkIndex(src, nodeCount);
  Objects.checkIndex(dst, nodeCount);
  return Optional.ofNullable(graph[src * nodeCount + dst]);
}
```

4. **On souhaite maintenant implanter une méthode mergeAll qui permet d'ajouter les valeurs des arcs d'un graphe au graphe courant.
   Dans le cas où on souhaite ajouter une valeur à un arc qui possède déjà une valeur, on utilise une fonction prise en second paramètre qui prend deux valeurs et renvoie la nouvelle valeur.**

On implante la méthode `mergeAll`:
```java
@Override
public void mergeAll(Graph<? extends T> graph, BiFunction<? super T, ? super T, ? extends T> merger) {
  Objects.requireNonNull(graph);
  Objects.requireNonNull(merger);
  
  if (graph.nodeCount() != nodeCount) {
    throw new IllegalArgumentException();
  }
  
  for (int src = 0; src < nodeCount; src++) {
    for (int dst = 0; dst < nodeCount; dst++) {
      var weight = graph.getWeight(src, dst);
      if (weight.isPresent()) {
        var currentWeight = getWeight(src, dst);
        if (currentWeight.isPresent()) {
          addEdge(src, dst, merger.apply(currentWeight.get(), weight.get()));
        } else {
          addEdge(src, dst, weight.get());
        }
      }
    }
  }
}
```

5. **En fait, on peut remarquer que l'on peut écrire le code de mergeAll pour qu'il soit indépendant de l'implantation et donc écrire l'implantation de mergeAll directement dans l'interface.
   Déplacer l'implantation de mergeAll dans l'interface et si nécessaire modifier le code pour qu'il soit indépendant de l'implantation.**

On déplace la méthode dans l'interface, en faisant de la méthode une méthode par défaut:
```java
default void mergeAll(Graph<? extends T> graph, BiFunction<? super T, ? super T, ? extends T> merger) {
  Objects.requireNonNull(graph);
  Objects.requireNonNull(merger);
  if (graph.nodeCount() != nodeCount()) {
    throw new IllegalArgumentException();
  }
  
  for (int src = 0; src < nodeCount(); src++) {
    for (int dst = 0; dst < nodeCount(); dst++) {
      var weight = graph.getWeight(src, dst);
      if (weight.isPresent()) {
        var currentWeight = getWeight(src, dst);
        if (currentWeight.isPresent()) {
          addEdge(src, dst, merger.apply(currentWeight.get(), weight.get()));
        } else {
          addEdge(src, dst, weight.get());
        }
      }
    }
  }
}
```

6. **Rappeler le fonctionnement d'un itérateur et de ses méthodes hasNext et next.**

Un itérateur est un objet permettant de parcourir une collection d'éléments. Il permet de parcourir une collection sans avoir à connaître la structure ou la taille de cette dernière.

Elle possède un total de 4 méthodes, dont `hasNext()` et `next()`. La première renvoi une valeur booléenne en fonction de s'il existe un élément suivant dans la collection, la seconde 
renvoi l'élément suivant E de la collection.

**Que renvoie next si hasNext retourne false ?**

Dans le cas ou aucun élément n'est présent et que l'on appelle la méthode `next()`, cette dernière renvoi une `NoSuchElementException`.

**Expliquer pourquoi il n'est pas nécessaire, dans un premier temps, d'implanter la méthode remove qui fait pourtant partie de l'interface.**

Il ne sera pas nécessaire d'implanter cette méthode car il s'agit d'une méthode par défaut, contrairement à `next()` et `hasNext()`, 

**Implanter la méthode neighborsIterator(src) qui renvoie un itérateur sur tous les nœuds ayant un arc dont la source est src.**

On implante la méthode `neighborsIterator`:
```java
@Override
public Iterator<Integer> neighborIterator(int src) {
  Objects.checkIndex(src, nodeCount);
  
  return new Iterator<>() {
    private int counter; // 0 by default
    
    @Override
    public boolean hasNext() {
      for (; counter < nodeCount; counter++) {
        if (getWeight(src, counter).isPresent()) {
          return true;
        }
      }
      return false;
    }
    
    @Override
    public Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return counter++;
    }
  };
}
```

7. **Expliquer le fonctionnement précis de la méthode remove de l'interface Iterator.**

La méthode `remove()` permet de supprimer l'élément E renvoyé en dernier par l'itérateur. 
Cette méthode ne peut être appelée qu'une fois par cellule.

Si l'itérateur ne supporte pas la suppression, alors cette méthode renvoi une `UnsupportedOperationException`.

**Implanter la méthode remove de l'itérateur.**

On implante la méthode `remove()`:
```java
@Override
public Iterator<Integer> neighborIterator(int src) {
  Objects.checkIndex(src, nodeCount);
  return new Iterator<>() {
    private int counter;
    private int last = -1;

    @Override
    public boolean hasNext() {
      for (; counter < nodeCount; counter++) {
        if (getWeight(src, counter).isPresent()) {
          return true;
        }
      }

      return false;
    }

    @Override
    public Integer next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      last = counter;
      return counter++;
    }

    @Override
    public void remove() {
      if (last == -1 || graph[src * nodeCount + last] == null) {
        throw new IllegalStateException();
      }

      graph[src * nodeCount + last] = null;
    }
  };
}
```


