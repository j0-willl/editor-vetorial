package org.example;

import javafx.application.Application;
import org.example.Views.ContainerApp;

/**
 * Ponto de entrada do programa. Fica separado da ContainerApp porque, se a classe
 * principal estender Application, o Java reclama que faltam os componentes do JavaFX
 * ao rodar pela IDE.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(ContainerApp.class, args); // o JavaFX cria a ContainerApp e chama start()
    }
}
