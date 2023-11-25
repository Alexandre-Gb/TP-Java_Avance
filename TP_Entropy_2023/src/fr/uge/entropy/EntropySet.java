package fr.uge.entropy;

import java.util.*;

public class EntropySet<T> implements Iterable<T> {
  private static final int CACHE_SIZE = 4;
  private final HashSet<T> set = new HashSet<>();
  private final T[] cache;
  private boolean frozen;

  public EntropySet() {
    @SuppressWarnings("unchecked")
    var cache = (T[]) new Object[CACHE_SIZE];
    this.cache = cache;
  }

  public void add(T value) {
    Objects.requireNonNull(value);
    if (frozen) { throw new UnsupportedOperationException(); }
    if (containsNoFreeze(value)) { return; } // Avoid freeze as it should only do so when using an API call

    var emptySlot = emptyCacheSpace();
    emptySlot.ifPresentOrElse(
        index -> {
          cache[index] = value;
        },
        () -> set.add(value)
    );
  }

  public int size() {
    freeze();
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
        return i < CACHE_SIZE || setIterator.hasNext();
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
