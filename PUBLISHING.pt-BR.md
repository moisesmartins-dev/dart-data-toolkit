# Guia de Publicação — GitHub & JetBrains Marketplace

## Parte 1: Criar Repositório no GitHub

### Passo 1: Criar repositório na web

1. Acesse [github.com/new](https://github.com/new)
2. **Repository name:** `dart-data-toolkit`
3. **Description:** `Generate copyWith, toJson, fromJson, toString and barrel files for Dart`
4. **Visibility:** Public
5. **Initialize with:** None (o projeto já está pronto)
6. Clique em "Create repository"

### Passo 2: Push do código local

```bash
git remote add origin https://github.com/moisesmartins-dev/dart-data-toolkit.git
git branch -M main
git push -u origin main
```

### Passo 3: Configurar o repositório

Vá em **Settings → About** e adicione os tópicos:

```
dart  flutter  intellij-plugin  android-studio  code-generation  barrel-file
```

---

## Parte 2: Preparar para o JetBrains Marketplace

### Passo 1: Gerar o build

```bash
./gradlew clean buildPlugin
# Resultado: build/distributions/dart-data-plugin-1.0.0.zip
```

### Passo 2: Testar localmente

```bash
./gradlew runIde
```

Verificar:
- `Ctrl+Alt+D` — abre o diálogo de geração de métodos
- `Ctrl+Alt+J` — abre o diálogo de JSON para Dart
- `Ctrl+Alt+B` — abre o diálogo de barrel file
- `Alt+Insert` — exibe as ações individuais no menu Generate

---

## Parte 3: Publicar no JetBrains Marketplace

### Passo 1: Criar conta

1. Acesse [plugins.jetbrains.com](https://plugins.jetbrains.com)
2. Faça login ou crie uma conta JetBrains
3. Vá em **My Plugins → Add plugin**

### Passo 2: Preencher as informações

**Nome do plugin**
```
Dart Data Toolkit
```
> Nome com 1–4 palavras, sem termos genéricos como "Plugin" ou "Tool". ✅

**Resumo (40 caracteres — aparece nos cards do Marketplace)**
```
Dart code generator for Flutter devs
```

**Descrição**

Use a descrição já configurada no `plugin.xml`. Ela segue as boas práticas:
- Sem adjetivos de marketing ("poderoso", "leve", "simples")
- Estruturada em listas com os benefícios reais
- Inclui atalhos de teclado
- Menciona suporte a tipos nullable e genéricos

**Tags**

Selecione ao menos uma tag relevante. Sugestões:
- `Code editing`
- `Code tools`

> Não selecione categorias que o plugin não se encaixa.

**Links do vendor** (configurados no `plugin.xml`)
- Website: `https://github.com/moisesmartins-dev`
- Source code: `https://github.com/moisesmartins-dev/dart-data-toolkit`
- Bug tracker: `https://github.com/moisesmartins-dev/dart-data-toolkit/issues`

**Licença:** MIT (obrigatório para publicação)

### Passo 3: Screenshots

- Resolução mínima: **1200 × 760 px**
- Mostre a interface real do IntelliJ/Android Studio com o plugin em uso
- Sugestões de capturas:
  1. Diálogo de seleção de métodos (`Ctrl+Alt+D`)
  2. Resultado gerado no editor (antes e depois)
  3. Diálogo de JSON para Dart com JSON colado
  4. Diálogo de barrel file com arquivos selecionados
- Sem fundos de desktop, janelas de browser ou marcas de terceiros

### Passo 4: Upload e envio

1. Faça upload do arquivo: `build/distributions/dart-data-plugin-1.0.0.zip`
2. Preencha todos os campos e clique em **Submit**
3. Aguarde a revisão: estimativa de 2–7 dias úteis
4. Você receberá um e-mail com o resultado

---

## Parte 4: Manutenção

### Criar tag e release

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

O GitHub cria a release automaticamente a partir da tag.

### Atualizar versão

Em `build.gradle.kts`:
```kotlin
version = "1.1.0"
```

Em `CHANGELOG.md` e `CHANGELOG.pt-BR.md`, adicione a nova entrada seguindo o formato existente.

Rebuild e reenvie o `.zip` atualizado no painel do Marketplace.

---

## Checklist de Publicação

### GitHub
- [ ] Remote adicionado e push feito
- [ ] Tópicos configurados no repositório
- [ ] LICENSE presente (MIT)
- [ ] README em inglês como arquivo principal

### JetBrains Marketplace
- [ ] Plugin testado localmente (`./gradlew runIde`)
- [ ] Build gerado (`./gradlew buildPlugin`)
- [ ] Screenshots preparadas (mín. 1200×760 px)
- [ ] Resumo de 40 caracteres escrito
- [ ] Links do vendor preenchidos (website, source, bug tracker)
- [ ] Licença informada
- [ ] Tags selecionadas

### Após publicação
- [ ] Monitorar reviews e responder a feedback no Marketplace
- [ ] Habilitar Discussions no GitHub para suporte da comunidade

---

## Troubleshooting

**Build falhando**
```bash
./gradlew clean build
./gradlew --version
java -version  # requer JDK 17+
```

**Plugin não instala na IDE**
- Verifique a compatibilidade: `since-build="233"` até `until-build="262.*"`
- Confirme que o `.zip` não está corrompido
- Tente instalar via **File → Settings → Plugins → gear → Install from Disk**

---

## Links Úteis

- [JetBrains Marketplace — Boas práticas de listagem](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html)
- [Documentação de plugins IntelliJ](https://plugins.jetbrains.com/docs/intellij/)
- [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

---

> Also available in [English](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/PUBLISHING.md)
