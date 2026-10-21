# Editor de Desenho Vetorial (JavaFX)

Avaliação Prática N2 — Programação Orientada a Objetos — UniRV 2026.

**Feito por:** Jorge Willian Cunha Oliveira

Esse é o meu editor de desenho vetorial feito em Java com JavaFX. Dá pra desenhar polígonos, quadrados, círculos e hexágonos, mexer neles (mover, aumentar, girar, entortar), mudar as cores, agrupar formas, desfazer as besteiras com Ctrl+Z e salvar tudo num arquivo JSON pra abrir depois.

Foi o trabalho onde eu mais usei padrões de projeto até agora, então tentei explicar aqui embaixo onde cada um aparece.

## O que precisa ter instalado

- Java 17 ou mais novo (JDK)
- Maven não é obrigatório: o projeto já vem com o Maven Wrapper (`mvnw`), que baixa sozinho o que precisa

## Como rodar

No terminal, dentro da pasta do projeto:

```bash
./mvnw javafx:run
```

No Windows também dá pra usar `.\mvnw.cmd javafx:run` ou só dar dois cliques no `rodar.bat`.

Pela IDE (IntelliJ, VS Code, Eclipse...), é só rodar a classe `org.example.Main`. Rodar direto a `ContainerApp` dá o erro "JavaFX runtime components are missing", por isso criei a `Main` separada.

Também fiz uns testes automáticos pra conferir as contas de área e perímetro, as validações, o clone, o desfazer/refazer, os grupos e o salvar/carregar:

```bash
./mvnw test
```

## Como o código tá organizado

Separei em três pacotes, do jeito que o professor pediu:

| Pacote | O que tem lá |
|---|---|
| `org.example.Models` | As formas: `FormaGeometrica` (interface), `FormaBase`, `Ponto`, `Poligono`, `Circulo`, `Quadrado` e `Hexagono` |
| `org.example.Controller` | A lógica: `EditorContext`, `HistoricoController`, `PersistenciaController`, a interface `Ferramenta` e as ferramentas |
| `org.example.Views` | As telas: `ContainerApp`, `BarraLateralView` e `PainelStatusView` |

Os Models não sabem nada de JavaFX, e o Controller não conhece as telas: ele só avisa "redesenha aí" por uns callbacks, e a View é que se vira.

O diagrama de classes completo tá no [`diagrama_classes.pdf`](diagrama_classes.pdf). Fiz ele com Mermaid, e o arquivo fonte tá em `docs/diagrama_classes.html`, caso precise gerar de novo.

## Os padrões de projeto

- **State:** cada ferramenta (desenhar, selecionar, transformar, editar vértice, inserir forma) é uma classe que implementa `Ferramenta`. O `EditorContext` guarda qual tá ativa, e o canvas só repassa o clique pra ela. Assim não precisei de um `if` gigante pra saber qual botão o usuário escolheu.
- **Memento:** antes de qualquer mudança, o `HistoricoController` guarda uma cópia de todas as formas numa pilha. O Ctrl+Z pega a cópia anterior e o Ctrl+Y volta.
- **Prototype:** toda forma tem o método `clonar()`, que cria uma cópia de verdade, sem compartilhar os pontos com a original. É ele que o histórico usa, e também o botão *Duplicar*.
- **Composite:** quando agrupa formas, todas recebem o mesmo `groupId`. Aí clicar numa seleciona o grupo todo, e mover, girar ou aumentar funciona como se fosse uma forma só, em volta do centro do grupo.

## Regras das formas

- Polígono precisa ter pelo menos 3 pontos. Com menos, o editor avisa e não deixa criar.
- Círculo, quadrado e hexágono precisam de raio maior que zero, e os vértices são calculados sozinhos.
- A área é calculada com a fórmula de Gauss (a do "cadarço"), e o perímetro é a soma das distâncias entre os pontos.
- Mover soma `dx` e `dy` em todos os pontos. Aumentar, girar e entortar acontecem em volta do centro da forma.
- O círculo, na real, é um polígono de 64 lados. Assim ele aceita todas as transformações igual as outras formas (dá até pra entortar ele).

## Como usar

| Quero... | Faço assim |
|---|---|
| Desenhar um polígono | Ferramenta *Polígono*: um clique pra cada ponto e duplo clique pra fechar (trocar de ferramenta também fecha) |
| Colocar quadrado, círculo ou hexágono | Clica onde vai ser o centro e arrasta até o tamanho que quiser (um clique só cria no tamanho padrão) |
| Selecionar e mover | Clica na forma e arrasta. Shift + clique seleciona mais de uma, e arrastar no vazio faz uma caixa de seleção |
| Aumentar / girar / entortar | Escolhe *Escalar*, *Rotacionar* ou *Cisalhar* e arrasta em cima da forma |
| Mexer num ponto só | *Editar Vértice* e arrasta uma das bolinhas vermelhas |
| Encaixar na grade | Marca *Grade Magnética*, e os pontos grudam numa grade de 20 px |
| Mudar cor e borda | Escolhe as cores e a espessura. Vale pras formas selecionadas e pras próximas que eu desenhar |
| Atalhos | Ctrl+Z desfaz, Ctrl+Y refaz, Ctrl+S salva, Ctrl+D duplica e Del apaga as selecionadas |

Lá embaixo tem uma barra que mostra a posição do mouse, quantas formas tem na tela e a área e o perímetro do que tá selecionado.

## Salvar e carregar

Os desenhos são salvos em JSON (usei a biblioteca Gson). Um arquivo fica mais ou menos assim:

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

Se o arquivo estiver corrompido (JSON quebrado, forma com menos de 3 pontos, cor inválida...), aparece um aviso explicando o problema e o programa continua aberto. E se você tentar fechar ou abrir outro arquivo sem ter salvo, ele pergunta antes se pode descartar as alterações.
