package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Memento: cada estado é uma cópia profunda (via clonar) da lista de formas. */
public class HistoricoController {
    private final Stack<List<FormaGeometrica>> undoStack = new Stack<>();
    private final Stack<List<FormaGeometrica>> redoStack = new Stack<>();

    /** Chamar ANTES de alterar as formas. */
    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(copiar(formas));
        redoStack.clear();
    }

    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) return atual;
        redoStack.push(copiar(atual));
        return undoStack.pop();
    }

    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) return atual;
        undoStack.push(copiar(atual));
        return redoStack.pop();
    }

    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }

    private static List<FormaGeometrica> copiar(List<FormaGeometrica> formas) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica f : formas) copia.add(f.clonar());
        return copia;
    }
}
