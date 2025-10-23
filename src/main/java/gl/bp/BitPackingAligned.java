package gl.bp;

public class BitPackingAligned extends BitPacking{
    @Override
    public int[] compress(int[] input) {
        // on calcule k (nb de bits de l'entier le plus grand tu tableau)
        this.calculateK(input);
        int K = this.k;

        /*if (this.k == 0 ) {
            return new int[0];
        }*/
        // combien d'entiers de k bits tiennent dans 32 bits
        final int ITEMS_PER_CONTAINER = INT_BITS / K;

        int compressedSize = (int) Math.ceil((double) this.originalSize / ITEMS_PER_CONTAINER); // j'ai fais la div en flottant pour eviter des trucs du genre 3/2 = 1
        int[] compressedArray = new int[compressedSize];

        // Variables que j'utilise pour le suivi
        int outputIndex = 0; // Index du tableau de sortie
        int bitOffset = 0;   // Décalage en bits dans compressedArray[outputIndex]

        //boucle principale (inputVal = chaque valeur d'entrée)
        for (int inputVal :input){
            //on vérifie que le prochain paquet de k bits rentre dans le conteneur
            //sinon bourrage et on passe au prochain conteneur
            if(bitOffset + K > INT_BITS){
                outputIndex++;
                bitOffset = 0; // décalage remis à 0 car je passe au prochain conteneur
            }

            // = distance entre le bit 0 à droite et le début du paquet
            int positionnement = INT_BITS - K - bitOffset;

            // je positionne le paquet par un décalage à gauche et je l'insère avec l'op OU binaire
            compressedArray[outputIndex] |= (inputVal << positionnement);


            // mise à jour du décalage (k bits ducoup)
            bitOffset += K;
        }


        //tableau compressé final :)
        return compressedArray;

    }

    @Override
    public int[] decompress(int[] compressedArray) {
        int K = this.k;

        /*
        if (K == 0) {
            throw new IllegalStateException("k n'est pas initialisé.");
        }*/

        int[] outputArray = new int[this.originalSize];


        int indexContainer = 0;
        int bitOffset = 0;


        for (int i = 0; i<this.originalSize;i++){
            if (bitOffset + K > INT_BITS){
                indexContainer ++;
                bitOffset =0;
            }
            // decalage vers la droit pour amener les k bits au bit 0
            int decalage = INT_BITS - K - bitOffset;

            // décalage à droite
            outputArray[i] = (compressedArray[indexContainer] >> decalage) & ((1 << K) - 1);
            bitOffset += K;
        }

        //tableau décompressé final :)
        return outputArray;
    }

    @Override
    public int get(int[] compressedArray, int i) {

        // GERER LE CAS OU K N'EST PAS INITIALISE
        final int K = this.k;

        /*if (K == 0) {
            throw new IllegalStateException("k n'est pas initialisé.");
        }*/

        // je pense que le nom est assez explicite..
        int itemsPerContainer = INT_BITS / K;

        // index du conteneur contenant l'entier i
        int indexContainer = i / itemsPerContainer;

        // index de l'item dans le conteneur ( 0 : itemsPerContainer-1)
        int itemInContainer = i % itemsPerContainer;

        // Position du bit de départ du i-ème élément (0 pour le bit de poids fort)
        int bitOffset = itemInContainer * K;

        /* isoler kbits qui nous intéresse dans le mot de 32 bit et le transformer en entier
         >> décale vers la droite par un nombre de positions que nous calculons pour que
         notre nombre soit tout à droite ( 32 bits - k bits - pos de départ)
         + masque ! (j'ai chois de soustraire 1 au masque pour n'avoir que des 1 pour touts les bits à droite, et ducoup on aura des
         0 à gauche ce qui permettra d'ignorer les éventuels entiers a gauche de notre nombre (PB) */

        int decalage = INT_BITS - K - bitOffset;
        int result = (compressedArray[indexContainer] >> decalage) & ((1 << K) - 1);
        return result;

    }
}
