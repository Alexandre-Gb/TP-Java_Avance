package fr.uge.query;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface Query<T> permits Query.QueryImpl {
  static <T, U> Query<U> fromList(List<? extends T> list, Function<? super T,? extends Optional<? extends U>> mapper) {
    Objects.requireNonNull(list);
    Objects.requireNonNull(mapper);

    return new QueryImpl<>(Collections.unmodifiableList(list), mapper); // UnmodifiableList for initial null values
  }

  static <T> Query<T> fromIterable(Iterable<? extends T> iterable) {
    Objects.requireNonNull(iterable);
    return new QueryImpl<>(iterable, Optional::of);
  }

  List<T> toList();

  List<T> toLazyList();

  Stream<T> toStream();

  Query<T> filter(Predicate<? super T> predicate);

  <U> Query<U> map(Function<? super T,? extends U> function);

  final class QueryImpl<T, U> implements Query<U> {
    private final Iterable<? extends T> elements;
    private final Function<? super T,? extends Optional<? extends U>> mapper;

    QueryImpl(Iterable<? extends T> elements, Function<? super T,? extends Optional<? extends U>> mapper) {
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
        private final Iterator<? extends T> iterator = elements.iterator();
        private final List<U> cache = new ArrayList<>();
        @Override
        public U get(int index) {
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
      return StreamSupport.stream(elements.spliterator(), false)
              .flatMap(e -> mapper.apply(e).stream());
    }

    @Override
    public Query<U> filter(Predicate<? super U> predicate) {
      Objects.requireNonNull(predicate);
      return new QueryImpl<>(elements, mapper.andThen(o -> o.filter(predicate)));
    }

    @Override
    public <V> Query<V> map(Function<? super U,? extends V> function) {
      Objects.requireNonNull(function);
      return new QueryImpl<>(elements, this.mapper.andThen(o -> o.map(function)));
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
