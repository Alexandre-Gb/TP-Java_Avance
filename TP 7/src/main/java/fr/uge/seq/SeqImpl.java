package fr.uge.seq;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class SeqImpl<T, U> implements Seq<T> {
  private final List<U> elements;
  private final Function<? super U, ? extends T> mapper;

  SeqImpl(List<? extends U> elements, Function<? super U, ? extends T> mapper) {
    this.elements = List.copyOf(elements);
    this.mapper = mapper;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public T get(int index) {
    Objects.checkIndex(index, size());
    return mapper.apply(elements.get(index));
  }

  @Override
  public Optional<T> findFirst() {
    if (size() == 0) {
      return Optional.empty();
    }

    return Optional.of(get(0)); // Will map automatically via get
  }

  @Override
  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(0, size()), false);
  }

  private Spliterator<T> spliterator(int start, int end) {
    return new Spliterator<>() {
      private int i = start;

      @Override
      public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (i < end) {
          action.accept(get(i++));
          return true;
        }

        return false;
      }

      @Override
      public Spliterator<T> trySplit() {
        var middle = (i + end) >>> 1;
        if (middle == i) {
          return null;
        }

        var spliterator = spliterator(i, middle);
        i = middle;
        return spliterator;
      }

      @Override
      public long estimateSize() {
        return end - i;
      }

      @Override
      public int characteristics() {
        return IMMUTABLE | ORDERED | SIZED;
      }
    };
  }

  @Override
  public <E> Seq<E> map(Function<? super T, ? extends E> mapper) {
    Objects.requireNonNull(mapper);

    // return new SeqImpl<E, U>(elements, this.mapper.andThen(mapper));
    return new SeqImpl<>(elements, this.mapper.andThen(mapper));
  }

  @Override
  public String toString() {
    return elements.stream()
            .map(mapper)
            .map(Objects::toString)
            .collect(Collectors.joining(", ", "<", ">"));
  }
}
