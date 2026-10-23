package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/** Clique seleciona (Shift adiciona), arrastar move a seleção, arrastar no vazio faz caixa de seleção. */
public class FerramentaSelecionar implements Ferramenta {
    private boolean arrastandoCaixa; // true quando o arrasto começou fora de qualquer forma
    private double selecaoStartX;
    private double selecaoStartY;
    private double ultimoX, ultimoY; // última posição do mouse, para calcular o deslocamento
    private boolean moveu; // garante um único registro no histórico por arrasto

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
            if (!e.isShiftDown()) ctx.selecionar(null, false); // clicou no vazio: limpa a seleção
        } else if (!ctx.getSelecionados().contains(f)) {
            ctx.selecionar(f, e.isShiftDown());
        }
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (arrastandoCaixa) { // só atualiza o retângulo tracejado
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
        // move as selecionadas pelo quanto o mouse andou desde o último evento
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
