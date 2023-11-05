package fr.uge.seq;

import java.util.List;
import java.util.Objects;

public sealed interface Seq<T> permits SeqImpl {
  static <T> Seq<T> from(List<? extends T> list) {
    Objects.requireNonNull(list);
    return new SeqImpl<>(List.copyOf(list));
  }

  int size();

  T get(int index);
}
