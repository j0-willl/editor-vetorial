package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

/** Polígono por cliques: cada clique adiciona um vértice, duplo clique fecha a forma. */
public class FerramentaDesenhar implements Ferramenta {
    private static final double DISTANCIA_MINIMA = 3; // px entre um vértice e o anterior

    @Override
    public void aoClicar(MouseEvent e, EditorContext ctx) {
        if (!e.isStillSincePress()) return; // ignora "clique" que na verdade foi um arrasto
        // o polígono em andamento fica guardado no contexto (para a tela desenhar a prévia)
        Poligono p = ctx.getFormaEmAndamento() instanceof Poligono emAndamento ? emAndamento : null;

        if (e.getClickCount() >= 2) { // duplo clique fecha a forma; o 3º, 4º... clique rápido é ignorado
            if (e.getClickCount() == 2 && p != null) finalizar(p, ctx);
            return;
        }
        Ponto novo = new Ponto(ctx.aplicarSnap(e.getX()), ctx.aplicarSnap(e.getY()));
        if (p == null) { // primeiro clique começa um polígono novo
            p = new Poligono();
            ctx.setFormaEmAndamento(p);
        } else if (p.getPontos().get(p.getQuantidadePontos() - 1).distancia(novo) < DISTANCIA_MINIMA) {
            return; // clique repetido no mesmo lugar não vira ponto novo
        }
        p.adicionarPonto(novo);
        ctx.requestRedraw();
    }

    /** Valida (mínimo 3 pontos e área maior que zero) e, se estiver ok, adiciona o polígono ao desenho. */
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
