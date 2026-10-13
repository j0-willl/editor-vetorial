# Editor de Desenho Vetorial Interativo (JavaFX)

Avaliação Prática N3 — Programação Orientada a Objetos 1 — UniRV 2026/1.

Editor desktop para criar, editar, transformar e salvar formas geométricas 2D.

## Requisitos

- Java 17 ou superior (JDK)
- Apache Maven 3.8+

## Como executar

```bash
mvn javafx:run
```

Testes automatizados (geometria, validações, clone, desfazer/refazer, grupos e JSON):

```bash
mvn test
```

Pela IDE, rode a classe `org.example.Main`.

## Estrutura de pacotes

| Pacote | Conteúdo |
|---|---|
| `org.example.Models` | `FormaGeometrica`, `FormaBase`, `Ponto`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono` |
| `org.example.Controller` | `EditorContext`, `HistoricoController`, `PersistenciaController`, `Ferramenta` e ferramentas concretas, `ModoFerramenta` |
| `org.example.Views` | `ContainerApp`, `BarraLateralView`, `PainelStatusView` |

## Diagrama de classes

O diagrama UML completo está em [`diagrama_classes.pdf`](diagrama_classes.pdf) (fonte Mermaid em `docs/diagrama_classes.html`).

## Padrões de projeto

- **State** — `EditorContext` guarda a `ferramentaAtual` (`Ferramenta`). O canvas só repassa os eventos do mouse para ela, sem `if/switch` por ferramenta. Estados: `FerramentaDesenhar`, `FerramentaSelecionar`, `FerramentaTransformar` (escalar, rotacionar, cisalhar, excluir), `FerramentaEditarVertice` e `FerramentaInserirForma`.
- **Memento (Desfazer/Refazer)** — `HistoricoController.registrarEstado` empilha uma cópia profunda da lista de formas antes de cada alteração; `desfazer`/`refazer` trocam estados entre `undoStack` e `redoStack`.
- **Prototype** — `clonar()` em cada forma (e em `Ponto`) cria cópias sem compartilhar referências. É usado pelo histórico e pelo botão *Duplicar*.
- **Composite** — formas com o mesmo `groupId` são selecionadas juntas e transladadas, escaladas, rotacionadas e cisalhadas em torno do centro comum do grupo, como uma única forma.

## Regras de negócio

- Um polígono precisa de no mínimo 3 pontos (`FormaBase.validar`).
- Círculo, quadrado e hexágono exigem raio `r > 0`; os vértices são calculados automaticamente.
- A área usa a fórmula de Gauss; o perímetro é a soma das distâncias euclidianas.
- A translação soma `dx`/`dy` a cada ponto. Escala, rotação (radianos) e cisalhamento são feitos em torno do centroide.

## Como usar

| Ação | Como |
|---|---|
| Polígono | Ferramenta *Polígono*: um clique por vértice, duplo clique (ou trocar de ferramenta) para fechar |
| Quadrado / círculo / hexágono | Pressione no centro e arraste até o raio (um clique simples insere no tamanho padrão) |
| Selecionar / mover | Clique na forma (Shift adiciona à seleção), arraste para mover, arraste no vazio para a caixa de seleção |
| Escalar / Rotacionar / Cisalhar | Arraste sobre a forma (para cima aumenta / na horizontal gira / em X e Y inclina) |
| Editar vértice | Arraste um vértice (pontos vermelhos) |
| Grade magnética | Checkbox *Grade Magnética*: coordenadas arredondadas para a grade de 20 px |
| Estilo | Cores e espessura valem para as próximas formas e para as selecionadas |
| Atalhos | Ctrl+Z desfazer, Ctrl+Y refazer, Ctrl+S salvar, Ctrl+D duplicar, Del excluir selecionados |

A barra de status mostra coordenadas do cursor, total de formas e área/perímetro da seleção em tempo real.

## Persistência (JSON)

```json
{
  "versao": 1,
  "canvas": { "largura": 1050, "altura": 770, "gradeHabilitada": false, "tamanhoGrade": 20 },
  "formas": [
    {
      "tipo": "Quadrado",
      "pontos": [ { "x": 120, "y": 80 }, { "x": 40, "y": 80 }, { "x": 40, "y": 0 }, { "x": 120, "y": 0 } ],
      "corPreenchimentoHex": "#d3d3d3",
      "corBordaHex": "#000000",
      "espessuraBorda": 2.0,
      "groupId": "6f1c…"
    }
  ]
}
```

Arquivos corrompidos ou inválidos (JSON quebrado, tipo desconhecido, formas com menos de 3 pontos, cores inválidas) mostram um alerta explicativo e não fecham a aplicação. Ao fechar ou carregar com alterações não salvas, o editor pede confirmação.
