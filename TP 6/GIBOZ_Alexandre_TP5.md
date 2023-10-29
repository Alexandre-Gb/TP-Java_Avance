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
  private final int capacity;

   public MatrixGraph(int capacity) {
      if (capacity < 0) {
         throw new IllegalArgumentException();
      }

      @SuppressWarnings("unchecked") // Réduction de porté à la variable locale 
      T[] graph = (T[]) new Object[capacity * capacity];
      this.graph = graph;
      this.capacity = capacity;
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
    return capacity;
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

