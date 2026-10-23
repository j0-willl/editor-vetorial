package org.example.Models;

/** Quadrado alinhado aos eixos; o raio vai do centro até um canto (lado = raio * raiz de 2). */
public class Quadrado extends FormaBase {
    /** Construtor vazio privado: usado só pelo clonar() e pelo Gson ao carregar. */
    private Quadrado() {
    }

    /** 4 vértices começando em 45° para os lados ficarem retos. */
    public Quadrado(Ponto centro, double raio) {
        gerarRegular(centro, raio, 4, Math.PI / 4);
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Quadrado());
    }
}
