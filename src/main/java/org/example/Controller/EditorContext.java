package org.example.Controller;

import org.example.Models.FormaBase;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Contexto do padrão State e "cérebro" do editor: guarda as formas, a seleção,
 * a ferramenta ativa, o estilo atual e o histórico. Não conhece nenhuma tela.
 */
public class EditorContext {
    public static final double TAMANHO_GRADE = 20; // espaçamento da grade magnética, em pixels

    private List<FormaGeometrica> formas = new ArrayList<>(); // ordem da lista = ordem das camadas
    private final List<FormaGeometrica> selecionados = new ArrayList<>();
    private FormaGeometrica formaEmAndamento; // prévia da forma sendo desenhada
    private Ferramenta ferramentaAtual = new FerramentaSelecionar(); // estado atual (State)
    private boolean gridSnappingHabilitado;

    private final HistoricoController historico = new HistoricoController();
    private boolean alteracoesNaoSalvas;
    private double[] caixaSelecao; // x1, y1, x2, y2 enquanto arrasta a caixa de seleção

    // Estilo usado nas próximas formas desenhadas
    private String corPreenchimentoHex = "#d3d3d3";
    private String corBordaHex = "#000000";
    private double espessuraBorda = 2;

    // Callbacks da View: o controller não conhece as classes de tela.
    private Runnable onRedraw = () -> {};
    private Runnable onStats = () -> {};
    private Consumer<String> onAviso = msg -> {};

    public void requestRedraw() { onRedraw.run(); }
    public void requestStatsUpdate() { onStats.run(); }
    public void avisar(String mensagem) { onAviso.accept(mensagem); }

    /** Grade magnética: arredonda para o múltiplo de 20 mais próximo (se ligada). */
    public double aplicarSnap(double valor) {
        return gridSnappingHabilitado ? Math.round(valor / TAMANHO_GRADE) * TAMANHO_GRADE : valor;
    }

    // ---------- histórico ----------

    /** Tira uma "foto" do desenho antes de qualquer alteração (Memento). */
    public void registrarEstado() {
        historico.registrarEstado(formas);
        alteracoesNaoSalvas = true;
    }

    public void desfazer() {
        formas = historico.desfazer(formas);
        aposHistorico();
    }

    public void refazer() {
        formas = historico.refazer(formas);
        aposHistorico();
    }

    /** Depois de desfazer/refazer, a seleção antiga não vale mais (são objetos novos). */
    private void aposHistorico() {
        selecionados.clear();
        alteracoesNaoSalvas = true;
        requestRedraw();
        requestStatsUpdate();
    }

    /** Substitui o documento inteiro (carregar arquivo). */
    public void carregar(List<FormaGeometrica> novasFormas, boolean grade) {
        formas = new ArrayList<>(novasFormas);
        selecionados.clear();
        formaEmAndamento = null;
        gridSnappingHabilitado = grade;
        historico.limpar();
        alteracoesNaoSalvas = false;
        requestRedraw();
        requestStatsUpdate();
    }

    // ---------- formas e seleção ----------

    public void adicionarForma(FormaGeometrica f) {
        registrarEstado();
        formas.add(f);
    }

    /** Forma mais ao topo sob o cursor, ou null (percorre de trás pra frente). */
    public FormaGeometrica formaEm(double x, double y) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(x, y)) return formas.get(i);
        }
        return null;
    }

    /** Composite: selecionar uma forma de um grupo seleciona o grupo inteiro. */
    public void selecionar(FormaGeometrica f, boolean adicionar) {
        if (!adicionar) selecionados.clear();
        if (f == null) return;
        for (FormaGeometrica g : formas) {
            boolean mesmoGrupo = g == f || (f.getGroupId() != null && f.getGroupId().equals(g.getGroupId()));
            if (mesmoGrupo && !selecionados.contains(g)) selecionados.add(g);
        }
    }

    /** Seleciona as formas cujo centroide está dentro do retângulo arrastado. */
    public void selecionarNaCaixa(double x1, double y1, double x2, double y2, boolean adicionar) {
        if (!adicionar) selecionados.clear();
        double minX = Math.min(x1, x2), maxX = Math.max(x1, x2), minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        for (FormaGeometrica f : new ArrayList<>(formas)) {
            Ponto c = f.getCentroide();
            if (c.getX() >= minX && c.getX() <= maxX && c.getY() >= minY && c.getY() <= maxY) selecionar(f, true);
        }
    }

    public void excluir(FormaGeometrica f) {
        registrarEstado();
        formas.remove(f);
        selecionados.remove(f);
        atualizarTudo();
    }

    public void excluirSelecionados() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        formas.removeAll(selecionados);
        selecionados.clear();
        atualizarTudo();
    }

    /** Composite: dá o mesmo groupId (aleatório e único) a todas as selecionadas. */
    public void agruparSelecionados() {
        if (selecionados.size() < 2) {
            avisar("Selecione ao menos 2 formas para agrupar (Shift + clique ou caixa de seleção).");
            return;
        }
        registrarEstado();
        String id = UUID.randomUUID().toString();
        for (FormaGeometrica f : selecionados) f.setGroupId(id);
        atualizarTudo();
    }

    public void desagruparSelecionados() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        for (FormaGeometrica f : selecionados) f.setGroupId(null);
        atualizarTudo();
    }

    /** Camadas: o fim da lista é desenhado por último, então fica por cima. */
    public void trazerParaFrente() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        formas.removeAll(selecionados);
        formas.addAll(selecionados);
        atualizarTudo();
    }

    /** Camadas: o começo da lista é desenhado primeiro, então fica por baixo. */
    public void enviarParaTras() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        formas.removeAll(selecionados);
        formas.addAll(0, selecionados);
        atualizarTudo();
    }

    /** Prototype: duplica via clonar(); cópias de um grupo formam um grupo novo. */
    public void duplicarSelecionados() {
        if (selecionados.isEmpty()) return;
        registrarEstado();
        Map<String, String> novosGrupos = new HashMap<>(); // groupId antigo -> groupId novo
        List<FormaGeometrica> copias = new ArrayList<>();
        for (FormaGeometrica f : selecionados) {
            FormaGeometrica c = f.clonar();
            if (c.getGroupId() != null) c.setGroupId(novosGrupos.computeIfAbsent(c.getGroupId(), k -> UUID.randomUUID().toString()));
            c.transladar(TAMANHO_GRADE, TAMANHO_GRADE); // desloca para a cópia aparecer
            copias.add(c);
        }
        formas.addAll(copias);
        selecionados.clear();
        selecionados.addAll(copias);
        atualizarTudo();
    }

    // ---------- transformações (o grupo gira/escala em torno do centro comum) ----------

    public void escalarSelecionados(double k) {
        transformarSelecionados(f -> f.escalar(k), v -> new Ponto(v.getX() * k, v.getY() * k));
    }

    public void rotacionarSelecionados(double a) {
        double cos = Math.cos(a), sin = Math.sin(a);
        transformarSelecionados(f -> f.rotacionar(a),
                v -> new Ponto(v.getX() * cos - v.getY() * sin, v.getX() * sin + v.getY() * cos));
    }

    public void cisalharSelecionados(double shx, double shy) {
        transformarSelecionados(f -> f.cisalhar(shx, shy),
                v -> new Ponto(v.getX() + shx * v.getY(), v.getY() + shy * v.getX()));
    }

    /**
     * Cada forma se transforma em torno do próprio centroide; depois é transladada para que o
     * resultado seja o mesmo de transformar em torno do centro da seleção (comportamento de grupo).
     * op = a transformação da forma; linear = a mesma conta aplicada a um vetor.
     */
    private void transformarSelecionados(Consumer<FormaGeometrica> op, UnaryOperator<Ponto> linear) {
        if (selecionados.isEmpty()) return;
        // pivô = média dos centroides das formas selecionadas
        double px = 0, py = 0;
        for (FormaGeometrica f : selecionados) {
            px += f.getCentroide().getX();
            py += f.getCentroide().getY();
        }
        px /= selecionados.size();
        py /= selecionados.size();
        for (FormaGeometrica f : selecionados) {
            Ponto c = f.getCentroide();
            op.accept(f);
            Ponto v = linear.apply(new Ponto(c.getX() - px, c.getY() - py)); // onde o centro da forma deveria ir
            f.transladar(px + v.getX() - c.getX(), py + v.getY() - c.getY());
        }
    }

    // ---------- estilo ----------

    /** Muda o estilo das próximas formas e aplica nas selecionadas. */
    public void setEstilo(String preenchimento, String borda, double espessura) {
        setEstiloAtual(preenchimento, borda, espessura);
        if (selecionados.isEmpty()) return;
        registrarEstado();
        for (FormaGeometrica f : selecionados) {
            f.setCorPreenchimentoHex(preenchimento);
            f.setCorBordaHex(borda);
            f.setEspessuraBorda(espessura);
        }
        requestRedraw();
    }

    /** Só muda o estilo das próximas formas (usado quando a seleção muda). */
    public void setEstiloAtual(String preenchimento, String borda, double espessura) {
        corPreenchimentoHex = preenchimento;
        corBordaHex = borda;
        espessuraBorda = espessura;
    }

    private void atualizarTudo() {
        requestRedraw();
        requestStatsUpdate();
    }

    // ---------- getters / setters ----------

    public List<FormaGeometrica> getFormas() { return formas; }
    public List<FormaGeometrica> getSelecionados() { return selecionados; }
    public FormaGeometrica getFormaEmAndamento() { return formaEmAndamento; }
    public Ferramenta getFerramentaAtual() { return ferramentaAtual; }
    public boolean isGridSnappingHabilitado() { return gridSnappingHabilitado; }
    public boolean temAlteracoesNaoSalvas() { return alteracoesNaoSalvas; }
    public double[] getCaixaSelecao() { return caixaSelecao; }

    /** Forma sendo desenhada (prévia). Recebe o estilo atual. */
    public void setFormaEmAndamento(FormaGeometrica f) {
        if (f != null) {
            f.setCorPreenchimentoHex(corPreenchimentoHex);
            f.setCorBordaHex(corBordaHex);
            f.setEspessuraBorda(espessuraBorda);
        }
        formaEmAndamento = f;
    }

    /** State: troca a ferramenta. Finaliza o polígono em andamento se ele passar na validação. */
    public void setFerramentaAtual(Ferramenta ferramenta) {
        if (formaEmAndamento != null) {
            try {
                ((FormaBase) formaEmAndamento).validar(); // mesma regra do duplo clique
                adicionarForma(formaEmAndamento);
            } catch (IllegalStateException ex) {
                avisar("Polígono descartado: " + ex.getMessage());
            }
        }
        ferramentaAtual = ferramenta;
        formaEmAndamento = null;
        caixaSelecao = null;
        requestRedraw();
        requestStatsUpdate();
    }

    public void setGridSnappingHabilitado(boolean habilitado) { gridSnappingHabilitado = habilitado; }
    public void setCaixaSelecao(double[] caixa) { caixaSelecao = caixa; }
    public void marcarSalvo() { alteracoesNaoSalvas = false; }

    // A View registra aqui o que fazer quando o Controller pedir redesenho, estatísticas ou aviso
    public void setOnRedraw(Runnable r) { onRedraw = r; }
    public void setOnStats(Runnable r) { onStats = r; }
    public void setOnAviso(Consumer<String> c) { onAviso = c; }
}
