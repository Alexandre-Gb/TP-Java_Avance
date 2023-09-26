package fr.uge.sed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public final class StreamEditor {
  private final Rule rule;

  public StreamEditor(Rule rule) {
    Objects.requireNonNull(rule);
    this.rule = rule;
  }

  public void rewrite(BufferedReader reader, Writer writer) throws IOException {
    Objects.requireNonNull(reader);
    Objects.requireNonNull(writer);

    String line;
    while ((line = reader.readLine()) != null) {
      var rewrite = rule.rewrite(line);

      if (rewrite.isPresent()) {
        // writer.write(rule.rewrite(line).orElse("")+"\n");

        // Redondant puisque l'on vérifie que l'optional n'est pas vide, mais la convention veut que l'on utilise au lieu de "x.get()"
        writer.write(rewrite.orElseThrow());
        writer.write('\n');
      }
    }
  }

  public void rewrite(Path input, Path output) throws IOException {
    Objects.requireNonNull(input);
    Objects.requireNonNull(output);

    try (var reader = Files.newBufferedReader(input); var writer = Files.newBufferedWriter(output)) {
      rewrite(reader, writer);
    }
  }

  private static Rule createRule(char rule) {
    return switch (rule) {
      case 's' -> s -> Optional.of(s.replaceAll("\\s+", ""));
      // case "u" -> s -> Optional.of(s.toUpperCase());
      // case "l" -> s -> Optional.of(s.toLowerCase());
      // Locale.ROOT to avoid turkish letters etc
      case 'u' -> s -> Optional.of(s.toUpperCase(Locale.ROOT));
      case 'l' -> s -> Optional.of(s.toLowerCase(Locale.ROOT));
      case 'd' -> s -> Optional.empty();
      default -> throw new IllegalArgumentException("Unknown rule: " + rule);
    };
  }

  public static Rule createRules(String rules) {
    Objects.requireNonNull(rules);

    Rule rule = Optional::of;
    for (int i = 0; i < rules.length(); i++) {
      rule = Rule.andThen(rule, createRule(rules.charAt(i)));
    }

    return rule;
  }


  @FunctionalInterface
  public interface Rule {
    Optional<String> rewrite(String input);

    static Rule andThen(Rule first, Rule second) {
      Objects.requireNonNull(first);
      Objects.requireNonNull(second);

      return s -> first.rewrite(s).flatMap(second::rewrite);
    }

    default Rule andThen(Rule second) {
      Objects.requireNonNull(second);
      return andThen(this, second);
    }

    static Rule guard(Predicate<String> predicate, Rule rule) {
      Objects.requireNonNull(predicate);
      Objects.requireNonNull(rule);

      return s -> predicate.test(s) ? rule.rewrite(s) : Optional.of(s);
    }
  }
}
