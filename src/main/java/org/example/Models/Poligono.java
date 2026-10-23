package org.example.Models;

/** Polígono livre: os pontos são adicionados um a um pelos cliques do usuário. */
public class Poligono extends FormaBase {
    public Poligono() {
    }

    /** Prototype: cria um polígono novo e copia tudo para ele. */
    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Poligono());
    }
}
