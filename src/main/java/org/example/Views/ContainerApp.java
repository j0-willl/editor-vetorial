package org.example.Views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
import org.example.Controller.PersistenciaController;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ContainerApp extends Application {
    private Canvas areaDeDesenho;
    private EditorContext contexto;
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        contexto = new EditorContext();
        areaDeDesenho = new Canvas();
        painelStatus = new PainelStatusView(contexto);
        barraLateral = new BarraLateralView(contexto, this::salvarArquivo, this::carregarArquivo);

        Pane centro = new Pane(areaDeDesenho);
        areaDeDesenho.widthProperty().bind(centro.widthProperty());
        areaDeDesenho.heightProperty().bind(centro.heightProperty());

        ScrollPane lateral = new ScrollPane(barraLateral);
        lateral.setFitToWidth(true);
        lateral.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        BorderPane raiz = new BorderPane(centro, null, null, painelStatus, lateral);
        Scene cena = new Scene(raiz, 1280, 800);

        inicializarEventos(cena);
        stage.setTitle("Editor de Desenho Vetorial - POO 1");
        stage.setScene(cena);
        stage.setOnCloseRequest(e -> {
            if (!confirmarDescarte()) e.consume();
        });
        stage.show();
        redesenharCanvas();
    }

    public void inicializarEventos(Scene cena) {
        contexto.setOnRedraw(this::redesenharCanvas);
        contexto.setOnStats(() -> {
            painelStatus.atualizar();
            barraLateral.atualizarCoresInterface();
        });
        contexto.setOnAviso(msg -> new Alert(Alert.AlertType.WARNING, msg).show());

        areaDeDesenho.widthProperty().addListener(o -> redesenharCanvas());
        areaDeDesenho.heightProperty().addListener(o -> redesenharCanvas());

        // State: o canvas só repassa o evento para a ferramenta ativa.
        areaDeDesenho.setOnMousePressed(e -> contexto.getFerramentaAtual().aoPressionarMouse(e, contexto));
        areaDeDesenho.setOnMouseDragged(e -> {
            painelStatus.setCoordenadas(e.getX(), e.getY());
            contexto.getFerramentaAtual().aoArrastarMouse(e, contexto);
        });
        areaDeDesenho.setOnMouseReleased(e -> contexto.getFerramentaAtual().aoSoltarMouse(e, contexto));
        areaDeDesenho.setOnMouseClicked(e -> contexto.getFerramentaAtual().aoClicar(e, contexto));
        areaDeDesenho.setOnMouseMoved(e -> painelStatus.setCoordenadas(e.getX(), e.getY()));

        var atalhos = cena.getAccelerators();
        atalhos.put(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), contexto::desfazer);
        atalhos.put(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN), contexto::refazer);
        atalhos.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), this::salvarArquivo);
        atalhos.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), contexto::duplicarSelecionados);
        atalhos.put(new KeyCodeCombination(KeyCode.DELETE), contexto::excluirSelecionados);
    }

    private void redesenharCanvas() {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        double w = areaDeDesenho.getWidth(), h = areaDeDesenho.getHeight();
        gc.setLineDashes();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        if (contexto.isGridSnappingHabilitado()) {
            gc.setStroke(Color.web("#e6e6e6"));
            gc.setLineWidth(1);
            for (double x = 0; x < w; x += EditorContext.TAMANHO_GRADE) gc.strokeLine(x, 0, x, h);
            for (double y = 0; y < h; y += EditorContext.TAMANHO_GRADE) gc.strokeLine(0, y, w, y);
        }

        for (FormaGeometrica f : contexto.getFormas()) desenharForma(f, contexto.getSelecionados().contains(f));

        FormaGeometrica emAndamento = contexto.getFormaEmAndamento();
        if (emAndamento != null) {
            gc.setLineDashes(6);
            desenharForma(emAndamento, false);
            gc.setLineDashes();
        }

        double[] caixa = contexto.getCaixaSelecao();
        if (caixa != null) {
            gc.setStroke(Color.DODGERBLUE);
            gc.setLineWidth(1);
            gc.setLineDashes(5);
            gc.strokeRect(Math.min(caixa[0], caixa[2]), Math.min(caixa[1], caixa[3]),
                    Math.abs(caixa[2] - caixa[0]), Math.abs(caixa[3] - caixa[1]));
            gc.setLineDashes();
        }
    }

    private void desenharForma(FormaGeometrica f, boolean selecionada) {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        List<Ponto> pontos = f.getPontos();
        int n = pontos.size();
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = pontos.get(i).getX();
            ys[i] = pontos.get(i).getY();
        }

        gc.setStroke(Color.web(f.getCorBordaHex()));
        gc.setLineWidth(f.getEspessuraBorda());
        if (n < 3) { // polígono ainda sendo desenhado
            gc.strokePolyline(xs, ys, n);
            gc.setFill(Color.RED);
            for (int i = 0; i < n; i++) gc.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
            return;
        }
        gc.setFill(Color.web(f.getCorPreenchimentoHex()));
        gc.fillPolygon(xs, ys, n);
        gc.strokePolygon(xs, ys, n);

        if (selecionada) {
            gc.setStroke(Color.LIME);
            gc.setLineWidth(2);
            gc.strokePolygon(xs, ys, n);
            gc.setFill(Color.RED);
            for (int i = 0; i < n; i++) gc.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
        }
    }

    private void salvarArquivo() {
        FileChooser fc = seletorJson();
        fc.setInitialFileName("desenho.json");
        File arquivo = fc.showSaveDialog(stage);
        if (arquivo == null) return;
        try {
            Files.writeString(arquivo.toPath(), PersistenciaController.paraJson(contexto.getFormas(),
                    areaDeDesenho.getWidth(), areaDeDesenho.getHeight(), contexto.isGridSnappingHabilitado()));
            contexto.marcarSalvo();
        } catch (IOException e) {
            erro("Não foi possível salvar o arquivo.", e.getMessage());
        }
    }

    private void carregarArquivo() {
        if (!confirmarDescarte()) return;
        File arquivo = seletorJson().showOpenDialog(stage);
        if (arquivo == null) return;
        try {
            PersistenciaController.Documento doc = PersistenciaController.deJson(Files.readString(arquivo.toPath()));
            contexto.carregar(doc.formas(), doc.gradeHabilitada());
            barraLateral.atualizarGrade();
        } catch (IOException | IllegalArgumentException e) {
            erro("Não foi possível abrir \"" + arquivo.getName() + "\".", e.getMessage());
        }
    }

    /** true se pode seguir (não há alterações pendentes ou o usuário aceitou descartá-las). */
    private boolean confirmarDescarte() {
        if (!contexto.temAlteracoesNaoSalvas()) return true;
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Existem alterações não salvas. Deseja descartá-las?", ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText("Alterações não salvas");
        return alerta.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    private void erro(String cabecalho, String detalhe) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, detalhe);
        alerta.setHeaderText(cabecalho);
        alerta.showAndWait();
    }

    private static FileChooser seletorJson() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho JSON", "*.json"));
        return fc;
    }
}
