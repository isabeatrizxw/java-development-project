package br.com.fiap.nano.produtos.model;

import java.util.Locale;
import java.util.Objects;

public class Categoria {
    private Long id;
    private String nome;

    public Categoria() {
    }

    public Categoria(String nome) {
        this.setNome(nome);
    }

    public Categoria(Long id, String nome) {
        this.setId(id);
        this.setNome(nome);
    }

    public Long getId() {
        return id;
    }

    public Categoria setId(Long id) {
        this.id = id;
        return this;
    }

    public String getNome() {
        return nome;
    }

    public Categoria setNome(String nome) {
        this.nome = nome;
        return this;
    }

    @Override
    public String toString() {
        return getNome().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        // compara o nome sem diferenciar maiúsculas de minúsculas ("livros" = "Livros")
        return getNome() == null ? categoria.getNome() == null : getNome().equalsIgnoreCase(categoria.getNome());
    }

    @Override
    public int hashCode() {
        // precisa seguir a mesma regra do equals
        return Objects.hash(getNome() == null ? null : getNome().toLowerCase(Locale.ROOT));
    }
}
