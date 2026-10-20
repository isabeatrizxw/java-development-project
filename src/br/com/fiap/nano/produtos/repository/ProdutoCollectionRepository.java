package br.com.fiap.nano.produtos.repository;

import br.com.fiap.nano.produtos.model.Categoria;
import br.com.fiap.nano.produtos.model.Produto;
import br.com.fiap.nano.produtos.repository.CategoriaCollectionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ProdutoCollectionRepository {

    private static List<Produto> produtos;

    static {
        produtos = new Vector<>();

        Produto kindle = new Produto();
        kindle.setCategoria(CategoriaCollectionRepository.findById(1l))
                .setNome("kindle")
                .setDescricao("e-reader da Amazon")
                .setDataCadastro(LocalDateTime.now())
                .setPreco(BigDecimal.valueOf(899.99));

        Produto iphone = new Produto();
        iphone.setCategoria(CategoriaCollectionRepository.findById(2l))
                .setNome("Iphone 14 PRO MAX")
                .setDescricao("Aparelho celular de última geração da Apple")
                .setDataCadastro(LocalDateTime.now())
                .setPreco(BigDecimal.valueOf(12999.99));

        Arrays.asList(kindle, iphone).forEach(ProdutoCollectionRepository::save);
    }

    public static Produto save(Produto produto) {
        if (!produtos.contains(produto)) {
            produto.setId((long) produtos.size() + 1);
            produtos.add(produto);
            return produto;
        } else {
            return null;
        }
    }

    public static Produto findById(Long id) {
        return produtos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static List<Produto> findByCategoria(Categoria categoria) {
        return produtos.stream()
                .filter(p -> p.getCategoria().equals(categoria))
                .toList();
    }

    public static List<Produto> findAll() {
        return produtos;
    }
}
