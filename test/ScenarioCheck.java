import br.com.fiap.nano.produtos.App;
import br.com.fiap.nano.produtos.model.Produto;
import br.com.fiap.nano.produtos.repository.CategoriaCollectionRepository;
import br.com.fiap.nano.produtos.repository.ProdutoCollectionRepository;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.System.lineSeparator;

public class ScenarioCheck {
    static int falhas = 0;
    static void check(String nome, boolean ok) { System.out.println((ok ? "OK      " : "FALHA   ") + nome); if (!ok) falhas++; }
    static void cenario(String t) { System.out.println(System.lineSeparator() + "== " + t); }

    /** Executa App.main com as respostas dadas e devolve as mensagens exibidas nessa execução. */
    static List<String> rodar(DialogBot.Acao... acoes) {
        int antes = DialogBot.vistos.size();
        DialogBot.responder(acoes);
        App.main(new String[0]);
        return DialogBot.vistos.subList(antes, DialogBot.vistos.size()).stream().map(DialogBot.Visto::mensagem).toList();
    }

    /** Executa App.main esperando que ele quebre; devolve a exceção (ou null se não quebrou). */
    static Throwable rodarQuebrando(DialogBot.Acao... acoes) {
        DialogBot.responder(acoes);
        try { App.main(new String[0]); return null; } catch (Throwable t) { return t; }
    }

    static String show(String nome, String desc, String cat, String preco) {
        return "PRODUTO: " + nome + lineSeparator() + "DESCRIÇÃO: " + desc + lineSeparator() + "CATEGORIA: " + cat + lineSeparator() + "PREÇO: " + preco;
    }

    static DialogBot.Acao menu(String op) { return DialogBot.escolhe(op); }
    static final DialogBot.Acao SAIR = DialogBot.escolhe("Encerrar Sistema");
    static final DialogBot.Acao OK = DialogBot.ok();

    public static void main(String[] a) {
        cenario("1. Aplicação inicia e apresenta o menu");
        List<String> m = rodar(SAIR);
        DialogBot.Visto menu = DialogBot.vistos.get(0);
        check("primeira tela = menu 'Selecione uma opção', título 'Menu'", m.get(0).equals("Selecione uma opção") && menu.titulo().equals("Menu"));
        check("menu com as 6 opções", menu.opcoes().size() == 6);

        cenario("2. Categorias e produtos iniciais carregados");
        m = rodar(menu("Consultar Produto Por Categoria"), DialogBot.escolhe("CELULARES"), OK, SAIR);
        check("seletor de categoria lista as 4 iniciais", DialogBot.vistos.stream().filter(v -> "Selecione uma categoria".equals(v.mensagem())).reduce((x, y) -> y).get()
                .opcoes().equals(List.of("ELETRÔNICOS", "CELULARES", "LIVROS", "GAMES")));
        check("Celulares mostra o Iphone inicial", m.contains(show("Iphone 14 PRO MAX", "Aparelho celular de última geração da Apple", "CELULARES", "12.999,99")));
        m = rodar(menu("Consultar Produto Por ID"), DialogBot.digita("1"), OK, SAIR);
        check("id 1 = kindle inicial", m.contains(show("kindle", "e-reader da Amazon", "ELETRÔNICOS", "899,99")));

        cenario("3. Cadastrar nova categoria");
        m = rodar(menu("Cadastrar Categoria"), DialogBot.digita("Informática"), OK, SAIR);
        check("mensagem 'Categoria INFORMÁTICA salva com sucesso!'", m.contains("Categoria INFORMÁTICA salva com sucesso!"));
        check("categoria salva com id 5", CategoriaCollectionRepository.findById(5L).getNome().equals("Informática"));
        m = rodar(menu("Cadastrar Categoria"), DialogBot.digita("Livros"), OK, SAIR);
        check("duplicada 'Livros' não entra no repositório", CategoriaCollectionRepository.findAll().size() == 5);
        check("duplicada 'Livros' avisa 'já cadastrada' e não diz sucesso", m.contains("Categoria LIVROS já cadastrada!") && !m.contains("Categoria LIVROS salva com sucesso!"));
        m = rodar(menu("Cadastrar Categoria"), DialogBot.digita("livros"), OK, SAIR);
        check("'livros' minúsculo é recusado como duplicada", CategoriaCollectionRepository.findAll().size() == 5 && m.contains("Categoria LIVROS já cadastrada!"));
        check("findByCategoria com 'celulares' minúsculo acha o Iphone", ProdutoCollectionRepository.findByCategoria(new br.com.fiap.nano.produtos.model.Categoria("celulares")).size() == 1);

        cenario("4. Cadastrar produto válido em categoria existente");
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("INFORMÁTICA"), DialogBot.digita("Notebook Dell"),
                DialogBot.digita("Notebook para desenvolvimento"), DialogBot.digita("4599.90"), OK, SAIR);
        Produto nb = ProdutoCollectionRepository.findById(3L);
        check("mensagem 'Produto NOTEBOOK DELL salvo com sucesso!'", m.contains("Produto NOTEBOOK DELL salvo com sucesso!"));
        check("produto salvo id 3, categoria Informática, preço 4599.9, data preenchida", nb != null && nb.getNome().equals("Notebook Dell")
                && nb.getCategoria().getNome().equals("Informática") && nb.getPreco().compareTo(new BigDecimal("4599.90")) == 0 && nb.getDataCadastro() != null);
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("ELETRÔNICOS"), DialogBot.digita("kindle"), DialogBot.digita("outro kindle"), DialogBot.digita("1"), OK, SAIR);
        check("produto duplicado (kindle/Eletrônicos) não entra no repositório", ProdutoCollectionRepository.findAll().size() == 3);
        check("produto duplicado avisa 'já cadastrado' e não diz sucesso", m.contains("Produto KINDLE já cadastrado na categoria ELETRÔNICOS!") && !m.contains("Produto KINDLE salvo com sucesso!"));

        cenario("5. Dados inválidos");
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("GAMES"),
                DialogBot.digita(""), OK, DialogBot.digita("Mouse Gamer"),
                DialogBot.digita(""), OK, DialogBot.digita("Mouse com RGB"),
                DialogBot.digita("abc"), OK, DialogBot.digita("-5"), OK, DialogBot.digita("0"), OK, DialogBot.digita("29,90"),
                OK, SAIR);
        check("nome vazio: aviso e pede de novo", m.contains("O nome do produto precisa ter no mínimo 3 caracteres") && m.stream().filter("Nome do Produto"::equals).count() == 2);
        check("descrição vazia: aviso e pede de novo", m.contains("A descrição do produto precisa ter no mínimo 5 caracteres") && m.stream().filter("Descrição do Produto"::equals).count() == 2);
        check("preço 'abc', '-5', '0' rejeitados com 'Valor inválido'", m.stream().filter("Valor inválido"::equals).count() == 3);
        check("preço '29,90' (vírgula) aceito como 29.90", ProdutoCollectionRepository.findById(4L).getPreco().compareTo(new BigDecimal("29.90")) == 0);
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("GAMES"), DialogBot.digita("ab"), OK, DialogBot.digita("Joy"),
                DialogBot.digita("abc"), OK, DialogBot.digita("Joystick sem fio"), DialogBot.digita("10"), OK, SAIR);
        check("nome 'ab' e descrição 'abc' recusados (aviso e pede de novo)", m.stream().filter("Nome do Produto"::equals).count() == 2
                && m.stream().filter("Descrição do Produto"::equals).count() == 2);
        check("salvo só com nome 'Joy' e descrição 'Joystick sem fio'", ProdutoCollectionRepository.findById(5L).getNome().equals("Joy")
                && ProdutoCollectionRepository.findById(5L).getDescricao().equals("Joystick sem fio"));
        m = rodar(menu("Consultar Produto Por ID"), DialogBot.digita("abc"), OK, DialogBot.digita("0"), OK, DialogBot.digita("-1"), OK, DialogBot.digita("1"), OK, SAIR);
        check("id 'abc', '0' e '-1' mostram 'Id inválido!' e pedem de novo", m.stream().filter("Id inválido!"::equals).count() == 3
                && m.stream().filter("Informe o id do produto"::equals).count() == 4);

        cenario("6. Alterar dados de produto");
        m = rodar(menu("Alterar dados do Produto"), DialogBot.escolhe("Notebook Dell"), DialogBot.escolhe("INFORMÁTICA"),
                DialogBot.digita("Notebook Dell - 2ª Geração"), DialogBot.mantem(), DialogBot.digita("4299.00"), OK, OK, SAIR);
        check("seletor lista produtos cadastrados", DialogBot.vistos.stream().filter(v -> "Selecione um produto".equals(v.mensagem())).reduce((x, y) -> y).get().opcoes().contains("Notebook Dell"));
        check("mensagem de sucesso da alteração", m.contains("Produto NOTEBOOK DELL - 2ª GERAÇÃO salvo com sucesso!"));
        check("show com dados novos", m.contains(show("Notebook Dell - 2ª Geração", "Notebook para desenvolvimento", "INFORMÁTICA", "4.299,00")));
        check("repositório atualizado (mesmo id 3)", ProdutoCollectionRepository.findById(3L).getNome().equals("Notebook Dell - 2ª Geração")
                && ProdutoCollectionRepository.findById(3L).getPreco().compareTo(new BigDecimal("4299")) == 0);
        rodar(menu("Alterar dados do Produto"), DialogBot.escolhe("Notebook Dell - 2ª Geração"), DialogBot.escolhe("ELETRÔNICOS"),
                DialogBot.mantem(), DialogBot.mantem(), DialogBot.mantem(), OK, OK, SAIR);
        check("troca de categoria: notebook passa para Eletrônicos", ProdutoCollectionRepository.findById(3L).getCategoria().getNome().equals("Eletrônicos"));

        cenario("7. Consulta por ID");
        m = rodar(menu("Consultar Produto Por ID"), DialogBot.digita("2"), OK, SAIR);
        check("id 2 existente mostra o Iphone", m.contains(show("Iphone 14 PRO MAX", "Aparelho celular de última geração da Apple", "CELULARES", "12.999,99")));
        m = rodar(menu("Consultar Produto Por ID"), DialogBot.digita("99"), OK, SAIR);
        check("id 99 inexistente = 'Produto não encontrado!'", m.contains("Produto não encontrado!"));

        cenario("8. Consulta por categoria");
        m = rodar(menu("Consultar Produto Por Categoria"), DialogBot.escolhe("ELETRÔNICOS"), OK, OK, SAIR);
        check("Eletrônicos lista 2 produtos (kindle e notebook), um show por produto", m.stream().filter(x -> x.startsWith("PRODUTO: ")).count() == 2
                && m.stream().anyMatch(x -> x.startsWith("PRODUTO: kindle")) && m.stream().anyMatch(x -> x.startsWith("PRODUTO: Notebook Dell")));
        m = rodar(menu("Consultar Produto Por Categoria"), DialogBot.escolhe("INFORMÁTICA"), OK, SAIR);
        check("categoria sem produtos = 'Não encontramos produtos cadastrados para a categoria Informática'",
                m.contains("Não encontramos produtos cadastrados para a categoria Informática"));

        cenario("9. Mensagens e dados exibidos");
        check("formato do show: PRODUTO/DESCRIÇÃO/CATEGORIA/PREÇO com %,.2f (pt_BR)", DialogBot.vistos.stream().anyMatch(v -> v.mensagem().equals(show("kindle", "e-reader da Amazon", "ELETRÔNICOS", "899,99"))));
        check("nenhum diálogo ficou sem resposta programada", DialogBot.pendentes() == 0);

        cenario("10. Encerramento");
        int antes = DialogBot.vistos.size();
        rodar(SAIR);
        check("'Encerrar Sistema' finaliza o laço (main retorna)", DialogBot.vistos.size() == antes + 1);
        antes = DialogBot.vistos.size();
        rodar(DialogBot.cancela());
        check("fechar/cancelar o menu também encerra", DialogBot.vistos.size() == antes + 1);

        cenario("11. Alteração não pode gerar produto duplicado");
        Produto nb3 = ProdutoCollectionRepository.findById(3L); // Notebook, hoje em Eletrônicos, junto com o kindle
        String nomeAntes = nb3.getNome();
        m = rodar(menu("Alterar dados do Produto"), DialogBot.escolhe(nomeAntes), DialogBot.escolhe("ELETRÔNICOS"),
                DialogBot.digita("kindle"), DialogBot.mantem(), DialogBot.mantem(), OK, SAIR);
        check("renomear Notebook para 'kindle' na mesma categoria: aviso de duplicado", m.contains("Produto KINDLE já cadastrado na categoria ELETRÔNICOS!"));
        check("produto não foi alterado e não houve mensagem de sucesso", nb3.getNome().equals(nomeAntes) && m.stream().noneMatch(x -> x.endsWith("salvo com sucesso!")));
        m = rodar(menu("Alterar dados do Produto"), DialogBot.escolhe(nomeAntes), DialogBot.escolhe("GAMES"),
                DialogBot.digita("kindle"), DialogBot.mantem(), DialogBot.mantem(), OK, OK, SAIR);
        check("mesmo nome 'kindle' em OUTRA categoria (Games) é permitido", nb3.getNome().equals("kindle") && nb3.getCategoria().getNome().equals("Games"));
        m = rodar(menu("Alterar dados do Produto"), DialogBot.escolhe("Iphone 14 PRO MAX"), DialogBot.escolhe("CELULARES"),
                DialogBot.mantem(), DialogBot.mantem(), DialogBot.digita("11999,90"), OK, OK, SAIR);
        check("alterar só o preço mantendo o próprio nome não é tratado como duplicado", m.contains("Produto IPHONE 14 PRO MAX salvo com sucesso!")
                && ProdutoCollectionRepository.findById(2L).getPreco().compareTo(new BigDecimal("11999.90")) == 0);

        cenario("12. Categoria com nome vazio não é aceita");
        int qtdCat0 = CategoriaCollectionRepository.findAll().size();
        m = rodar(menu("Cadastrar Categoria"), DialogBot.digita(""), OK, DialogBot.digita("   "), OK, DialogBot.digita("  Áudio  "), OK, SAIR);
        check("'' e '   ' recusados com aviso e pedido de novo", m.stream().filter("O nome da categoria não pode ficar em branco"::equals).count() == 2
                && m.stream().filter("Informe o nome da Categoria"::equals).count() == 3);
        check("'  Áudio  ' salvo como 'Áudio' (sem espaços)", CategoriaCollectionRepository.findAll().size() == qtdCat0 + 1
                && CategoriaCollectionRepository.findAll().get(qtdCat0).getNome().equals("Áudio") && m.contains("Categoria ÁUDIO salva com sucesso!"));
        m = rodar(menu("Cadastrar Categoria"), DialogBot.digita(" livros "), OK, SAIR);
        check("' livros ' com espaços também é reconhecida como duplicada", m.contains("Categoria LIVROS já cadastrada!") && CategoriaCollectionRepository.findAll().size() == qtdCat0 + 1);

        cenario("13. Duplicidade de produto ignora maiúsculas/minúsculas");
        int qtdP0 = ProdutoCollectionRepository.findAll().size();
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("ELETRÔNICOS"), DialogBot.digita("KINDLE"), DialogBot.digita("outro kindle"),
                DialogBot.digita("1"), OK, SAIR);
        check("cadastrar 'KINDLE' em Eletrônicos (já existe 'kindle'): aviso de duplicado e nada salvo",
                m.contains("Produto KINDLE já cadastrado na categoria ELETRÔNICOS!") && ProdutoCollectionRepository.findAll().size() == qtdP0);
        m = rodar(menu("Alterar dados do Produto"), DialogBot.escolhe("Iphone 14 PRO MAX"), DialogBot.escolhe("ELETRÔNICOS"),
                DialogBot.digita("Kindle"), DialogBot.mantem(), DialogBot.mantem(), OK, SAIR);
        check("alterar Iphone para 'Kindle' em Eletrônicos: aviso de duplicado e Iphone intacto",
                m.contains("Produto KINDLE já cadastrado na categoria ELETRÔNICOS!") && ProdutoCollectionRepository.findById(2L).getNome().equals("Iphone 14 PRO MAX"));
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("ELETRÔNICOS"), DialogBot.digita("Kindle Paperwhite"), DialogBot.digita("Modelo com luz"),
                DialogBot.digita("799,90"), OK, SAIR);
        check("modelo diferente 'Kindle Paperwhite' é aceito", m.contains("Produto KINDLE PAPERWHITE salvo com sucesso!") && ProdutoCollectionRepository.findAll().size() == qtdP0 + 1);

        cenario("14. Textos com %, preços inválidos e nomes só com espaços");
        m = rodar(menu("Cadastrar Produto"), DialogBot.escolhe("GAMES"),
                DialogBot.digita("   "), OK, DialogBot.digita("  Camiseta 100% algodão  "),
                DialogBot.digita("Desconto de 10%"),
                DialogBot.digita("NaN"), OK, DialogBot.digita("Infinity"), OK, DialogBot.digita("29.999"), OK, DialogBot.digita("29,99"),
                OK, SAIR);
        List<Produto> todos = ProdutoCollectionRepository.findAll();
        Produto cam = todos.get(todos.size() - 1);
        check("nome '   ' recusado com aviso", m.contains("O nome do produto precisa ter no mínimo 3 caracteres"));
        check("nome salvo sem espaços nas pontas", cam.getNome().equals("Camiseta 100% algodão"));
        check("'NaN', 'Infinity' e '29.999' recusados com 'Valor inválido', sem quebrar", m.stream().filter("Valor inválido"::equals).count() == 3);
        check("'29,99' salvo como 29.99", cam.getPreco().compareTo(new BigDecimal("29.99")) == 0);
        Throwable erro = rodarQuebrando(menu("Consultar Produto Por ID"), DialogBot.digita(String.valueOf(cam.getId())), OK, SAIR);
        check("consultar produto com '%' no nome e na descrição não quebra", erro == null);
        check("tela mostra os textos com '%' corretamente", DialogBot.vistos.stream().anyMatch(v ->
                v.mensagem().equals(show("Camiseta 100% algodão", "Desconto de 10%", "GAMES", "29,99"))));

        cenario("15. Cancelar qualquer diálogo volta ao menu, sem exceção e sem efeitos");
        int qtdProd = ProdutoCollectionRepository.findAll().size();
        String[][] casos = {{"Cadastrar Produto", "0"}, {"Cadastrar Produto", "1"}, {"Cadastrar Produto", "2"}, {"Cadastrar Produto", "3"}};
        for (String[] c : casos) {
            int passosAntes = Integer.parseInt(c[1]);
            DialogBot.Acao[] preenche = {DialogBot.escolhe("GAMES"), DialogBot.digita("Teclado"), DialogBot.digita("Teclado mecânico")};
            DialogBot.Acao[] acoes = new DialogBot.Acao[passosAntes + 3];
            acoes[0] = menu(c[0]);
            System.arraycopy(preenche, 0, acoes, 1, passosAntes);
            acoes[passosAntes + 1] = DialogBot.cancela();
            acoes[passosAntes + 2] = SAIR;
            String[] passo = {"categoria", "nome", "descrição", "preço"};
            Throwable t = rodarQuebrando(acoes);
            String ultimaTela = DialogBot.vistos.get(DialogBot.vistos.size() - 1).mensagem();
            check("Cadastrar Produto, cancelar no passo " + passo[passosAntes] + ": volta ao menu", t == null && ultimaTela.equals("Selecione uma opção"));
        }
        check("nenhum produto salvo pelos cancelamentos", ProdutoCollectionRepository.findAll().size() == qtdProd);

        int qtdCat = CategoriaCollectionRepository.findAll().size();
        Throwable t = rodarQuebrando(menu("Cadastrar Categoria"), DialogBot.cancela(), SAIR);
        check("Cadastrar Categoria, cancelar: volta ao menu e não salva categoria nula", t == null && CategoriaCollectionRepository.findAll().size() == qtdCat);

        t = rodarQuebrando(menu("Alterar dados do Produto"), DialogBot.cancela(), SAIR);
        check("Alterar, cancelar a seleção do produto: volta ao menu", t == null);
        Produto ip = ProdutoCollectionRepository.findById(2L);
        String[] original = {ip.getNome(), ip.getDescricao(), ip.getCategoria().getNome(), ip.getPreco().toString()};
        t = rodarQuebrando(menu("Alterar dados do Produto"), DialogBot.escolhe("Iphone 14 PRO MAX"), DialogBot.escolhe("GAMES"),
                DialogBot.digita("Nome Novo"), DialogBot.digita("Descrição nova"), DialogBot.cancela(), SAIR);
        check("Alterar, cancelar no preço: volta ao menu e o produto NÃO fica alterado pela metade", t == null
                && ip.getNome().equals(original[0]) && ip.getDescricao().equals(original[1]) && ip.getCategoria().getNome().equals(original[2])
                && ip.getPreco().toString().equals(original[3]));

        t = rodarQuebrando(menu("Consultar Produto Por Categoria"), DialogBot.cancela(), SAIR);
        check("Consultar por Categoria, cancelar: volta ao menu", t == null);
        m = rodar(menu("Consultar Produto Por ID"), DialogBot.cancela(), SAIR);
        check("Consultar por ID, cancelar: volta ao menu sem 'Id inválido!'", !m.contains("Id inválido!") && m.get(2).equals("Selecione uma opção"));
        check("todas as respostas usadas", DialogBot.pendentes() == 0);

        System.out.println(System.lineSeparator() + (falhas == 0 ? "RESULTADO: nenhuma falha inesperada" : "RESULTADO: " + falhas + " falha(s) inesperada(s)"));
        System.exit(falhas);
    }
}
