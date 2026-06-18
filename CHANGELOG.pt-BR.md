# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto segue [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- ✨ Gerador de métodos Dart (copyWith, toJson, fromJson, toString)
- ✨ Gerador de barrel files (index.dart com 3 estilos)
- 🎨 Dialog interativo para escolher métodos
- 🎨 Dialog configurável para barrel files
- 📚 Documentação completa
- 🧪 Exemplos funcionando
- ⌨️ Atalhos de teclado (Ctrl+Alt+D e Ctrl+Alt+B)
- ✅ Suporte a tipos anuláveis
- ✅ Suporte a diretórios recursivos
- 🏗️ Suporte a objetos JSON aninhados no gerador JSON-to-Dart — cada objeto aninhado gera uma classe Dart separada
- ✅ `toJson()` chama `.toJson()` em propriedades de classes customizadas (objetos aninhados)
- ✅ `fromJson()` chama `NomeClasse.fromJson(json['key'] as Map<String, dynamic>)` em propriedades de classes customizadas

### Fixed
- 🐛 A lista de parâmetros do `copyWith` agora inclui corretamente a vírgula final (trailing comma)

---

> Also available in [English](CHANGELOG.md)
