package gl.bp;

public abstract class BitPacking {

    protected static final int INT_BITS = 32;
    protected int k;
    protected int originalSize;

    // --- methodes abs

    public abstract int[] compress(int[] input);

    public abstract int[] decompress(int[] compressedArray);

    public abstract int get(int[] compressedArray, int i);

    // --- methodes partagées

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

        // nb de bits k pour maxVal
        // https://www.geeksforgeeks.org/java/integer-numberofleadingzeros-method-in-java-with-example/
        this.k = (maxVal == 0) ? 1 : INT_BITS - Integer.numberOfLeadingZeros(maxVal);
        this.originalSize = input.length;
    }

    // --- méthodes + chrono

    public long timeCompress(int[] input) {
        long start = System.nanoTime();
        compress(input);
        return System.nanoTime() - start;
    }

    public long timeDecompress(int[] compressedArray) {
        long start = System.nanoTime();
        decompress(compressedArray);
        return System.nanoTime() - start;
    }

    public long timeGet(int[] compressedArray, int i) {
        long start = System.nanoTime();
        get(compressedArray, i);
        return System.nanoTime() - start;
    }
}
