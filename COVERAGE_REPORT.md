# Relatório de Cobertura de Testes

## 📊 Como Gerar o Relatório

### Método 1: Script Automático (Recomendado)

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
./gerar-relatorio-cobertura.sh
```

### Método 2: Comando Maven Direto

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
mvn clean test jacoco:report
```

## 🌐 Abrir o Relatório

Após gerar, abra o relatório HTML:

```bash
# Opção 1: Firefox
firefox target/site/jacoco/index.html

# Opção 2: Chrome
google-chrome target/site/jacoco/index.html

# Opção 3: Navegador padrão
xdg-open target/site/jacoco/index.html
```

## 📁 Localização dos Arquivos

```
target/site/jacoco/
├── index.html          # Relatório principal (abra este!)
├── jacoco.xml          # Formato XML (para CI/CD)
├── jacoco.csv          # Formato CSV (para análise)
└── com.magacho.aiToSql/
    ├── index.html      # Cobertura por pacote
    ├── controller/
    ├── service/
    ├── tools/
    └── ...
```

## 📈 O Que o Relatório Mostra

### Página Principal (index.html)

O relatório mostra métricas detalhadas:

1. **Cobertura por Pacote**
   - `com.magacho.aiToSql.service`
   - `com.magacho.aiToSql.controller`
   - `com.magacho.aiToSql.tools`
   - etc.

2. **Métricas de Cobertura**
   - **Missed Instructions** (instruções não testadas)
   - **Cov. (Coverage)** - Porcentagem coberta
   - **Missed Branches** (branches não testados)
   - **Cxty (Complexity)** - Complexidade ciclomática
   - **Missed Lines** (linhas não testadas)
   - **Methods** (métodos cobertos/total)
   - **Classes** (classes cobertas/total)

3. **Código Colorido**
   - 🟢 **Verde** - Código coberto por testes
   - 🟡 **Amarelo** - Parcialmente coberto
   - 🔴 **Vermelho** - Não coberto

### Navegação Detalhada

1. **Clique em um pacote** → Ver classes do pacote
2. **Clique em uma classe** → Ver código-fonte com cobertura
3. **Passe o mouse** → Ver número de execuções

## 🎯 Metas de Cobertura

O projeto está configurado com meta mínima de **80%**:

```xml
<limit>
    <counter>LINE</counter>
    <value>COVEREDRATIO</value>
    <minimum>0.80</minimum>
</limit>
```

### Cobertura Atual Esperada

| Componente | Cobertura Estimada |
|------------|-------------------|
| **SecureQueryService** | ~95% |
| **McpController** | ~90% |
| **McpToolsRegistry** | ~85% |
| **SchemaIntrospectionService** | ~60%* |
| **TableDetailsService** | ~60%* |
| **TriggerService** | ~50%* |
| **TOTAL GERAL** | **~75-85%** |

\* *Serviços de infraestrutura com menos testes unitários (testados via integração)*

## 📊 Exemplo Visual do Relatório

### Estrutura da Página

```
┌─────────────────────────────────────────────────────┐
│ JaCoCo Coverage Report                              │
├─────────────────────────────────────────────────────┤
│                                                     │
│ Package                    Coverage   Complexity    │
│ ─────────────────────────  ─────────  ──────────   │
│ ├─ com.magacho.aiToSql    ██████░░ 85%   120      │
│ │  ├─ controller           ████████ 90%    45      │
│ │  ├─ service              ███████░ 82%    78      │
│ │  ├─ tools                ████████ 88%    32      │
│ │  ├─ dto                  ████████ 95%    12      │
│ │  ├─ jsonrpc              ██████░░ 75%    18      │
│ │  └─ config               ████████ 100%    8      │
│                                                     │
│ Total: 25 classes, 156 methods                     │
│ Coverage: 85.3%                                     │
└─────────────────────────────────────────────────────┘
```

### Cores no Código-fonte

```java
@Service
public class SecureQueryService {
    
    // 🟢 VERDE = Coberto (linha executada pelos testes)
    public boolean validateQuery(String query) {
        if (query == null) return false;
        return SELECT_PATTERN.matcher(query).find();
    }
    
    // 🔴 VERMELHO = Não coberto (linha nunca executada)
    private void unusedMethod() {
        System.out.println("Never called");
    }
    
    // 🟡 AMARELO = Parcialmente coberto (alguns branches não testados)
    public String process(String input) {
        if (input != null) {  // Branch testado
            return input.toUpperCase();
        } else {
            return "";  // Branch NÃO testado
        }
    }
}
```

## 🔍 Analisando o Relatório

### 1. Verificar Cobertura Geral

Na página principal, procure:
- **Total Coverage**: Deve ser > 80%
- **Pacotes em vermelho**: Precisam mais testes

### 2. Identificar Código Não Testado

1. Clique no pacote com menor cobertura
2. Clique na classe com linhas vermelhas
3. Veja as linhas não cobertas
4. Adicione testes para essas linhas

### 3. Melhorar Cobertura

**Exemplo: Adicionar teste para branch não coberto**

```java
// Código com branch não testado
public String formatName(String name) {
    if (name == null) return "Unknown";  // ← Branch não testado
    return name.toUpperCase();
}

// Adicionar teste
@Test
void testFormatNameWithNull() {
    String result = service.formatName(null);
    assertThat(result).isEqualTo("Unknown");
}
```

## 📤 Exportar Relatório

### Formato XML (para CI/CD)

```bash
# Relatório XML está em:
cat target/site/jacoco/jacoco.xml
```

**Usar em GitHub Actions:**

```yaml
- name: Generate coverage report
  run: mvn test jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: ./target/site/jacoco/jacoco.xml
```

### Formato CSV (para Excel)

```bash
# Relatório CSV está em:
libreoffice target/site/jacoco/jacoco.csv
```

## 🎨 Personalizar Relatório

Editar `pom.xml` para customizar:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <!-- Excluir classes de configuração -->
        <excludes>
            <exclude>**/*Config.class</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
        
        <!-- Mudar meta de cobertura -->
        <rules>
            <rule>
                <limits>
                    <limit>
                        <minimum>0.90</minimum> <!-- 90% -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

## 🚀 Integração Contínua

### GitHub Actions

```yaml
name: Test Coverage

on: [push, pull_request]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests with coverage
        run: mvn test jacoco:report
      - name: Upload to Codecov
        uses: codecov/codecov-action@v3
```

### SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=mcp-server \
  -Dsonar.host.url=http://localhost:9000
```

## 📝 Comandos Úteis

```bash
# Gerar relatório
mvn test jacoco:report

# Verificar meta de cobertura
mvn test jacoco:check

# Limpar e gerar
mvn clean test jacoco:report

# Sem executar testes (usar cache)
mvn jacoco:report

# Gerar + abrir navegador
mvn test jacoco:report && xdg-open target/site/jacoco/index.html
```

## 🎯 Resumo Executivo

**Para gerar e visualizar a cobertura:**

```bash
# 1. Gerar relatório
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
mvn clean test jacoco:report

# 2. Abrir no navegador
firefox target/site/jacoco/index.html
```

**O relatório mostrará:**
- ✅ Porcentagem de código coberto por testes
- 🎨 Código-fonte colorido (verde/amarelo/vermelho)
- 📊 Métricas por pacote, classe e método
- 🔍 Linhas e branches não testados
- 📈 Tendências de cobertura

**Meta atual:** 80% de cobertura  
**Cobertura esperada:** 75-85%

---

**Execute agora e explore o relatório interativo!** 🚀
