package fr.uge.query;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Query<T> permits Query.QueryImpl {
  static <T, U> Query<U> fromList(List<? extends T> list, Function<? super T,? extends Optional<? extends U>> mapper) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(mapper);

    return new QueryImpl<>(Collections.unmodifiableList(list), mapper); // UnmodifiableList for initial null values
  }

  List<T> toList();

  List<T> toLazyList();

  Stream<T> toStream();

  final class QueryImpl<T, U> implements Query<U> {
    private final List<T> elements;
    private final Function<? super T,? extends Optional<? extends U>> mapper;

    QueryImpl(List<T> elements, Function<? super T,? extends Optional<? extends U>> mapper) {
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
    public List<U> toLazyList() {
      return new AbstractList<>() {
        private final Iterator<T> iterator = elements.iterator();
        private final List<U> cache = new ArrayList<>();
        @Override
        public U get(int index) {
          Objects.checkIndex(index, elements.size());
          if (index < cache.size()) {
            return cache.get(index);
          }

          while (iterator.hasNext()) {
            var optional = mapper.apply(iterator.next());
            if (optional.isPresent()) {
              cache.add(optional.get());
              if (index == cache.size() - 1) {
                return optional.get();
              }
            }
          }

          throw new ArrayIndexOutOfBoundsException();
        }

        @Override
        public int size() {
          while (iterator.hasNext()) {
            var optional = mapper.apply(iterator.next());
            optional.ifPresent(cache::add);
          }

          return cache.size();
        }
      };
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
