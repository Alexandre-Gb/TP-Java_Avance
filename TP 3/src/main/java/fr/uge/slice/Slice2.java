package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice2<T> permits Slice2.ArraySlice, Slice2.ArraySlice.SubArraySlice {
  static <T> Slice2<T> array(T[] array) {
    Objects.requireNonNull(array);

    return new ArraySlice<>(array);
  }

  static <T> Slice2<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    return new ArraySlice<>(array).new SubArraySlice(from, to);
  }

  T get(int index);

  int size();

  Slice2<T> subSlice(int from, int to);


  final class ArraySlice<T> implements Slice2<T> {
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
    public Slice2<T> subSlice(int from, int to) {
      Objects.checkFromToIndex(from, to, size());

      return new SubArraySlice(from, to);
    }

    @Override
    public String toString() {
      return Arrays.stream(array)
              .map(Objects::toString)
              .collect(Collectors.joining(", ", "[", "]"));
    }

    public final class SubArraySlice implements Slice2<T> {
      private final int from;
      private final int to;

      private SubArraySlice(int from, int to) {
        Objects.requireNonNull(array);
        Objects.checkFromToIndex(from, to, array.length);

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
      public Slice2<T> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());

        return new SubArraySlice(this.from + from, this.from + to);
      }

      @Override
      public String toString() {
        return Arrays.stream(array, from, to)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    }
  }
}
