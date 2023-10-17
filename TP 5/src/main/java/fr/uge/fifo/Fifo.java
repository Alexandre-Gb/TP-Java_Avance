package fr.uge.fifo;

import java.util.*;

public class Fifo<E> implements Iterable<E> {
  private static final int DEFAULT_CAPACITY = 16;
  private E[] fifo;
  private int capacity;
  private int head;
  private int tail;
  private int size;

  @SuppressWarnings("unchecked")
  public Fifo(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException();
    }

    this.capacity = capacity;
    fifo = (E[]) new Object[capacity];
  }

  public Fifo() {
    this(DEFAULT_CAPACITY);
  }

  public void offer(E value) {
    Objects.requireNonNull(value);

    if (size == capacity) {
      resize();
    }

    fifo[tail] = value;
    tail = reindex(tail + 1);
    size++;
  }

  public E peek() {
    return fifo[head];
  }

  public E poll() {
    if (fifo[head] == null) {
      return null;
    }

    var value = fifo[head];
    fifo[head] = null;
    size--;
    head = reindex(head + 1);
    return value;
  }

  private int reindex(int index) {
    return index % capacity;
  }

  public void resize() {
    var newcapacity = capacity * 2;

    @SuppressWarnings("unchecked")
    var newFifo = (E[]) new Object[newcapacity];

    if (head < tail) {
      System.arraycopy(fifo, head, newFifo, 0, size);
    } else {
      System.arraycopy(fifo, head, newFifo, 0, fifo.length - head);
      System.arraycopy(fifo, 0, newFifo, fifo.length - head, tail + 1);
    }

    fifo = newFifo;
    head = 0;
    tail = capacity;
    capacity = newcapacity;
  }

  public int size() {
    return size;
  }

  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int index = head;
      private int count = 0;

      @Override
      public boolean hasNext() {
        return count < size;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        var value = fifo[index];
        index = reindex(index + 1);
        count++;
        return value;
      }
    };
  }

  @Override
  public String toString() {
    var stringJoiner = new StringJoiner(", ", "[", "]");
    var index = head;
    for (var i = 0; i < size; i++) {
      stringJoiner.add(fifo[index].toString());
      index = reindex(index + 1);
    }

    return stringJoiner.toString();
  }
}
