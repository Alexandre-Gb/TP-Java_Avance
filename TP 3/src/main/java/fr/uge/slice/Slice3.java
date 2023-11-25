package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Slice3<T> {
  T get(int index);

  int size();

  Slice3<T> subSlice(int form, int to);

  static <T> Slice3<T> array(T[] array) {
    Objects.requireNonNull(array);

    return new Slice3<>() {
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
      public Slice3<T> subSlice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());

        return Slice3.array(array, from, to);
      }

      @Override
      public String toString() {
        return Arrays.stream(array)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    };
  }

  static <T> Slice3<T> array(T[] array, int from, int to) {
    Objects.requireNonNull(array);
    Objects.checkFromToIndex(from, to, array.length);

    var sliceArray = Slice3.array(array);
    return new Slice3<>() {
      @Override
      public T get(int index) {
        Objects.checkIndex(index, size());

        return sliceArray.get(from + index);
      }

      @Override
      public int size() {
        return to - from;
      }

      @Override
      public Slice3<T> subSlice(int arrayFrom, int arrayTo) {
        Objects.checkFromToIndex(arrayFrom, arrayTo, size());

        return Slice3.array(array, from + arrayFrom, from + arrayTo);
      }

      @Override
      public String toString() {
        return Arrays.stream(array, from, to)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
      }
    };
  }
}
