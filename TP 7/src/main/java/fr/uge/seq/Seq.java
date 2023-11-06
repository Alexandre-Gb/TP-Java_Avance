package fr.uge.seq;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public sealed interface Seq<T> permits SeqImpl {
  static <T> Seq<T> from(List<? extends T> list) {
    Objects.requireNonNull(list);
    return new SeqImpl<>(list, Function.identity());
  }

  @SafeVarargs
  static <T> Seq<T> of(T... elements) {
    Objects.requireNonNull(elements);
    return new SeqImpl<>(Arrays.asList(elements), Function.identity());
  }

  int size();

  T get(int index);

  <U> Seq<U> map(Function<? super T, ? extends U> function);
}
