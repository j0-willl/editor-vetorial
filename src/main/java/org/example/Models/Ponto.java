package org.example.Models;

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

    public void mover(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public double distancia(Ponto outro) {
        return Math.hypot(x - outro.x, y - outro.y);
    }

    public Ponto clonar() {
        return new Ponto(x, y);
    }
}
