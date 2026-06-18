# Arquitetura

Uma visão geral de como o plugin Dart Data Class Generator é estruturado internamente.

## Índice

- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Visão em Camadas](#visão-em-camadas)
- [Parser](#parser)
- [Generators](#generators)
- [Actions](#actions)
- [Dialogs](#dialogs)
- [Fluxo de Dados](#fluxo-de-dados)

---

## Estrutura de Pacotes

```
src/main/kotlin/com/kxaca/dartplugin/
├── actions/                        # Pontos de entrada de actions do IntelliJ e diálogos
│   ├── ChooseMethodsDialog.kt
│   ├── GenerateBarrelDialog.kt
│   ├── GenerateBarrelFileAction.kt
│   ├── GenerateDataClassAction.kt
│   ├── GenerateMethodActions.kt    # Actions individuais do Alt+Insert
│   ├── JsonToDartAction.kt
│   └── JsonToDartDialog.kt
├── generators/                     # Lógica pura de geração de código
│   ├── BarrelFileGenerator.kt      # BarrelFileGenerator + BarrelFileAnalyzer
│   ├── CopyWithGenerator.kt
│   ├── GeneratorsKit.kt            # ToJsonGenerator, FromJsonGenerator, ToStringGenerator
│   └── JsonToDartGenerator.kt
└── parser/
    └── DartClassParser.kt          # DartClass + DartProperty + DartClassParser
```

---

## Visão em Camadas

```
┌─────────────────────────────────────┐
│             Actions                 │  ← Pontos de entrada do IntelliJ (AnAction)
│  GenerateDataClassAction            │
│  GenerateMethodActions              │
│  JsonToDartAction                   │
│  GenerateBarrelFileAction           │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│             Dialogs                 │  ← UI Swing (DialogWrapper)
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

As camadas são estritamente unidirecionais: Actions chamam Dialogs e Generators; Generators são funções puras sem dependências do IntelliJ.

---

## Parser

**`DartClassParser`** (`parser/DartClassParser.kt`)

Analisa texto-fonte Dart bruto para extrair a estrutura da classe. Não utiliza AST — usa regex diretamente sobre o texto do documento.

### Tipos principais

```kotlin
data class DartProperty(
    val name: String,
    val type: String,
    val isNullable: Boolean
)

data class DartClass(
    val name: String,
    val properties: List<DartProperty>,
    val startOffset: Int,   // offset de caractere da palavra-chave 'class'
    val endOffset: Int,     // offset de caractere do '}' de fechamento
    val constructorName: String
)
```

### Como funciona

1. Localiza todas as declarações `class` via regex
2. Seleciona a última cujo offset de início é anterior ao cursor
3. Encontra a chave de fechamento correspondente com um contador de profundidade
4. Extrai propriedades usando uma regex de duas alternativas:
   - Alternativa 1: `Type? name;` (nullable)
   - Alternativa 2: `Type name;` (não-nullable)
5. Filtra palavras-chave do Dart para evitar falsos positivos em declarações de métodos

### Formas de propriedade suportadas

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

Todos os generators são **funções puras** — recebem um `DartClass` e retornam uma `String`. Sem APIs do IntelliJ, sem efeitos colaterais.

### CopyWithGenerator

Produz um método `copyWith()`. Todos os parâmetros são tipados como nullable para que os chamadores possam omitir campos não alterados, usando `??` para recorrer a `this.field`.

### ToJsonGenerator

Produz um método `toJson()` que retorna `Map<String, dynamic>`. Cada propriedade é mapeada para `'key': value`.

### FromJsonGenerator

Produz um construtor factory `ClassName.fromJson(Map<String, dynamic> json)`. Cada campo é atribuído a partir de `json['key']`.

### ToStringGenerator

Produz um `@override String toString()` usando uma string literal multilinha para saída legível.

### JsonToDartGenerator

Aceita uma string JSON bruta e um conjunto de nomes de métodos. Usa `com.google.gson` (embutido na plataforma IntelliJ) para analisar o JSON e inferir tipos Dart:

| JSON | Dart |
|---|---|
| string | `String` |
| integer | `int` |
| float | `double` |
| boolean | `bool` |
| null | `dynamic` |
| array | `List<T>` |
| object | `Map<String, dynamic>` |

Chaves em snake\_case e kebab-case são convertidas para camelCase antes de se tornarem nomes de campo.

### BarrelFileGenerator / BarrelFileAnalyzer

`BarrelFileAnalyzer` percorre uma árvore de diretórios para encontrar arquivos `.dart`, excluindo arquivos de índice conhecidos (`index.dart`, `barrel.dart`) e diretórios especiais (`build/`, `.dart_tool/`, `.git/`).

`BarrelFileGenerator` recebe uma lista de objetos `File` e produz instruções de exportação com caminhos relativos ao diretório base.

---

## Actions

Actions são subclasses de `AnAction` do IntelliJ registradas no `plugin.xml`. Elas:

1. Leem o contexto do editor/projeto a partir de `AnActionEvent`
2. Abrem um diálogo se a entrada do usuário for necessária
3. Chamam o parser e/ou generators
4. Escrevem o resultado no documento via `WriteCommandAction`

### Método `update()`

Actions que aparecem no menu **Generate** (`Alt+Insert`) não devem sobrescrever `update()` — o popup Generate oculta actions que retornam `isEnabled = false`. A validação de arquivo Dart acontece dentro de `actionPerformed()`.

Actions registradas em **EditorPopupMenu** ou com atalho de teclado usam `update()` para habilitar/desabilitar com base no tipo de arquivo.

---

## Dialogs

Todos os diálogos estendem o `DialogWrapper` do IntelliJ. Eles coletam entrada do usuário e a expõem via métodos getter tipados:

| Dialog | Getters |
|---|---|
| `ChooseMethodsDialog` | `getSelectedMethods(): Set<String>` |
| `JsonToDartDialog` | `getClassName()`, `getJsonText()`, `getSelectedMethods()` |
| `GenerateBarrelDialog` | `getSelectedFiles()`, `getFileName()` |

---

## Fluxo de Dados

### Geração de métodos (`Ctrl+Alt+D` / `Cmd+Alt+D`)

```
Usuário dispara a action
  → GenerateDataClassAction.actionPerformed()
    → DartClassParser.parseClass(documentText, cursorOffset)
      → DartClass (name, properties, endOffset)
    → ChooseMethodsDialog.showAndGet()
      → Set<String> (nomes dos métodos selecionados)
    → CopyWithGenerator / ToJsonGenerator / ... .generate(dartClass)
      → String (código gerado)
    → WriteCommandAction → document.insertString(endOffset, code)
```

### JSON para classe Dart (`Ctrl+Alt+J` / `Cmd+Alt+J`)

```
Usuário dispara a action
  → JsonToDartAction.actionPerformed()
    → JsonToDartDialog.showAndGet()
      → className, jsonText, selectedMethods
    → JsonToDartGenerator.generateClass(className, json, methods)
      → Gson analisa o JSON
      → Inferência de tipo por campo
      → Conversão de chaves para camelCase
      → Generators chamados para os métodos selecionados
      → String (classe completa)
    → WriteCommandAction → document.insertString(cursorOffset, code)
```

### Barrel file (`Ctrl+Alt+B` / `Cmd+Alt+B`)

```
Usuário dispara a action
  → GenerateBarrelFileAction.actionPerformed()
    → GenerateBarrelDialog.showAndGet()
      → selectedFiles: List<File>, fileName: String
    → BarrelFileGenerator.generateBarrel(files, baseDir)
      → String (instruções de exportação)
    → File(baseDir, fileName).writeText(content)
    → baseVDir.refresh() → IDE detecta o novo arquivo
```
