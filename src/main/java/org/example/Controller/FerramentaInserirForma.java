package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/** Formas regulares por arrasto: pressiona no centro e arrasta até o raio. Um clique simples insere tamanho padrão. */
public class FerramentaInserirForma implements Ferramenta {
    public enum Tipo { QUADRADO, CIRCULO, HEXAGONO }

    private static final double RAIO_PADRAO = 60;

    private final Tipo tipo;
    private Ponto centroInsercao;

    public FerramentaInserirForma(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        centroInsercao = new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY()));
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        double raio = centroInsercao.distancia(new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY())));
        if (raio > 0) ctx.setFormaEmAndamento(criar(raio));
        ctx.requestRedraw();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        FormaGeometrica f = ctx.getFormaEmAndamento();
        if (f == null || f.calcularArea() < 4) {
            f = criar(RAIO_PADRAO);
            ctx.setFormaEmAndamento(f); // aplica o estilo atual
        }
        ctx.setFormaEmAndamento(null);
        ctx.adicionarForma(f);
        ctx.selecionar(f, false);
        centroInsercao = null;
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    private FormaGeometrica criar(double raio) {
        return switch (tipo) {
            case QUADRADO -> new Quadrado(centroInsercao.clonar(), raio);
            case CIRCULO -> new Circulo(centroInsercao.clonar(), raio);
            case HEXAGONO -> new Hexagono(centroInsercao.clonar(), raio);
        };
    }
}
