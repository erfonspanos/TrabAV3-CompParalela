package framework;

import algoritmos.ParallelCPU;
import algoritmos.SerialCPU;
import algoritmos.ParallelGPU;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestRunner {

    public static void executarTestes() {
        String[] arquivos = {
                "amostras/DonQuixote-388208.txt",
                "amostras/Dracula-165307.txt",
                "amostras/MobyDick-217452.txt"
        };

        String[] palavrasAlvo = {"que", "the", "the"};

        int[] threadsCPU = {2, 4, 8};
        int numAmostras = 3;

        File diretorioResultados = new File("resultados");
        if (!diretorioResultados.exists()) {
            diretorioResultados.mkdirs();
        }

        try (PrintWriter csvWriter = new PrintWriter(new FileWriter("resultados/analise_desempenho.csv"))) {
            csvWriter.println("Algoritmo,Arquivo,Palavra,Threads,Iteracao,Ocorrencias,Tempo_ms");

            for (int i = 0; i < arquivos.length; i++) {
                String arquivo = arquivos[i];
                String palavra = palavrasAlvo[i];

                System.out.println("\n--------------------------------------------------");
                System.out.println("Processando arquivo: " + arquivo + " | Buscando: '" + palavra + "'");
                String texto = lerArquivo(arquivo);

                if(texto.isEmpty()) continue;

                for (int iteracao = 1; iteracao <= numAmostras; iteracao++) {
                    System.out.println("\n--- Iteração " + iteracao + " ---");

                    // 1. Teste Serial
                    long inicio = System.currentTimeMillis();
                    long contagem = SerialCPU.contar(texto, palavra);
                    long tempo = System.currentTimeMillis() - inicio;
                    salvarResultado(csvWriter, "SerialCPU", arquivo, palavra, 1, iteracao, contagem, tempo);

                    // 2. Testes Paralelos CPU
                    for (int threads : threadsCPU) {
                        inicio = System.currentTimeMillis();
                        try {
                            contagem = ParallelCPU.contar(texto, palavra, threads);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tempo = System.currentTimeMillis() - inicio;
                        salvarResultado(csvWriter, "ParallelCPU", arquivo, palavra, threads, iteracao, contagem, tempo);
                    }

                    // 3. Teste Paralelo GPU
                    inicio = System.currentTimeMillis();
                    try {
                        contagem = ParallelGPU.contar(texto, palavra);
                        tempo = System.currentTimeMillis() - inicio;
                        salvarResultado(csvWriter, "ParallelGPU", arquivo, palavra, 0, iteracao, contagem, tempo);
                    } catch (Exception e) {
                        System.err.println("Erro na execução da GPU: " + e.getMessage());
                    }
                }
            }
            System.out.println("\n--------------------------------------------------");
            System.out.println("Arquivo CSV gerado com sucesso na pasta /resultados/");

        } catch (IOException e) {
            System.err.println("Erro ao gravar CSV: " + e.getMessage());
        }
    }

    private static String lerArquivo(String caminho) {
        try {
            return new String(Files.readAllBytes(Paths.get(caminho)));
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + caminho + ". Verifique se o nome e a pasta estão corretos!");
            return "";
        }
    }

    private static void salvarResultado(PrintWriter writer, String alg, String arq, String palavra, int threads, int iter, long cont, long tempo) {
        String nomeLimpo = arq.replace("amostras/", "");
        writer.printf("%s,%s,%s,%d,%d,%d,%d\n", alg, nomeLimpo, palavra, threads, iter, cont, tempo);
        System.out.printf("%-12s | %8d ocorrências | %4d ms | (Threads: %d)\n", alg, cont, tempo, threads);
    }
}