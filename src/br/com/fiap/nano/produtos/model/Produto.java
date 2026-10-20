package br.com.fiap.nano.produtos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Categoria categoria;
    private LocalDateTime dataCadastro;

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, BigDecimal preco, Categoria categoria, LocalDateTime dataCadastro) {
        this.setId(id);
        this.setNome(nome);
        this.setDescricao(descricao);
        this.setPreco(preco);
        this.setCategoria(categoria);
        this.setDataCadastro(dataCadastro);
    }

    public Produto(String nome, String descricao, BigDecimal preco, Categoria categoria, LocalDateTime dataCadastro) {
        this.setNome(nome);
        this.setDescricao(descricao);
        this.setPreco(preco);
        this.setCategoria(categoria);
        this.setDataCadastro(dataCadastro);
    }

    @Override
    public String toString() {
        return getNome();
    }

    public Long getId() {
        return id;
    }

    public Produto setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNome() {
        return nome;
    }

    public Produto setNome(String nome) {
        this.nome = nome;
        return this;
    }

    public String getDescricao() {
        return descricao;
    }

    public Produto setDescricao(String descricao) {
        this.descricao = descricao;
        return this;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Produto setPreco(BigDecimal preco) {
        this.preco = preco;
        return this;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Produto setCategoria(Categoria categoria) {
        this.categoria = categoria;
        return this;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public Produto setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        // mesmo nome (sem diferenciar maiúsculas) na mesma categoria = mesmo produto
        boolean mesmoNome = nome == null ? produto.nome == null : nome.equalsIgnoreCase(produto.nome);
        return mesmoNome && Objects.equals(categoria, produto.categoria);
    }

    @Override
    public int hashCode() {
        // precisa seguir a mesma regra do equals
        return Objects.hash(nome == null ? null : nome.toLowerCase(Locale.ROOT), categoria);
    }
}
