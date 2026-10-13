package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/** Clique seleciona (Shift adiciona), arrastar move a seleção, arrastar no vazio faz caixa de seleção. */
public class FerramentaSelecionar implements Ferramenta {
    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;
    private double ultimoX, ultimoY;
    private boolean moveu;

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        ultimoX = ctx.aplicarSnap(e.getX());
        ultimoY = ctx.aplicarSnap(e.getY());
        moveu = false;
        FormaGeometrica f = ctx.formaEm(e.getX(), e.getY());
        arrastandoCaixa = f == null;
        if (arrastandoCaixa) {
            selecaoStartX = e.getX();
            selecaoStartY = e.getY();
            if (!e.isShiftDown()) ctx.selecionar(null, false);
        } else if (!ctx.getSelecionados().contains(f)) {
            ctx.selecionar(f, e.isShiftDown());
        }
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (arrastandoCaixa) {
            ctx.setCaixaSelecao(new double[]{selecaoStartX, selecaoStartY, e.getX(), e.getY()});
            ctx.requestRedraw();
            return;
        }
        double x = ctx.aplicarSnap(e.getX()), y = ctx.aplicarSnap(e.getY());
        if (ctx.getSelecionados().isEmpty() || (x == ultimoX && y == ultimoY)) return;
        if (!moveu) {
            ctx.registrarEstado();
            moveu = true;
        }
        for (FormaGeometrica f : ctx.getSelecionados()) f.transladar(x - ultimoX, y - ultimoY);
        ultimoX = x;
        ultimoY = y;
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(MouseEvent e, EditorContext ctx) {
        if (!arrastandoCaixa) return;
        arrastandoCaixa = false;
        ctx.selecionarNaCaixa(selecaoStartX, selecaoStartY, e.getX(), e.getY(), e.isShiftDown());
        ctx.setCaixaSelecao(null);
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }
}
