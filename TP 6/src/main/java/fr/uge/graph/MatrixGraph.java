package fr.uge.graph;

public final class MatrixGraph<T> implements Graph<T> {
  private final T[] graph;
  private final int capacity;

  public MatrixGraph(int capacity) {
    if (capacity < 0) {
      throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    T[] graph = (T[]) new Object[capacity * capacity];
    this.graph = graph;
    this.capacity = capacity;
  }

  @Override
  public int nodeCount() {
    return capacity;
  }
}
