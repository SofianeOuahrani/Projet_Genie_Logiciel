// Main - OUAHRANI KHALDI Sofiane
package gl.bp;

import java.util.*;

//la classe principale de mon projet, exec du protocole de benchmarks, analyse de la rentabilité et utilisation des trois algorithmes
public class Main {

    // Paramètres du protocole de test
    private static final int INT_BITS = 32;
    private static final double NETWORK_SPEED_MBPS = 10.0; // vitesse de référence pour simuler le temps de transmission des données compressées
    private static final int BENCHMARK_SIZE = 1_000_000; // grooooos tableau pour les benchmarks

    // j'ai def ces valeurs max aléatoires que j'ai choisies pour générer les donnée pour obtenir des benchmarks plus réalistes sans nombres extrêmes
    private static final int MIN_RANDOM_MAX_VALUE = 511;
    private static final int MAX_RANDOM_MAX_VALUE = 131071;

    public static void main(String[] args) {
        System.out.println("--- Projet de Software Engineering 2025 : Bit Packing | OUAHRANI KHALDI Sofiane ---");

        try (Scanner scanner = new Scanner(System.in)) {

            //Menu principal
            System.out.println("\n[1] Choisissez le mode d'opération :");
            System.out.println(" 1 - Mode DÉMONSTRATION (Entrée/Sortie/Vérification)"); //pour tester les différentes méthodes de compression/décompress
            System.out.println(" 2 - Mode BENCHMARK (Statistiques globales sur gros tableaux)"); //pour avoir des stats globales sur un tableau d'1M de valeurs (sur les 3 méthodes)
            System.out.print(">>> Entrez votre choix (1-2) : ");
            int operationChoice = scanner.nextInt();

            if (operationChoice == 1) {
                runDemonstration(scanner);
            } else if (operationChoice == 2) {
                runBenchmark(scanner);
            } else {
                System.err.println("Choix invalide.");
            }

        } catch (InputMismatchException e) {
            System.err.println("\nErreur: Veuillez entrer un nombre valide.");
        } catch (Exception e) {
            System.err.println("\nUne erreur critique est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Mode DEMO (compression et decompression d'un tableau personnalisé)
    private static void runDemonstration(Scanner scanner) {
        System.out.println("\n--- Mode DÉMONSTRATION ---");

        //choix du type de compression
        BitPacking compressor = selectCompressor(scanner);
        String name = getCompressorName(compressor);

        //entrée des données
        int[] data = getCustomInput(scanner);

        if (data.length == 0) return;

        System.out.println("\nMode sélectionné : " + name);

        // ResultWithTime<T>  => (résultat + durée) (c'est ma classe générique dans BitPacking.java)
        BitPacking.ResultWithTime<int[]> compressResult = compressor.timeCompress(data);
        int[] compressedArray = compressResult.result;
        long timeCompress = compressResult.timeNs;

        BitPacking.ResultWithTime<int[]> decompressResult = compressor.timeDecompress(compressedArray);
        int[] decompressed = decompressResult.result;
        long timeDecompress = decompressResult.timeNs;

        BitPacking.ResultWithTime<Integer> getResult = compressor.timeGet(compressedArray, data.length / 2);
        long timeGet = getResult.timeNs;

        //calcul taille et taux de compression
        long originalBytes = (long) data.length * (INT_BITS / 8);
        long compressedBytes = (long) compressedArray.length * (INT_BITS / 8);
        long originalBits = originalBytes * 8;
        long compressedBits = compressedBytes * 8;

        //taux de compress = 1 - (taille_compressée / taille_originale)

        double compressionRate = 100.0 * (1.0 - (double) compressedBytes / originalBytes);


        System.out.println("\n=============================================");
        System.out.println("RÉSULTATS DE LA DÉMONSTRATION");
        System.out.println("=============================================");

        //je verif si le tableau est le meme avant la compression et après la compression+decompression
        if (Arrays.equals(data, decompressed)) {
            //j'ai mis des emojis pour mieux lire parceque je trouvais pas ça flagrant quand je testais
            System.out.println("✅ VÉRIFICATION : La décompression est exacte.");
        } else {
            System.out.println("❌ VÉRIFICATION : Erreur de données après décompression.");
        }

        //affichage temps + taille & taux de compress
        System.out.println("\n--- Taille et Taux ---");
        System.out.printf("   Taille Org. : %,d octets (%,d bits)\n", originalBytes, originalBits);
        System.out.printf("   Taille Comp.: %,d octets (%,d bits)\n", compressedBytes, compressedBits);
        System.out.printf("   Taux de Compression : %.2f%%\n", compressionRate);

        //temps que met le prog à compresser/décompresser/get
        System.out.println("\n--- Temps CPU (Nanosecondes) ---");
        System.out.printf("   T_Comp. (ns) : %,d\n", timeCompress);
        System.out.printf("   T_Décomp. (ns) : %,d\n", timeDecompress);
        System.out.printf("   T_Get (ns) : %,d\n", timeGet);

        //affichage tableau compressé si je veux
        System.out.print("\n[i] Afficher le tableau compressé ? (y/n) : ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            System.out.println("\nTableau Compressé (" + compressedArray.length + " élts) : " + Arrays.toString(compressedArray));
        }
        // voila démo terminée :)
        System.out.println("\n--- Démonstration Terminée ---");
    }


    //Mode BENCHMARK -> exécute les 3 algos sur un grand tableau pour obtenir des statistiques significatives
    private static void runBenchmark(Scanner scanner) {
        System.out.println("\n--- Mode BENCHMARK (Aléatoire) ---");

        //j'ai choisi de l'exec sur les trois modes pour pouvoir les comparer directement
        String[] modes = {
                CompressionFactory.TYPE_ALIGNED,
                CompressionFactory.TYPE_OVERLAP,
                CompressionFactory.TYPE_OVERFLOW
        };

        Random rand = new Random();

        // On exécute le test NUM_RUNS fois pour avoirs des résultats plus random
        final int NUM_RUNS = 2;
        System.out.printf("Exécution de %d tests complets sur un tableau de %,d éléments.\n", NUM_RUNS, BENCHMARK_SIZE);

        for (int run = 1; run <= NUM_RUNS; run++) {
            //K random généré entre les bornes que j'ai init au début
            int randomMaxValue = rand.nextInt(MAX_RANDOM_MAX_VALUE - MIN_RANDOM_MAX_VALUE + 1) + MIN_RANDOM_MAX_VALUE;
            int[] data = generateRandomData(BENCHMARK_SIZE, randomMaxValue);

            //calcul taille standard = nb octets + calcul temps de trans du tab standard
            long originalBytes = (long) data.length * (INT_BITS / 8);
            double timeStd_ns = calculateTransmissionTime(originalBytes);

            System.out.printf("\n=== TEST GLOBAL #%d (Max Val: %,d) ===\n", run, randomMaxValue);
            System.out.printf("   T_Trans. Standard (BASELINE) : %,.0f ns\n", timeStd_ns);

            //pour les 3 modes de compression
            for (String type : modes) {
                BitPacking compressor = CompressionFactory.createCompressor(type);
                runDiagnostic(compressor, type, data, randomMaxValue, timeStd_ns);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("\n--- Benchmark Global Terminé ---");
    }

    //exec les calculs et l'analyse de la rentabilité pour un mode en particulier
    //la j'utilise ducoup la classe générique que j'ai créée dans BitPacking
    private static void runDiagnostic(BitPacking compressor, String name, int[] data, int currentMaxValue, double timeStd_ns) {

        BitPacking.ResultWithTime<int[]> compressResult = compressor.timeCompress(data);
        int[] compressedArray = compressResult.result; //resultat de la compression
        long timeCompress = compressResult.timeNs; //temps
        //res decomp
        BitPacking.ResultWithTime<int[]> decompressResult = compressor.timeDecompress(compressedArray);
        long timeDecompress = decompressResult.timeNs; //temps decompress
        //resultat get
        BitPacking.ResultWithTime<Integer> getResult = compressor.timeGet(compressedArray, data.length / 2);
        long timeGet = getResult.timeNs; //temps get
        //temps compression + decompression
        long totalCpuTime = timeCompress + timeDecompress;
        //taille tableau original en octets
        long originalBytes = (long) data.length * (INT_BITS / 8);
        long compressedBytes = (long) compressedArray.length * (INT_BITS / 8); //taille tableau compressé en octets

        long originalBits = originalBytes * 8; //nb bits
        long compressedBits = compressedBytes * 8; //nb bits

        //taux de compression = taille tab compressé / tab original
        double compressionRate = 100.0 * (1.0 - (double) compressedBytes / originalBytes);

        //temps de trans. du tab compressé
        double timeComp_ns = calculateTransmissionTime(compressedBytes);
        double gainTrans_ns = timeStd_ns - timeComp_ns; //temps gagné

        System.out.println("\n=============================================");
        System.out.println("RÉSULTATS DIAGNOSTIC : " + name);
        System.out.println("=============================================");
        System.out.printf("   K : %d bits (Max Val: %,d)\n", compressor.k, currentMaxValue);
        System.out.printf("   Taille Org. : %,d octets (%,d bits)\n", originalBytes, originalBits);
        System.out.printf("   Taille Comp.: %,d octets (%,d bits)\n", compressedBytes, compressedBits);
        System.out.printf("   Taux de Compression : %.2f%%\n", compressionRate);

        //seuil optimal (K')
        if (compressor instanceof BitPackingOverflow) {
            BitPackingOverflow overflowCompressor = (BitPackingOverflow) compressor;
            System.out.printf("   [i] K' (pour les petits nombres) : %d bits\n", overflowCompressor.getKPrime());
        }

        System.out.println("\n--- Coût CPU et Rentabilité ---");
        System.out.printf("   Coût CPU Total : %,d ns\n", totalCpuTime);
        System.out.printf("   Gain Temps Réseau : %,.0f ns\n", gainTrans_ns);
        System.out.printf("   T_Get (ns) : %,d\n", timeGet);

        //j'estime que c'est rentable dès que le temps gagné est supérieur au coût CPU total (compression + decompression)
        // j'ai encore mis des emojis pour qu'on s'y retrouve lors de la lecture des résultats
        if (gainTrans_ns > totalCpuTime) {
            System.out.println("\n✅ RENTABLE : Le gain de temps réseau dépasse le coût du CPU.");
        } else {
            System.out.println("\n❌ NON RENTABLE : Le coût du CPU est trop élevé pour ce gain.");
        }
    }
    //fonction pour renvoyer un tableau d'entier correspondant aux valeurs customs entrées
    private static int[] getCustomInput(Scanner scanner) {
        System.out.println("\n--- Entrée personnalisée ---");
        System.out.print("Entrez les entiers séparés par des espaces (ex: 10 5000 3 1): ");
        scanner.nextLine();
        String line = scanner.nextLine().trim();

        String[] parts = line.split("\\s+");
        List<Integer> intList = new ArrayList<>();

        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    intList.add(Integer.parseInt(part));
                } catch (NumberFormatException e) {
                    System.err.println("Ignoré: '" + part + "' n'est pas un nombre. Veuillez entrer des nombres valides.");
                }
            }
        }

        if (intList.isEmpty()) {
            System.out.println("Aucun nombre valide entré. Utilisation d'un tableau par défaut {1, 2, 3}.");
            return new int[]{1, 2, 3};
        }

        int[] data = new int[intList.size()];
        for(int i = 0; i < intList.size(); i++) {
            data[i] = intList.get(i);
        }
        return data;
    }
    //fct pour le calcul du temps de transmission
    private static double calculateTransmissionTime(long bytes) {
        //(octets en bits)* 1 000 000 000 pour obtenir le temps en ns  / vitesse réseau en Mbps * 1 000 000 pour avoir le temps en sec
        return (bytes * 8.0 * 1_000_000_000.0) / (NETWORK_SPEED_MBPS * 1_000_000.0); //résultat en ns
    }

    //genere un tableau d'int random
    private static int[] generateRandomData(int size, int max) {
        Random r = new Random();
        int[] tab = new int[size];
        for (int i = 0; i < size; i++) {
            tab[i] = r.nextInt(max + 1);
        }
        if (size > 0 && max > 0) {
            tab[0] = max; //pour être sur que la valeur max est présente
        }
        return tab;
    }
    //fct qui return le nom du compresseur selon son instance
    private static String getCompressorName(BitPacking compressor) {
        if (compressor instanceof BitPackingAligned) return CompressionFactory.TYPE_ALIGNED;
        if (compressor instanceof BitPackingOverlap) return CompressionFactory.TYPE_OVERLAP;
        if (compressor instanceof BitPackingOverflow) return CompressionFactory.TYPE_OVERFLOW;
        return "Inconnu";
    }

    //pour selectionner de manière interactive le compresseur qu'on veux
    private static BitPacking selectCompressor(Scanner scanner) {
        System.out.println("\n[1] Choisissez le mode de compression :");
        System.out.println(" 1 - Aligned (V1 - Sans chevauchement)");
        System.out.println(" 2 - Overlap (V2 - Avec chevauchement)");
        System.out.println(" 3 - Overflow (V3)");
        System.out.print(">>> Entrez votre choix (1-3) : ");

        int modeChoice = scanner.nextInt();
        String type;

        switch (modeChoice) {
            case 1: type = CompressionFactory.TYPE_ALIGNED; break;
            case 2: type = CompressionFactory.TYPE_OVERLAP; break;
            case 3: type = CompressionFactory.TYPE_OVERFLOW; break;
            default:
                System.err.println("Choix invalide. Utilisation du mode par défaut : V1 Aligned.");
                type = CompressionFactory.TYPE_ALIGNED;
        }
        return CompressionFactory.createCompressor(type);
    }
}

//finalement
