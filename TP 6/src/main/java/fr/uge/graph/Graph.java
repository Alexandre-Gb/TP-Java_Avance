package fr.uge.graph;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An oriented graph with values on edges and not on nodes.
 */
public sealed interface Graph<T> permits MatrixGraph, NodeMapGraph {
  /**
   * Returns the number of nodes of this graph.
   *
   * @return the number of nodes of this graph.
   */
  int nodeCount();

  /**
   * Create a graph implementation based on a matrix.
   *
   * @param <T>       type of the edge weight.
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

  /**
   * Add an edge between two nodes or replace it if an edge already exists.
   *
   * @param src    source node.
   * @param dst    destination node.
   * @param weight weight of the edge.
   * @throws NullPointerException      if weight is {@code null}.
   * @throws IndexOutOfBoundsException if src or dst is not a valid node number.
   */
  void addEdge(int src, int dst, T weight);

  /**
   * Return the weight of an edge.
   *
   * @param src source ndoe.
   * @param dst destination nde.
   * @return the weight of the edge between {@code src}and {@code dst} or Optional.empty().
   * @throws IndexOutOfBoundsException if src or dst is not a valid node number.
   */
  Optional<T> getWeight(int src, int dst);

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

  /**
   * Returns all the nodes that are connected to the node taken as parameter.
   * The order of the nodes may be different that the insertion order.
   *
   * @param src a node.
   * @return an iterator on all nodes connected to the specified source node.
   * @throws IndexOutOfBoundsException if src is not a valid node number.
   */
  Iterator<Integer> neighborIterator(int src);

  /**
   * An edge of the graph.
   *
   * @param src    the index of the source node.
   * @param dst    the index of the destination node.
   * @param weight the weight associated to the edge.
   * @param <T>    the type of the weight
   */
  record Edge<T>(int src, int dst, T weight) {
    public Edge {
      Objects.requireNonNull(weight);
      if (src < 0 || dst < 0) {
        throw new IllegalArgumentException();
      }
    }
  }

  /**
   * Call the consumer for each edge associated to the source node.
   *
   * @param src      the source node.
   * @param function the function called for all edge that have src as source node.
   * @throws NullPointerException      if consumer is null.
   * @throws IndexOutOfBoundsException if src is not a valid index for a node.
   */
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

  /**
   * Returns all the edges of the graph that have a value using mapmulti on stream.
   *
   * @return all the edges of the graph that have a value in any order.
   */
  default Stream<Edge<T>> edges() {
    return IntStream.range(0, nodeCount())
      .boxed()
      .flatMap(src -> {
        var stream = Stream.<Edge<T>>builder();
        forEachEdge(src, stream::add);
        return stream.build();
      });
  }

  /**
   * Create a graph implementation based on a node map.
   *
   * @param nodeCount the number of nodes
   * @param <T>       type of the edge weight
   * @return a new graph implementation
   */
  static <T> Graph<T> createNodeMapGraph(int nodeCount) {
    return new NodeMapGraph<>(nodeCount);
  }
}