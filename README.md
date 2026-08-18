# Order Management API

API REST para gerenciamento de pedidos desenvolvida com **Java 21 e Spring Boot 3.3**.

O projeto implementa autenticação com **JWT**, controle de acesso por roles e um fluxo de estados para os pedidos, garantindo que apenas transições válidas sejam realizadas.

## Tecnologias

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

## Funcionalidades

- Cadastro e autenticação de usuários
- Autenticação baseada em JWT
- Controle de acesso com `ROLE_USER` e `ROLE_ADMIN`
- Criação e consulta de pedidos
- Paginação de resultados
- Atualização controlada do status dos pedidos
- Validação de dados e tratamento centralizado de exceções
- Documentação da API com Swagger / OpenAPI
- Health check com Spring Boot Actuator
- Versionamento do banco de dados com Flyway

## Fluxo dos pedidos

Os pedidos seguem um fluxo de estados definido pela regra de negócio:

```text
CREATED → PAID → PROCESSING → SHIPPED → DELIVERED
   │        │          │
   └────────┴──────────┴──→ CANCELED
