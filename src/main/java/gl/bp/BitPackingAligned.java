package gl.bp;

public class BitPackingAligned extends BitPacking{
    @Override
    public int[] compress(int[] input) {
        return new int[0];
    }

    @Override
    public int[] decompress(int[] compressedArray) {
        return new int[0];
    }

    @Override
    public int get(int[] compressedArray, int i) {

        // = combien d'entiers compressés rentrent dans un entier de 32 bits
        int itemsPerContainer = INT_BITS / this.k;

        // index du conteneur contenant l'entier i
        int indexContainer = i / itemsPerContainer;

        // index de l'item dans le conteneur ( 0 : itemsPerContainer-1)
        int itemInContainer = i % itemsPerContainer;

        // Position du bit de départ du i-ème élément (0 pour le bit de poids fort)
        int bitOffset = itemInContainer * this.k;

        /* isoler kbits qui nous intéresse dans le mot de 32 bit et le transformer en entier
         Alignement puis Nettoyage
         >> décale vers la droite par un nombre de positions que nous calculons pour que
         notre nombre soit tout à droite
         & ... => applique ET binaire(&) avec un masque sur le résultat décalé
         efface donc tout les autres 1 pour ne garder que des 0 a gauche de notre nombre
         on obtiens finalement un entier de 32 bits qui ne contient que le nombre à l'indice voulu */

        int result = (compressedArray[indexContainer] >> (INT_BITS - this.k - bitOffset)) & ((1 << this.k) - 1);
        return result;

    }
}
