package gl.bp;

public class BitPackingOverlap extends BitPacking {

    @Override
    public int[] compress(int[] input) {
        this.calculateK(input);
        if (this.k == 0 ) {
            return new int[0];
        }
        return null;

    }

    @Override
    public int[] decompress(int[] compressedArray) {
        return null;
    }

    @Override
    public int get(int[] compressedArray, int i) {

        if (this.k == 0) {
            throw new IllegalStateException("k n'existe pas.");
        }

        //histoire de ne pas réécrire this.k a chaque fois et je verrais mieux K dans mon code
        final int K = this.k;
        final int MASK_K_BITS = (1 << K) - 1; //masque de k bits (pour isoler note entier ducoup)

        int bitPosition = i * K;

        // pour trouver le conteneur de départ et le décalage
        int indexContainer = (int) (bitPosition/INT_BITS);
        int bitOffset = (int)(bitPosition % INT_BITS);

        if (bitOffset + K <= INT_BITS){

            int decalageFinal = (INT_BITS - (bitOffset + K));
            // je décale mon nombre vers la droite du nombre de bits qu'il faut pour qu'il soit le plus a droite possible
            // et je lui applique le ET binaire 000...11 avec k 1 pour ne garder que les bits qui m'intéressent ;)
            int result = (compressedArray[indexContainer] >> decalageFinal) & MASK_K_BITS;

            return result;
        }

        else {
            int bits1 = INT_BITS - bitOffset; //premiere partie du nb (=nb de bits restants)

            int decalagePart1 = INT_BITS - bits1;
            int partie1_1 = (compressedArray[indexContainer] >> decalagePart1) & ((1 << bits1)-1);
            int partie1_2 = partie1_1 << (K - bits1);//décal à gauche pour laisse de la place à la deuxième partie

            int bits2 = K - bits1;
            int decalagePart2 = INT_BITS - bits2;
            int partie2 = (compressedArray[indexContainer + 1 ]>>decalagePart2) & ((1 << bits2)-1);

            int result = (partie1_2 | partie2) & MASK_K_BITS; // masque pour éviter des débordement on sait jamais...

            return result;

        }


    }
}
