package br.com.fiap.nano.produtos.view;

import br.com.fiap.nano.produtos.model.Categoria;
import br.com.fiap.nano.produtos.model.Produto;
import br.com.fiap.nano.produtos.repository.ProdutoCollectionRepository;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProdutoView {

    public static Produto select() {
        // @formatter:off
        Produto ret = (Produto) JOptionPane.showInputDialog(
                null, // componente pai. Como não temos será null
                "Selecione um produto",
                "Menu",
                JOptionPane.QUESTION_MESSAGE,
                null, // icone
                ProdutoCollectionRepository.findAll().toArray(), // Número da opção
                1);

        return ret;
        // @formatter:on
    }

    public static Produto form() {

        // se o usuário cancelar em qualquer etapa, retorna null e o App volta ao menu
        Categoria categoria = CategoriaView.select(null);
        if (categoria == null) return null;

        String nome = "";

        do {
            nome = JOptionPane.showInputDialog("Nome do Produto");
            if (nome == null) return null;

            nome = nome.trim();
            if (nome.length() < 3)
                JOptionPane.showMessageDialog(null, "O nome do produto precisa ter no mínimo 3 caracteres");

        } while (nome.length() < 3);

        String descricao = "";

        do {
            descricao = JOptionPane.showInputDialog("Descrição do Produto");
            if (descricao == null) return null;

            descricao = descricao.trim();
            if (descricao.length() < 5)
                JOptionPane.showMessageDialog(null, "A descrição do produto precisa ter no mínimo 5 caracteres");

        } while (descricao.length() < 5);

        BigDecimal preco = BigDecimal.ZERO;
        boolean invalido;

        do {
            String entrada = JOptionPane.showInputDialog("Preço do Produto");
            if (entrada == null) return null;

            try {
                // BigDecimal direto, sem passar por double (aceita vírgula ou ponto)
                preco = new BigDecimal(entrada.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                preco = BigDecimal.ZERO;
            }

            // preço tem que ser positivo e ter no máximo 2 casas decimais
            invalido = preco.signum() <= 0 || preco.stripTrailingZeros().scale() > 2;
            if (invalido) JOptionPane.showMessageDialog(null, "Valor inválido");

        } while (invalido);

        Produto ret = new Produto();
        ret.setNome(nome)
                .setCategoria(categoria)
                .setDescricao(descricao)
                .setPreco(preco)
                .setDataCadastro(LocalDateTime.now());

        return ret;
    }

    public static void update(Produto produto) {

        // os dados só são gravados no final, assim cancelar no meio não deixa o produto alterado pela metade
        Categoria categoria = CategoriaView.select(produto.getCategoria());
        if (categoria == null) return;

        String nome = "";

        do {
            nome = JOptionPane.showInputDialog("Nome do Produto", produto.getNome());
            if (nome == null) return;

            nome = nome.trim();
            if (nome.length() < 3)
                JOptionPane.showMessageDialog(null, "O nome do produto precisa ter no mínimo 3 caracteres");

        } while (nome.length() < 3);

        String descricao = "";

        do {
            descricao = JOptionPane.showInputDialog("Descrição do Produto", produto.getDescricao());
            if (descricao == null) return;

            descricao = descricao.trim();
            if (descricao.length() < 5)
                JOptionPane.showMessageDialog(null, "A descrição do produto precisa ter no mínimo 5 caracteres");

        } while (descricao.length() < 5);

        BigDecimal preco = BigDecimal.ZERO;
        boolean invalido;

        do {
            String entrada = JOptionPane.showInputDialog("Preço do Produto", produto.getPreco());
            if (entrada == null) return;

            try {
                // BigDecimal direto, sem passar por double (aceita vírgula ou ponto)
                preco = new BigDecimal(entrada.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                preco = BigDecimal.ZERO;
            }

            // preço tem que ser positivo e ter no máximo 2 casas decimais
            invalido = preco.signum() <= 0 || preco.stripTrailingZeros().scale() > 2;
            if (invalido) JOptionPane.showMessageDialog(null, "Valor inválido");

        } while (invalido);

        // não deixa a alteração repetir o nome de outro produto da mesma categoria
        Produto alterado = new Produto().setNome(nome).setCategoria(categoria);
        boolean duplicado = ProdutoCollectionRepository.findAll().stream()
                .anyMatch(outro -> outro != produto && outro.equals(alterado));
        if (duplicado) {
            JOptionPane.showMessageDialog(null, "Produto " + nome.toUpperCase() + " já cadastrado na categoria " + categoria + "!");
            return;
        }

        produto.setCategoria(categoria)
                .setNome(nome)
                .setDescricao(descricao)
                .setPreco(preco);

        sucesso(produto);
        show(produto);
    }

    public static void sucesso() {
        JOptionPane.showMessageDialog(null, "Produto salvo com sucesso!");
    }

    public static void sucesso(Produto produto) {
        System.out.println(produto);
        JOptionPane.showMessageDialog(null, "Produto " + produto.getNome().toUpperCase() + " salvo com sucesso!");
    }

    public static void show(Produto p) {
        System.out.println(p);
        // os textos entram como parâmetros (%s) para um "%" digitado não quebrar o format
        String produtoString = String.format("PRODUTO: %s%nDESCRIÇÃO: %s%nCATEGORIA: %s%nPREÇO: %,.2f",
                p.getNome(), p.getDescricao(), p.getCategoria(), p.getPreco());
        JOptionPane.showMessageDialog(null, produtoString);
    }
}
