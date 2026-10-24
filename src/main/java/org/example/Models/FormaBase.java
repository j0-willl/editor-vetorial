package org.example.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstrata com tudo que é comum às formas: atributos, cálculos e transformações.
 * Não implementa clonar(), então cada forma concreta é obrigada a implementar.
 */
public abstract class FormaBase implements FormaGeometrica {
    // protected: as subclasses acessam; o resto do sistema usa getters/setters
    protected List<Ponto> pontos = new ArrayList<>();
    protected String corPreenchimentoHex = "#d3d3d3";
    protected String corBordaHex = "#000000";
    protected double espessuraBorda = 2;
    protected String groupId; // null = forma fora de grupo

    @Override public void adicionarPonto(Ponto p) { pontos.add(p); }
    @Override public List<Ponto> getPontos() { return pontos; }
    @Override public int getQuantidadePontos() { return pontos.size(); }

    // Os setters de cor validam o formato antes de aceitar
    @Override public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    @Override public void setCorPreenchimentoHex(String hex) { corPreenchimentoHex = validarHex(hex); }
    @Override public String getCorBordaHex() { return corBordaHex; }
    @Override public void setCorBordaHex(String hex) { corBordaHex = validarHex(hex); }
    @Override public double getEspessuraBorda() { return espessuraBorda; }
    @Override public String getGroupId() { return groupId; }
    @Override public void setGroupId(String groupId) { this.groupId = groupId; }

    @Override
    public void setEspessuraBorda(double espessura) {
        if (!(espessura > 0)) throw new IllegalArgumentException("Espessura da borda deve ser positiva.");
        espessuraBorda = espessura;
    }

    /** Fórmula de Gauss (cadarço): metade do módulo da soma de (xi*yi+1 - xi+1*yi). */
    @Override
    public double calcularArea() {
        int n = pontos.size();
        double soma = 0;
        for (int i = 0; i < n; i++) {
            Ponto a = pontos.get(i), b = pontos.get((i + 1) % n); // % n: o último fecha com o primeiro
            soma += a.getX() * b.getY() - b.getX() * a.getY();
        }
        return Math.abs(soma) / 2; // abs: no sentido horário a soma fica negativa
    }

    /** Soma das distâncias euclidianas, fechando o último vértice no primeiro. */
    @Override
    public double calcularPerimetro() {
        int n = pontos.size();
        double soma = 0;
        for (int i = 0; i < n; i++) soma += pontos.get(i).distancia(pontos.get((i + 1) % n));
        return soma;
    }

    /** Média dos vértices; é o pivô de escala, rotação e cisalhamento. */
    @Override
    public Ponto getCentroide() {
        double sx = 0, sy = 0;
        for (Ponto p : pontos) {
            sx += p.getX();
            sy += p.getY();
        }
        return new Ponto(sx / pontos.size(), sy / pontos.size());
    }

    /**
     * Ray casting: traça uma linha do ponto para a direita e conta quantas bordas ela cruza.
     * Número ímpar de cruzamentos = dentro.
     */
    @Override
    public boolean contemPonto(double px, double py) {
        boolean dentro = false;
        for (int i = 0, j = pontos.size() - 1; i < pontos.size(); j = i++) { // j = vértice anterior a i
            Ponto a = pontos.get(i), b = pontos.get(j);
            // a borda a-b cruza a altura py, e o cruzamento fica à direita de px?
            if ((a.getY() > py) != (b.getY() > py)
                    && px < (b.getX() - a.getX()) * (py - a.getY()) / (b.getY() - a.getY()) + a.getX()) {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    /** Soma o deslocamento (dx, dy) em todos os pontos. */
    @Override
    public void transladar(double dx, double dy) {
        for (Ponto p : pontos) p.mover(dx, dy);
    }

    /** Multiplica a distância de cada ponto ao centroide pelo fator. */
    @Override
    public void escalar(double fator) {
        if (!(fator > 0)) throw new IllegalArgumentException("Fator de escala deve ser positivo.");
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            p.setX(c.getX() + (p.getX() - c.getX()) * fator);
            p.setY(c.getY() + (p.getY() - c.getY()) * fator);
        }
    }

    /** Gira em torno do centroide: x' = cx + dx*cos - dy*sen, y' = cy + dx*sen + dy*cos. */
    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto c = getCentroide();
        double cos = Math.cos(anguloRadianos), sin = Math.sin(anguloRadianos);
        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX(), dy = p.getY() - c.getY(); // posição relativa ao centro
            p.setX(c.getX() + dx * cos - dy * sin);
            p.setY(c.getY() + dx * sin + dy * cos);
        }
    }

    /** Inclina a forma: x ganha shx*dy e y ganha shy*dx, em torno do centroide. */
    @Override
    public void cisalhar(double shx, double shy) {
        Ponto c = getCentroide();
        for (Ponto p : pontos) {
            double dx = p.getX() - c.getX(), dy = p.getY() - c.getY();
            p.setX(c.getX() + dx + shx * dy);
            p.setY(c.getY() + shy * dx + dy);
        }
    }

    /** Regra de negócio: no mínimo 3 pontos válidos, área maior que zero e estilo válido. Usada ao fechar polígono e ao carregar arquivo. */
    public void validar() {
        if (pontos == null || pontos.size() < 3)
            throw new IllegalStateException("Um polígono deve ter no mínimo 3 pontos.");
        for (Ponto p : pontos) {
            if (p == null || !Double.isFinite(p.getX()) || !Double.isFinite(p.getY()))
                throw new IllegalStateException("Ponto com coordenada inválida.");
        }
        // pontos repetidos ou todos alinhados dão área zero: seria uma linha, não uma forma
        if (calcularArea() < 1e-6)
            throw new IllegalStateException("Os pontos não podem estar todos na mesma linha.");
        validarHex(corPreenchimentoHex);
        validarHex(corBordaHex);
        setEspessuraBorda(espessuraBorda); // reaproveita a validação do setter
    }

    /** Prototype: copia pontos (um a um, cópia profunda) e estilo para a instância nova. */
    protected FormaGeometrica copiarPara(FormaBase destino) {
        destino.pontos = new ArrayList<>();
        for (Ponto p : pontos) destino.pontos.add(p.clonar());
        destino.corPreenchimentoHex = corPreenchimentoHex;
        destino.corBordaHex = corBordaHex;
        destino.espessuraBorda = espessuraBorda;
        destino.groupId = groupId;
        return destino;
    }

    /** Formas regulares: distribui 'lados' vértices num círculo de raio r em volta do centro. */
    protected void gerarRegular(Ponto centro, double raio, int lados, double anguloInicial) {
        if (!(raio > 0)) throw new IllegalArgumentException("O raio deve ser estritamente positivo (r > 0).");
        pontos.clear();
        for (int i = 0; i < lados; i++) {
            double a = anguloInicial + 2 * Math.PI * i / lados; // ângulos igualmente espaçados
            pontos.add(new Ponto(centro.getX() + raio * Math.cos(a), centro.getY() + raio * Math.sin(a)));
        }
    }

    /** Aceita só cores no formato #rrggbb. */
    private static String validarHex(String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("Cor inválida: " + hex);
        return hex;
    }
}
