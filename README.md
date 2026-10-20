# Cadastro de Produtos em Java

Sistema de cadastro de produtos para uma pequena loja de eletrônicos, desenvolvido como projeto
do curso **Nano Java (FIAP)**. Cada produto tem nome, descrição, categoria, preço e data de
cadastro, e toda a interação com o usuário acontece por caixas de diálogo (`JOptionPane`).

## Funcionalidades

- Cadastrar categorias
- Cadastrar produtos vinculados a uma categoria
- Alterar os dados de um produto
- Consultar produto por ID
- Listar os produtos de uma categoria

O sistema já começa com algumas categorias (Eletrônicos, Celulares, Livros e Games) e dois
produtos de exemplo. Os dados ficam em memória, usando coleções do Java no lugar de um banco
de dados, então são perdidos quando o programa é fechado.

## Tecnologias

- Java 21 (mínimo Java 16, por causa de `Stream.toList()` e do `switch` com `->`)
- Swing (`JOptionPane`) para a interface
- API de Coleções para simular a persistência

## Estrutura

```
src/br/com/fiap/nano/produtos/
├── App.java          ponto de entrada: menu e fluxos
├── model/            Categoria, Produto
├── repository/       CategoriaCollectionRepository, ProdutoCollectionRepository
└── view/             Opcao, OpcaoView, CategoriaView, ProdutoView
test/                 testes automatizados dos fluxos
```

O projeto é dividido em camadas: `model` tem as entidades, `repository` guarda e consulta os
dados, `view` cuida das telas e `App` liga tudo pelo menu.

## Como executar

**IntelliJ IDEA:** abra a pasta do projeto, selecione um JDK 21 em
*File > Project Structure > SDK* e rode o método `main` da classe `App`.

**Terminal** (na pasta do projeto):

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out br.com.fiap.nano.produtos.App
```

No Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out br.com.fiap.nano.produtos.App
```

Se aparecer `UnsupportedClassVersionError`, o `java` do PATH é mais antigo que o JDK usado
para compilar. Confira com `java -version`.

## Testes

Os testes abrem as janelas reais do programa e respondem a elas automaticamente, passando
pelos fluxos de cadastro, alteração, consultas, validações e cancelamento. Enquanto rodam,
evite clicar nas janelas que aparecem.

```bash
javac -encoding UTF-8 -d out-test $(find src test -name "*.java")
java -cp out-test ScenarioCheck
```

Ao final deve aparecer `RESULTADO: nenhuma falha inesperada`.

## Melhorias em relação ao projeto base

Além do que o curso pedia, adicionei algumas validações e correções:

- cancelar qualquer janela volta ao menu, em vez de encerrar o programa com erro
- cadastro duplicado mostra um aviso, em vez da mensagem de sucesso
- categorias e produtos são comparados sem diferenciar maiúsculas e minúsculas
- a alteração de um produto também não permite duplicar outro da mesma categoria
- nome com no mínimo 3 caracteres e descrição com no mínimo 5, ignorando espaços nas pontas
- categoria não pode ter nome vazio
- o preço aceita vírgula ou ponto, precisa ser positivo e ter no máximo 2 casas decimais
- textos com `%` não quebram mais a exibição dos dados do produto
- ID zero ou negativo mostra "Id inválido!"
