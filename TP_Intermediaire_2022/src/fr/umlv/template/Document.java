package fr.umlv.template;

import java.util.List;
import java.util.Objects;

public class Document {
  public record Template(List<String> fragments) {
    public Template(List<String> fragments) {
      Objects.requireNonNull(fragments);
      if (fragments.isEmpty()) {
        throw new IllegalArgumentException("fragments is empty");
      }

      this.fragments = List.copyOf(fragments);
    }

    public <T> String interpolate(List<T> values) {
      Objects.requireNonNull(values);
      if (values.size() + 1 != fragments.size()) {
        throw new IllegalArgumentException();
      }

      var stringBuilder = new StringBuilder();
      var fragmentsIterator = fragments.iterator();

      for (T value : values) {
        stringBuilder.append(fragmentsIterator.next()).append(value);
      }

      return stringBuilder.append(fragmentsIterator.next()).toString();
    }

    @Override
    public String toString() {
      return String.join("@", fragments);
    }
  }
}
