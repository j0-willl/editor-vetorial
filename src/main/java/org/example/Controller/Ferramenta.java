package org.example.Controller;

import javafx.scene.input.MouseEvent;

/** State: cada ferramenta é um estado do EditorContext. Eventos não usados ficam sem efeito. */
public interface Ferramenta {
    default void aoPressionarMouse(MouseEvent e, EditorContext ctx) {}
    default void aoArrastarMouse(MouseEvent e, EditorContext ctx) {}
    default void aoSoltarMouse(MouseEvent e, EditorContext ctx) {}
    default void aoClicar(MouseEvent e, EditorContext ctx) {}
}
