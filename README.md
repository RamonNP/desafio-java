
# Desafio Java

## Descrição
Um serviço de gerenciamento de pedidos desenvolvido em Java com Spring Boot, projetado para integrar sistemas externos A e B via Kafka. Gerencia criação, processamento e consulta de pedidos com alta capacidade (150-200 mil pedidos/dia), garantindo consistência, idempotência e alta disponibilidade.

## Tecnologias
- **Java 17**
- **Spring Boot 3.4.4**
- **Kafka**: Integração assíncrona com sistemas externos
- **PostgreSQL**: Persistência de dados
- **Redis**: Cache e idempotência
- **MapStruct**: Mapeamento de objetos
- **Lombok**: Redução de código boilerplate
- **Docker Compose**: Configuração do ambiente

## Pré-requisitos
- Java 17
- Maven 3.8+
- Docker e Docker Compose
- Git

## Instruções de Configuração
1. Clone o repositório:
   ```bash
   git clone https://github.com/RamonNP/desafio-java.git
   cd desafio-java
Inicie os serviços com Docker Compose:
bash
docker-compose up -d
Compile e execute a aplicação:
bash
mvn clean install
mvn spring-boot:run
Endpoints da API
POST /orders: Criar um pedido
json
{
"orderId": "123",
"customerId": "cust-001",
"items": [
{
"productId": "prod-001",
"quantity": 2,
"price": 50.00
}
]
}
GET /orders/{id}: Consultar um pedido por ID
Exemplo: GET /orders/123
GET /orders/{id}/status: Verificar status do pedido
Exemplo: GET /orders/123/status
Fluxo do Sistema
O sistema segue um fluxo assíncrono baseado em eventos com Kafka e CQRS para separação de comandos e consultas. Abaixo está o fluxo principal com diagramas ilustrativos:
Criação de Pedido:
O cliente envia um POST /orders com os dados do pedido.
O OrderController valida a requisição e converte para um CreateOrderCommand.
O CreateOrderCommandHandler verifica idempotência usando Redis (chave com TTL de 24h).
Um evento OrderProcessingEvent é publicado no tópico Kafka orders-to-process.
Diagrama:
Fluxo de Criação de Pedido
Processamento de Pedido:
O OrderProcessingConsumer consome eventos do tópico orders-to-process.
Calcula o valor total dos itens e publica um OrderProcessedEvent no tópico orders-processed.
Em caso de erro, o evento é enviado para a DLQ (orders-to-process-dlq).
Persistência:
O OrderSavingConsumer consome eventos do tópico orders-processed.
Persiste o pedido no PostgreSQL com status PROCESSED usando o OrderWriteRepository.
Em caso de erro, o evento é enviado para a DLQ (orders-processed-dlq).
Consulta de Pedido:
O cliente consulta via GET /orders/{id} ou GET /orders/{id}/status.
O OrderApplicationService verifica o cache Redis.
Se não estiver no cache, o FindOrderByIdQueryHandler busca no PostgreSQL.
O status é retornado como COMPLETED (se persistido) ou PROCESSING (se apenas no Redis).
Diagrama:
Fluxo de Consulta de Status
Integrações Externas
Sistema A: Envia pedidos para o tópico Kafka orders-to-process.
Sistema B: Consulta pedidos via GET /orders/{id}.
Estrutura do Projeto
desafio-java/
├── src/
│   ├── main/
│   │   ├── java/br/com/desafiojava/
│   │   │   ├── api/                # Controladores REST
│   │   │   ├── application/        # Serviços de aplicação
│   │   │   ├── command/            # Comandos CQRS
│   │   │   ├── common/exception/   # Tratamento de erros
│   │   │   ├── config/             # Configurações de Kafka, Redis, DB
│   │   │   ├── domain/             # Entidades e DTOs
│   │   │   ├── event/              # Eventos Kafka
│   │   │   ├── mapper/             # Mapeadores MapStruct
│   │   │   ├── query/              # Consultas CQRS
│   │   │   ├── repository/         # Repositórios JPA
│   │   ├── resources/
│   │   │   ├── application.properties # Configurações
├── desenho/                        # Diagramas do fluxo
├── docker-compose.yml              # Configuração Docker
├── pom.xml                         # Dependências Maven
├── LICENSE                         # Licença Apache 2.0
Configurações
Definidas em src/main/resources/application.properties:
Kafka: kafka.bootstrap-servers=kafka:29092
PostgreSQL: spring.datasource.url=jdbc:postgresql://postgres:5432/desafio_db
Redis: spring.redis.host=redis
Executando Testes
bash
mvn test
Monitoramento
AKHQ: Interface de gerenciamento do Kafka em http://localhost:9090
Logs: Configurados com SLF4J, visíveis no console ou em arquivos
Escalabilidade e Performance
Kafka: Suporta alta volumetria com particionamento e replicação
Redis: Cache de consultas e idempotência com TTL
PostgreSQL: Usa índices e transações ACID para consistência
HikariCP: Pool de conexões otimizado
Contribuição
Faça um fork do repositório
Crie uma branch: git checkout -b feature/nova-funcionalidade
Faça commit das alterações: git commit -m 'Adiciona nova funcionalidade'
Envie para a branch: git push origin feature/nova-funcionalidade
Abra um Pull Request

### Alterações Realizadas
1. **Referência às Imagens**:
    - As imagens são referenciadas com o caminho relativo `./desenho/create-order.png` e `./desenho/status-order.png`. O GitHub reconhece caminhos relativos a partir da raiz do repositório, então isso deve funcionar se as imagens estiverem na pasta `desenho/`.
    - Usei a sintaxe `![Alt text](path)` para garantir que as imagens sejam renderizadas inline no README.

2. **Organização do Fluxo**:
    - Mantive a seção "Fluxo do Sistema" com as descrições detalhadas de cada etapa (criação, processamento, persistência, consulta).
    - Posicionei as imagens imediatamente após as descrições correspondentes (criação e consulta) com a legenda "Diagrama" para clareza.
    - As imagens são exibidas logo abaixo de suas respectivas explicações, facilitando a associação entre texto e visual.

3. **Garantia de Visualização no GitHub**:
    - O caminho `./desenho/` é compatível com a forma como o GitHub renderiza arquivos markdown.
    - Assegurei que a formatação markdown está correta para evitar quebras de renderização.

### Instruções Adicionais
- **Confirme a Presença das Imagens**: Certifique-se de que os arquivos `create-order.png` e `status-order.png` estão realmente na pasta `desenho/` do repositório e foram enviados (commit e push) para o GitHub. Caso contrário, o GitHub mostrará um link quebrado.
- **Nomenclatura Exata**: Verifique se os nomes dos arquivos estão exatamente como `create-order.png` e `status-order.png` (sensível a maiúsculas e minúsculas).
- **Teste Localmente**: Antes de subir para o GitHub, você pode visualizar o README localmente usando um visualizador markdown (como o do VS Code ou ferramentas online) para confirmar que as referências estão corretas.
- **Push para o GitHub**:
  ```bash
  git add README.md desenho/create-order.png desenho/status-order.png
  git commit -m "Adiciona imagens ao README"
  git push origin main
