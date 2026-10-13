package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.Models.Circulo;
import org.example.Models.FormaBase;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Poligono;
import org.example.Models.Quadrado;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Conversão do documento para/de JSON. */
public class PersistenciaController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Class<? extends FormaBase>> TIPOS = Map.of(
            "Poligono", Poligono.class,
            "Circulo", Circulo.class,
            "Quadrado", Quadrado.class,
            "Hexagono", Hexagono.class);

    public record Documento(boolean gradeHabilitada, List<FormaGeometrica> formas) {}

    public static String paraJson(List<FormaGeometrica> formas, double largura, double altura, boolean grade) {
        JsonObject canvas = new JsonObject();
        canvas.addProperty("largura", largura);
        canvas.addProperty("altura", altura);
        canvas.addProperty("gradeHabilitada", grade);
        canvas.addProperty("tamanhoGrade", EditorContext.TAMANHO_GRADE);

        JsonArray lista = new JsonArray();
        for (FormaGeometrica f : formas) {
            JsonObject o = GSON.toJsonTree(f).getAsJsonObject();
            o.addProperty("tipo", f.getClass().getSimpleName());
            lista.add(o);
        }

        JsonObject raiz = new JsonObject();
        raiz.addProperty("versao", 1);
        raiz.add("canvas", canvas);
        raiz.add("formas", lista);
        return GSON.toJson(raiz);
    }

    /** @throws IllegalArgumentException se o conteúdo estiver corrompido ou violar as regras das formas. */
    public static Documento deJson(String json) {
        try {
            JsonObject raiz = JsonParser.parseString(json).getAsJsonObject();
            boolean grade = raiz.getAsJsonObject("canvas").get("gradeHabilitada").getAsBoolean();
            List<FormaGeometrica> formas = new ArrayList<>();
            for (JsonElement el : raiz.getAsJsonArray("formas")) {
                JsonObject o = el.getAsJsonObject();
                Class<? extends FormaBase> classe = TIPOS.get(o.get("tipo").getAsString());
                if (classe == null) throw new IllegalArgumentException("tipo de forma desconhecido: " + o.get("tipo"));
                FormaBase f = GSON.fromJson(o, classe);
                f.validar();
                formas.add(f);
            }
            return new Documento(grade, formas);
        } catch (RuntimeException e) { // JsonParseException, NullPointerException, IllegalStateException...
            throw new IllegalArgumentException("Arquivo corrompido ou inválido: " + e.getMessage(), e);
        }
    }
}
