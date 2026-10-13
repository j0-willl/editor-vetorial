package org.example.Models;

public class Poligono extends FormaBase {
    public Poligono() {
    }

    @Override
    public FormaGeometrica clonar() {
        return copiarPara(new Poligono());
    }
}
