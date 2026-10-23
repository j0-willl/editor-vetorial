package org.example.Models;

/** Um ponto (x, y) no canvas. Toda forma é uma lista de pontos. */
public class Ponto {
    private double x;
    private double y;

    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    /** Desloca o ponto (usado na translação). */
    public void mover(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /** Distância euclidiana (Pitágoras): raiz de (dx² + dy²). */
    public double distancia(Ponto outro) {
        return Math.hypot(x - outro.x, y - outro.y);
    }

    /** Prototype: cópia independente deste ponto. */
    public Ponto clonar() {
        return new Ponto(x, y);
    }
}
