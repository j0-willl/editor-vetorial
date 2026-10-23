package org.example.Models;

/** Círculo aproximado por 64 vértices, para aceitar as mesmas transformações dos polígonos. */
public class Circulo extends FormaBase {
    /** Construtor vazio privado: usado só pelo clonar() e pelo Gson ao carregar. */
    private Circulo() {
    }

    /** Gera os 64 vértices automaticamente; lança exceção se raio <= 0. */
    public Circulo(Ponto centro, double raio) {
        gerarRegular(centro, raio, 64, 0);
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Circulo());
    }
}
