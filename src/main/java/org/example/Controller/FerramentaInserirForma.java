package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

/** Formas regulares por arrasto: pressiona no centro e arrasta até o raio. Um clique simples insere tamanho padrão. */
public class FerramentaInserirForma implements Ferramenta {
    /** Qual forma este botão insere. */
    public enum Tipo { QUADRADO, CIRCULO, HEXAGONO }

    private static final double RAIO_PADRAO = 60;
    private static final double RAIO_MINIMO = 3; // arrasto menor que isso conta como clique

    private final Tipo tipo;
    private Ponto centroInsercao; // onde o mouse foi pressionado

    public FerramentaInserirForma(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        centroInsercao = new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY()));
    }

    /**
     * Enquanto arrasta, recria a prévia com raio = distância do centro até o mouse.
     * Só o centro gruda na grade; o raio é livre, senão formas pequenas seriam impossíveis.
     */
    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        double raio = centroInsercao.distancia(new Ponto(e.getX(), e.getY()));
        ctx.setFormaEmAndamento(raio >= RAIO_MINIMO ? criar(raio) : null);
        ctx.requestRedraw();
    }

    /** Ao soltar, a prévia vira forma de verdade (ou uma de tamanho padrão, se foi só um clique). */
    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        if (centroInsercao == null) return;
        FormaGeometrica f = ctx.getFormaEmAndamento();
        if (f == null) { // não houve arrasto: foi um clique
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
