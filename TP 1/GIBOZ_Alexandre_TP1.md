# TP1 - Rappel de notions de programmation objet
## GIBOZ Alexandre, INFO2 2023-2025
***

[Énoncé](https://www-igm.univ-mlv.fr/ens/IR/IR2/2023-2024/JavaAvance/td01.php)
***

## Exercice 1 - Maven

Fichier `pom.xml` à la racine du projet:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>fr.uge.ymca</groupId>
    <artifactId>TP1</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>20</release>
                    <compilerArgs>
                        <compilerArg>--enable-preview</compilerArg>
                    </compilerArgs>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <argLine>--enable-preview</argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

<br>

## Exercice 2 - YMCA

1. **Écrire le code de VillagePeople de telle façon que l'on puisse créer des VillagePeople avec leur nom et leur sorte. 
    Vérifiez que les tests Q1 passent**

Fichier `VillagePeople.java`:
```java
package fr.uge.ymca;

import java.util.Objects;

public record VillagePeople(String name, Kind kind) {
  public VillagePeople {
    Objects.requireNonNull(name);
    Objects.requireNonNull(kind);
  }

  @Override
  public String toString() {
    return name + " (" + kind + ")";
  }
}
```

Fichier `Kind.java`:
```java
package fr.uge.ymca;

public enum Kind {
  COP, NATIVE, GI, BIKER, CONSTRUCTION, COWBOY, ADMIRAL, ATHLETE, GIGOLO, SAILOR
}
```

2. **On veut maintenant introduire une maison House qui va contenir des VillagePeople. Une maison possède une méthode add qui permet d'ajouter un VillagePeople dans la maison (Il est possible d'ajouter plusieurs fois le même).**

Fichier `House.java`:
```java
package fr.uge.ymca;

import java.util.ArrayList;
import java.util.Objects;

public class House {
  private final ArrayList<VillagePeople> people;

  public House() {
    this.people = new ArrayList<>();
  }

  public void add(VillagePeople person) {
    Objects.requireNonNull(person);
    this.people.add(person);
  }
}
```

3. **Dans un premier temps, réécrire le code de l'affichage (commenter l'ancien) pour utiliser un Stream sans se préoccuper du tri et vérifier que les tests de la question précédente passent toujours. 
    Puis demander au Stream de se trier et vérifier que les tests marqués "Q3" passent.**

Stream pour affichage non trié:
```java
@Override
public String toString() {
  if (people.isEmpty()) {
    return "Empty House";
  }
  
  return "House with ".concat(
          people.stream()
        .map(VillagePeople::name)
        .collect(Collectors.joining(", ")));
}
```

Stream pour affichage trié:
```java
@Override
public String toString() {
  if (people.isEmpty()) {
    return "Empty House";
  }
  
  return "House with ".concat(
          people.stream()
        .map(VillagePeople::name)
        .sorted()
        .collect(Collectors.joining(", ")));
}
```

4. **On souhaite donc ajouter un type Minion (qui possède juste un nom name) et changer le code de House pour permettre d'ajouter des VillagePeople ou des Minion. 
    Un Minion affiche son nom suivi entre parenthèse du texte "MINION".**

Les minions et les villagepeople seront mis en commun via une interface `Inmate`:
```java
package fr.uge.ymca;

public interface Inmate {
  String name();
}
```

l'interface devra, plus tard, être scellée afin de n'autoriser l'implémentation que des `Minion` et `VillagePeople`.

Fichier `Minion.java`:
```java
package fr.uge.ymca;

import java.util.Objects;

public record Minion(String name) {
  public Minion {
    Objects.requireNonNull(name);
  }

  @Override
  public String toString() {
    return name + " (MINION)";
  }
}
```

Fichier `VillagePeople.java`:
```java
package fr.uge.ymca;

import java.util.Objects;

public record VillagePeople(String name, Kind kind) {
  public VillagePeople {
    Objects.requireNonNull(name);
    Objects.requireNonNull(kind);
  }

  @Override
  public String toString() {
    return name + " (" + kind + ")";
  }
}
```

Fichier `House.java`:
```java
package fr.uge.ymca;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public final class House {
  private final ArrayList<Inmate> people;

  public House() {
    people = new ArrayList<>();
  }

  public void add(Inmate person) {
    Objects.requireNonNull(person);
    people.add(person);
  }

  @Override
  public String toString() {
    if (people.isEmpty()) {
      return "Empty House";
    }

    return "House with ".concat(
            people.stream()
                    .map(Inmate::name)
                    .sorted()
                    .collect(Collectors.joining(", "))
    );
  }
}
```

5. **Écrire la méthode averagePrice en utilisant le polymorphisme (late dispatch) pour trouver le prix de chaque VillagePeople ou Minion.**

On ajoute une méthode abstraite `price()` dans l'interface `Inmate`, cette dernière allant être implémentée par chaque classe:
```java
package fr.uge.ymca;

public interface Inmate {
  String name();
  int price();
}
```

Fichier `Minion.java`:
```java
@Override
public int price() {
  return 1;
}
```

Fichier `VillagePeople.java`:
```java
@Override
public int price() {
  return 100;
}
```

On peut alors implémenter la méthode `averagePrice()` qui calcule le coût moyen, ou renvoie NaN si la maison est vide:
```java
public double averagePrice() {
  return people.stream()
        .mapToInt(Inmate::price)
        .average()
        .orElse(Double.NaN);
}
```

6. **Modifier votre code pour introduire une méthode privée qui prend en paramètre un VillagePeople ou un Minion et renvoie son prix par nuit puis utilisez cette méthode pour calculer le prix moyen par nuit d'une maison.**

On commente les méthodes `price()` des classes `Minion` et `VillagePeople` et on ajoute une méthode privée `price(Inmate)` dans la classe `House`:
```java
private int price(Inmate person) {
  return switch (person) {
    case Minion minion -> 1;
    case VillagePeople villagePeople -> 100;
  };
}

public double averagePrice() {
  return people.stream()
        .mapToInt(this::price)
        .average()
        .orElse(Double.NaN);
}
```

7. **L'implantation précédente pose problème : il est possible d'ajouter une autre personne qu'un VillagePeople ou un Minion, mais celle-ci ne sera pas prise en compte par le pattern matching. 
    Pour cela, on va interdire qu'une personne soit autre chose qu'un VillagePeople ou un Minion en scellant le super type commun.**

On scelle l'interface `Inmate`:
```java
public sealed interface Inmate permits Minion, VillagePeople {
  String name();
//  int price();
}
```

Fichier `Minion.java`:
```java
public record Minion(String name) implements Inmate {
    ...
}
```

Fichier `VillagePeople.java`:
```java
public record VillagePeople(String name, Kind kind) implements Inmate {
    ...
}
```

8. **On veut périodiquement faire un geste commercial pour une maison envers une catégorie/sorte de VillagePeople en appliquant une réduction de 80% pour tous les VillagePeople ayant la même sorte (par exemple, pour tous les BIKERs). Pour cela, on se propose d'ajouter une méthode addDiscount qui prend une sorte en paramètre et offre un discount pour tous les VillagePeople de cette sorte. 
    Si l'on appelle deux fois addDiscount avec la même sorte, le discount n'est appliqué qu'une fois.**

On ajoute à la classe `House` une HashMap qui associe une sorte à une valeur entière, ici 80:
```java
public final class House {
  private final ArrayList<Inmate> people = new ArrayList<>();
  private final HashMap<Kind, Integer> discount = new HashMap<>();

  ...
}
```

On modifie la méthode `price(Inmate)` pour qu'elle prenne en compte le discount pour les VillagePeople:
```java
private double price(Inmate person) {
  return switch (person) {
    case Minion minion -> 1;
    case VillagePeople villagePeople -> {
      var kind = villagePeople.kind();
      if (discount.containsKey(kind)) {
        yield 100 * (1 - discount.get(kind) / 100.0);
      } else {
        yield 100;
      }
    }
  };
}
```

On ajoute la méthode `addDiscount(Kind)` qui ajoute une unique fois un discount de 80% pour une sorte donnée:
```java
public void addDiscount(Kind kind) {
  Objects.requireNonNull(kind);
  
  if (!discount.containsKey(kind)) {
    discount.put(kind, 80);
  }
}
```

Étant donné que l'on manipule à présent des valeurs à virgule flottante, on modifie la méthode `averagePrice()` pour qu'elle renvoie un `double`:
```java
public double averagePrice() {
  return people.stream()
        .mapToDouble(this::price)
        .average()
        .orElse(Double.NaN);
}
```

9. **Enfin, on souhaite pouvoir supprimer l'offre commerciale (discount) en ajoutant la méthode removeDiscount qui supprime le discount si celui-ci a été ajouté précédemment ou plante s'il n'y a pas de discount pour la sorte prise en paramètre.**

On ajoute la méthode `removeDiscount(Kind)` qui supprime le discount pour une sorte donnée, ou renvoi une erreur si le discount n'existe pas:
```java
public void removeDiscount(Kind kind) {
  Objects.requireNonNull(kind);

  if (discount.containsKey(kind)) {
    discount.remove(kind);
  } else {
    throw new IllegalStateException("No discount for " + kind);
  }
}
```
