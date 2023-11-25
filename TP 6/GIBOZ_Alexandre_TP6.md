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
public final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int nodeCount;

  MatrixGraph(int nodeCount) {
     if (nodeCount < 0) { throw new IllegalArgumentException(); }

     @SuppressWarnings("unchecked") // Réduction de portée à la variable locale 
     var graph = (T[]) new Object[nodeCount * nodeCount];
     this.graph = graph;
     this.nodeCount = nodeCount;
  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }
}
```

On spécifie un `@SuppressWarnings("unchecked")` au-dessus du cast pour affirmer au compilateur que ce cast est safe et qu'il peut faire confiance à
notre encapsulation pour que le cast soit systématiquement acceptable.

On modifie également l'interface `Graph` pour qu'elle devienne une interface scellée ne permettant son implantation uniquement 
par la classe `MatrixGraph`. 
On décommente aussi la méthode `nodeCount` en y ajoutant son type de retour :
```java
/**
 * An oriented graph with values on edges and not on nodes.
 */
public sealed interface Graph<T> permits MatrixGraph {
  /**
   * Returns the number of nodes of this graph.
   * 
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
   * @throws IllegalArgumentException if nodeCount is a negative value.
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
méthode statique est à présent l'unique point d'accès permettant d'obtenir une MatrixGraph. 
On conserve cette comparaison dans le constructeur uniquement pour passer le test Q1.

On modifie la classe en conséquence en faisant de cette dernière une classe à la visibilité `package`:
```java
final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int nodeCount;

  MatrixGraph(int nodeCount) {
//    if (nodeCount < 0) {
//      throw new IllegalArgumentException();
//    }
    
    @SuppressWarnings("unchecked")
    var graph = (T[]) new Object[nodeCount * nodeCount];
    
    this.graph = graph;
    this.nodeCount = nodeCount;
  }

  @Override
  public int nodeCount() {
    return nodeCount;
  }
}
```

3. **Indiquer comment trouver la case (i, j) dans un tableau à une seule dimension de taille nodeCount * nodeCount.**

Pour trouver la case (i, j) au travers d'un tableau à une seule dimension, il est nécessaire d'y accéder en utilisant l'opération suivante : nodeCount * i + j. 

**Afin d'implanter correctement la méthode getWeight, rappeler à quoi sert la classe java.util.Optional en Java.**

La classe `Optional` permet de renvoyer une valeur contenant potentiellement une valeur.
L'intérêt est d'indiquer qu'il existe un scénario ou la méthode ne renvoi rien (comportement dù à l'état actuel du graph et de son contenu), et que les méthodes
utilisant cette dernière devront s'assurer que la valeur existe, et adapter le comportement de l'application en fonction de l'existence ou non de la valeur.

**Implanter la méthode addEdge en utilisant la javadoc pour savoir quelle est la sémantique exacte.**

On implante la méthode `addEdge` et `getWeight`:
```java
@Override
public void addEdge(int src, int dst, T weight) {
  Objects.requireNonNull(weight);
  checkBothIndexes(src, dst);
  
  graph[src * nodeCount + dst] = weight;
}

@Override
public Optional<T> getWeight(int src, int dst) {
  checkBothIndexes(src, dst);

  return Optional.ofNullable(graph[src * nodeCount + dst]);
}

private void checkBothIndexes(int src, int dst) {
  Objects.checkIndex(src, nodeCount);
  Objects.checkIndex(dst, nodeCount);
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
/**
 * Adds all the edge values of the graph taken as parameter to the current graph,
 * uses the {@code merger} if there is already a value to merge the value.
 *
 * @param graph  a graph
 * @param merger the function to call if there are two values to merge.
 * @throws NullPointerException     if either graph or merger is null.
 * @throws IllegalArgumentException if the graphs do not have the same number of nodes.
 */
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

Un itérateur est un objet permettant de parcourir une collection d'éléments. 
Il permet de parcourir une collection sans avoir à connaître la structure ou la taille de cette dernière, tout en assurant une complexité O(n).

Elle possède un total de 4 méthodes, dont `hasNext()` et `next()` qui sont les deux méthodes abstraites à implanter systématiquement. 
La première méthode renvoi une valeur booléenne en fonction de s'il existe un élément suivant dans la collection, la seconde méthode renvoi l'élément E suivant de la collection.

**Que renvoie next si hasNext retourne false ?**

Dans le cas ou aucun élément n'est présent et que l'on appelle la méthode `next()`, une `NoSuchElementException` est renvoyée.

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

8. **On souhaite ajouter une méthode forEachEdge qui prend en paramètre un index d'un nœud et une fonction qui est appel cette fonction avec chaque arc sortant de ce nœud.
     Pour cela, nous allons, dans un premier temps, définir le type Graph.Edge à l'intérieur de l'interface Graph. 
     Un Graph.Edge est définie par un entier src, un entier dst et un poids weight.
     Écrire la méthode forEachEdge en utilisant la javadoc pour savoir quelle est la sémantique exacte.**

On ajoute un record `Edge` dans l'interface `Graph`:
```java
record Edge<T>(int src, int dst, T weight) {
  public Edge {
    Objects.requireNonNull(weight);
    if (src < 0 || dst < 0) {
      throw new IllegalArgumentException();
    }
  }
}
```

On ajoute à présent la méthode par défaut `forEachEdge`:
```java
default void forEachEdge(int src, Consumer<? super Edge<T>> function) {
  Objects.requireNonNull(function);
  Objects.checkIndex(src, nodeCount());
  
  for (var dst = 0; dst < nodeCount(); dst++) {
    var weight = getWeight(src, dst);
    if (weight.isPresent()) {
      function.accept(new Edge<>(src, dst, weight.get()));
    }
  }
}
```

9. **Enfin, on souhaite écrire une méthode edges qui renvoie tous les arcs du graphe sous forme d'un stream.
   L'idée ici n'est pas de réimplanter son propre stream (c'est prévu dans la suite du cours) mais de créer un stream sur tous les nœuds (sous forme d'entier) puis pour chaque nœud de renvoyer tous les arcs en réutilisant la méthode forEachEdge que l'on vient d'écrire.
   Écrire la méthode edges en utilisant la javadoc pour savoir quelle est la sémantique exacte.**

On implante la méthode `edges`:
```java
  default Stream<Edge<T>> edges() {
    return IntStream.range(0, nodeCount())
      .boxed()
      .flatMap(src -> {
        var stream = Stream.<Edge<T>>builder();
        forEachEdge(src, stream::add);
        return stream.build();
      });
  }
```

<br>

## Exercice 3 - NodeMapGraph

On souhaite fournir une implantation de l'interface Graph par table de hachage qui pour chaque nœud permet de stocker l'ensemble des arcs sortant. 
Pour un nœud donné, on utilise une table de hachage qui a un nœud destination associe le poids de l'arc. 
Si un nœud destination n'est pas dans la table de hachage cela veut dire qu'il n'y a pas d'arc entre le nœud source et le nœud destination.
Le graphe est représenté par un tableau dont chaque case correspond à un nœud, donc chaque case contient une table de hachage qui associe à un nœud destination le poids de l'arc correspondant.

Les tests unitaires sont les mêmes que précédemment car NodeMapGraph est une autre implantation de l'interface Graph, il suffit de dé-commenter la méthode référence dans graphFactoryProvider.

1. **Écrire dans l'interface Graph la méthode createNodeMapGraph et implanter la classe NodeMapGraph (toujours non publique).**

On commence par modifier l'interface `Graph`:
```java
public sealed interface Graph<T> permits MatrixGraph, NodeMapGraph {
  // ...
  static <T> Graph<T> createNodeMapGraph(int nodeCount) {
    return new NodeMapGraph<>(nodeCount);
  }
}
```

On crée à présent la classe `NodeMapGraph`:
```java
public final class NodeMapGraph<T> implements Graph<T> {
   private final int nodeCount;
   private final HashMap<Integer, T>[] map;

   NodeMapGraph(int nodeCount) {
      if (nodeCount < 0) { throw new IllegalArgumentException(); }

      @SuppressWarnings("unchecked")
      var map = (HashMap<Integer, T>[]) new HashMap<?, ?>[nodeCount];
      this.map = map;
      this.nodeCount = nodeCount;
   }

   @Override
   public int nodeCount() {
      return nodeCount;
   }

   @Override
   public void addEdge(int src, int dst, T weight) {
      Objects.requireNonNull(weight);
      Objects.checkIndex(src, nodeCount);
      Objects.checkIndex(dst, nodeCount);
      map[src].put(dst, weight);
   }

   @Override
   public Optional<T> getWeight(int src, int dst) {
      Objects.checkIndex(src, nodeCount);
      Objects.checkIndex(dst, nodeCount);
      return Optional.ofNullable(map[src].get(dst));
   }

   @Override
   public Iterator<Integer> neighborIterator(int src) {
      Objects.checkIndex(src, nodeCount);
      return map[src].keySet().iterator();
   }
}
```