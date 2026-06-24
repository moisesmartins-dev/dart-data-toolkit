# Dart Data Toolkit

Um plugin poderoso para **Android Studio** e **IntelliJ IDEA** que gera automaticamente:

1. **Métodos Dart** — copyWith, toJson, fromJson, toString
2. **Classes Dart a partir de JSON** — cole um JSON e obtenha uma classe completa
3. **Barrel files** — index.dart com exports organizados

[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-brightgreen)](https://plugins.jetbrains.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

## Funcionalidades

### Gerador de Métodos Dart
- **copyWith()** — cria cópias modificadas da classe
- **toJson()** — serializa a classe para um mapa JSON
- **fromJson()** — desserializa uma classe a partir de um mapa JSON
- **toString()** — representação em string formatada com multiline
- Suporte a tipos anuláveis (`String?`, `int?`, etc.)
- Dialog interativo para escolher métodos individualmente
- Tratamento inteligente de duplicatas — o dialog em lote avisa e pula métodos que já existem; ações individuais substituem o método existente no lugar
- Atalho: `Ctrl+Alt+D` (Windows/Linux) / `Cmd+Alt+D` (macOS)

### JSON para Classe Dart
- Cole qualquer objeto JSON e gere uma classe Dart completa
- **Objetos aninhados** geram classes Dart separadas com tipagem correta
- `toJson()` e `fromJson()` tratam classes aninhadas automaticamente
- Escolha quais métodos incluir
- Formatação automática do JSON ao colar
- Atalho: `Ctrl+Alt+J` (Windows/Linux) / `Cmd+Alt+J` (macOS)

### Gerador de Barrel File
- Exporta automaticamente todos os arquivos Dart de uma pasta
- Lista de arquivos com checkboxes — escolha exatamente o que incluir
- Modo recursivo opcional para subdiretórios
- Atalho: `Ctrl+Alt+B` (Windows/Linux) / `Cmd+Alt+B` (macOS)

## Instalação

1. Compile o plugin: `./gradlew buildPlugin`
2. No Android Studio / IntelliJ IDEA: **File → Settings → Plugins**
3. Clique no ícone de engrenagem → **Install Plugin from Disk**
4. Selecione `build/distributions/dart-data-toolkit-1.2.0.zip`
5. Reinicie a IDE

## Como usar

> Para exemplos detalhados e uso avançado, consulte o [Guia de Uso](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/docs/usage-guide.md).

### Gerar métodos Dart

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

1. Coloque o cursor dentro da classe
2. Pressione `Ctrl+Alt+D` / `Cmd+Alt+D` (ou clique direito → **Dart Data Methods...**)
3. Selecione os métodos desejados
4. O código é inserido automaticamente

### Gerar classe a partir de JSON

1. Pressione `Ctrl+Alt+J` / `Cmd+Alt+J` em qualquer arquivo Dart
2. Digite o nome da classe (ex: `User`)
3. Cole o JSON (formatado automaticamente)
4. Selecione quais métodos incluir
5. Clique em OK

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "age": 30
}
```

Gera:

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

Objetos JSON aninhados são detectados automaticamente — cada um vira sua própria classe Dart.

Dado:

```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "address": {
    "street": "Rua Principal, 123",
    "city": "São Paulo"
  }
}
```

Duas classes são geradas: `Address` e `User`. `User.toJson()` chama `address.toJson()` e `User.fromJson()` chama `Address.fromJson(json['address'] as Map<String, dynamic>)`.

### Gerar barrel file

1. Clique direito em uma pasta no painel de projeto
2. Pressione `Ctrl+Alt+B` / `Cmd+Alt+B` (ou **Dart Barrel File...**)
3. Escolha os arquivos, habilite recursivo se necessário
4. Clique em OK — `pasta.dart` é criado

## Exemplo de resultado

### Antes
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

### Depois (`Ctrl+Alt+D` / `Cmd+Alt+D` com todos os métodos selecionados)
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

## Arquitetura

```
src/main/kotlin/com/kxaca/dartdatatoolkit/
├── actions/
│   ├── GenerateDataClassAction.kt     # Entry point do Ctrl+Alt+D
│   ├── GenerateMethodActions.kt       # Ações individuais (Alt+Insert)
│   ├── JsonToDartAction.kt            # Entry point do Ctrl+Alt+J
│   ├── GenerateBarrelFileAction.kt    # Entry point do Ctrl+Alt+B
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

Requisitos: JDK 17+, Gradle 8.8 (fixado via wrapper)

```bash
./gradlew buildPlugin    # gera build/distributions/dart-data-toolkit-1.2.0.zip
./gradlew runIde         # abre uma IDE sandbox com o plugin instalado
```

## Contribuindo

Veja [CONTRIBUTING.pt-BR.md](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/CONTRIBUTING.pt-BR.md) para diretrizes sobre como reportar bugs, sugerir features e enviar pull requests.

## Licença

MIT License — veja o arquivo [LICENSE](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/LICENSE) para detalhes.

---

**Feito com ❤️ para a comunidade Dart e Flutter**

---

## Aviso de Marca Registrada

O logotipo do Flutter utilizado no ícone deste plugin é uma marca registrada da Google LLC. Seu uso está em conformidade com as [Diretrizes de Marca do Flutter](https://flutter.dev/brand) sob a [Licença Creative Commons Attribution 4.0 International](https://creativecommons.org/licenses/by/4.0/). Este plugin não tem afiliação com o Google ou com a equipe do Flutter, nem é endossado por eles.

---

> Also available in [English](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/README.md)
