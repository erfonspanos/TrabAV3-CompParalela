package framework;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChartViewer extends JPanel {

    private Map<String, Long> mediasTempo = new HashMap<>();
    private int valorMaximo = 0;

    public ChartViewer() {
        carregarDadosDoCSV();
    }

    private void carregarDadosDoCSV() {
        Map<String, Long> somaTempos = new HashMap<>();
        Map<String, Integer> contagemAmostras = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("resultados/analise_desempenho.csv"))) {
            String linha = br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(",");
                if (colunas.length < 7) continue;

                String algoritmo = colunas[0];
                String threads = colunas[3];
                long tempo = Long.parseLong(colunas[6]);

                String chave = algoritmo;
                if (!threads.equals("0") && !threads.equals("1")) {
                    chave += " (" + threads + "T)";
                }

                somaTempos.put(chave, somaTempos.getOrDefault(chave, 0L) + tempo);
                contagemAmostras.put(chave, contagemAmostras.getOrDefault(chave, 0) + 1);
            }

            for (String chave : somaTempos.keySet()) {
                long media = somaTempos.get(chave) / contagemAmostras.get(chave);
                mediasTempo.put(chave, media);
                if (media > valorMaximo) {
                    valorMaximo = (int) media;
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler o CSV para o gráfico: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int larguraPainel = getWidth();
        int alturaPainel = getHeight();
        int margemEsquerda = 60;
        int margemInferior = 50;
        int margemSuperior = 40;

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, larguraPainel, alturaPainel);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Tempo Médio de Execução (Menos é Melhor)", larguraPainel / 2 - 200, 25);

        g2d.drawLine(margemEsquerda, margemSuperior, margemEsquerda, alturaPainel - margemInferior);
        g2d.drawLine(margemEsquerda, alturaPainel - margemInferior, larguraPainel - 20, alturaPainel - margemInferior);

        if (mediasTempo.isEmpty()) {
            g2d.drawString("Nenhum dado encontrado no CSV.", 100, 100);
            return;
        }

        int quantidadeBarras = mediasTempo.size();
        int larguraBarra = (larguraPainel - margemEsquerda - 40) / quantidadeBarras - 20;
        int posicaoX = margemEsquerda + 20;

        int maxEixoY = Math.max(valorMaximo, 10);

        for (Map.Entry<String, Long> entrada : mediasTempo.entrySet()) {
            String rotulo = entrada.getKey();
            int tempo = entrada.getValue().intValue();

            int alturaBarra = (int) (((double) tempo / maxEixoY) * (alturaPainel - margemSuperior - margemInferior - 20));
            int posicaoY = alturaPainel - margemInferior - alturaBarra;

            if (rotulo.contains("Serial")) g2d.setColor(new Color(200, 50, 50));
            else if (rotulo.contains("GPU")) g2d.setColor(new Color(50, 200, 50));
            else g2d.setColor(new Color(50, 100, 200)); // CPU Threads

            g2d.fillRect(posicaoX, posicaoY, larguraBarra, alturaBarra);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(posicaoX, posicaoY, larguraBarra, alturaBarra);

            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(tempo + " ms", posicaoX + (larguraBarra / 4), posicaoY - 5);

            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            g2d.drawString(rotulo, posicaoX, alturaPainel - margemInferior + 20);

            posicaoX += larguraBarra + 20;
        }
    }

    public static void exibirGraficos() {
        JFrame frame = new JFrame("Análise de Desempenho - Computação Paralela");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null); // Centraliza na tela
        frame.add(new ChartViewer());
        frame.setVisible(true);
    }
}