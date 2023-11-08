package fr.uge.seq;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    return Optional.of(get(0)); // Will map automatically
  }

  @Override
  public <E> Seq<E> map(Function<? super T, ? extends E> mapper) {
    Objects.requireNonNull(mapper);

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
