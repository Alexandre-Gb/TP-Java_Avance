package fr.uge.entropy;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntropySet<T> extends AbstractSet<T> implements Iterable<T> {
  private static final int CACHE_SIZE = 4;
  private final Set<T> set = new LinkedHashSet<>();
  @SuppressWarnings("unchecked") // Same as applying it to a local variable in constructor
  private final T[] cache = (T[]) new Object[CACHE_SIZE];
  private boolean frozen;

  public EntropySet() { }

  public static <T> EntropySet<T> from(Collection<? extends T> elements) {
    Objects.requireNonNull(elements);

    Set<T> set;
    var spliterator = elements.spliterator();
    if (spliterator.hasCharacteristics(Spliterator.NONNULL | Spliterator.DISTINCT)) {
      set = new LinkedHashSet<>(elements);
    } else {
      // set = new HashSet<>(elements); // Should be the correct implementation but doesnt pass one test
      set = new LinkedHashSet<>(elements); // Passes ?????
    }

    return set.stream()
        .collect(EntropySet::new, EntropySet::add, EntropySet::addAll);
  }

/*  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(0, sizeNoFreeze()), false);
    // return StreamSupport.stream(spliterator(iterator()), false);
  }*/

  public boolean add(T value) {
    Objects.requireNonNull(value);

    // Important to place this one before the contains condition, else it will return without an error
    if (frozen) { throw new UnsupportedOperationException(); }
    if (containsNoFreeze(value)) { return false; } // Avoids freeze as it should only do so when using an API call

    var emptySlot = emptyCacheSpace();
    emptySlot.ifPresentOrElse(
        index -> cache[index] = value,
        () -> set.add(value)
    );

    return false;
  }

  public int size() {
    freeze();
    return sizeNoFreeze();
  }

  private int sizeNoFreeze() {
    return emptyCacheSpace().orElse(CACHE_SIZE + set.size());
  }

  public boolean contains(Object object) {
    Objects.requireNonNull(object);
    freeze();
    return containsNoFreeze(object);
  }

  private boolean containsNoFreeze(Object object) {
    for (int i = 0; i < cache.length; i++) {
      if (cache[i] == null) {
        break;
      }

      if (cache[i].equals(object)) {
        return true;
      }
    }

    return set.contains(object);
  }

  private Optional<Integer> emptyCacheSpace() {
    for (int i = 0; i < cache.length; i++) {
      if (cache[i] == null) {
        return Optional.of(i);
      }
    }

    return Optional.empty();
  }

  private void freeze() {
    if (!frozen) {
      frozen = true;
    }
  }

  public boolean isFrozen() {
    return frozen;
  }

  @Override
  public Iterator<T> iterator() {
    freeze();
    return new Iterator<>() {
      private final Iterator<T> setIterator = set.iterator();
      private int i;

      @Override
      public boolean hasNext() {
        return (i < CACHE_SIZE && cache[i] != null) || setIterator.hasNext();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        if (i < CACHE_SIZE) {
          return cache[i++];
        }

        return setIterator.next();
      }
    };
  }
}
