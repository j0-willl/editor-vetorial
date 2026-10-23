package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

/** Pressiona perto de um vértice e arrasta para movê-lo. */
public class FerramentaEditarVertice implements Ferramenta {
    private static final double TOLERANCIA = 8; // distância máxima (px) para "pegar" um vértice

    private Ponto verticeSelecionado; // referência ao próprio Ponto da forma: mexer nele mexe na forma
    private boolean alterou;

    /** Procura o vértice mais próximo, começando pela forma que está por cima. */
    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        verticeSelecionado = null;
        alterou = false;
        Ponto cursor = new Ponto(e.getX(), e.getY());
        List<FormaGeometrica> formas = ctx.getFormas();
        for (int i = formas.size() - 1; i >= 0 && verticeSelecionado == null; i--) {
            for (Ponto p : formas.get(i).getPontos()) {
                if (p.distancia(cursor) <= TOLERANCIA) {
                    verticeSelecionado = p;
                    if (!ctx.getSelecionados().contains(formas.get(i))) ctx.selecionar(formas.get(i), false);
                    break;
                }
            }
        }
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (verticeSelecionado == null) return;
        if (!alterou) {
            ctx.registrarEstado();
            alterou = true;
        }
        verticeSelecionado.setX(ctx.aplicarSnap(e.getX()));
        verticeSelecionado.setY(ctx.aplicarSnap(e.getY()));
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        verticeSelecionado = null;
    }
}
