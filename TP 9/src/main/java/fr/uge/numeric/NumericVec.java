package fr.uge.numeric;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NumericVec<T> extends AbstractList<T> {
  private long[] values;
  private final LongFunction<T> from;
  private final ToLongFunction<T> into;
  private int size;

  private NumericVec(long[] values, LongFunction<T> from, ToLongFunction<T> into) {
    this.values = values;
    this.size = values.length;
    this.from = from;
    this.into = into;
  }

  public static NumericVec<Long> longs(long... values) {
    Objects.requireNonNull(values);

    return new NumericVec<>(Arrays.copyOf(values, values.length), e -> e, e -> e);
  }

  public static NumericVec<Integer> ints(int... values) {
    Objects.requireNonNull(values);

    var array = Arrays.stream(values).mapToLong(e -> e).toArray();
    return new NumericVec<>(array, e -> (int) e, Integer::longValue);
  }

  public static NumericVec<Double> doubles(double... values) {
    Objects.requireNonNull(values);

    var array = Arrays.stream(values).mapToLong(Double::doubleToRawLongBits).toArray();
    return new NumericVec<>(array, Double::longBitsToDouble, Double::doubleToRawLongBits);
  }

  public static <T> Collector<T, ?, NumericVec<T>> toNumericVec(Supplier<NumericVec<T>> factory) {
    Objects.requireNonNull(factory);

    return Collectors.toCollection(factory);
  }

  public boolean add(T value) {
    Objects.requireNonNull(value);

    if (size == 0) {
      values = new long[1];
    } else if (size >= values.length) {
      values = Arrays.copyOf(values, size * 2);
    }

    values[size] = into.applyAsLong(value);
    size++;
    return true;
  }

  public boolean addAll(Collection<? extends T> collection) {
    Objects.requireNonNull(collection);
    if (collection.isEmpty()) {
      return false;
    }

    if (collection instanceof NumericVec<?> numericVec) {
      if (values.length < size + numericVec.size) {
        values = Arrays.copyOf(values, size + numericVec.size);
      }
      System.arraycopy(numericVec.values, 0, values, size, numericVec.size);
      size += numericVec.size;
    } else {
      collection.forEach(this::add);
    }

    return true;
  }

  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(0, size), false);
  }

  @Override
  public Spliterator<T> spliterator() {
    return spliterator(0, size);
  }

  private Spliterator<T> spliterator(int start, int end) {
    return new Spliterator<>() {
      private int i = start;

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        if (i < end) {
          action.accept(from.apply(values[i++]));
          return true;
        }
        return false;
      }

      @Override
      public Spliterator<T> trySplit() {
        if (size < 1024) {
          return null;
        }

        var middle = (i + end) >>> 1;
        if (middle == i) {
          return null;
        }

        var split = spliterator(i, middle);
        i = middle;
        return split;
      }

      @Override
      public long estimateSize() {
        return end - i;
      }

      @Override
      public int characteristics() {
        return NONNULL | ORDERED | IMMUTABLE | SIZED;
      }
    };
  }

  public T get(int index) {
    Objects.checkIndex(index, size());
    return from.apply(values[index]);
  }

  public int size() {
    return size;
  }
}
