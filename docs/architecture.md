# Architecture

An overview of how the Dart Data Toolkit plugin is structured internally.

## Table of Contents

- [Package Structure](#package-structure)
- [Layer Overview](#layer-overview)
- [Parser](#parser)
- [Generators](#generators)
- [Inspections](#inspections)
- [Actions](#actions)
- [Dialogs](#dialogs)
- [Data Flow](#data-flow)

---

## Package Structure

```
src/main/kotlin/com/kxaca/dartdatatoolkit/
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
├── inspections/                    # Editor warnings and quick fixes (need the Dart plugin)
│   ├── CopyWithAnalysis.kt         # Finds outdated copyWith methods, builds the replacement edit
│   ├── CopyWithInspection.kt       # LocalInspectionTool: "copyWith() is missing class fields"
│   └── GenerateCopyWithQuickFix.kt # LocalQuickFix: "Generate copyWith"
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

Inspections are a second entry point beside Actions: the IDE runs them in the background instead of the user triggering them. They call the Parser and `CopyWithGenerator` directly and never open dialogs.

---

## Parser

**`DartClassParser`** (`parser/DartClassParser.kt`)

Parses raw Dart source text to extract class structure. No AST — uses regex directly on the document text.

### Key types

```kotlin
data class DartProperty(
    val name: String,
    val type: String,
    val isNullable: Boolean = false,
    val isRequired: Boolean = false,
    val defaultValue: String? = null,
    val isCustomClass: Boolean = false,
    val isStatic: Boolean = false
)

data class DartClass(
    val name: String,
    val properties: List<DartProperty>,
    val startOffset: Int,   // character offset of 'class' keyword
    val endOffset: Int,     // character offset of closing '}'
    val constructorName: String
) {
    // Non-static, non-private fields: the ones copyWith can take as named parameters
    val copyableProperties: List<DartProperty>
}
```

### How it works

1. Finds all `class` declarations via regex
2. Picks the last one whose start offset is before the cursor
3. Finds the matching closing brace with a depth counter
4. Strips method bodies from the class text (depth-counter brace scan) to avoid false positives from local variables and return types inside methods
5. Extracts properties using a two-alternative regex on the stripped text:
   - Alternative 1: `Type? name;` (nullable)
   - Alternative 2: `Type name;` (non-nullable)
6. Filters out Dart keywords to avoid false positives from method declarations
7. Marks a field `isStatic` when a `static` modifier precedes it (including `static final` and `static const`)

`parseClass` returns only the class before the cursor. `parseAllClasses(content)` returns every class in the file and is used by the inspection, which has no cursor.

### Supported property forms

```dart
final String name;
final int? age;
final List<String> tags;
final Map<String, dynamic> meta;
String? description;
late double value;
```

`static` fields are parsed but flagged `isStatic`, and private fields (`_name`) are parsed as usual; both are left out of `copyableProperties`.

---

## Generators

All generators are **pure functions** — they take a `DartClass` and return a `String`. No IntelliJ APIs, no side effects.

### CopyWithGenerator

Produces a `copyWith()` method. All parameters are typed as nullable so callers can omit unchanged fields, using `??` to fall back to `this.field`. It uses `DartClass.copyableProperties`, so `static` and private (`_name`) fields are not included: they cannot be passed as named parameters.

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

`BarrelFileAnalyzer` walks a directory tree to find `.dart` files, excluding existing barrel files (detected by reading their first line for the `// This file is a barrel file` header; the legacy `///` form written by versions up to 1.2.0 is also recognised), known index files (`index.dart`, `barrel.dart`), and special directories (`build/`, `.dart_tool/`, `.git/`).

`BarrelFileGenerator` takes a list of `File` objects and produces export statements with paths relative to the base directory.

---

## Inspections

**`CopyWithInspection`** (`inspections/CopyWithInspection.kt`) is a `LocalInspectionTool` that reports a `copyWith()` with no parameter for one or more fields of its class. Like the rest of the plugin it works on the file text, not on Dart PSI, so it needs no Dart classes at compile time.

| Class | Role |
|---|---|
| `CopyWithAnalysis` | Pure logic. `findOutdatedCopyWith(text)` compares `copyableProperties` with the parameter names read from each class's `copyWith`; `buildReplacement(text, className)` returns the edit that regenerates it |
| `CopyWithInspection` | Turns each finding into a `WARNING` on the `copyWith` identifier |
| `GenerateCopyWithQuickFix` | Applies the edit from `buildReplacement`. The class is looked up again by name when the fix runs, because the document may have changed since the inspection ran |

The analysis stays silent instead of guessing when it cannot read the method: a `copyWith(...)` call inside another method is not treated as the declaration, and a `copyWith` it cannot parse produces no warning.

### Registration

The inspection is declared in `META-INF/dart-inspections.xml`, loaded through an optional dependency in `plugin.xml`:

```xml
<depends optional="true" config-file="dart-inspections.xml">Dart</depends>
```

`language="Dart"` only resolves when the Dart plugin is loaded. With the optional dependency the toolkit still installs and works without it; only the inspection is missing. The inspection description lives in `inspectionDescriptions/DartCopyWithMissingParams.html`.

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

### Method generation (`Ctrl+Alt+D` / `Cmd+Alt+D`)

```
User triggers action
  → GenerateDataClassAction.actionPerformed()
    → DartClassParser.parseClass(documentText, cursorOffset)
      → DartClass (name, properties, endOffset)
    → ChooseMethodsDialog.showAndGet()
      → Set<String> (selected method names)
    → CopyWithGenerator / ToJsonGenerator / ... .generate(dartClass)
      → String (generated code)
    → WriteCommandAction
        → if method exists: document.deleteString(start, end) + insertString(newOffset, code)
        → otherwise: document.insertString(endOffset, code)
```

### JSON to Dart class (`Ctrl+Alt+J` / `Cmd+Alt+J`)

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

### Barrel file (`Ctrl+Alt+B` / `Cmd+Alt+B`)

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

No confirmation dialog is shown on success; only errors are reported.

### Outdated copyWith (inspection + quick fix)

```
IDE runs CopyWithInspection.checkFile(file)
  → CopyWithAnalysis.findOutdatedCopyWith(file.text)
    → DartClassParser.parseAllClasses(text)
    → per class: copyableProperties minus the copyWith parameter names
  → ProblemDescriptor (WARNING on "copyWith") + GenerateCopyWithQuickFix(className)

User presses Alt+Enter → "Generate copyWith"
  → CopyWithAnalysis.buildReplacement(document.text, className)
    → findMethodBounds(...) locates the existing method
    → CopyWithGenerator.generate(dartClass)
  → document.replaceString(start, end, code)   # same position, not moved to the end of the class
```
