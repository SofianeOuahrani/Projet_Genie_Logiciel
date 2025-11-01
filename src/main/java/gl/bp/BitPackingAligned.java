// BitPackingAligned - OUAHRANI KHALDI Sofiane
package gl.bp;

public class BitPackingAligned extends BitPacking {
    @Override
    public int[] compress(int[] input) {

        this.calculateK(input); //mtn k est init
        int K = this.k; // juste pour plus de lisibilité

        if (K == 0 ) {
            return new int[0]; // au cas ou le tableau est vide
        }
        //si un seul elt rentre par conteneur
        else if (K > INT_BITS / 2) {
            //ma petite optimisation du soir
            return input;
        }



        final int ITEMS_PER_CONTAINER = INT_BITS / K;

        // taille tableau de sortie en nb de 32bits
        int compressedSize = (int) Math.ceil((double) this.originalSize / ITEMS_PER_CONTAINER); // j'ai fais la div en flottant pour eviter des trucs du genre 3/2 = 1
        int[] compressedArray = new int[compressedSize];

        // Variables que j'utilise pour le suivi
        int outputIndex = 0; // Index du tableau de sortie
        int bitOffset = 0;   // position de bit actuelle en partant de la droite (bit 0)

        for (int inputVal : input) {
            // je vérifie si l'ajout du prochain paquet ne fit pas dans le conteneur -> conteneur suivant
            if (bitOffset + K > INT_BITS) {
                outputIndex++;
                bitOffset = 0;
            }


            compressedArray[outputIndex] |= (inputVal << bitOffset); /* je décale inputVal du nb de bits nécessaire vers la gauche comme ça
            mes prochains nombres s'ajouterons à la suite jusqu'a changement du conteneur et comme ça je ne risquerais pas de modif les nb deja placés,
            et je l'ajoute au tableau compressé avec le OU BINAIRE*/

            // ducoup K bits supp sont occupés
            bitOffset += K;
        }

        //tableau compressé final :)
        return compressedArray;
    }

    @Override
    public int[] decompress(int[] compressedArray) {
        int K = this.k;

        // CAS OU K == 0 (tab vide ou pas init)
        if (K == 0) {
            return new int[0];
        }

        int[] outputArray = new int[this.originalSize];

        //comme dans mes autres classes je vais juste appeler get pour chaque indice
        for (int i = 0; i < this.originalSize; i++) {
            outputArray[i] = get(compressedArray, i);
        }

        //tableau décompressé final :)
        return outputArray;
    }

    @Override
    public int get(int[] compressedArray, int i) {
        final int K = this.k;

        // c'est pas cool on stop direct
        if (K == 0) {
            throw new IllegalStateException("k n'est pas initialisé : merci d'appeler compress() sur le tableau d'abord./ tableau vide");
        }


        // nb d'elt par conteneur max
        int itemsPerContainer = INT_BITS / K;

        // index du conteneur contenant l'entier i
        int indexContainer = i / itemsPerContainer;

        // index de l'item dans le conteneur
        int itemInContainer = i % itemsPerContainer;

        // Position du bit de départ du i-ème élément (0 pour le bit de poids fort)
        int bitOffset = itemInContainer * K;

        int result = (compressedArray[indexContainer] >>> bitOffset) & ((1 << K) - 1);
        return result;
    }
}
