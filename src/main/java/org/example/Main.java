package org.example;

import javafx.application.Application;
import org.example.Views.ContainerApp;

/** Lançador separado: permite rodar pela IDE sem configurar module-path. */
public class Main {
    public static void main(String[] args) {
        Application.launch(ContainerApp.class, args);
    }
}
