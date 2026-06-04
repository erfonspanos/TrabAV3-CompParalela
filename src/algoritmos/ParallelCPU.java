package algoritmos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelCPU {
    public static long contar(String texto, String palavra, int numThreads) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int tamanhoBloco = texto.length() / numThreads;
        List<Callable<Long>> tarefas = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int inicio = i * tamanhoBloco;
            final int fim = (i == numThreads - 1) ? texto.length() : (i + 1) * tamanhoBloco + palavra.length() - 1;

            tarefas.add(() -> {
                String bloco = texto.substring(inicio, Math.min(fim, texto.length()));
                return SerialCPU.contar(bloco, palavra);
            });
        }

        long contagemTotal = 0;
        for (Future<Long> resultado : executor.invokeAll(tarefas)) {
            contagemTotal += resultado.get();
        }

        executor.shutdown();
        return contagemTotal;
    }
}