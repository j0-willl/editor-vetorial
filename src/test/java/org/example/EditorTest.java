package org.example;

import org.example.Controller.EditorContext;
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

class EditorTest {
    private static final double EPS = 1e-9;

    private static Poligono triangulo() { // catetos 4 e 3: área 6, perímetro 12
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(4, 0));
        p.adicionarPonto(new Ponto(0, 3));
        return p;
    }

    @Test
    void areaEPerimetro() {
        assertEquals(6, triangulo().calcularArea(), EPS);
        assertEquals(12, triangulo().calcularPerimetro(), EPS);
        Quadrado q = new Quadrado(new Ponto(0, 0), 5 * Math.sqrt(2)); // lado 10
        assertEquals(100, q.calcularArea(), 1e-6);
        assertEquals(40, q.calcularPerimetro(), 1e-6);
    }

    @Test
    void validacoes() {
        Poligono p = new Poligono();
        p.adicionarPonto(new Ponto(0, 0));
        p.adicionarPonto(new Ponto(1, 1));
        assertThrows(IllegalStateException.class, p::validar);
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), -1));
    }

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

    @Test
    void trocarFerramentaFinalizaPoligonoValido() {
        EditorContext ctx = new EditorContext();
        ctx.setFormaEmAndamento(triangulo());
        ctx.setFerramentaAtual(new org.example.Controller.FerramentaSelecionar());
        assertEquals(1, ctx.getFormas().size());
        assertNull(ctx.getFormaEmAndamento());
    }

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
