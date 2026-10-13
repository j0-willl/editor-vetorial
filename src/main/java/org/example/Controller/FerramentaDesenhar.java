package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

/** Polígono por cliques: cada clique adiciona um vértice, duplo clique fecha a forma. */
public class FerramentaDesenhar implements Ferramenta {

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
        if (!e.isStillSincePress()) return;
        Poligono p = ctx.getFormaEmAndamento() instanceof Poligono emAndamento ? emAndamento : null;

        if (e.getClickCount() >= 2) {
            if (p != null) finalizar(p, ctx);
            return;
        }
        if (p == null) {
            p = new Poligono();
            ctx.setFormaEmAndamento(p);
        }
        p.adicionarPonto(new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY())));
        ctx.requestRedraw();
    }

    private void finalizar(Poligono p, EditorContext ctx) {
        try {
            p.validar();
        } catch (IllegalStateException ex) {
            ctx.avisar(ex.getMessage() + " Continue clicando para adicionar vértices.");
            return;
        }
        ctx.setFormaEmAndamento(null);
        ctx.adicionarForma(p);
        ctx.selecionar(p, false);
        ctx.requestRedraw();
        ctx.requestStatsUpdate();
    }
}
