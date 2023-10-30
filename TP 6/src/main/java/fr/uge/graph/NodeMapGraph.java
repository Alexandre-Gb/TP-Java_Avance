package fr.uge.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

final class NodeMapGraph<T> implements Graph<T> {
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
