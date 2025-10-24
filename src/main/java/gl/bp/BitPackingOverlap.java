package gl.bp;


public class BitPackingOverlap extends BitPacking {

    @Override
    public int[] compress(int[] input) {
        this.calculateK(input); //0 si vide
        final int K = this.k;
        if (K == 0 ) {
            return new int[0];
        }
        long nbBits = (long) this.originalSize * K;
        int tailleTab = (int) Math.ceil((double)nbBits / INT_BITS);

        int[] outputArray = new int[tailleTab];

        int outputIndex = 0; //Index conteneur
        int bitOffset = 0; //décalage depuis la gauche

        for(int inputVal : input){

            //le cas ou tout est beau dans la vie
            if (bitOffset + K <= INT_BITS) {

                int positionnement = INT_BITS - K - bitOffset;
                outputArray[outputIndex] |=  (inputVal << (positionnement)) ;

                bitOffset += K;

                //PB : j'avais oublié d'incrémenter outputindex quand le decal == 32
                if (bitOffset == INT_BITS) {
                    outputIndex++;
                    bitOffset = 0;
                }
            }
            //chevauchement ;(
            else {
                // les 2 parties de mon paquet a découper
                int bits1 = INT_BITS - bitOffset; // pour le conteneur actuel
                int bits2 = K - bits1;           // pour le suivant

                // 1) insertion premiere partie

                // je masque les bits de poids faible
                int valPartie1 = inputVal;
                valPartie1 = valPartie1 >> bits2;
                valPartie1 = valPartie1 << bits2;

                int positionnement1 = INT_BITS - bits1 - bitOffset;

                outputArray[outputIndex] |= (valPartie1 << positionnement1);


                // conten. suivant
                outputIndex++;

                // 2) insertion deuxieme partie

                // doit commencer tt a gauche !!!
                int positionnement2 = INT_BITS - bits2;

                // masque bit poids faible
                int masquePartie2 = (1 << bits2) - 1;
                int valPartie2 = inputVal & masquePartie2;

                outputArray[outputIndex] |= (valPartie2 << positionnement2);

                bitOffset = bits2; //!!!
            }
        }
        return outputArray;
    }

    @Override
    public int[] decompress(int[] compressedArray) {
        if (this.originalSize == 0){
            return new int[0];
        }
        if (this.k == 0) {
            throw new IllegalStateException("k n'est pas initialisé.");
        }
        int[] outputArray = new int[this.originalSize];

        for(int i = 0; i < this.originalSize; i++) {
            outputArray[i] = get(compressedArray,i);
        }
        return outputArray;
    }

    @Override
    public int get(int[] compressedArray, int i) {

        if (this.k == 0) {
            throw new IllegalStateException("k n'existe pas.");
        }

        //histoire de ne pas réécrire this.k a chaque fois et je verrais mieux K dans mon code
        final int K = this.k;
        final int MASK_K_BITS = (1 << K) - 1; //masque de k bits (pour isoler note entier ducoup)

        long bitPosition = (long) i * K;

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
