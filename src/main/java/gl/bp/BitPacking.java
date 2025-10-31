// BitPacking - OUAHRANI KHALDI Sofiane
package gl.bp;

public abstract class BitPacking {

    protected static final int INT_BITS = 32;
    protected int k;
    protected int originalSize;

    // methodes abstraites

    public abstract int[] compress(int[] input);

    public abstract int[] decompress(int[] compressedArray);

    public abstract int get(int[] compressedArray, int i);

    // methodes partagées

    protected void calculateK(int[] input) {
        if (input == null || input.length == 0) {
            this.k = 0;
            this.originalSize = 0;
            return;
        }

        int maxVal = 0;
        for (int val : input) {

            // je n'ai pas choisi d'évaluer les nombre négatifs (bonus) pour l'instant.
            if (val > maxVal) {
                maxVal = val;
            }
        }

        // https://www.geeksforgeeks.org/java/integer-numberofleadingzeros-method-in-java-with-example/
        this.k = (maxVal == 0) ? 1 : INT_BITS - Integer.numberOfLeadingZeros(maxVal);
        this.originalSize = input.length;
    }


    //je vais utiliser une classe générique pour renvoyer un tableau contenant le résultat de l'opération et le temps que ça prend
    //car avant ça je faisais deux compressions pour rien dans mon main pour le benchmark (une pour la compression et une pour afficher le temps de celle-ci soit 2 compressions)
    // https://www.jmdoudoux.fr/java/dej/chap-generique.htm
    public class ResultWithTime<T> {
        public final T result;
        public final long timeNs;

        public ResultWithTime(T result, long timeNs) {
            this.result = result;
            this.timeNs = timeNs;
        }
    }

    //resultat + temps de l'op de compress
    public ResultWithTime<int[]> timeCompress(int[] input) {
        long start = System.nanoTime();
        int[] compressed = compress(input);
        long end = System.nanoTime();
        return new ResultWithTime<>(compressed, end - start);
    }

    // résultat + temps de l'op de decompress
    public ResultWithTime<int[]> timeDecompress(int[] compressedArray) {
        long start = System.nanoTime();
        int[] decompressed = decompress(compressedArray);
        long end = System.nanoTime();
        return new ResultWithTime<>(decompressed, end - start);
    }

    // resultat + temps de l'op get
    public ResultWithTime<Integer> timeGet(int[] compressedArray, int i) {
        long start = System.nanoTime();
        int value = get(compressedArray, i);
        long end = System.nanoTime();
        return new ResultWithTime<>(value, end - start);
    }

}
