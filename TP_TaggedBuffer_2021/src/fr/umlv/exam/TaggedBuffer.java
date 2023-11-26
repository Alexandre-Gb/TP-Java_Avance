package fr.umlv.exam;

import java.util.*;
import java.util.function.Predicate;

public class TaggedBuffer<T> {
  private final static int DEFAULT_SIZE = 4;
  @SuppressWarnings("unchecked")
  private T[] elements = (T[]) new Object[DEFAULT_SIZE];
  private int size;
  private int nbImportants;
  private final Predicate<? super T> function;

  public TaggedBuffer(Predicate<? super T> function) {
    Objects.requireNonNull(function);
    this.function = function;
  }

  public void add(T element) {
    Objects.requireNonNull(element);
    if (size == elements.length) {
      elements = Arrays.copyOf(elements, size * 2);
    }

    elements[size] = element;
    size++;
    if (function.test(element)) {
      nbImportants++;
    }
  }

  public Optional<T> findFirst(boolean isImportant) {
    for (var element : elements) {
      if (element == null) {
        break;
      }

      if (isImportant && !function.test(element)) {
        continue;
      }

      return Optional.of(element);
    }

    return Optional.empty();
  }

  public void forEach(boolean isImportant, Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);

    for (var element : elements) {
      if (element == null) {
        return;
      }

      if (isImportant && !function.test(element)) {
        continue;
      }

      predicate.test(element);
    }
  }

  public int size(boolean isImportant) {
    if (isImportant) {
      return nbImportants;
    }

    return size;
  }

  public Iterator<T> iterator(boolean isImportant) {
    return new Iterator<>() {
      private final T[] array = Arrays.copyOf(elements, elements.length);
      private int i;

      @Override
      public boolean hasNext() {
        while (i < size) {
          if (array[i] == null) {
            break;
          }

          if (!isImportant || function.test(array[i])) {
            return true;
          }
          i++;
        }
        return false;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        return array[i++];
      }
    };
  }
}
