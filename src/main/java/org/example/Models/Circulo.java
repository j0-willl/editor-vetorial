package org.example.Models;

/** Círculo aproximado por 64 vértices, para aceitar as mesmas transformações dos polígonos. */
public class Circulo extends FormaBase {
    private Circulo() {
    }

    public Circulo(Ponto centro, double raio) {
        gerarRegular(centro, raio, 64, 0);
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Circulo());
    }
}
