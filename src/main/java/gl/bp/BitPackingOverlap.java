// BitPackingOverlap - OUAHRANI KHALDI Sofiane
package gl.bp;

public class BitPackingOverlap extends BitPacking {

    @Override
    public int[] compress(int[] input) {

        this.calculateK(input);
        final int K = this.k;

        if (K == 0 ) {
            return new int[0];
        }

        long nbBits = (long) this.originalSize * K; //nb de bits nécessaires pour tt les entiers
        int tailleTab = (int) Math.ceil((double)nbBits / INT_BITS); // div par paquets de 32 bits

        int[] outputArray = new int[tailleTab];

        int outputIndex = 0; //Index conteneur
        int bitOffset = 0; //pos du bit à partir de la droite (donc bit 0)


        for(int inputVal : input){

            //le cas ou tout est beau dans la vie = pas de chevauchement
            if (bitOffset + K <= INT_BITS) {

                // je les place dans le tableau de sortie avec et OU binaire en veillant à de décaler de bitOffset bits vers la gauche
                outputArray[outputIndex] |= (inputVal << bitOffset);

                bitOffset += K;

                // la je gère le cas of la position = 32 donc = conteneur plein
                if (bitOffset == INT_BITS) {
                    outputIndex++;
                    bitOffset = 0;
                }
            }

            //chevauchement ;(
            else {
                // les 2 parties de mon paquet a découper
                int bits1 = INT_BITS - bitOffset; // nb de bits restants dans le conteneur actuel
                int bits2 = K - bits1;           // nb de bits à stocker pour le suivant

                // 1) insertion premiere partie
                int valPartie1 = inputVal & ((1 << bits1) - 1); //je prend juste les bits qui tiennent dans le conteneur actuel
                outputArray[outputIndex] |= (valPartie1 << bitOffset); // je les ajoute encore une fois avec OU et en décalant vers la gauche

                // conteneur  suivant
                outputIndex++;

                // 2) insertion deuxieme partie
                int valPartie2 = inputVal >>> bits1; //le reste des bits
                outputArray[outputIndex] |= valPartie2; // et la pas besoin de décalage, ils commencent à gauche :)

                bitOffset = bits2;
            }
        }
        return outputArray; // finalement le tableau compressé
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
            outputArray[i] = get(compressedArray,i); //j'ai voulu simplement rappeler la méthode get i fois pour me faciliter la tache
        }
        return outputArray;
    }

    @Override
    public int get(int[] compressedArray, int i) {

        if (this.k == 0) {
            throw new IllegalStateException("k = 0/ tableau vide.");
        }

        //histoire de ne pas réécrire this.k a chaque fois et je verrais mieux K dans mon code
        final int K = this.k;
        final int MASK_K_BITS = (1 << K) - 1; //masque de k bits (pour isoler note entier ducoup)

        long bitPosition = (long) i * K;

        // pour trouver le conteneur de départ et le début de l'entier qu'on cherche
        int indexContainer = (int) (bitPosition / INT_BITS);
        int bitOffset = (int)(bitPosition % INT_BITS);


        if (bitOffset + K <= INT_BITS){
            int result = (compressedArray[indexContainer] >>> bitOffset) & MASK_K_BITS; //decalage + masque pour ne garder que notre entier
            return result;
        }
        else {
            int bits1 = INT_BITS - bitOffset; //premiere partie du nb (=nb de bits restants dans le conteneur)
            //décalage + masque encore une fois
            int partie1 = (compressedArray[indexContainer] >>> bitOffset) & ((1 << bits1) - 1);

            int bits2 = K - bits1; //2eme partie de l'entier
            int partie2 = compressedArray[indexContainer + 1] & ((1 << bits2) - 1); //ducoup elle est dans le conteneur suivant si elle a été fractionnée

            int result = partie1 | (partie2 << bits1); //OU sur les 2 partie, et partie2 décalée direct juste a gauche de la partie 1 pour former l'entier de k bits
            return result; //et voila le résultat !
        }
    }
}
