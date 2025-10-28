# Como Executar os Testes - MCP Server

## 🎯 Banco de Dados para Testes

**Você NÃO precisa instalar banco de dados!**

Os testes usam **H2 Database in-memory**:
- ✅ Banco em memória RAM (rápido)
- ✅ Modo PostgreSQL (compatível)
- ✅ Auto-destruído após testes
- ✅ Já incluído no pom.xml

## 📋 Comandos para Executar

### 1. Verificar se Maven está instalado

```bash
mvn -version
```

Se não estiver instalado:

```bash
sudo apt-get update
sudo apt-get install maven
```

### 2. Ir para o projeto

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
```

### 3. Executar os testes

```bash
# Executar TODOS os testes (25 testes)
mvn clean test

# OU executar teste específico
mvn test -Dtest=SecureQueryServiceTest      # 13 testes de segurança
mvn test -Dtest=McpControllerTest            # 7 testes de integração
mvn test -Dtest=McpToolsRegistryTest         # 5 testes de ferramentas

# Com logs detalhados
mvn test -X
```

## 📊 O Que Será Testado

### ✅ SecureQueryServiceTest (13 testes)
```
✓ Valida queries SELECT
✓ Rejeita UPDATE, DELETE, DROP, INSERT, CREATE
✓ Detecta SQL injection
✓ Valida queries com JOIN e WHERE
✓ Testa exceções de segurança
```

### ✅ McpControllerTest (7 testes)
```
✓ GET /mcp retorna info do servidor
✓ POST com initialize
✓ POST com tools/list (4 ferramentas)
✓ POST com ping
✓ Valida JSON-RPC 2.0
✓ Testa erros e parâmetros inválidos
```

### ✅ McpToolsRegistryTest (5 testes)
```
✓ Valida 4 ferramentas registradas
✓ Valida parâmetros obrigatórios
✓ Testa execução de ferramentas
✓ Valida definições
```

## 🗄️ Estrutura do Banco H2

**Schema criado automaticamente:**

```sql
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    age INT,
    country VARCHAR(50)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2),
    status VARCHAR(20),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0
);
```

**Dados inseridos:**
- 5 clientes (João, Maria, Pedro, Ana, Carlos)
- 5 produtos (Laptop, Mouse, Keyboard, Monitor, Chair)
- 5 pedidos

## ✅ Resultado Esperado

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.magacho.aiToSql.service.SecureQueryServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Running com.magacho.aiToSql.controller.McpControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Running com.magacho.aiToSql.tools.McpToolsRegistryTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 🐳 Alternativa: Testar com Banco Real (Opcional)

Se quiser testar com PostgreSQL real via Docker:

```bash
# 1. Iniciar PostgreSQL
docker run --name postgres-test \
  -e POSTGRES_PASSWORD=test123 \
  -e POSTGRES_DB=testdb \
  -p 5432:5432 \
  -d postgres:15

# 2. Aguardar 5 segundos
sleep 5

# 3. Verificar se está rodando
docker exec postgres-test psql -U postgres -c "SELECT version();"

# 4. Executar testes
mvn test

# 5. Parar quando terminar
docker stop postgres-test
docker rm postgres-test
```

Para usar PostgreSQL real, edite `src/test/resources/application-test.properties`:

```properties
# Comentar H2
# spring.datasource.url=jdbc:h2:mem:testdb

# Descomentar PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/testdb
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=test123
```

## 🔍 Verificar Arquivos de Teste

```bash
# Ver estrutura
tree src/test/

# Ver classes de teste
ls -la src/test/java/com/magacho/aiToSql/*/

# Ver configuração
cat src/test/resources/application-test.properties

# Ver schema
cat src/test/resources/test-schema.sql

# Ver dados
cat src/test/resources/test-data.sql
```

## 📈 Gerar Relatório de Cobertura

```bash
# Executar testes com cobertura
mvn clean test jacoco:report

# Abrir relatório no navegador
firefox target/site/jacoco/index.html
# ou
google-chrome target/site/jacoco/index.html
```

## 🐛 Troubleshooting

### Erro: "mvn: command not found"
```bash
sudo apt-get install maven
```

### Erro: "JAVA_HOME not set"
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### Erro: "Connection refused"
```bash
# Isso é normal! Os testes usam H2 in-memory, não precisam de conexão externa
```

### Ver logs detalhados
```bash
mvn test -X
```

## 🎉 Pronto para Testar!

**Execute agora:**

```bash
cd /home/flavio.magacho/Dropbox/dev/PromptToSql
mvn clean test
```

**Aguarde ~30 segundos** para:
- Baixar dependências (primeira vez)
- Compilar código
- Executar 25 testes
- Ver resultado: **BUILD SUCCESS** ✅
