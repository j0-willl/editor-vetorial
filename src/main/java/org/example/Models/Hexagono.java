package org.example.Models;

public class Hexagono extends FormaBase {
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
