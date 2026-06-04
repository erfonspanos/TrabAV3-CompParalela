# Análise Comparativa de Algoritmos de Busca com Uso de Paralelismo

**Disciplina:** Computação Paralela e Concorrente  
**Dupla:** Erfon Spanos e Rodrigo Cirino  

---

## Resumo
Neste trabalho, analisamos o desempenho de diferentes algoritmos buscando palavras em textos grandes, rodando tudo em Java. A ideia foi comparar três cenários: execução Serial (CPU), Paralela (Multithreading na CPU) e Paralela na GPU (usando OpenCL via JOCL). Coletamos os tempos de execução variando o tamanho dos arquivos e a quantidade de threads, salvando tudo num arquivo CSV. Como um bônus para facilitar a análise, criamos uma interface gráfica nativa em Java Swing que lê esse CSV e plota os resultados direto na tela.

## Introdução
Entender como o hardware lida com diferentes cargas de trabalho é fundamental. Por isso, montamos um framework de testes em Java que lê livros inteiros e conta quantas vezes uma palavra específica aparece. Fizemos isso de três jeitos:

1. **SerialCPU:** Aquela busca sequencial padrão, rodando em uma única thread. Usamos isso como nossa base de comparação (baseline).
2. **ParallelCPU:** Aqui dividimos o texto usando um `ExecutorService`. O arquivo é fatiado dinamicamente dependendo de quantas threads (núcleos) a gente escolhe usar.
3. **ParallelGPU:** A força bruta. Usamos a biblioteca JOCL para alocar espaço na VRAM da placa de vídeo e rodar um *Kernel* escrito em C (OpenCL). Basicamente, passamos o processamento para os milhares de núcleos da GPU.

## Metodologia
Para ter dados confiáveis e conseguir comparar os algoritmos de forma justa, estruturamos os testes assim:

* **Textos:** Usamos três livros de tamanhos diferentes (*Don Quixote*, *Dracula* e *Moby Dick*).
* **O que buscamos:** Procuramos a palavra "que" no livro em espanhol e "the" nos em inglês. Escolhemos palavras muito comuns para forçar o algoritmo a trabalhar de verdade e incrementar bastante a contagem.
* **Threads:** Testamos a `ParallelCPU` com 2, 4 e 8 threads.
* **Amostras:** Rodamos cada cenário 3 vezes seguidas para tirar uma média precisa.
* **Logs:** Tudo foi salvo automaticamente no arquivo `analise_desempenho.csv`.

## Resultados e Discussão
Primeiro, o mais importante: os algoritmos estão funcionando perfeitamente. Todas as três abordagens contaram exatamente o mesmo número de palavras (por exemplo, a contagem sempre bateu 25.828 ocorrências no arquivo do *Don Quixote*). 

Olhando os gráficos gerados no nosso painel em Swing, deu para notar na prática alguns conceitos clássicos de computação concorrente:

1. **O peso do Overhead na GPU:** A `ParallelGPU` foi a que mais demorou na média (entre 90ms e 110ms). Motivo? Nossos arquivos txt têm só alguns megabytes. O tempo que o sistema perde alocando memória e mandando esses dados da RAM para a VRAM da placa de vídeo (via PCI-Express) acaba sendo muito maior do que o tempo de fato procurando as palavras. O arquivo era pequeno demais para saturar o paralelismo da GPU.
2. **O Custo de Compilação OpenCL:** A primeira vez que a GPU roda no teste, ela sempre dá um pico de latência bem alto (bateu uns 900ms). Isso acontece por causa do custo da compilação JIT (*Just-In-Time*) do código do Kernel C para a arquitetura nativa da placa de vídeo ali na hora da execução.
3. **Criar Threads também custa tempo:** A `SerialCPU` voou nos testes (levando de 0 a 1ms). Como os textos são relativamente pequenos, o acesso sequencial no cache L1/L2 do processador resolve o problema quase na mesma hora. Quando tentamos usar 4 ou 8 threads na `ParallelCPU`, o tempo subiu para uns 2ms a 9ms em alguns casos. Ou seja, o custo do Sistema Operacional criando as threads e organizando o escalonamento foi maior do que o ganho que tivemos dividindo o trabalho.

## Conclusão
Na prática, o experimento provou que paralelismo não é bala de prata. Jogar o processamento para a GPU (OpenCL) ou encher de threads na CPU só vale a pena quando o volume de dados (*payload*) é gigantesco — grande o suficiente para compensar o tempo perdido com transferência de dados e inicialização de contexto (o famoso *overhead*). Para cargas de trabalho na casa dos poucos megabytes, a arquitetura de cache das CPUs modernas rodando de forma sequencial ainda ganha de lavada.

## Referências
* Documentação Oficial Java (Oracle): Executores e Concorrência.
* JOCL - Java Bindings for OpenCL (http://www.jocl.org/).
* Khronos Group - OpenCL Specification.

## Anexos e Instruções de Execução

### Dependências (Atenção na hora da correção!)
Para rodar este projeto na IDE, é necessário configurar a biblioteca do OpenCL para Java.
O arquivo `jocl-2.0.4.jar` está contido dentro da pasta `/lib` na raiz deste repositório. **Explicação:** Em vez de usar gerenciadores como o Maven, optamos por incluir o arquivo `.jar` diretamente no projeto para cumprir os requisitos da atividade.

**Como configurar:** Adicione a pasta `/lib` ao *Build Path* / *Libraries* do seu projeto na IDE (IntelliJ, Eclipse, etc). Além disso, a máquina que for rodar o código precisa ter os drivers de vídeo com suporte a OpenCL instalados.

### Link do Repositório (GITHUB)
Acesse os códigos das implementações completas no link abaixo:
**[https://github.com/erfonspanos/TrabAV3-CompParalela](https://github.com/erfonspanos/TrabAV3-CompParalela)**
