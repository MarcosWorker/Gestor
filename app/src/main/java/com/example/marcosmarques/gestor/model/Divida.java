package com.example.marcosmarques.gestor.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Divida {

    private String uid;
    private String titulo;
    private Boolean cartao;
    private Long numeroParcelas;
    private Double valor;
    private String local;
    private String data;

    public Divida() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Boolean getCartao() {
        return cartao;
    }

    public void setCartao(Boolean cartao) {
        this.cartao = cartao;
    }

    public Long getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Long numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
