package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** Memento: cada estado é uma cópia profunda (via clonar) da lista de formas. */
public class HistoricoController {
    // Pilhas (LIFO): o último estado salvo é o primeiro a voltar
    private final Stack<List<FormaGeometrica>> undoStack = new Stack<>();
    private final Stack<List<FormaGeometrica>> redoStack = new Stack<>();

    /** Chamar ANTES de alterar as formas. Uma ação nova invalida o refazer. */
    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(copiar(formas));
        redoStack.clear();
    }

    /** Guarda o estado atual no refazer e devolve o último estado salvo. */
    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) return atual;
        redoStack.push(copiar(atual));
        return undoStack.pop();
    }

    /** O inverso do desfazer. */
    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) return atual;
        undoStack.push(copiar(atual));
        return redoStack.pop();
    }

    /** Usado ao carregar um arquivo: o histórico do desenho anterior não vale mais. */
    public void limpar() {
        undoStack.clear();
        redoStack.clear();
    }

    /** Cópia profunda: clona cada forma (Prototype), assim a "foto" não muda depois. */
    private static List<FormaGeometrica> copiar(List<FormaGeometrica> formas) {
        List<FormaGeometrica> copia = new ArrayList<>();
        for (FormaGeometrica f : formas) copia.add(f.clonar());
        return copia;
    }
}
