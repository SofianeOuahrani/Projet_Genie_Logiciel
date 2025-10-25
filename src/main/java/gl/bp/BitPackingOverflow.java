package gl.bp;

public class BitPackingOverflow extends BitPacking {

    // var pour l'état interne
    private int k_prime; // petits paquets
    private int k_overflow; // grands nombres
    private int flag_bits = 1; // bit de flag (0=normal, 1=overflow)
    //!!! jdois réfléchir à quoi sruveiller encore => d'autres var d'états

    @Override
    public int[] compress(int[] input) {
        return null;
    }

    @Override
    public int[] decompress(int[] compressedArray) {
        return new int[0];
    }

    @Override
    public int get(int[] compressedArray, int i) {
        return 0;
    }


}
