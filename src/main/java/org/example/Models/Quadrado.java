package org.example.Models;

/** Quadrado alinhado aos eixos; o raio vai do centro até um canto. */
public class Quadrado extends FormaBase {
    private Quadrado() {
    }

    public Quadrado(Ponto centro, double raio) {
        gerarRegular(centro, raio, 4, Math.PI / 4);
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Quadrado());
    }
}
