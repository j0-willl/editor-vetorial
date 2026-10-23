package org.example.Models;

/** Hexágono regular com 6 vértices calculados a partir do centro e do raio. */
public class Hexagono extends FormaBase {
    /** Construtor vazio privado: usado só pelo clonar() e pelo Gson ao carregar. */
    private Hexagono() {
    }

    public Hexagono(Ponto centro, double raio) {
        gerarRegular(centro, raio, 6, 0);
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Hexagono());
    }
}
