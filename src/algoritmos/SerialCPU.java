package algoritmos;

public class SerialCPU {
    public static long contar(String texto, String palavra) {
        long contagem = 0;
        int indice = 0;
        while ((indice = texto.indexOf(palavra, indice)) != -1) {
            contagem++;
            indice += palavra.length();
        }
        return contagem;
    }
}