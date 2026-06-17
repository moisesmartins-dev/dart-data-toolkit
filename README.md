# Dart Data Toolkit

A powerful plugin for **Android Studio** and **IntelliJ IDEA** that automatically generates:

1. **Dart methods** — copyWith, toJson, fromJson, toString
2. **Dart classes from JSON** — paste JSON, get a complete class
3. **Barrel files** — index.dart with organized exports

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-brightgreen)](https://plugins.jetbrains.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

## Features

### Dart Method Generator
- **copyWith()** — creates modified copies of the class
- **toJson()** — serializes the class to a JSON map
- **fromJson()** — deserializes a class from a JSON map
- **toString()** — formatted multiline string representation
- Supports nullable types (`String?`, `int?`, etc.)
- Interactive dialog to choose individual methods
- Keyboard shortcut: `Ctrl+Alt+D`

### JSON to Dart Class
- Paste any JSON object and generate a complete Dart class
- Choose which methods to include
- Auto-formats JSON on paste
- Keyboard shortcut: `Ctrl+Alt+J`

### Barrel File Generator
- Automatically exports all Dart files from a folder
- File list with checkboxes — choose exactly what to include
- Optional recursive mode for subdirectories
- Keyboard shortcut: `Ctrl+Alt+B`

## Installation

1. Build the plugin: `./gradlew buildPlugin`
2. In Android Studio / IntelliJ IDEA: **File → Settings → Plugins**
3. Click the gear icon → **Install Plugin from Disk**
4. Select `build/distributions/dart-data-plugin-1.0.0.zip`
5. Restart the IDE

## Usage

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
2. Press `Ctrl+Alt+D` (or right-click → **Dart Data Methods...**)
3. Select the methods you want
4. Code is inserted automatically

### Generate class from JSON

1. Press `Ctrl+Alt+J` anywhere in a Dart file
2. Enter a class name
3. Paste your JSON (auto-formatted)
4. Select which methods to include
5. Click OK

### Generate barrel file

1. Right-click on a folder in the Project panel
2. Press `Ctrl+Alt+B` (or **Dart Barrel File...**)
3. Choose files, enable recursive if needed
4. Click OK — `folder.dart` is created

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

### After (`Ctrl+Alt+D` with all methods selected)
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

  User copyWith({String? name, String? email, int? age}) {
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
└── parser/
    └── DartClassParser.kt
```

## Build

Requirements: JDK 17+, Gradle 8.8 (pinned via wrapper)

```bash
./gradlew buildPlugin    # produces build/distributions/dart-data-plugin-1.0.0.zip
./gradlew runIde         # launches a sandbox IDE with the plugin installed
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on reporting bugs, suggesting features, and submitting pull requests.

## License

MIT License — see [LICENSE](LICENSE) for details.

---

**Made with ❤️ for the Dart and Flutter community**

---

> Also available in [Português (pt-BR)](README.pt-BR.md)
