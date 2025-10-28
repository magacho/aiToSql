# 📊 Rastreamento de Cobertura de Testes

## 🎯 Objetivo

Este projeto **não possui cobertura mínima obrigatória**. O foco é **rastrear a evolução** da cobertura ao longo das releases.

## 📈 Histórico de Cobertura

O histórico completo de cobertura está documentado em:
- **[RELEASE_HISTORY.md](RELEASE_HISTORY.md)** - Histórico completo de releases
- **[COVERAGE_REPORT.md](COVERAGE_REPORT.md)** - Relatório detalhado atual

## 🔄 Como Funciona

### Em Cada Commit (CI)
```bash
# Testes executados automaticamente
mvn clean test

# Relatório gerado (sem validação mínima)
mvn jacoco:report
```

### Em Cada Release (REL-X.X.X)
Quando você cria uma tag de release, o workflow automático:

1. ✅ **Executa todos os testes**
2. �� **Gera relatório de cobertura**
3. 📝 **Registra métricas no RELEASE_HISTORY.md**:
   - Porcentagem de cobertura
   - Total de testes
   - Taxa de sucesso
4. 📦 **Anexa relatório JaCoCo à release**
5. 🚀 **Publica release no GitHub**

## 🚀 Criar uma Release

```bash
# 1. Criar tag de release
git tag -a REL-0.0.1 -m "Release 0.0.1 - Primeira versão estável"

# 2. Enviar para GitHub
git push origin REL-0.0.1

# 3. Aguardar workflow automático
# - Testes serão executados
# - Cobertura será calculada
# - Release será criada automaticamente
```

## 📊 Interpretar Métricas

### Status da Cobertura

| Cobertura | Status | Significado |
|-----------|--------|-------------|
| ≥ 90% | ✅ Excelente | Código muito bem testado |
| 80-89% | 🟢 Bom | Boa cobertura de testes |
| 70-79% | 🟡 Aceitável | Considere adicionar testes |
| < 70% | ⚠️ Atenção | Recomendado adicionar testes |

**Nota**: Estes são apenas **indicadores visuais**. Nenhuma release será bloqueada por cobertura baixa.

## 📦 Artefatos de Cobertura

Cada release inclui:

1. **JAR da aplicação**: `aiToSql-X.X.X.jar`
2. **Relatório JaCoCo**: `jacoco-X.X.X.zip`
   - HTML navegável
   - CSV com dados brutos
   - XML para ferramentas externas

## 🔍 Visualizar Cobertura Localmente

```bash
# Gerar relatório
mvn clean test jacoco:report

# Abrir no navegador
firefox target/site/jacoco/index.html
```

## 📈 Evolução Esperada

```
v0.0.1 → 75%   (Base inicial)
v0.0.2 → 80%   (+5% - Mais testes de integração)
v0.1.0 → 85%   (+5% - Testes de edge cases)
v1.0.0 → 90%+  (Release estável com cobertura completa)
```

## 🎯 Benefícios

✅ **Transparência**: Métricas visíveis em cada release  
✅ **Histórico**: Acompanhar evolução ao longo do tempo  
✅ **Flexibilidade**: Sem bloqueios por cobertura baixa  
✅ **Documentação**: Relatórios completos disponíveis  

## 🛠️ Ferramentas Utilizadas

- **JaCoCo**: Análise de cobertura
- **Maven Surefire**: Execução de testes
- **GitHub Actions**: Automação CI/CD
- **Codecov**: Visualização de tendências (opcional)

## 📚 Documentação Relacionada

- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Guia de testes
- [RELEASE_HISTORY.md](RELEASE_HISTORY.md) - Histórico de releases
- [COVERAGE_REPORT.md](COVERAGE_REPORT.md) - Relatório atual
- [COMO_FAZER_RELEASE.md](COMO_FAZER_RELEASE.md) - Processo de release

---

**Última atualização**: 28 de Outubro de 2024  
**Filosofia**: Rastrear, não bloquear. Melhorar continuamente. 📈
