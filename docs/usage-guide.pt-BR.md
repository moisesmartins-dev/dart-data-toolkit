# Guia de Uso

Exemplos detalhados e uso avançado de cada funcionalidade do Dart Data Toolkit.

## Índice

- [Gerar Métodos Dart](#gerar-métodos-dart)
  - [copyWith()](#copywith)
  - [toJson()](#tojson)
  - [fromJson()](#fromjson)
  - [toString()](#tostring)
- [Gerar Classe a partir de JSON](#gerar-classe-a-partir-de-json)
- [Gerar Barrel File](#gerar-barrel-file)

---

## Gerar Métodos Dart

Posicione o cursor em qualquer lugar dentro de uma classe Dart e pressione `Ctrl+Alt+D` / `Cmd+Alt+D` (ou `Alt+Insert` para métodos individuais).

O plugin analisa a classe na posição do cursor e gera apenas os métodos que você selecionar.

### copyWith()

Cria uma nova instância da classe com alguns campos substituídos.

**Entrada:**
```dart
class User {
  final String name;
  final String email;
  final int? age;

  User({required this.name, required this.email, this.age});
}
```

**Gerado:**
```dart
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
```

**Uso em Dart:**
```dart
final user = User(name: 'Alice', email: 'alice@example.com', age: 30);
final updated = user.copyWith(name: 'Bob');
// updated.name == 'Bob', updated.email == 'alice@example.com'
```

> Todos os parâmetros são tornados nullable para que você precise passar apenas os campos que deseja alterar.

---

### toJson()

Serializa a classe para um `Map<String, dynamic>`.

**Gerado:**
```dart
Map<String, dynamic> toJson() {
  return {
    'name': name,
    'email': email,
    'age': age,
  };
}
```

**Uso em Dart:**
```dart
final json = user.toJson();
// {'name': 'Alice', 'email': 'alice@example.com', 'age': 30}

// Converter para string JSON:
import 'dart:convert';
final jsonString = jsonEncode(user.toJson());
```

---

### fromJson()

Gera um construtor factory que cria uma instância a partir de um `Map<String, dynamic>`.

**Gerado:**
```dart
factory User.fromJson(Map<String, dynamic> json) {
  return User(
    name: json['name'],
    email: json['email'],
    age: json['age'],
  );
}
```

**Uso em Dart:**
```dart
import 'dart:convert';

final jsonString = '{"name":"Alice","email":"alice@example.com","age":30}';
final user = User.fromJson(jsonDecode(jsonString));
```

---

### toString()

Gera um override de `toString()` multilinha para saída de depuração legível.

**Gerado:**
```dart
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
```

**Uso em Dart:**
```dart
print(user);
// User(
// name: Alice,
// email: alice@example.com,
// age: 30,
// )
```

---

## Gerar Classe a partir de JSON

Pressione `Ctrl+Alt+J` / `Cmd+Alt+J` em qualquer lugar de um arquivo Dart para abrir o diálogo.

### Passos

1. Insira um **nome de classe** (ex.: `Product`)
2. Selecione quais **métodos** incluir
3. Cole seu JSON — ele é formatado automaticamente ao colar
4. Clique em **OK**

### Inferência de tipos

| Valor JSON | Tipo Dart |
|---|---|
| `"hello"` | `String` |
| `42` | `int` |
| `9.99` | `double` |
| `true` / `false` | `bool` |
| `null` | `dynamic` |
| `[...]` | `List<T>` (inferido a partir do primeiro elemento) |
| `{...}` | `Map<String, dynamic>` |

### Conversão de chaves

Chaves em snake\_case e kebab-case são automaticamente convertidas para camelCase:

| Chave JSON | Campo Dart |
|---|---|
| `first_name` | `firstName` |
| `created-at` | `createdAt` |
| `userId` | `userId` |

### Exemplo

**Entrada JSON:**
```json
{
  "id": 1,
  "product_name": "Widget",
  "price": 9.99,
  "in_stock": true,
  "tags": ["sale", "new"]
}
```

**Classe gerada:**
```dart
class Product {
  final int id;
  final String productName;
  final double price;
  final bool inStock;
  final List<String> tags;

  Product({
    required this.id,
    required this.productName,
    required this.price,
    required this.inStock,
    required this.tags,
  });

  // Métodos selecionados gerados aqui...
}
```

---

## Gerar Barrel File

Clique com o botão direito em uma pasta no painel Project e pressione `Ctrl+Alt+B` / `Cmd+Alt+B`.

### O que é um barrel file?

Um barrel file (também chamado de index file) re-exporta múltiplos arquivos Dart a partir de um único ponto de entrada:

```dart
// models/models.dart
export 'user.dart';
export 'product.dart';
export 'order.dart';
```

Isso permite que os consumidores importem tudo de um único lugar:

```dart
import 'package:myapp/models/models.dart';
```

### Opções

| Opção | Descrição |
|---|---|
| **Nome do arquivo** | Nome do arquivo de saída (padrão: `nomedapasta.dart`) |
| **Incluir subdiretórios** | Inclui recursivamente arquivos Dart de subpastas |
| **Checkboxes** | Selecione exatamente quais arquivos exportar |
| **Selecionar todos / Desmarcar todos** | Alterna todos os checkboxes de uma vez |

### Exemplo de estrutura

```
lib/models/
├── user.dart
├── product.dart
├── order.dart
└── models.dart   ← barrel file gerado
```

**`models.dart` gerado:**
```dart
// This file is a barrel file. It exports all public APIs from this package.
// Generated by Dart Data Toolkit
export 'order.dart';
export 'product.dart';
export 'user.dart';
```

> Os arquivos são sempre ordenados alfabeticamente. Barrel files existentes (detectados pelo header), arquivos de índice comuns (`index.dart`, `barrel.dart`) e diretórios especiais (`build/`, `.dart_tool/`) são excluídos automaticamente.
