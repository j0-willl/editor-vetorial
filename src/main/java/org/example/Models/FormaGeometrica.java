package org.example.Models;

import java.util.List;

/** Contrato de toda forma do editor (igual à listagem do enunciado). */
public interface FormaGeometrica {
    // Pontos (vértices) da forma
    void adicionarPonto(Ponto p);
    List<Ponto> getPontos();
    int getQuantidadePontos();

    // Estilo: cores em hexadecimal (#rrggbb) e espessura da borda
    String getCorPreenchimentoHex();
    void setCorPreenchimentoHex(String hex);
    String getCorBordaHex();
    void setCorBordaHex(String hex);
    double getEspessuraBorda();
    void setEspessuraBorda(double espessura);

    // Composite: formas com o mesmo groupId agem como uma só
    String getGroupId();
    void setGroupId(String groupId);

    // Cálculos geométricos
    double calcularArea();
    double calcularPerimetro();
    Ponto getCentroide();
    boolean contemPonto(double px, double py);

    // Transformações (em torno do centroide, exceto a translação)
    void transladar(double dx, double dy);
    void escalar(double fator);
    void rotacionar(double anguloRadianos);
    void cisalhar(double shx, double shy);

    /** Prototype: devolve uma cópia profunda, sem compartilhar pontos. */
    FormaGeometrica clonar();
}
