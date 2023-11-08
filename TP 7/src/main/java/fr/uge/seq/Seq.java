package fr.uge.seq;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Seq<T> extends Iterable<T> {
  static <T> Seq<T> from(List<? extends T> list) {
    Objects.requireNonNull(list);
    return new SeqImpl<>(list, Function.identity());
  }

  @SafeVarargs
  static <T> Seq<T> of(T... elements) {
    Objects.requireNonNull(elements);
    return new SeqImpl<>(Arrays.asList(elements), Function.identity());
  }

  default Iterator<T> iterator() {
    return new Iterator<>() {
      private int i;
      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        return get(i++);
      }
    };
  }

  int size();

  T get(int index);

  <U> Seq<U> map(Function<? super T, ? extends U> function);

  Optional<T> findFirst();

  Stream<T> stream();
}
