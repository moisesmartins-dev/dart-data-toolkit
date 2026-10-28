# Arquitetura

Uma visão geral de como o plugin Dart Data Toolkit é estruturado internamente.

## Índice

- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Visão em Camadas](#visão-em-camadas)
- [Parser](#parser)
- [Generators](#generators)
- [Inspections](#inspections)
- [Actions](#actions)
- [Dialogs](#dialogs)
- [Fluxo de Dados](#fluxo-de-dados)

---

## Estrutura de Pacotes

```
src/main/kotlin/com/kxaca/dartdatatoolkit/
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
├── inspections/                    # Warnings e quick fixes no editor (exigem o plugin Dart)
│   ├── CopyWithAnalysis.kt         # Encontra copyWith desatualizado e monta a edição de substituição
│   ├── CopyWithInspection.kt       # LocalInspectionTool: "copyWith() is missing class fields"
│   └── GenerateCopyWithQuickFix.kt # LocalQuickFix: "Generate copyWith"
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

Inspections são um segundo ponto de entrada ao lado das Actions: quem as executa é a IDE, em segundo plano, e não o usuário. Elas chamam o Parser e o `CopyWithGenerator` diretamente e nunca abrem dialogs.

---

## Parser

**`DartClassParser`** (`parser/DartClassParser.kt`)

Analisa texto-fonte Dart bruto para extrair a estrutura da classe. Não utiliza AST — usa regex diretamente sobre o texto do documento.

### Tipos principais

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
    val startOffset: Int,   // offset de caractere da palavra-chave 'class'
    val endOffset: Int,     // offset de caractere do '}' de fechamento
    val constructorName: String
) {
    // Campos não estáticos e não privados: os que o copyWith aceita como parâmetros nomeados
    val copyableProperties: List<DartProperty>
}
```

### Como funciona

1. Localiza todas as declarações `class` via regex
2. Seleciona a última cujo offset de início é anterior ao cursor
3. Encontra a chave de fechamento correspondente com um contador de profundidade
4. Remove corpos de métodos do texto da classe (varredura de chaves com contador de profundidade) para evitar falsos positivos de variáveis locais e tipos de retorno dentro de métodos
5. Extrai propriedades usando uma regex de duas alternativas sobre o texto processado:
   - Alternativa 1: `Type? name;` (nullable)
   - Alternativa 2: `Type name;` (não-nullable)
6. Filtra palavras-chave do Dart para evitar falsos positivos em declarações de métodos
7. Marca o campo como `isStatic` quando um modificador `static` o precede (incluindo `static final` e `static const`)

`parseClass` retorna apenas a classe anterior ao cursor. `parseAllClasses(content)` retorna todas as classes do arquivo e é usado pela inspeção, que não tem cursor.

### Formas de propriedade suportadas

```dart
final String name;
final int? age;
final List<String> tags;
final Map<String, dynamic> meta;
String? description;
late double value;
```

Campos `static` são analisados, mas marcados como `isStatic`, e campos privados (`_name`) são analisados normalmente; ambos ficam fora de `copyableProperties`.

---

## Generators

Todos os generators são **funções puras** — recebem um `DartClass` e retornam uma `String`. Sem APIs do IntelliJ, sem efeitos colaterais.

### CopyWithGenerator

Produz um método `copyWith()`. Todos os parâmetros são tipados como nullable para que os chamadores possam omitir campos não alterados, usando `??` para recorrer a `this.field`. Ele usa `DartClass.copyableProperties`, então campos `static` e privados (`_name`) não são incluídos: não podem ser passados como parâmetros nomeados.

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

`BarrelFileAnalyzer` percorre uma árvore de diretórios para encontrar arquivos `.dart`, excluindo barrel files existentes (detectados pela leitura da primeira linha com o header `// This file is a barrel file`; a forma antiga com `///`, escrita até a versão 1.2.0, também é reconhecida), arquivos de índice conhecidos (`index.dart`, `barrel.dart`) e diretórios especiais (`build/`, `.dart_tool/`, `.git/`).

`BarrelFileGenerator` recebe uma lista de objetos `File` e produz instruções de exportação com caminhos relativos ao diretório base.

---

## Inspections

**`CopyWithInspection`** (`inspections/CopyWithInspection.kt`) é uma `LocalInspectionTool` que reporta um `copyWith()` sem parâmetro para um ou mais campos da sua classe. Como o resto do plugin, ela trabalha sobre o texto do arquivo, não sobre o PSI do Dart, então não precisa de classes do Dart em tempo de compilação.

| Classe | Papel |
|---|---|
| `CopyWithAnalysis` | Lógica pura. `findOutdatedCopyWith(text)` compara `copyableProperties` com os nomes de parâmetros lidos do `copyWith` de cada classe; `buildReplacement(text, className)` devolve a edição que o regenera |
| `CopyWithInspection` | Transforma cada achado em um `WARNING` sobre o identificador `copyWith` |
| `GenerateCopyWithQuickFix` | Aplica a edição de `buildReplacement`. A classe é procurada de novo pelo nome quando o fix roda, porque o documento pode ter mudado desde a inspeção |

A análise fica em silêncio em vez de adivinhar quando não consegue ler o método: uma chamada `copyWith(...)` dentro de outro método não é tratada como a declaração, e um `copyWith` que ela não consegue interpretar não gera warning.

### Registro

A inspeção é declarada em `META-INF/dart-inspections.xml`, carregado por uma dependência opcional no `plugin.xml`:

```xml
<depends optional="true" config-file="dart-inspections.xml">Dart</depends>
```

`language="Dart"` só resolve quando o plugin Dart está carregado. Com a dependência opcional, o toolkit continua instalando e funcionando sem ele; apenas a inspeção fica ausente. A descrição da inspeção fica em `inspectionDescriptions/DartCopyWithMissingParams.html`.

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
    → WriteCommandAction
        → se o método existe: document.deleteString(start, end) + insertString(novoOffset, code)
        → caso contrário: document.insertString(endOffset, code)
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

Nenhum diálogo de confirmação é exibido em caso de sucesso; apenas erros são informados.

### copyWith desatualizado (inspeção + quick fix)

```
A IDE executa CopyWithInspection.checkFile(file)
  → CopyWithAnalysis.findOutdatedCopyWith(file.text)
    → DartClassParser.parseAllClasses(text)
    → por classe: copyableProperties menos os nomes de parâmetros do copyWith
  → ProblemDescriptor (WARNING em "copyWith") + GenerateCopyWithQuickFix(className)

Usuário pressiona Alt+Enter → "Generate copyWith"
  → CopyWithAnalysis.buildReplacement(document.text, className)
    → findMethodBounds(...) localiza o método existente
    → CopyWithGenerator.generate(dartClass)
  → document.replaceString(start, end, code)   # mesma posição, não vai para o fim da classe
```
