# Dart Data Toolkit

A powerful plugin for **Android Studio** and **IntelliJ IDEA** that automatically generates:

1. **Dart methods** — copyWith, toJson, fromJson, toString
2. **Dart classes from JSON** — paste JSON, get a complete class
3. **Barrel files** — index.dart with organized exports

It also keeps your `copyWith()` up to date: a built-in inspection warns when a field is missing and fixes it in one click.

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-brightgreen)](https://plugins.jetbrains.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

## Features

### Dart Method Generator
- **copyWith()** — creates modified copies of the class
- **toJson()** — serializes the class to a JSON map
- **fromJson()** — deserializes a class from a JSON map
- **toString()** — formatted multiline string representation
- Supports nullable types (`String?`, `int?`, etc.)
- `static` and private (`_name`) fields are left out of `copyWith()`, since they cannot be passed as named parameters
- Interactive dialog to choose individual methods
- Smart duplicate handling — the batch dialog warns and skips methods that already exist; individual actions replace the existing method in place
- Keyboard shortcut: `Ctrl+Alt+D` (Windows/Linux) / `Cmd+Alt+D` (macOS)

### copyWith Inspection
- Warns when a class's `copyWith()` has no parameter for one or more of its fields (for example after adding a new field)
- **Generate copyWith** quick fix (`Alt+Enter`) regenerates the method in place, in the same position
- Requires the Dart plugin; without it the rest of the plugin works as usual

### JSON to Dart Class
- Paste any JSON object and generate a complete Dart class
- **Nested objects** generate separate, properly typed Dart classes
- `toJson()` and `fromJson()` handle nested classes automatically
- Choose which methods to include
- Auto-formats JSON on paste
- Keyboard shortcut: `Ctrl+Alt+J` (Windows/Linux) / `Cmd+Alt+J` (macOS)

### Barrel File Generator
- Automatically exports all Dart files from a folder
- File list with checkboxes — choose exactly what to include
- Optional recursive mode for subdirectories
- Generated files start with a plain `//` header; barrel files created by earlier versions (`///`) are still recognised and never exported again
- Runs silently: the file is created without a confirmation dialog, and only errors are reported
- Keyboard shortcut: `Ctrl+Alt+B` (Windows/Linux) / `Cmd+Alt+B` (macOS)

## Installation

1. Build the plugin: `./gradlew buildPlugin`
2. In Android Studio / IntelliJ IDEA: **File → Settings → Plugins**
3. Click the gear icon → **Install Plugin from Disk**
4. Select `build/distributions/dart-data-toolkit-1.3.0.zip`
5. Restart the IDE

Compatible with IntelliJ Platform builds 233 through 262 (IntelliJ IDEA 2023.3 to 2026.2, and Android Studio builds based on them). The copyWith inspection also needs the Dart plugin to be installed.

## Usage

> For detailed examples and advanced usage, see the [Usage Guide](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/docs/usage-guide.md).

### Generate Dart methods

```dart
class User {
  final String name;
  final String email;
  final int? age;

  User({
    required this.name,
    required this.email,
    this.age,
  });
}
```

1. Place the cursor inside the class
2. Press `Ctrl+Alt+D` / `Cmd+Alt+D` (or right-click → **Dart Data Methods...**)
3. Select the methods you want
4. Code is inserted automatically

### Keep copyWith up to date

Add a field to a class later and the existing `copyWith()` is no longer complete:

```dart
class User {
  final String name;
  final String email;
  final int? age;
  final String phone;          // new field

  User copyWith({              // ⚠ copyWith() is missing parameters for: phone
    String? name,
    String? email,
    int? age,
  }) {
    return User(
      name: name ?? this.name,
      email: email ?? this.email,
      age: age ?? this.age,
      phone: this.phone,
    );
  }
}
```

1. Hover over the highlighted `copyWith` to read which fields are missing
2. Press `Alt+Enter` (`Option+Enter` on macOS) and choose **Generate copyWith**
3. The method is regenerated in place with every field, and the warning disappears

The inspection is called *copyWith() is missing class fields* and can be turned off in **Settings → Editor → Inspections** (look for *Dart Data Toolkit*).

### Generate class from JSON

1. Press `Ctrl+Alt+J` / `Cmd+Alt+J` anywhere in a Dart file
2. Enter a class name (e.g. `User`)
3. Paste your JSON (auto-formatted)
4. Select which methods to include
5. Click OK

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "age": 30
}
```

Generates:

```dart
class User {
  final String name;
  final String email;
  final int age;

  User({
    required this.name,
    required this.email,
    required this.age,
  });

  User copyWith({
    String? name,
    String? email,
    int? age,
  }) {
    return User(
      name: name ?? this.name,
      email: email ?? this.email,
      age: age ?? this.age,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'email': email,
      'age': age,
    };
  }

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      name: json['name'],
      email: json['email'],
      age: json['age'],
    );
  }

  @override
  String toString() {
    return '''
      User(
      name: $name,
      email: $email,
      age: $age,
      )
    ''';
  }
}
```

Nested JSON objects are detected automatically — each becomes its own Dart class.

Given:
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "address": {
    "street": "123 Main St",
    "city": "Springfield"
  }
}
```

Two classes are generated: `Address` and `User`. `User.toJson()` calls `address.toJson()` and `User.fromJson()` calls `Address.fromJson(json['address'] as Map<String, dynamic>)`.

### Generate barrel file

1. Right-click on a folder in the Project panel
2. Press `Ctrl+Alt+B` / `Cmd+Alt+B` (or **Dart Barrel File...**)
3. Choose files, enable recursive if needed
4. Click OK — `folder.dart` is created, with no confirmation dialog

The generated file looks like this:

```dart
// This file is a barrel file. It exports all public APIs from this package.
// Generated by Dart Data Toolkit
export 'address.dart';
export 'user.dart';
```

## Example output

### Before
```dart
class User {
  final String name;
  final String email;
  final int? age;

  User({
    required this.name,
    required this.email,
    this.age,
  });
}
```

### After (`Ctrl+Alt+D` / `Cmd+Alt+D` with all methods selected)
```dart
class User {
  final String name;
  final String email;
  final int? age;

  User({
    required this.name,
    required this.email,
    this.age,
  });

  User copyWith({
    String? name,
    String? email,
    int? age,
  }) {
    return User(
      name: name ?? this.name,
      email: email ?? this.email,
      age: age ?? this.age,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'email': email,
      'age': age,
    };
  }

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      name: json['name'],
      email: json['email'],
      age: json['age'],
    );
  }

  @override
  String toString() {
    return '''
      User(
      name: $name,
      email: $email,
      age: $age,
      )
    ''';
  }
}
```

## Architecture

```
src/main/kotlin/com/kxaca/dartdatatoolkit/
├── actions/
│   ├── GenerateDataClassAction.kt     # Ctrl+Alt+D entry point
│   ├── GenerateMethodActions.kt       # Individual method actions (Alt+Insert)
│   ├── JsonToDartAction.kt            # Ctrl+Alt+J entry point
│   ├── GenerateBarrelFileAction.kt    # Ctrl+Alt+B entry point
│   ├── ChooseMethodsDialog.kt
│   ├── JsonToDartDialog.kt
│   └── GenerateBarrelDialog.kt
├── generators/
│   ├── CopyWithGenerator.kt
│   ├── GeneratorsKit.kt               # ToJson, FromJson, ToString
│   ├── JsonToDartGenerator.kt
│   └── BarrelFileGenerator.kt
├── inspections/
│   ├── CopyWithInspection.kt          # "copyWith() is missing class fields" warning
│   ├── GenerateCopyWithQuickFix.kt    # "Generate copyWith" quick fix
│   └── CopyWithAnalysis.kt            # Finds outdated copyWith, builds the replacement
└── parser/
    └── DartClassParser.kt
```

## Build

Requirements: JDK 17+, Gradle 8.14.4 (pinned via wrapper)

```bash
./gradlew buildPlugin    # produces build/distributions/dart-data-toolkit-1.3.0.zip
./gradlew runIde         # launches a sandbox IDE with the plugin installed
```

## Contributing

See [CONTRIBUTING.md](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/CONTRIBUTING.md) for guidelines on reporting bugs, suggesting features, and submitting pull requests.

## License

MIT License — see [LICENSE](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/LICENSE) for details.

---

**Made with ❤️ for the Dart and Flutter community**

---

## Trademark Notice

The Flutter logo used in this plugin's icon is a trademark of Google LLC. It is used in accordance with the [Flutter Brand Guidelines](https://flutter.dev/brand) under the [Creative Commons Attribution 4.0 International License](https://creativecommons.org/licenses/by/4.0/). This plugin is not affiliated with or endorsed by Google or the Flutter team.

---

> Also available in [Português (pt-BR)](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/README.pt-BR.md)
