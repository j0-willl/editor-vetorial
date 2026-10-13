package org.example.Views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;

public class PainelStatusView extends HBox {
    private final EditorContext contexto;
    private final Label coordenadas = new Label("X: 0  Y: 0");
    private final Label totais = new Label();
    private final Label medidas = new Label();

    public PainelStatusView(EditorContext contexto) {
        super(30);
        this.contexto = contexto;
        setPadding(new Insets(4, 10, 4, 10));
        setStyle("-fx-background-color: #e8e8e8; -fx-border-color: #c8c8c8; -fx-border-width: 1 0 0 0;");
        getChildren().addAll(coordenadas, totais, medidas);
        atualizar();
    }

    public void atualizar() {
        double area = 0, perimetro = 0;
        for (FormaGeometrica f : contexto.getSelecionados()) {
            area += f.calcularArea();
            perimetro += f.calcularPerimetro();
        }
        totais.setText("Formas: " + contexto.getFormas().size() + "  |  Selecionadas: " + contexto.getSelecionados().size());
        medidas.setText(String.format("Área: %.2f px²  |  Perímetro: %.2f px", area, perimetro));
    }

    public void setCoordenadas(double x, double y) {
        coordenadas.setText(String.format("X: %.0f  Y: %.0f", contexto.aplicarSnap(x), contexto.aplicarSnap(y)));
    }
}
