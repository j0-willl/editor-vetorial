package org.example;

import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Controller.EditorContext;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.HistoricoController;
import org.example.Controller.PersistenciaController;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Testes automáticos (JUnit 5) das regras, cálculos e padrões. Rodar com: mvnw test */
class EditorTest {
    private static final double EPS = 1e-9; // margem de erro para comparar doubles

    private static Poligono triangulo() { // catetos 4 e 3: área 6, perímetro 12
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(4, 0));
        p.adicionarPonto(new Ponto(0, 3));
        return p;
    }

    /** Gauss e perímetro no triângulo 3-4-5 e num quadrado de lado 10. */
    @Test
    void areaEPerimetro() {
        assertEquals(6, triangulo().calcularArea(), EPS);
        assertEquals(12, triangulo().calcularPerimetro(), EPS);
        Quadrado q = new Quadrado(new Ponto(0, 0), 5 * Math.sqrt(2)); // lado 10
        assertEquals(100, q.calcularArea(), 1e-6);
        assertEquals(40, q.calcularPerimetro(), 1e-6);
    }

    /** Polígono com 2 pontos e círculo com raio 0 ou negativo são recusados. */
    @Test
    void validacoes() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(1, 1));
        assertThrows(IllegalStateException.class, p::validar);
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), -1));
    }

    /** Escala 2 quadruplica a área; rotação e cisalhamento mantêm a área; translação move o centro. */
    @Test
    void transformacoes() {
        Poligono p = triangulo();
        p.escalar(2);
        assertEquals(24, p.calcularArea(), 1e-6);
        p.rotacionar(Math.PI / 3);
        assertEquals(24, p.calcularArea(), 1e-6);
        Ponto c = p.getCentroide();
        p.transladar(10, -5);
        assertEquals(c.getX() + 10, p.getCentroide().getX(), 1e-6);
        assertEquals(c.getY() - 5, p.getCentroide().getY(), 1e-6);
        p.cisalhar(0.5, 0); // cisalhamento preserva área
        assertEquals(24, p.calcularArea(), 1e-6);
    }

    /** Prototype: mexer na cópia não pode mexer na original. */
    @Test
    void clonarEhCopiaProfunda() {
        Poligono original = triangulo();
        FormaGeometrica copia = original.clonar();
        copia.transladar(100, 100);
        copia.setCorPreenchimentoHex("#ff0000");
        assertEquals(0, original.getPontos().get(0).getX(), EPS);
        assertEquals("#d3d3d3", original.getCorPreenchimentoHex());
        assertInstanceOf(Poligono.class, copia);
    }

    /** Memento: desfazer volta a posição antiga e refazer recupera a nova. */
    @Test
    void desfazerRefazer() {
        HistoricoController h = new HistoricoController();
        List<FormaGeometrica> formas = new ArrayList<>(List.of(triangulo()));
        h.registrarEstado(formas);
        formas.get(0).transladar(50, 0);
        List<FormaGeometrica> desfeito = h.desfazer(formas);
        assertEquals(0, desfeito.get(0).getPontos().get(0).getX(), EPS);
        List<FormaGeometrica> refeito = h.refazer(desfeito);
        assertEquals(50, refeito.get(0).getPontos().get(0).getX(), EPS);
    }

    /** Trocar de ferramenta salva o polígono em andamento se ele tiver 3+ pontos. */
    @Test
    void trocarFerramentaFinalizaPoligonoValido() {
        EditorContext ctx = new EditorContext();
        ctx.setFormaEmAndamento(triangulo());
        ctx.setFerramentaAtual(new org.example.Controller.FerramentaSelecionar());
        assertEquals(1, ctx.getFormas().size());
        assertNull(ctx.getFormaEmAndamento());
    }

    /** Evento de mouse falso, para simular cliques e arrastos nos testes. */
    private static MouseEvent mouse(EventType<MouseEvent> tipo, double x, double y) {
        return mouse(tipo, x, y, 1);
    }

    /** cliques = 1 para clique simples, 2 para duplo clique e assim por diante. */
    private static MouseEvent mouse(EventType<MouseEvent> tipo, double x, double y, int cliques) {
        return new MouseEvent(tipo, x, y, x, y, MouseButton.PRIMARY, cliques,
                false, false, false, false, true, false, false, false, false, true, null);
    }

    /** Vários cliques no mesmo ponto não viram vértices repetidos, e uma "linha" nunca vira forma. */
    @Test
    void cliquesRepetidosNaoGeramLinha() {
        EditorContext ctx = new EditorContext();
        FerramentaDesenhar poligono = new FerramentaDesenhar();

        poligono.aoClicar(mouse(MouseEvent.MOUSE_CLICKED, 0, 0, 1), ctx);
        for (int i = 0; i < 4; i++) poligono.aoClicar(mouse(MouseEvent.MOUSE_CLICKED, 50, 0, 1), ctx);
        assertEquals(2, ctx.getFormaEmAndamento().getQuantidadePontos()); // e não 5

        poligono.aoClicar(mouse(MouseEvent.MOUSE_CLICKED, 50, 0, 2), ctx); // duplo clique com 2 pontos: não fecha
        poligono.aoClicar(mouse(MouseEvent.MOUSE_CLICKED, 100, 0, 1), ctx); // 3º ponto, mas alinhado
        poligono.aoClicar(mouse(MouseEvent.MOUSE_CLICKED, 100, 0, 2), ctx);
        assertTrue(ctx.getFormas().isEmpty()); // pontos alinhados = área zero, recusado

        ctx.setFerramentaAtual(new org.example.Controller.FerramentaSelecionar()); // trocar também não salva a linha
        assertTrue(ctx.getFormas().isEmpty());
    }

    /** Com a grade ligada, dá pra criar círculo pequeno; um clique sem arrasto usa o tamanho padrão. */
    @Test
    void circuloPequenoComGradeLigada() {
        EditorContext ctx = new EditorContext();
        ctx.setGridSnappingHabilitado(true);
        FerramentaInserirForma circulo = new FerramentaInserirForma(FerramentaInserirForma.Tipo.CIRCULO);

        circulo.aoPressionarMouse(mouse(MouseEvent.MOUSE_PRESSED, 101, 99), ctx); // centro gruda em (100, 100)
        circulo.aoArrastarMouse(mouse(MouseEvent.MOUSE_DRAGGED, 106, 100), ctx);
        circulo.aoSoltarMouse(mouse(MouseEvent.MOUSE_RELEASED, 106, 100), ctx);
        FormaGeometrica pequeno = ctx.getFormas().get(0);
        assertEquals(100, pequeno.getCentroide().getX(), 1e-6);
        assertEquals(6, pequeno.getPontos().get(0).distancia(pequeno.getCentroide()), 1e-6); // raio 6, não 60

        circulo.aoPressionarMouse(mouse(MouseEvent.MOUSE_PRESSED, 300, 300), ctx);
        circulo.aoSoltarMouse(mouse(MouseEvent.MOUSE_RELEASED, 300, 300), ctx);
        FormaGeometrica padrao = ctx.getFormas().get(1);
        assertEquals(60, padrao.getPontos().get(0).distancia(padrao.getCentroide()), 1e-6);
    }

    /** Composite: dois quadrados agrupados escalam em torno do centro do grupo (x = 50). */
    @Test
    void grupoEscalaEmTornoDoCentroComum() {
        EditorContext ctx = new EditorContext();
        Quadrado a = new Quadrado(new Ponto(0, 0), 10), b = new Quadrado(new Ponto(100, 0), 10);
        ctx.adicionarForma(a);
        ctx.adicionarForma(b);
        ctx.selecionar(a, false);
        ctx.selecionar(b, true);
        ctx.agruparSelecionados();
        ctx.selecionar(a, false); // seleciona o grupo todo
        assertEquals(2, ctx.getSelecionados().size());
        ctx.escalarSelecionados(2);
        assertEquals(-50, a.getCentroide().getX(), 1e-6);
        assertEquals(150, b.getCentroide().getX(), 1e-6);
    }

    /** Salvar e carregar mantém tipo, grupo e área; JSON quebrado ou forma inválida dá erro. */
    @Test
    void jsonIdaEVoltaEArquivoCorrompido() {
        Circulo c = new Circulo(new Ponto(10, 10), 5);
        c.setGroupId("g1");
        String json = PersistenciaController.paraJson(List.of(triangulo(), c), 800, 600, true);
        PersistenciaController.Documento doc = PersistenciaController.deJson(json);
        assertTrue(doc.gradeHabilitada());
        assertEquals(2, doc.formas().size());
        assertInstanceOf(Circulo.class, doc.formas().get(1));
        assertEquals("g1", doc.formas().get(1).getGroupId());
        assertEquals(c.calcularArea(), doc.formas().get(1).calcularArea(), 1e-6);

        assertThrows(IllegalArgumentException.class, () -> PersistenciaController.deJson("{ lixo"));
        assertThrows(IllegalArgumentException.class, () -> PersistenciaController.deJson(
                "{\"canvas\":{\"gradeHabilitada\":false},\"formas\":[{\"tipo\":\"Poligono\",\"pontos\":[{\"x\":0,\"y\":0}]}]}"));
    }
}
