//BitPackingOverflow - OUAHRANI KHALDI Sofiane
package gl.bp;

import java.util.ArrayList;
import java.util.List;

// 3 zones -> Header - Main data -> zone overflow
public class BitPackingOverflow extends BitPacking {

    //vars de suivi
    private int k_prime; // K' bits pour les petites valeurs
    private int k_overflow; // K_overflow pour les grandes valeurs
    private int k_ref; // K_ref bits pour l'index de l'overflow
    private int k_main; // bits totaux pour la zone principale
    private int k_main_payload; // bits du payload sans le flag
    private int overflow_start_index; // nb du conteneur ou l'overflow commence

    // const de masque
    private final int MASK_8_BITS = 255; //11111111
    private final int MASK_16_BITS = 65535; //1111111111111111
    private final int flag_bits = 1;
    private final int HEADER_SIZE_INTS = 2;

    // écriture/lecture => j'ai choisi d'utiliser la méthode par chevauchement comme dans ma classe BitPackingOverlap

    // j'insère un paquet de K bits de droite à gauche, en gérant le chevauchement

    private void writeOverlap(int[] array, int K_USE, int value, int[] state) {  //tab sortie/ K / valeur à add / états des pointeurs
        int index = state[0]; //num conteneur
        int bitOffset = state[1]; //pos bit de départ

        //si k = 32 -> 31 sinon erreur ou deborde
        final int K = (K_USE == INT_BITS) ? INT_BITS - 1 : K_USE;

        //cas ou pas de chevauchement , j'insère juste
        if (bitOffset + K <= INT_BITS) {
            array[index] |= (value << bitOffset);
            bitOffset += K;

            if (bitOffset == INT_BITS) { // conteneur suiv
                index++;
                bitOffset = 0;
            }
        } else {
            //cas avec chevauchement
            int bits1 = INT_BITS - bitOffset; // Bits qui rentrent
            int bits2 = K - bits1;      // Bits en plus pour le prochain conteneur

            // partie1
            int masquePartie1 = (1 << bits1) - 1;
            int valPartie1 = value & masquePartie1;
            array[index] |= (valPartie1 << bitOffset);

            index++;//cont suivant

            //partie2
            int valPartie2 = value >>> bits1; // decalage
            array[index] |= valPartie2; // Insère au Bit 0 du nouveau conteneur

            bitOffset = bits2; // nouveau decal = nb bits de la partie2
        }

        // je met à jours les var d'états pour reprendre au bon endroit au prochain appel de la fct
        state[0] = index;
        state[1] = bitOffset;
    }


    //lit un paquet de K b en mode overlap à l'aide des vars d'état
    private int readOverlap(int[] array, int K_USE, int[] state) {
        int index = state[0];
        int bitOffset = state[1];
        int result;
        final int K = (K_USE == INT_BITS) ? INT_BITS - 1 : K_USE;

        // masque
        final int MASK = (K_USE == INT_BITS) ? -1 : (1 << K_USE) - 1;

        if (bitOffset + K <= INT_BITS) {
            //pas d'overlap
            result = (array[index] >>> bitOffset) & MASK;

            bitOffset += K;
            if (bitOffset == INT_BITS) {
                index++;
                bitOffset = 0;
            }
        } else {
            // chevauchement
            int bits1 = INT_BITS - bitOffset; // bits première partie
            int bits2 = K - bits1;      // 2e partie

            // lecture bits part1
            int partie1 = (array[index] >>> bitOffset) & ((1 << bits1) - 1);

            index++;

            // lecture bits de conteneur suivant
            int partie2 = array[index] & ((1 << bits2)-1);

            // decal + fusion
            result = partie1 | (partie2 << bits1);

            // petit clean up
            result = result & MASK;

            bitOffset = bits2;
        }

        state[0] = index;
        state[1] = bitOffset;
        return result;
    }


    //j'ai écris cette fonction pour initialiser l'état interne de l'objet à partir du header
    //le tableau final utilise 2 int pour définir tout cela ([0] et [1])

    private void readHeader(int[] compressedArray) {
        //jdois toujours appeler cette fonction avant une décompression !!!
        if (compressedArray == null || compressedArray.length < HEADER_SIZE_INTS) {
            this.k = 0;
            return;
        }
        //premier int
        int header0 = compressedArray[0];
        // K_prime (bits 25-32) (pour les petits nb)
        this.k_prime = (header0 >>> 24) & MASK_8_BITS;

        // K_overflow (bits 17-24) (pour les grands nb)
        this.k_overflow = (header0 >>> 16) & MASK_8_BITS;

        // K_main (bits 9-16) pour la zone principale
        this.k_main = (header0 >>> 8) & MASK_8_BITS;

        // K_ref (bits 1-8) index de l'overflow
        this.k_ref = header0 & MASK_8_BITS;

        // K_main_payload taille du paquet sans le bit de flag -> jlutilise pour get
        this.k_main_payload = this.k_main - this.flag_bits;

        int header1 = compressedArray[1]; //2eme int de 32 bits

        // pour lire l'indice de l'entier ou la zone de l'overflow commence
        this.overflow_start_index = (header1 >>> 16) & MASK_16_BITS;


        this.originalSize = header1 & MASK_16_BITS; //nb de int du  tab original

        // Le K par défaut pour get
        this.k = this.k_main;
    }

    //ma fct pour analyser un tableau (j'ai mis static pour pas qu'elle ne dépende de l'instance de bitpackingOverflow) , et elle return les meilleures paramètres
    // !!! me concentrer sur avoir un cout total en bits le + petit
    private static double[] analyzeAndSetK(int[] array) {
        int maxBits = 0; //pour stocker le nb de bits du plus grand nombre
        for (int v : array) {
            int bits = (v == 0) ? 1 : 32 - Integer.numberOfLeadingZeros(v);
            if (bits > maxBits) {
                maxBits = bits;
            }
        }
        if (maxBits == 0) return new double[]{1, 0, 1, 0}; //tableau vide

        int bestKPrime = 1; //meilleur K'
        long bestTotalCost = Long.MAX_VALUE; //jle met au max comme ça le premier cout le remplace direct
        int bestKMainBits = 1; //pour stocker Kmain

        //teste chaque valeur de K'
        for (int k_prime_test = 1; k_prime_test <= maxBits; k_prime_test++) {
            int nOverflow = 0; //combien de nb doivent aller dans l'overflow
            for (int v : array) {
                if (((v == 0) ? 1 : 32 - Integer.numberOfLeadingZeros(v)) > k_prime_test) {
                    nOverflow++;
                }
            }
            //nb de bits pour coder l'index du nb de nombre dans l'overflow area
            int indexBits = (nOverflow > 0) ? (32 - Integer.numberOfLeadingZeros(nOverflow - 1)) : 0;
            //taille payload zone principale (max entre K' et Kref) -> de base j'avais juste mis K' mais enft quand j'ai beaucoup de nb dans l'overflow ou un K' tt petit jpeux pas coder Kref sur le nb de bits de K'..
            int k_main_payload = Math.max(k_prime_test, indexBits);
            int k_main_bits = 1 + k_main_payload; // bit de flag + payload = Kmain total

            long overflowCost = (long) nOverflow * maxBits; //cout overflow = nb de nombre en overflow * Kmax
            long mainCost = (long) array.length * k_main_bits; //taille tab * Kmain (pour la zone principale)
            long totalCost = mainCost + overflowCost; //cout total = taille de la zone principale + nb de int en overflow

            if (totalCost < bestTotalCost) { //ducoup si le coût actuel < meilleur coût précédent alors j'ai mon nouveau cout
                bestTotalCost = totalCost;
                bestKPrime = k_prime_test;
                bestKMainBits = k_main_bits;
            }
        }

        //et la on retourne nos 4 valeurs décisives :)
        return new double[]{(double) bestKPrime, (double) bestTotalCost, (double) bestKMainBits, (double) maxBits};
    }




    //analyse + sep + assemblage des 3 parties
    @Override
    public int[] compress(int[] input) {
        if (input == null || input.length == 0) {
            this.originalSize = 0;
            this.k = 0;
            return new int[0];
        }

        //calcul k' et séparation en 2 listes
        double[] meilleurs = analyzeAndSetK(input); //test de tt les k possibles et choix du meilleur k' pour le coût le plus faible

        this.k_prime = (int)meilleurs[0];
        int k_max_val = (int)meilleurs[3];

        //j'ai fait des listes séparées pour flag/payload pour que ce soit plus clair et pour garder l'ordre du tableau d'entrée
        List<Integer> largeNumbers = new ArrayList<>();
        List<Integer> finalPayloads = new ArrayList<>(); // contient la val ou l'index selon l'entier
        List<Integer> flags = new ArrayList<>(); // O ou 1

        //j'exam chaque valeur en entrée pour déterminer si je stocke sa valeur ou son index dans finalPayloads
        // et ducoup à quel "grand nombre" elle fait référence
        for (int val : input) {
            int bits_needed = (val == 0) ? 1 : 32 - Integer.numberOfLeadingZeros(val); //nb bits de la valeur
            if (bits_needed > this.k_prime) { //grand nombre
                largeNumbers.add(val);
                finalPayloads.add(largeNumbers.size() - 1); //index ajouté (=nb grands elts - 1)
                flags.add(1);
            } else { //petit nombre
                finalPayloads.add(val);
                flags.add(0);
            }
        }

        //calcul des k "finaux"

        int total_overflow_count = largeNumbers.size();

        //aucun grand nombre = cas simple
        if (total_overflow_count == 0) {
            super.calculateK(input);
            this.k_prime = this.k;
            this.k_ref = 0;
            k_max_val = this.k;
        } else {
            this.k_ref = (32 - Integer.numberOfLeadingZeros(total_overflow_count - 1)); //nb de bits pour coder l'index des nb en overflow
        }
        //---
        //nb de bits pour coder le plus grand entier en overflow
        this.k_overflow = k_max_val;
        //max entre k' pour les petits entiers et kref pour l'index
        this.k_main_payload = Math.max(this.k_prime, this.k_ref);
        //+ 1 bit de flag
        this.k_main = flag_bits + this.k_main_payload;

        this.k = this.k_main;
        this.originalSize = input.length;

        // allocation espace + finalisation de la compression

        long mainDataBits = (long) finalPayloads.size() * this.k_main; //nb bits nécessaires pour coder tt les petits nb + l'index des grands
        long mainDataSizeInts = (long) Math.ceil((double) mainDataBits / INT_BITS); //nb de conteneur de 32 bits nécessaires pour stocker tout ça
        long overflowBits = (long) largeNumbers.size() * this.k_overflow; //nb bits de la partie overflow

        //taille finale : header + partie principale + overflow
        int compressedSize = HEADER_SIZE_INTS + (int)mainDataSizeInts + (int)Math.ceil((double) overflowBits / INT_BITS);
        int[] compressedArray = new int[compressedSize]; //tableau final

        //### Conteneur ou commence la partie overflow => PROBLEME , ici je pourrais faire mieux car actuellement si le dernier entier
        //de la partie principale est représentable sur moins de k_main -1 bits alors j'aurais du bourrage avec des zéros
        int overflowStartIntIndex = HEADER_SIZE_INTS + (int)mainDataSizeInts;
        this.overflow_start_index = overflowStartIntIndex;

        //tableau pour écrire avec writeoverlap
        //tab[0] = index courant dans le tableau d'int et tab[1] = position du bit courant dans l'int
        int[] state = {0, 0};

        //jvais d'abord écrire dans la zone d'overflow
        state[0] = overflowStartIntIndex; //ducoup début de la zonne d'overflow
        state[1] = 0;

        for (int val : largeNumbers) {
            writeOverlap(compressedArray, this.k_overflow, val, state);
        }

        //écriture zone principale
        state[0] = HEADER_SIZE_INTS;
        state[1] = 0;

        for (int j = 0; j < finalPayloads.size(); j++) {

            int flag = flags.get(j); //leur flags respectifs
            int payload = finalPayloads.get(j); //lecture de tt les petits nb / index

            // Constr. paquet final (Flag + Payload)
            int payloadMask = (1 << this.k_main_payload) - 1;
            int finalPayload = (flag << this.k_main_payload) | (payload & payloadMask);

            writeOverlap(compressedArray, this.k_main, finalPayload, state);
        }

        //écriture header
        compressedArray[0] = (this.k_prime << 24) | (this.k_overflow << 16) | (this.k_main << 8) | this.k_ref;
        compressedArray[1] = (overflowStartIntIndex << 16) | this.originalSize;

        return compressedArray;
    }

    //fct de décompression
    @Override
    public int[] decompress(int[] compressedArray) {
        readHeader(compressedArray); //lecture header et init des var
        if (this.originalSize == 0) return new int[0];

        int[] outputArray = new int[this.originalSize];
        //j'appelle simplement get sur chaque elt
        for (int i = 0; i < this.originalSize; i++) {
            outputArray[i] = get(compressedArray, i, true); // J'AI RAJOUTE TRUE ici pour que ce soit plus optimisé !!!!!!
        }
        return outputArray;
    }

    @Override
    public int get(int[] compressedArray, int i){
        return get(compressedArray, i, false);
    }
    // juste j'ai surchargé la fonction car dans decompress les multiples appels à get ne faisait qu'appeller aussi readheader

    public int get(int[] compressedArray, int i, boolean headerDefined) {

        if (!headerDefined) {
            readHeader(compressedArray);
        } //lecture header =>  !!!! coute un peu cher si je commence à appeler get pleins de fois dans une boucle (decompress...)

        if (this.k == 0) {
            throw new IllegalStateException("Header invalide ou pas d'état initialisé");
        }

        // lecture du Paquet Principal (main yayload + flag)
        int totalBitPosition = HEADER_SIZE_INTS * INT_BITS + (i * this.k_main);

        //maj
        int[] state = {totalBitPosition / INT_BITS, totalBitPosition % INT_BITS};
        int mainPayloadWithFlag = readOverlap(compressedArray, this.k_main, state);

        // analyse du flag
        int flag = (mainPayloadWithFlag >>> this.k_main_payload) & 0x1;
        int payloadValue = mainPayloadWithFlag & ((1 << this.k_main_payload) - 1);

        if (flag == 0) {
            //val direct
            return payloadValue;
        } else {
            //ou index overflow
            int indexOverflow = payloadValue;

            //lecture overflow area
            int totalOverflowBitPosition = this.overflow_start_index * INT_BITS + (indexOverflow * this.k_overflow);

            state[0] = totalOverflowBitPosition / INT_BITS; //debut overflow
            state[1] = totalOverflowBitPosition % INT_BITS; //decalage

            return readOverlap(compressedArray, this.k_overflow, state);
        }
    }
}
