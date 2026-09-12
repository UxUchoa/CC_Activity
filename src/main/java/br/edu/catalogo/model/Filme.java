package br.edu.catalogo.model;

import java.util.Objects;

/**
 * Entidade de dominio do catalogo. Guarda apenas estado e comportamento
 * proprio do filme - nao conhece banco de dados nem requisicao HTTP.
 */
public class Filme {

    private Long id;
    private String titulo;
    private String diretor;
    private Integer ano;
    private String genero;
    private String sinopse;

    public Filme() {
    }

    public Filme(String titulo, String diretor, Integer ano, String genero, String sinopse) {
        this.titulo = titulo;
        this.diretor = diretor;
        this.ano = ano;
        this.genero = genero;
        this.sinopse = sinopse;
    }

    public Filme(Long id, String titulo, String diretor, Integer ano, String genero, String sinopse) {
        this(titulo, diretor, ano, genero, sinopse);
        this.id = id;
    }

    public boolean isNovo() {
        return id == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDiretor() {
        return diretor;
    }

    public void setDiretor(String diretor) {
        this.diretor = diretor;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Filme)) {
            return false;
        }
        Filme filme = (Filme) outro;
        return id != null && id.equals(filme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Filme{id=" + id + ", titulo='" + titulo + "', ano=" + ano + "}";
    }
}
