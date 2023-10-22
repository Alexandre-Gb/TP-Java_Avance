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

    @Override
    public String toString() {
      return String.join("@", fragments);
    }
  }
}
