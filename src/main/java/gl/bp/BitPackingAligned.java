package gl.bp;

public class BitPackingAligned extends BitPacking{
    @Override
    public int[] compress(int[] input) {
        // on calcule k (nb de bits de l'entier le plus grand tu tableau)
        this.calculateK(input);

        if (this.k == 0 ) {
            return new int[0];
        }
        // maintenant on regarde combien d'entiers de k bits tiennent dans 32 bits
        // j'ai mis final car cela ne changera pas
        final int ITEMS_PER_CONTAINER = INT_BITS / this.k;

        //on calcule la taille du tableau compressé sois le nb d'elts / le nombre d'éléments possible par conteneur de 32 bits
        int compressedSize = (int) Math.ceil((double) this.originalSize / ITEMS_PER_CONTAINER); // div en flottant pour evite des trucs du genre 3/2 = 1
        int[] compressedArray = new int[compressedSize]; // tableau d'int de la taille que l'on vient de calculer

        // Variables que j'utilise pour l'écriture
        int outputIndex = 0; // Index du tableau de sortie
        int bitOffset = 0;   // Décalage en bits dans compressedArray[outputIndex]

        //boucle principale (inputVal = chaque valeur d'entrée)
        for (int inputVal :input){
            //on vérifie que le prochain paquet de k bits rentre dans le conteneur
            //sinon bourrage et on passe au prochain conteneur
            if(bitOffset + this.k > INT_BITS){
                outputIndex++;
                bitOffset = 0; // décalage remis à 0 car je passe au prochain conteneur
            }
            
            // = distance entre le bit 0 à droite et le début du paquet
            int positionnement = INT_BITS - this.k - bitOffset;

            // je positionne le paquet par un décalage à gauche et je l'insère avec l'op OU binaire
            compressedArray[outputIndex] |= (inputVal << positionnement);


            // mise à jour du décalage (k bits ducoup)
            bitOffset += this.k;
        }


        //tableau compressé final :)
        return compressedArray;

    }

    @Override
    public int[] decompress(int[] compressedArray) {
        return new int[0];
    }

    @Override
    public int get(int[] compressedArray, int i) {
        // j'utilise pas calculateK car le tableau est deja compressé
        //je suppose que this.k est donc initialisé logiquement.
        // = combien d'entiers compressés rentrent dans un entier de 32 bits
        int itemsPerContainer = INT_BITS / this.k;

        // index du conteneur contenant l'entier i
        int indexContainer = i / itemsPerContainer;

        // index de l'item dans le conteneur ( 0 : itemsPerContainer-1)
        int itemInContainer = i % itemsPerContainer;

        // Position du bit de départ du i-ème élément (0 pour le bit de poids fort)
        int bitOffset = itemInContainer * this.k;

        /* isoler kbits qui nous intéresse dans le mot de 32 bit et le transformer en entier -> Alignement + Nettoyage
         >> décale vers la droite par un nombre de positions que nous calculons pour que
         notre nombre soit tout à droite ( 32 bits - k bits - pos de départ)
         & ... => applique ET binaire(&) avec un masque sur le résultat décalé
         efface donc tout les autres 1 pour ne garder que des 0 a gauche de notre nombre*/

        int result = (compressedArray[indexContainer] >> (INT_BITS - this.k - bitOffset)) & ((1 << this.k) - 1);
        return result;

    }
}
