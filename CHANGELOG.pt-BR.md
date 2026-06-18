# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto segue [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-18

### Added
- ✨ Gerador de métodos Dart (copyWith, toJson, fromJson, toString)
- ✨ Gerador de barrel files (index.dart com 3 estilos)
- 🎨 Dialog interativo para escolher métodos
- 🎨 Dialog configurável para barrel files
- 📚 Documentação completa
- 🧪 Exemplos funcionando
- ⌨️ Atalhos de teclado (Ctrl+Alt+D, Ctrl+Alt+J, Ctrl+Alt+B no Windows/Linux; Cmd+Alt+D, Cmd+Alt+J, Cmd+Alt+B no macOS)
- ✅ Suporte a tipos anuláveis
- ✅ Suporte a diretórios recursivos
- 🏗️ Suporte a objetos JSON aninhados no gerador JSON-to-Dart — cada objeto aninhado gera uma classe Dart separada
- ✅ `toJson()` chama `.toJson()` em propriedades de classes customizadas (objetos aninhados)
- ✅ `fromJson()` chama `NomeClasse.fromJson(json['key'] as Map<String, dynamic>)` em propriedades de classes customizadas

---

> Also available in [English](CHANGELOG.md)
