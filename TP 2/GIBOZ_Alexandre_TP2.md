# TP2 - Sed, the stream editor
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td02.php)
***

## Exercice 2 - Astra inclinant, sed non obligant

1. **On va dans un premier temps définir une interface Rule qui va représenter une règle. 
    Une règle prend en entrée une ligne (une String) et renvoie soit une nouvelle ligne soit rien (on peut supprimer une ligne).**

**Rappeler comment on indique, en Java, qu'une méthode peut renvoyer quelque chose ou rien ?**

On utilise un Optional<T> pour indiquer qu'une méthode peut renvoyer quelque chose ou rien.

**À l'intérieur de la classe StreamEditor, créer l'interface Rule avec sa méthode rewrite.**

On définit l'interface fonctionnelle au sein de la classe `StreamEditor`:
```java
package fr.uge.sed;

import java.util.Optional;

public final class StreamEditor {
  @FunctionalInterface
  public interface Rule {
    Optional<String> rewrite(String input);
  }
}
```

2. **Avant de créer, dans StreamEditor, la méthode rewrite qui prend deux fichiers, on va créer une méthode rewrite intermédiaire qui travaille sur des flux de caractères. 
    On souhaite écrire une méthode rewrite(reader, writer) qui prend en paramètre un BufferedReader (qui possède une méthode readLine()) ainsi qu'un Writer qui possède la méthode write(String).**

**Comment doit-on gérer l'IOException ?**

On peut la gérer avec un "throws IOException".

**Écrire la classe StreamEditor avec son constructeur qui se contente de stocker la règle prise en paramètre.**

On ajoute le constructeur de la classe `StreamEditor`:
```java
package fr.uge.sed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.Optional;

public final class StreamEditor {
  private final Rule rule;

  public StreamEditor(Rule rule) {
    Objects.requireNonNull(rule);
    this.rule = rule;
  }

  @FunctionalInterface
  public interface Rule {
    Optional<String> rewrite(String input);
  }
}
```


**Ajouter la méthode rewrite(reader, writer) qui, pour chaque ligne du reader, applique la règle puis écrit le résultat, s'il existe, dans le writer.**

On ajoute la méthode `rewrite`:
```java
  public void rewrite(BufferedReader reader, Writer writer) throws IOException {
    Objects.requireNonNull(reader);
    Objects.requireNonNull(writer);

    String line;
    while ((line = reader.readLine()) != null) {
      var rewrite = rule.rewrite(line);

      if (rewrite.isPresent()) {
        writer.write(rule.rewrite(line).orElse("")+"\n");
        
        // writer.write(rewrite.orElseThrow());
        // writer.write('\n');
      }
    }
  }
```

3. **On souhaite créer la méthode rewrite(input, output) qui prend deux fichiers (pour être exact, deux chemins vers les fichiers) en paramètre et applique la règle sur les lignes du fichier input et écrit le résultat dans le fichier output.**

**Comment faire en sorte que les fichiers ouverts soit correctement fermés ?**
**Comment doit-on gérer l'IOException ?**

On peut utiliser un try-with-resources pour s'assurer que l'erreur est bien gérée et que les fichiers sont bien fermés.

**Écrire la méthode rewrite(input, output).**

On ajoute la méthode `rewrite`:
```java
  public void rewrite(Path input, Path output) throws IOException {
    Objects.requireNonNull(input);
    Objects.requireNonNull(output);

    try (var reader = Files.newBufferedReader(input); var writer = Files.newBufferedWriter(output)) {
      rewrite(reader, writer);
    }
  }
```

4. **On va écrire la méthode createRules qui prend en paramètre une chaîne de caractères et qui construit la règle correspondante.
   Pour l'instant, on va considérer qu'une règle est spécifiée par un seul caractère :
   "s" veut dire strip (supprimer les espaces),
   "u" veut dire uppercase (mettre en majuscules),
   "l" veut dire lowercase (mettre en minuscules) et
   "d" veut dire delete (supprimer).**

**Écrire la méthode createRules(description).**

On ajoute la méthode `createRules`:
```java
  public static Rule createRules(String rules) {
    Objects.requireNonNull(rules);

    return switch (rules) {
      case "s" -> s -> Optional.of(s.replaceAll("\\s+", ""));
      case "u" -> s -> Optional.of(s.toUpperCase(Locale.ROOT));
      case "l" -> s -> Optional.of(s.toLowerCase(Locale.ROOT));
      case "d" -> s -> Optional.empty();
      default -> throw new IllegalArgumentException("Unknown rule: " + rules);
    };
  }
```

5. **On veut pouvoir composer les règles, par exemple, on veut que "sl" strip les espaces puis mette le résultat en minuscules. 
    Pour cela, dans un premier temps, on va écrire une méthode statique andThen dans Rule, qui prend en paramètre deux règles et renvoie une nouvelle règle qui applique la première règle puis applique la seconde règle sur le résultat de la première.**

Fonction statique `andThen` dans l'interface `Rule`:
```java
  static Rule andThen(Rule first, Rule second) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(second);

    return s -> first.rewrite(s).flatMap(second::rewrite);
  }
```

**Puis modifier le code de createRules pour que les règles soient appliquées les une après les autres.**

On commence par renommer la méthode `createRules` en `createRule`. Cette méthode prendre à présent un simple charactère et sera par conséquent privée:
```java
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
```

On met à présent en place une méthode `createRules` qui applique correctement les règles avec `andThen`:
```java
  public static Rule createRules(String rules) {
    Objects.requireNonNull(rules);

    Rule rule = Optional::of;
    for (int i = 0; i < rules.length(); i++) {
      rule = Rule.andThen(rule, createRule(rules.charAt(i)));
    }

    return rule;
  }
```

6. **Écrire la méthode d'instance andThen dans Rule et modifier createRules pour utiliser cette nouvelle méthode.**

On ajoute la méthode par défaut `andThen` à l'interface `Rule`:
```java
    default Rule andThen(Rule second) {
      Objects.requireNonNull(second);
      return andThen(this, second);
    }
```

7. **Quelle interface fonctionnelle correspond à une fonction qui prend une String et renvoie un boolean ?**

L'interface fonctionnelle correspondant à ce scénario est un Predicate<String>.

**Écrire la méthode statique guard(function, rule) et vérifier que les 4 premiers tests correspondant à "Q7" passent.**

On ajoute la méthode `guard(function, rule)`:
```java
    static Rule guard(Predicate<String> predicate, Rule rule) {
      Objects.requireNonNull(predicate);
      Objects.requireNonNull(rule);

      return s -> predicate.test(s) ? rule.rewrite(s) : Optional.of(s);
    }
```
