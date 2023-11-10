package fr.uge.query;

import java.util.*;
import java.util.function.Function;

public sealed interface Query<T> permits Query.QueryImpl {
  static <T, U> Query<U> fromList(List<T> list, Function<? super T, Optional<? extends U>> mapper) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(mapper);

    return new QueryImpl<>(Collections.unmodifiableList(list), mapper); // UnmodifiableList for initial null values
  }

  final class QueryImpl<T, U> implements Query<U> {
    private final List<T> elements;
    private final Function<? super T, Optional<? extends U>> mapper;

    QueryImpl(List<T> elements, Function<? super T, Optional<? extends U>> mapper) {
      this.elements = elements;
      this.mapper = mapper;
    }

    @Override
    public String toString() {
      var stringJoiner = new StringJoiner(" |> ");
      elements.forEach(e -> mapper.apply(e)
              .ifPresent(u -> stringJoiner.add(u.toString())));

      return stringJoiner.toString();
    }
  }
}
