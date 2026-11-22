# StreamStats

API em Java + Spring Boot para organizar as mídias que você assiste:
<br> Cada usuário monta sua própria lista de séries, filmes e animes com status como:
**quero assistir**, **assistindo**, **assistido**, assim como o progresso por episódio e a nota de 1 a 10.
O sistema também calcula quanto tempo de tela total o usuário já assistiu.

Trabalho de Programação Orientada a Objetos: Um sistema em Java com uma única framework (Spring Boot),
CRUDs, autenticação e banco de dados relacional modelado.

## Tecnologias Usadas

- Java 17 e Maven
- Spring Boot 3.3 (Web, Data JPA, Validation)
- MySQL 8
- BCrypt (`spring-security-crypto`) para hash de senha

## Como rodar

### Com MySQL

1. Primeiro ter o Banco de dados MySQL rodando na porta `localhost:3306`.
2. Fizemos implementações pelas variáveis de ambiente que tbm foram usadas para `DB_URL`, `DB_USER` e `DB_PASSWORD`. Caso queira, também pode ajustar um usuário e senha que fica no `src/main/resources/application.properties`.
3. O banco de dados `streamstats` e as suas tabelas são gerados na primeira execução do projeto. O DDL também se encontra em `docs/schema.sql`.
4. Execute:

```bash
mvn spring-boot:run
```

## Front End

Quando a aplicação tiver já rodando no local que desejar, abra **http://localhost:8080/**. A interface usada foi um HTML, CSS e JavaScript puro todos encontrados em `src/main/resources/static`, dada pelo próprio Spring Boot e apontando para a API.

### Apresenta:

- Cadastro e login.
- Um contador de tempo de tela (dias : horas : minutos) e nota média.
- Abas com o  **Assistindo**, **Quero assistir** e **Assistidos**, com seus respectivos pôsteres, barras de progresso e notas.
- As Janela com detalhes para trocar o status, ajustar o progresso por episódio e dar nota de 1 a 10.
- O **Catálogo** com busca, ordenação e CRUD de mídias como o (cadastrar, editar, excluir).
  Caso não possuir capa, a interface vai gerar um pôster colorido com o título.
- As fontes usadas no projeto ficam em `static/fonts`, então tudo funciona sem internet.

## Estrutura

```
src/main/java/com/example/streamstats
├── controllers/   os endpoints REST (Auth, Usuario, Midia, ItemLista)
├── services/      as regras de negócio e sistematização
├── repositories/  Spring Data JPA
├── entities/      as entidades Usuario, Midia, ItemLista + enums (TipoMidia, StatusLista)
├── dto/           os objetos de entrada e saída da API
├── exceptions/    as exceções de negócio + tratamento global de erros
├── config/        BCrypt e CORS
└── util/          e a conversão de minutos em dias/horas/minutos
```

## Modelo de dados

- **usuarios**: id, nome, email (único), senha_hash, data_criacao
- **midias**: id, titulo, tipo (SERIE, FILME, ANIME), genero, ano, total_episodios, duracao_media_episodio_min, sinopse, capa_url
- **itens_lista**: id, usuario_id, midia_id, status, episodios_assistidos, nota, data_atualizacao
  (único por usuário + mídia)

`midias` é um catálogo global; o que fica pessoal são os (status, progresso, nota) que ficam em `itens_lista`.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| POST | `/auth/register` | Cadastra usuário (senha vira hash BCrypt) |
| POST | `/auth/login` | Valida e-mail e senha |
| GET | `/usuarios` | Lista usuários |
| GET / PUT / DELETE | `/usuarios/{id}` | Busca, renomeia e remove |
| GET | `/midias?titulo=&tipo=` | Lista e busca no catálogo |
| GET / POST | `/midias`, `/midias/{id}` | Consulta e cadastra mídia |
| PUT / DELETE | `/midias/{id}` | Atualiza e remove mídia |
| GET | `/lista/usuario/{id}?status=` | Lista do usuário, filtrando por status |
| POST | `/lista` | Adiciona mídia à lista |
| PATCH | `/lista/{id}/status` | Troca o status |
| PATCH | `/lista/{id}/progresso` | Atualiza episódios assistidos |
| PATCH | `/lista/{id}/nota` | Dá uma nota de 1 a 10 |
| DELETE | `/lista/{id}` | Remove da lista |
| GET | `/lista/usuario/{id}/tempo-total` | Dá o Tempo de tela em dias, horas e minutos |
| GET | `/lista/usuario/{id}/estatisticas` | Usado para dar contagem por status, nota média e tempo total |

### Exemplo de uso

```bash
# Usado para cadastrar e logar
curl -X POST localhost:8080/auth/register -H "Content-Type: application/json" \
  -d '{"nome":"Cayo","email":"cayo@email.com","senha":"123456"}'

# Usado para cadastrar uma mídia no catálogo
curl -X POST localhost:8080/midias -H "Content-Type: application/json" \
  -d '{"titulo":"Breaking Bad","tipo":"SERIE","genero":"Drama","ano":2008,"totalEpisodios":62,"duracaoMediaEpisodioMin":47}'

# Usado para colocar na lista como "assistindo"
curl -X POST localhost:8080/lista -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"midiaId":1,"status":"ASSISTINDO"}'

# Usado para marcar 10 episódios vistos, dar nota e ver o tempo total
curl -X PATCH localhost:8080/lista/1/progresso -H "Content-Type: application/json" -d '{"episodios":10}'
curl -X PATCH localhost:8080/lista/1/nota -H "Content-Type: application/json" -d '{"nota":9}'
curl localhost:8080/lista/usuario/1/tempo-total
```

## Partes importantes usadas para a orientação a objetos

- O **Encapsulamento** usado que foi o `ItemLista` ele guarda as regras de consistência entre status, progresso e nota (`alterarStatus`, `atualizarProgresso`, `avaliar`); Fazendo assim o service só organizar as informações.
- A **Regra de domínio** usada em `Midia.calcularMinutos(int)` e `ItemLista.getMinutosAssistidos()`.
- A **Regra de negócio usada no Service**: `ItemListaService.calcularTempoTotal` que soma o tempo de tudo que foi assistido e converte para dias, horas e minutos no (`TempoUtil`).
- As **Exceções próprias** usadas em `RegraNegocioException`, `RecursoNaoEncontradoException`,
  `CredenciaisInvalidasException`, que foram tratadas pelo `@RestControllerAdvice`.
- As **Separações em camadas** como mencionadas pelo professor controller → service → repository → banco.

Possui também Regras gerais que o sistema aplica como :
- O filme sempre ter 1 episódio.
- Marcar todos os episódios para "assistido" automaticamente.
- E só dar para avaliar o que já foi começado; 
- A mesma mídia não entra duas vezes na lista.

## Autenticação do projeto

O cadastro e o login usam o BCrypt e a senha nunca é devolvida pela API. O login apenas valida as credenciais e retorna os dados do usuário, sem emitir token de sessão.
