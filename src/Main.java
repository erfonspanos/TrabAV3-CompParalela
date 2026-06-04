import framework.ChartViewer;
import framework.TestRunner;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Análise Comparativa de Paralelismo...");

        TestRunner.executarTestes();

        System.out.println("Iniciando renderização dos gráficos...");

        SwingUtilities.invokeLater(() -> {
            ChartViewer.exibirGraficos();
        });
    }
}