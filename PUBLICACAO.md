# 📤 Guia de Publicação - GitHub & JetBrains Marketplace

## Parte 1: Criar Repositório no GitHub

### Passo 1: Criar repositório na web
1. Vá para [github.com/new](https://github.com/new)
2. **Repository name:** `dart-data-toolkit`
3. **Description:** "A powerful Android Studio/IntelliJ plugin for generating Dart data class methods and barrel files"
4. **Visibility:** Public ✅
5. **Initialize with:** None (você já tem tudo pronto)
6. Clique em "Create repository"

### Passo 2: Fazer push do código local

```bash
# Navegar para o diretório do repositório
cd dart-data-plugin-repo

# Inicializar git (se ainda não foi feito)
git init
git add .
git commit -m "Initial commit: Dart Data Toolkit plugin"

# Adicionar remote (substitua seu-usuario pelo seu)
git remote add origin https://github.com/seu-usuario/dart-data-toolkit.git

# Push para main
git branch -M main
git push -u origin main
```

### Passo 3: Adicionar tópicos ao repositório
1. Vá para Settings → About
2. Adicione tópicos:
   - `dart`
   - `flutter`
   - `plugin`
   - `android-studio`
   - `intellij-plugin`
   - `code-generation`

### Passo 4: Habilitar GitHub Pages (opcional)
1. Settings → Pages
2. Source: Deploy from branch
3. Branch: main, folder: /docs
4. Save

---

## Parte 2: Preparar para JetBrains Marketplace

### Passo 1: Atualizar Informações no Plugin

**Arquivo:** `build.gradle.kts`

```kotlin
intellij {
    version.set("2023.3.1")
    type.set("IC")
    plugins.set(listOf("Dart"))
}

tasks {
    patchPluginXml {
        changeNotes.set("""
            v1.0.0: Initial release with:
            - Data class method generation (copyWith, toJson, fromJson, toString)
            - Barrel file generation with 3 organization styles
            - Interactive dialogs for configuration
        """.trimIndent())
        
        // Até qual versão é compatível
        untilBuild.set("")
    }
}
```

### Passo 2: Gerar Plugin Build

```bash
# Compilar
gradle clean build

# Criar plugin distribuível
gradle buildPlugin

# Resultado: build/distributions/dart-data-toolkit-1.0.0.zip
```

### Passo 3: Testar Localmente (Importante!)

```bash
# Testar com IDE de teste
gradle runIde

# Verificar se funciona:
# 1. Ctrl+Alt+D - gera métodos
# 2. Ctrl+Alt+B - gera barrels
```

---

## Parte 3: Publicar no JetBrains Marketplace

### Passo 1: Criar Conta
1. Vá para [plugins.jetbrains.com](https://plugins.jetbrains.com)
2. Faça login ou crie uma conta
3. Vá para seu perfil → My Plugins

### Passo 2: Submeter Plugin

1. Clique em "Add plugin"
2. Preencha as informações:

**Nome:** Dart Data Toolkit

**Resumo:** Generate copyWith, toJson, fromJson, toString and barrel files for Dart

**Descrição:**
```
A powerful Android Studio and IntelliJ IDEA plugin that automatically generates:

✨ Dart Data Class Methods:
- copyWith() - Create copies with modified fields
- toJson() - Serialize to JSON
- fromJson() - Deserialize from JSON  
- toString() - Formatted string representation

📂 Barrel Files:
- Auto-generate index.dart files
- 3 organization styles (simple, organized, documented)
- Customizable with recursive support

🎯 Perfect for:
- Flutter/Dart projects
- Clean architecture
- API integration
- Code generation automation

Features:
✅ Interactive UI for method selection
✅ Support for nullable types
✅ Keyboard shortcuts (Ctrl+Alt+D / Ctrl+Alt+B)
✅ Well documented with examples
```

**Palavras-chave:**
- dart
- flutter
- code-generation
- plugin
- barrel-files
- android-studio

**Changelog:**
```
v1.0.0 - Initial release:
- Dart data class method generation
- Barrel file generation with 3 styles
- Interactive configuration dialogs
- Support for nullable types
- Keyboard shortcuts
```

**Link do repositório GitHub:**
```
https://github.com/seu-usuario/dart-data-toolkit
```

**Screenshots:** (Adicionar prints mostrando as features)

### Passo 3: Upload do Plugin

1. Selecione o arquivo: `build/distributions/dart-data-toolkit-1.0.0.zip`
2. IDE compatível:
   - ✅ Android Studio 2023.1+
   - ✅ IntelliJ IDEA 2023.1+
3. Clique em "Upload"

### Passo 4: Aguardar Revisão

- ⏱️ Tempo estimado: 2-7 dias
- 📧 Você receberá email quando for aprovado
- ✅ Depois estará disponível para download direto

---

## Parte 4: Manutenção do Repositório

### Releases e Tags

Sempre que lançar uma versão:

```bash
# Criar tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag
git push origin v1.0.0

# GitHub criará uma release automaticamente
```

### Atualizar Versão para Próxima

**Arquivo:** `build.gradle.kts`
```kotlin
version = "1.1.0"  // Incrementar
```

**Arquivo:** `CHANGELOG.md`
```markdown
## [1.1.0] - 2024-06-25

### Added
- Nova feature X
- Melhoria Y
```

---

## Checklist de Publicação

### Antes de Publicar no GitHub
- [ ] README.md atualizado
- [ ] LICENSE adicionado (MIT)
- [ ] .gitignore configurado
- [ ] Documentação em /docs
- [ ] Exemplos funcionando
- [ ] CHANGELOG.md atualizado

### Antes de Publicar no JetBrains Marketplace
- [ ] Plugin testado localmente (gradle runIde)
- [ ] Build criado (gradle buildPlugin)
- [ ] Screenshots preparadas
- [ ] Descrição clara e em inglês
- [ ] Repository link adicionado
- [ ] Changelog preenchido
- [ ] Compatibilidade verificada

### Após Publicação
- [ ] GitHub: Adicionar tópicos
- [ ] GitHub: Habilitar discussions
- [ ] GitHub: Adicionar templates (ISSUE_TEMPLATE, PULL_REQUEST_TEMPLATE)
- [ ] Marketplace: Monitorar reviews
- [ ] Marketplace: Responder a feedback
- [ ] Social: Anunciar no Twitter/LinkedIn

---

## Próximas Versões

### v1.1.0
- Sincronizar barrel files
- Atualizar existentes
- Re-exports

### v2.0.0
- @freezed support
- operator == e hashCode
- Tipos aninhados
- Publicar no Marketplace

---

## Troubleshooting

### Build falhando
```bash
gradle clean build
gradle --version  # Verificar versão do Gradle
java -version     # Verificar JDK
```

### Plugin não instala
```
Verifique:
✅ Compatibilidade de IDE
✅ Versão do JDK
✅ Arquivo .zip não corrompido
```

### GitHub Pages não funciona
```
Settings → Pages → Check build status
Certifique-se que /docs/README.md existe
```

---

## Links Úteis

- [JetBrains Plugin Docs](https://plugins.jetbrains.com/docs/intellij/)
- [Marketplace Documentation](https://plugins.jetbrains.com/docs/marketplace/)
- [Kotlin Plugin Dev](https://plugins.jetbrains.com/docs/intellij/kotlin.html)
- [GitHub Pages Guide](https://pages.github.com/)

---

**Pronto! Seu plugin está pronto para o mundo! 🚀**
