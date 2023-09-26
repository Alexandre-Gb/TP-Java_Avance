package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice<T> permits Slice.ArraySlice, Slice.SubArraySlice {
  static <T> Slice<T> array(T[] array) {
    Objects.requireNonNull(array);
    return new ArraySlice<>(array);
  }

  static <T> Slice<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    return new SubArraySlice<>(array, from, to);
  }

  T get(int index);

  int size();

  Slice<T> subSlice(int from, int to);


  final class ArraySlice<T> implements Slice<T> {
    private final T[] array;

    private ArraySlice(T[] array) {
      this.array = array;
    }

    @Override
    public T get(int index) {
      Objects.checkIndex(index, size());
      return array[index];
    }

    @Override
    public int size() {
      return array.length;
    }

    @Override
    public Slice<T> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, size());
      return new SubArraySlice<>(array, from, to);
    }

    @Override
    public String toString() {
      return Arrays.stream(array)
      .map(Objects::toString)
      .collect(Collectors.joining(", ", "[", "]"));
    }
  }

  final class SubArraySlice<T> implements Slice<T> {
    private final T[] array;
    private final int from;
    private final int to;

    private SubArraySlice(T[] array, int from, int to) {
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
    public Slice<T> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, size());
      return new SubArraySlice<>(array, this.from + from, this.from + to);
    }

    @Override
    public String toString() {
      return Arrays.stream(array, from, to)
      .map(Objects::toString)
      .collect(Collectors.joining(", ", "[", "]"));
    }
  }
}


