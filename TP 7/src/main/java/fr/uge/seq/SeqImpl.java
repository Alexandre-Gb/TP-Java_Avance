package fr.uge.seq;

import java.util.List;
import java.util.Objects;

final class SeqImpl<T> implements Seq<T> {
  private final List<T> elements;

  SeqImpl(List<T> elements) {
    // Objects.requireNonNull(elements); Not mandatory as it is an implementation
    // this.elements = List.copyOf(elements); Not mandatory as it is an implementation and we always make a defensive copy
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
