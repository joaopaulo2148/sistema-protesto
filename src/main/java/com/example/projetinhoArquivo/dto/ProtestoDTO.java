package com.example.projetinhoArquivo.dto;
public class ProtestoDTO {

    private String protocolo;
    private String livro;
    private String folha;

    public ProtestoDTO(String protocolo, String livro, String folha) {
        this.protocolo = protocolo;
        this.livro = livro;
        this.folha = folha;
    }

    public String getProtocolo() { return protocolo; }
    public String getLivro() { return livro; }
    public String getFolha() { return folha; }
}