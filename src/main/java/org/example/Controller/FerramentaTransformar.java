package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

/**
 * Escalar (arrastar para cima aumenta), rotacionar (arrastar na horizontal),
 * cisalhar (arrastar em X/Y) ou excluir (clicar na forma).
 */
public class FerramentaTransformar implements Ferramenta {
    private final ModoFerramenta modo;
    private double ultimoX, ultimoY;
    private boolean alterou; // um registro no histórico por arrasto

    public FerramentaTransformar(ModoFerramenta modo) {
        this.modo = modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent e, EditorContext ctx) {
        ultimoX = e.getX();
        ultimoY = e.getY();
        alterou = false;
        FormaGeometrica f = ctx.formaEm(e.getX(), e.getY());
        if (modo == ModoFerramenta.EXCLUIR) {
            if (f != null) ctx.excluir(f);
            return;
        }
        if (f != null && !ctx.getSelecionados().contains(f)) ctx.selecionar(f, false);
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }

    /** Converte o movimento do mouse (dx, dy) num pequeno passo da transformação. */
    @Override
    public void aoArrastarMouse(MouseEvent e, EditorContext ctx) {
        if (modo == ModoFerramenta.EXCLUIR || ctx.getSelecionados().isEmpty()) return;
        double dx = e.getX() - ultimoX, dy = e.getY() - ultimoY;
        ultimoX = e.getX();
        ultimoY = e.getY();
        if (!alterou) {
            ctx.registrarEstado();
            alterou = true;
        }
        switch (modo) {
            case ESCALAR -> ctx.escalarSelecionados(Math.max(0.1, 1 - dy * 0.01)); // mouse pra cima (dy < 0) aumenta
            case ROTACIONAR -> ctx.rotacionarSelecionados(dx * 0.01); // ângulo em radianos
            case CISALHAR -> ctx.cisalharSelecionados(dx * 0.005, dy * 0.005);
            default -> { }
        }
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }
}
