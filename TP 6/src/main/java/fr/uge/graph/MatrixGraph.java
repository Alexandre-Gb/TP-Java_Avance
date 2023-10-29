package fr.uge.graph;

import java.util.Objects;
import java.util.Optional;

final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int nodeCount;

  public MatrixGraph(int nodeCount) {
    if (nodeCount < 0) {
      throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    T[] graph = (T[]) new Object[nodeCount * nodeCount];
    this.graph = graph;
    this.nodeCount = nodeCount;
  }

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

  @Override
  public int nodeCount() {
    return nodeCount;
  }
}
