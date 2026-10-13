package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;
import org.example.Controller.ModoFerramenta;
import org.example.Models.FormaGeometrica;

public class BarraLateralView extends VBox {
    private final EditorContext contexto;
    private final ToggleGroup grupoFerramentas = new ToggleGroup();
    private final ColorPicker corPreenchimento = new ColorPicker(Color.LIGHTGRAY);
    private final ColorPicker corBorda = new ColorPicker(Color.BLACK);
    private final Slider espessura = new Slider(1, 10, 2);
    private final CheckBox grade = new CheckBox("Grade Magnética (Snapping)");
    private boolean atualizandoInterface; // evita que mudanças programáticas virem ações do usuário

    public BarraLateralView(EditorContext contexto, Runnable salvar, Runnable carregar) {
        super(6);
        this.contexto = contexto;
        setPadding(new Insets(10));
        setPrefWidth(230);
        setStyle("-fx-background-color: #f0f0f0;");

        construirSecaoEstilos();
        construirSecaoFerramentas();
        construirSecaoCamadas();

        titulo("SISTEMA");
        botao("↶ Desfazer (Ctrl+Z)", contexto::desfazer);
        botao("↷ Refazer (Ctrl+Y)", contexto::refazer);
        botao("Salvar (Ctrl+S)", salvar);
        botao("Carregar", carregar);
    }

    /** Mostra o estilo da primeira forma selecionada, que passa a ser o estilo das próximas formas. */
    public void atualizarCoresInterface() {
        if (contexto.getSelecionados().isEmpty()) return;
        FormaGeometrica f = contexto.getSelecionados().get(0);
        atualizandoInterface = true;
        corPreenchimento.setValue(Color.web(f.getCorPreenchimentoHex()));
        corBorda.setValue(Color.web(f.getCorBordaHex()));
        espessura.setValue(f.getEspessuraBorda());
        atualizandoInterface = false;
        contexto.setEstiloAtual(f.getCorPreenchimentoHex(), f.getCorBordaHex(), f.getEspessuraBorda());
    }

    public void atualizarGrade() {
        grade.setSelected(contexto.isGridSnappingHabilitado());
    }

    private void construirSecaoEstilos() {
        titulo("ESTILOS");
        espessura.setShowTickLabels(true);
        espessura.setMajorTickUnit(9);
        corPreenchimento.setOnAction(e -> aplicarEstilo());
        corBorda.setOnAction(e -> aplicarEstilo());
        espessura.setOnMouseReleased(e -> aplicarEstilo()); // um registro no histórico por arrasto
        grade.setOnAction(e -> {
            contexto.setGridSnappingHabilitado(grade.isSelected());
            contexto.requestRedraw();
        });
        getChildren().addAll(new Label("Cor Preenchimento:"), corPreenchimento,
                new Label("Cor Borda:"), corBorda,
                new Label("Espessura da Borda:"), espessura, grade);
    }

    private void construirSecaoFerramentas() {
        titulo("FERRAMENTAS");
        ferramenta("Polígono (cliques)", new FerramentaDesenhar());
        ToggleButton selecionar = ferramenta("Selecionar / Mover", new FerramentaSelecionar());
        ferramenta("Escalar", new FerramentaTransformar(ModoFerramenta.ESCALAR));
        ferramenta("Rotacionar", new FerramentaTransformar(ModoFerramenta.ROTACIONAR));
        ferramenta("Cisalhar", new FerramentaTransformar(ModoFerramenta.CISALHAR));
        ferramenta("Excluir (clique)", new FerramentaTransformar(ModoFerramenta.EXCLUIR));
        ferramenta("Editar Vértice", new FerramentaEditarVertice());
        botao("Excluir Selecionados (Del)", contexto::excluirSelecionados);

        titulo("FORMAS");
        ferramenta("Inserir Quadrado", new FerramentaInserirForma(FerramentaInserirForma.Tipo.QUADRADO));
        ferramenta("Inserir Círculo", new FerramentaInserirForma(FerramentaInserirForma.Tipo.CIRCULO));
        ferramenta("⬡ Inserir Hexágono", new FerramentaInserirForma(FerramentaInserirForma.Tipo.HEXAGONO));

        selecionar.fire();
    }

    private void construirSecaoCamadas() {
        titulo("CAMADAS/GRUPO");
        botao("⬆ Trazer p/ Frente", contexto::trazerParaFrente);
        botao("⬇ Enviar p/ Trás", contexto::enviarParaTras);
        botao("Agrupar", contexto::agruparSelecionados);
        botao("Desagrupar", contexto::desagruparSelecionados);
        botao("Duplicar (Ctrl+D)", contexto::duplicarSelecionados);
    }

    private void aplicarEstilo() {
        if (!atualizandoInterface) {
            contexto.setEstilo(hex(corPreenchimento.getValue()), hex(corBorda.getValue()), espessura.getValue());
        }
    }

    private ToggleButton ferramenta(String nome, Ferramenta f) {
        ToggleButton b = new ToggleButton(nome);
        b.setToggleGroup(grupoFerramentas);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> {
            b.setSelected(true); // clicar de novo não deixa nenhuma ferramenta ativa
            contexto.setFerramentaAtual(f);
        });
        getChildren().add(b);
        return b;
    }

    private void botao(String nome, Runnable acao) {
        Button b = new Button(nome);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> acao.run());
        getChildren().add(b);
    }

    private void titulo(String texto) {
        Label l = new Label("--- " + texto + " ---");
        l.setMaxWidth(Double.MAX_VALUE);
        l.setAlignment(Pos.CENTER);
        l.setPadding(new Insets(8, 0, 2, 0));
        getChildren().add(l);
    }

    static String hex(Color c) {
        return String.format("#%02x%02x%02x",
                Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
    }
}
