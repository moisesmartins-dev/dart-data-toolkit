# Contribuindo com o Dart Data Toolkit

Obrigado por considerar contribuir para este projeto!

## Reportando Bugs

Antes de criar um bug report, verifique se o issue já foi reportado. Se você encontrou um bug:

1. **Use um título claro e descritivo**
2. **Descreva os passos exatos** para reproduzir o problema
3. **Forneça exemplos específicos** para demonstrar os passos
4. **Descreva o comportamento observado** e **o que você esperava**
5. **Inclua screenshots/videos** se possível
6. **Mencione sua versão** do plugin, IDE e SO

## Sugerindo Features

Sugestões de novas features são sempre bem-vindas! Para sugerir uma feature:

1. **Use um título claro e descritivo**
2. **Forneça uma descrição detalhada** da feature sugerida
3. **Liste alguns exemplos** de como seria usada
4. **Explique por que** essa feature seria útil

## Pull Requests

### Preparação
1. Faça um fork do repositório
2. Clone seu fork localmente
3. Crie uma branch para sua feature
   ```bash
   git checkout -b feature/MinhaFeature
   ```

### Desenvolvimento
1. **Siga o style guide do Kotlin**
2. **Adicione testes** quando possível
3. **Atualize a documentação** se necessário
4. **Mantenha commits limpos e descritivos**

### Submissão
1. Faça push para sua branch
   ```bash
   git push origin feature/MinhaFeature
   ```
2. Abra um Pull Request
3. Descreva suas mudanças claramente
4. Referencie issues relacionadas (ex: "Closes #123")

## Style Guides

### Kotlin Style
- Use nomes de variáveis descritivos
- Prefira `val` em vez de `var` quando possível
- Use comments para explicar lógica complexa
- Siga as convenções de naming do Kotlin

### Mensagens de Commit
- Use o imperativo ("add feature" não "added feature")
- Limite a primeira linha a 50 caracteres
- Referencie issues e pull requests generosamente

Exemplo:
```
Add support for @freezed classes

- Analyze @freezed annotation
- Generate appropriate methods for freezed classes
- Add tests for freezed support

Closes #123
```

### Documentação
- Mantenha o README.md atualizado
- Adicione comentários em código complexo
- Documente mudanças no CHANGELOG.md

## Processo de Review

Todos os PRs passam por review antes de serem merged:

1. **Build status** — o PR deve passar em todos os builds
2. **Code review** — pelo menos um maintainer deve revisar
3. **Tests** — test coverage deve ser mantido
4. **Documentação** — docs devem estar atualizadas

## Dúvidas?

- 💬 Discussões: Abra uma Discussion no GitHub
- 🐛 Issues: Sinta-se livre para postar questões como issues

---

**Obrigado por contribuir!**

---

> Also available in [English](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/CONTRIBUTING.md)
