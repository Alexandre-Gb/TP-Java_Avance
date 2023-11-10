package fr.uge.query;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Query<T> permits Query.QueryImpl {
  static <T, U> Query<U> fromList(List<T> list, Function<? super T, Optional<? extends U>> mapper) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(mapper);

    return new QueryImpl<>(Collections.unmodifiableList(list), mapper); // UnmodifiableList for initial null values
  }

  List<T> toList();

  Stream<T> toStream();

  final class QueryImpl<T, U> implements Query<U> {
    private final List<T> elements;
    private final Function<? super T, Optional<? extends U>> mapper;

    QueryImpl(List<T> elements, Function<? super T, Optional<? extends U>> mapper) {
      this.elements = elements;
      this.mapper = mapper;
    }

    @Override
    public List<U> toList() {
      var list = new ArrayList<U>();
      elements.forEach(e -> mapper.apply(e).ifPresent(list::add));

      return List.copyOf(list);
    }

    @Override
    public Stream<U> toStream() {
      return elements.stream()
              .flatMap(e -> mapper.apply(e).stream());
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
