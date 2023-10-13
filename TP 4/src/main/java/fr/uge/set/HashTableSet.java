package fr.uge.set;

import java.util.Objects;
import java.util.function.Consumer;

public final class HashTableSet<E> {
  private final static int INIT_SIZE = 16;
  @SuppressWarnings("unchecked")
  private Entry<E>[] entries = new Entry[INIT_SIZE];
  private int size; // 0 by default

  public int size() {
    return size;
  }

  public void add(E key) {
    Objects.requireNonNull(key);
    int index = getIndex(key);

    if (!containsAtIndex(key, index)) {
      entries[index] = new Entry<>(key, entries[index]);
      size++;

      if (size >= entries.length / 2) {
        enlargeEntries();
      }
    }
  }

  public void forEach(Consumer<? super E> consumer) {
    Objects.requireNonNull(consumer);
    for (var entry : entries) {
      for (var e = entry; e != null; e = e.next) {
        consumer.accept(e.key);
      }
    }
  }

  public boolean contains(E key) {
    Objects.requireNonNull(key);
    return containsAtIndex(key, getIndex(key));
  }

  private boolean containsAtIndex(E key, int index) {
    Objects.requireNonNull(key);
    for (var entry = entries[index]; entry != null; entry = entry.next) {
      if (entry.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  private int getIndex(E key) {
    Objects.requireNonNull(key);
    return key.hashCode() & entries.length - 1;
  }

  @SuppressWarnings("unchecked")
  private void enlargeEntries() {
    var newEntries = new Entry[entries.length * 2];

    forEach(key -> {
      int index = getIndex(key);
      newEntries[index] = new Entry<E>(key, newEntries[index]);
    });

//    for (var entry : entries) {
//      for (var e = entry; e != null; e = e.next) {
//        int index = getIndex(e.key);
//        newEntries[index] = new Entry(e.key, newEntries[index]);
//      }
//    }

    entries = newEntries;
  }

  public void addAll(HashTableSet<? extends E> set) {
    Objects.requireNonNull(set);
    set.forEach(this::add);
  }

  public boolean equals(HashTableSet<? super E> set) {
    Objects.requireNonNull(set);
    if (set.size() != size) {
      return false;
    }

    for (var entry : entries) {
      for (var e = entry; e != null; e = e.next) {
        if (!set.contains(e.key)) {
          return false;
        }
      }
    }

    return true;
  }

  private record Entry<E>(E key, Entry<E> next) { }
}



/*
package fr.uge.set;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class HashTableSet {
  private final static int INIT_SIZE = 16;
  private Entry[] entries = new Entry[INIT_SIZE];
  private int size; // 0 by default

  public int size() {
    return size;
  }

  public void add(Object key) {
    Objects.requireNonNull(key);
    int index = getIndex(key);

    if (!containsAtIndex(key, index)) {
      entries[index] = new Entry(key, entries[index]);
      size++;

      if (size >= entries.length / 2) {
        enlargeEntries();
      }
    }
  }

  public void forEach(Consumer<Object> consumer) {
    Objects.requireNonNull(consumer);
    for (var entry : entries) {
      for (var e = entry; e != null; e = e.next) {
        consumer.accept(e.key);
      }
    }
  }

  public boolean contains(Object key) {
    Objects.requireNonNull(key);
    return containsAtIndex(key, getIndex(key));
  }

  private boolean containsAtIndex(Object key, int index) {
    Objects.requireNonNull(key);
    for (var entry = entries[index]; entry != null; entry = entry.next) {
      if (entry.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  private int getIndex(Object key) {
    Objects.requireNonNull(key);
    return key.hashCode() & entries.length - 1;
  }

  private void enlargeEntries() {
    var newEntries = new Entry[entries.length * 2];

    forEach(key -> {
      int index = getIndex(key);
      newEntries[index] = new Entry(key, newEntries[index]);
    });

//    for (var entry : entries) {
//      for (var e = entry; e != null; e = e.next) {
//        int index = getIndex(e.key);
//        newEntries[index] = new Entry(e.key, newEntries[index]);
//      }
//    }

    entries = newEntries;
  }

  private record Entry(Object key, Entry next) { }
}
*/
