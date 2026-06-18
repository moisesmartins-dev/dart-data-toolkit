# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- ✨ Dart method generator (copyWith, toJson, fromJson, toString)
- ✨ Barrel file generator (index.dart with 3 styles)
- 🎨 Interactive dialog to choose methods
- 🎨 Configurable dialog for barrel files
- 📚 Complete documentation
- 🧪 Working examples
- ⌨️ Keyboard shortcuts (Ctrl+Alt+D and Ctrl+Alt+B)
- ✅ Nullable type support
- ✅ Recursive directory support
- 🏗️ Nested JSON object support in JSON-to-Dart generator — each nested object generates a separate Dart class
- ✅ `toJson()` calls `.toJson()` on custom class properties (nested objects)
- ✅ `fromJson()` calls `ClassName.fromJson(json['key'] as Map<String, dynamic>)` on custom class properties

### Fixed
- 🐛 `copyWith` parameter list now correctly includes a trailing comma

---

> Also available in [Português (pt-BR)](CHANGELOG.pt-BR.md)
