# Architecture

An overview of how the Dart Data Class Generator plugin is structured internally.

## Table of Contents

- [Package Structure](#package-structure)
- [Layer Overview](#layer-overview)
- [Parser](#parser)
- [Generators](#generators)
- [Actions](#actions)
- [Dialogs](#dialogs)
- [Data Flow](#data-flow)

---

## Package Structure

```
src/main/kotlin/com/kxaca/dartplugin/
├── actions/                        # IntelliJ action entry points and dialogs
│   ├── ChooseMethodsDialog.kt
│   ├── GenerateBarrelDialog.kt
│   ├── GenerateBarrelFileAction.kt
│   ├── GenerateDataClassAction.kt
│   ├── GenerateMethodActions.kt    # Individual Alt+Insert actions
│   ├── JsonToDartAction.kt
│   └── JsonToDartDialog.kt
├── generators/                     # Pure code generation logic
│   ├── BarrelFileGenerator.kt      # BarrelFileGenerator + BarrelFileAnalyzer
│   ├── CopyWithGenerator.kt
│   ├── GeneratorsKit.kt            # ToJsonGenerator, FromJsonGenerator, ToStringGenerator
│   └── JsonToDartGenerator.kt
└── parser/
    └── DartClassParser.kt          # DartClass + DartProperty + DartClassParser
```

---

## Layer Overview

```
┌─────────────────────────────────────┐
│             Actions                 │  ← IntelliJ entry points (AnAction)
│  GenerateDataClassAction            │
│  GenerateMethodActions              │
│  JsonToDartAction                   │
│  GenerateBarrelFileAction           │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│             Dialogs                 │  ← Swing UI (DialogWrapper)
│  ChooseMethodsDialog                │
│  JsonToDartDialog                   │
│  GenerateBarrelDialog               │
└────────────┬────────────────────────┘
             │
             ▼
┌──────────────────┐  ┌───────────────────────────────────┐
│     Parser       │  │           Generators               │
│  DartClassParser │  │  CopyWithGenerator                 │
│                  │  │  ToJsonGenerator                   │
│  DartClass       │  │  FromJsonGenerator                 │
│  DartProperty    │  │  ToStringGenerator                 │
└──────────────────┘  │  JsonToDartGenerator               │
                      │  BarrelFileGenerator               │
                      │  BarrelFileAnalyzer                │
                      └───────────────────────────────────┘
```

The layers are strictly unidirectional: Actions call Dialogs and Generators; Generators are pure functions with no IntelliJ dependencies.

---

## Parser

**`DartClassParser`** (`parser/DartClassParser.kt`)

Parses raw Dart source text to extract class structure. No AST — uses regex directly on the document text.

### Key types

```kotlin
data class DartProperty(
    val name: String,
    val type: String,
    val isNullable: Boolean
)

data class DartClass(
    val name: String,
    val properties: List<DartProperty>,
    val startOffset: Int,   // character offset of 'class' keyword
    val endOffset: Int,     // character offset of closing '}'
    val constructorName: String
)
```

### How it works

1. Finds all `class` declarations via regex
2. Picks the last one whose start offset is before the cursor
3. Finds the matching closing brace with a depth counter
4. Extracts properties using a two-alternative regex:
   - Alternative 1: `Type? name;` (nullable)
   - Alternative 2: `Type name;` (non-nullable)
5. Filters out Dart keywords to avoid false positives from method declarations

### Supported property forms

```dart
final String name;
final int? age;
final List<String> tags;
final Map<String, dynamic> meta;
String? description;
late double value;
```

---

## Generators

All generators are **pure functions** — they take a `DartClass` and return a `String`. No IntelliJ APIs, no side effects.

### CopyWithGenerator

Produces a `copyWith()` method. All parameters are typed as nullable so callers can omit unchanged fields, using `??` to fall back to `this.field`.

### ToJsonGenerator

Produces a `toJson()` method returning `Map<String, dynamic>`. Each property maps to `'key': value`.

### FromJsonGenerator

Produces a `factory ClassName.fromJson(Map<String, dynamic> json)` constructor. Each field is assigned from `json['key']`.

### ToStringGenerator

Produces an `@override String toString()` using a multiline string literal for readable output.

### JsonToDartGenerator

Accepts a raw JSON string and a set of method names. Uses `com.google.gson` (bundled with the IntelliJ platform) to parse the JSON and infer Dart types:

| JSON | Dart |
|---|---|
| string | `String` |
| integer | `int` |
| float | `double` |
| boolean | `bool` |
| null | `dynamic` |
| array | `List<T>` |
| object | `Map<String, dynamic>` |

Snake\_case and kebab-case keys are converted to camelCase before becoming field names.

### BarrelFileGenerator / BarrelFileAnalyzer

`BarrelFileAnalyzer` walks a directory tree to find `.dart` files, excluding known index files (`index.dart`, `barrel.dart`) and special directories (`build/`, `.dart_tool/`, `.git/`).

`BarrelFileGenerator` takes a list of `File` objects and produces export statements with paths relative to the base directory.

---

## Actions

Actions are IntelliJ `AnAction` subclasses registered in `plugin.xml`. They:

1. Read editor/project context from `AnActionEvent`
2. Open a dialog if user input is needed
3. Call the parser and/or generators
4. Write the result to the document via `WriteCommandAction`

### `update()` method

Actions that appear in the **Generate** menu (`Alt+Insert`) must not override `update()` — the Generate popup hides actions that return `isEnabled = false`. Dart file validation happens inside `actionPerformed()` instead.

Actions registered in **EditorPopupMenu** or with a keyboard shortcut do use `update()` to enable/disable based on file type.

---

## Dialogs

All dialogs extend IntelliJ's `DialogWrapper`. They collect user input and expose it via typed getter methods:

| Dialog | Getters |
|---|---|
| `ChooseMethodsDialog` | `getSelectedMethods(): Set<String>` |
| `JsonToDartDialog` | `getClassName()`, `getJsonText()`, `getSelectedMethods()` |
| `GenerateBarrelDialog` | `getSelectedFiles()`, `getFileName()` |

---

## Data Flow

### Method generation (`Ctrl+Alt+D`)

```
User triggers action
  → GenerateDataClassAction.actionPerformed()
    → DartClassParser.parseClass(documentText, cursorOffset)
      → DartClass (name, properties, endOffset)
    → ChooseMethodsDialog.showAndGet()
      → Set<String> (selected method names)
    → CopyWithGenerator / ToJsonGenerator / ... .generate(dartClass)
      → String (generated code)
    → WriteCommandAction → document.insertString(endOffset, code)
```

### JSON to Dart class (`Ctrl+Alt+J`)

```
User triggers action
  → JsonToDartAction.actionPerformed()
    → JsonToDartDialog.showAndGet()
      → className, jsonText, selectedMethods
    → JsonToDartGenerator.generateClass(className, json, methods)
      → Gson parses JSON
      → Type inference per field
      → camelCase key conversion
      → Generators called for selected methods
      → String (complete class)
    → WriteCommandAction → document.insertString(cursorOffset, code)
```

### Barrel file (`Ctrl+Alt+B`)

```
User triggers action
  → GenerateBarrelFileAction.actionPerformed()
    → GenerateBarrelDialog.showAndGet()
      → selectedFiles: List<File>, fileName: String
    → BarrelFileGenerator.generateBarrel(files, baseDir)
      → String (export statements)
    → File(baseDir, fileName).writeText(content)
    → baseVDir.refresh() → IDE picks up new file
```
