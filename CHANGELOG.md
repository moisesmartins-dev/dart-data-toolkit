# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-18

### Added
- ✨ Dart method generator (copyWith, toJson, fromJson, toString)
- ✨ Barrel file generator (index.dart with 3 styles)
- 🎨 Interactive dialog to choose methods
- 🎨 Configurable dialog for barrel files
- 📚 Complete documentation
- 🧪 Working examples
- ⌨️ Keyboard shortcuts (Ctrl+Alt+D, Ctrl+Alt+J, Ctrl+Alt+B on Windows/Linux; Cmd+Alt+D, Cmd+Alt+J, Cmd+Alt+B on macOS)
- ✅ Nullable type support
- ✅ Recursive directory support
- 🏗️ Nested JSON object support in JSON-to-Dart generator — each nested object generates a separate Dart class
- ✅ `toJson()` calls `.toJson()` on custom class properties (nested objects)
- ✅ `fromJson()` calls `ClassName.fromJson(json['key'] as Map<String, dynamic>)` on custom class properties

---

> Also available in [Português (pt-BR)](CHANGELOG.pt-BR.md)
