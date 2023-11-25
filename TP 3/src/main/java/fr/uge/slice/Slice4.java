package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice4<T> permits SliceImpl {
  static <T> Slice4<T> array(T[] array) {
    Objects.requireNonNull(array);
    return array(array, 0, array.length);
  }

  static <T> Slice4<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    return new SliceImpl<>(array, from, to);
  }

  T get(int index);

  int size();

  Slice4<T> subSlice(int from, int to);
}

final class SliceImpl<T> implements Slice4<T> {
  private final T[] array;
  private final int from;
  private final int to;

  SliceImpl(T[] array, int from, int to) {
    this.array = array;
    this.from = from;
    this.to = to;
  }

  @Override
  public T get(int index) {
    Objects.checkIndex(index, size());

    return array[from + index];
  }

  @Override
  public int size() {
    return to - from;
  }

  @Override
  public Slice4<T> subSlice(int from, int to) {
    Objects.checkFromToIndex(from, to, size());

    return Slice4.array(array, this.from + from, this.from + to);
  }

  @Override
  public String toString() {
    return Arrays.stream(array, from, to)
            .map(Objects::toString)
            .collect(Collectors.joining(", ", "[", "]"));
  }
}

