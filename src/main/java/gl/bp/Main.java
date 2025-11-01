// Main - OUAHRANI KHALDI Sofiane
package gl.bp;

import java.util.*;

//la classe principale de mon projet, exec du protocole de benchmarks, analyse de la rentabilité et utilisation des trois algorithmes
public class Main {

    // Paramètres du protocole de test
    private static final int INT_BITS = 32;
    private static final double NETWORK_SPEED_MBPS = 10.0; // vitesse de référence pour simuler le temps de transmission des données compressées

    // j'ai def ces valeurs max aléatoires que j'ai choisies pour générer les donnée pour obtenir des benchmarks plus réalistes sans nombres extrêmes
    private static final int MIN_RANDOM_MAX_VALUE = 511;
    private static final int MAX_RANDOM_MAX_VALUE = 131071;

    public static void main(String[] args) {
        //Menu principal
        System.out.println("\n==============================================");
        System.out.println("     Projet Bit Packing - 2025");
        System.out.println("   Réalisé par : OUAHRANI KHALDI Sofiane");
        System.out.println("==============================================");

        try (Scanner scanner = new Scanner(System.in)) {

            boolean running = true; //var pour permettre le retour au menu principal à la fin
            while (running) {
                //Menu principal
                System.out.println("\n╔══════════════════════════════════════════════╗");
                System.out.println("║                 MENU PRINCIPAL               ║");
                System.out.println("╠══════════════════════════════════════════════╣");
                System.out.println("║ 1 - Mode DÉMONSTRATION (Entrée/Sortie/Vérif) ║");
                System.out.println("║ 2 - Mode BENCHMARK (Stats globales)          ║");
                System.out.println("║ q - Quitter                                  ║");
                System.out.println("╚══════════════════════════════════════════════╝");
                System.out.print(">>> Entrez votre choix (1-2 ou q) : ");

                String choice = scanner.next().trim().toLowerCase();

                switch (choice) {
                    case "1":
                        runDemonstration(scanner);
                        break;
                    case "2":
                        runBenchmark(scanner);
                        break;
                    case "q": //### option quitter
                        running = false;
                        System.out.println("\n Fin du programme. Merci pour votre attention !");
                        break;
                    default:
                        System.err.println("Choix invalide, réessayez.");
                }
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

        //entrée des données regroupées dans le tableau data
        int[] data = getCustomInput(scanner);
        //rien
        if (data.length == 0) return;

        System.out.println("\nMode sélectionné : " + name);

        // ResultWithTime<T>  => (résultat + durée) (c'est ma classe générique dans BitPacking.java)
        //res compress + temps compress
        BitPacking.ResultWithTime<int[]> compressResult = compressor.timeCompress(data);
        int[] compressedArray = compressResult.result;
        long timeCompress = compressResult.timeNs;
        //res decompress + temps decompress
        BitPacking.ResultWithTime<int[]> decompressResult = compressor.timeDecompress(compressedArray);
        int[] decompressed = decompressResult.result;
        long timeDecompress = decompressResult.timeNs;
        //res get + temps get
        BitPacking.ResultWithTime<Integer> getResult = compressor.timeGet(compressedArray, data.length / 2);
        long timeGet = getResult.timeNs;

        //calcul taille et taux de compression
        long originalBytes = (long) data.length * (INT_BITS / 8);
        long compressedBytes = (long) compressedArray.length * (INT_BITS / 8);
        long originalBits = originalBytes * 8;
        long compressedBits = compressedBytes * 8;

        //calculs pour la renta
        double timeStd_ns = calculateTransmissionTime(originalBytes);
        double timeComp_ns = calculateTransmissionTime(compressedBytes);
        double gainTrans_ns = timeStd_ns - timeComp_ns; //temps gagné
        long totalCpuTime = timeCompress + timeDecompress;
        double totalWithCompression_ns = timeCompress + timeComp_ns + timeDecompress;

        //taux de compress = nb bytes du tab compressé / nb bytes tab original
        double compressionRate = 100.0 * (1.0 - (double) compressedBytes / originalBytes);

        System.out.println("\n=============================================");
        System.out.println("RÉSULTATS DE LA DÉMO");
        System.out.println("=============================================");

        //si le tab compressé puis decomp = tab de départ alors validé :)
        if (Arrays.equals(data, decompressed)) {
            System.out.println("✅ VÉRIF : Le tableau est le même après décompression.");
        } else {
            System.out.println("❌ VÉRIF : Le tableau n'est pas le même après décompression.");
            return;
        }

        //affichage des infos sympas
        System.out.println("\n--- Taille et Taux ---");
        System.out.printf("   Taille Org. : %,d octets (%,d bits)\n", originalBytes, originalBits);
        System.out.printf("   Taille Comp.: %,d octets (%,d bits)\n", compressedBytes, compressedBits);
        System.out.printf("   Taux de Compression : %.2f%%\n", compressionRate);

        System.out.println("\n--- Temps CPU (Nanosecondes) ---");
        System.out.printf("   T_Comp. (ns) : %,d\n", timeCompress);
        System.out.printf("   T_Décomp. (ns) : %,d\n", timeDecompress);
        System.out.printf("   T_Get (ns) : %,d\n", timeGet);

        System.out.println("\n--- Temps de Transmission & Rentabilité ---");
        System.out.printf("   Transmission SANS compression : %,.0f ns\n", timeStd_ns);
        System.out.printf("   Transmission AVEC compression : %,.0f ns\n", timeComp_ns);
        System.out.printf("   Temps total (comp + trans + décomp) : %,.0f ns\n", totalWithCompression_ns);
        System.out.printf("   Gain Transmission : %,.0f ns\n", gainTrans_ns);

        //si le temps gagné en transmission < temps que prend le cpu a compresser puis decomp alors le temps de trans du tab compr + compr + decompr < temps trans original donc on est rentable
        if (gainTrans_ns > totalCpuTime) { //j'ai mis des emojis aussi pour que vous vous repèriez mieux Monsieur :)
            System.out.println("\n✅ RENTABLE : Le gain de temps réseau dépasse le coût du CPU.");
        } else {
            System.out.println("\n❌ NON RENTABLE : Le coût du CPU est trop élevé pour ce gain.");
        }
        //si je veux voir a quoi ressemble le tableau compressé
        System.out.print("\n[i] Afficher le tableau compressé ? (y/n) : ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            System.out.println("\nTableau Compressé (" + compressedArray.length + " élts) : " + Arrays.toString(compressedArray));
        }

        //pour tester get après la compress autant de fois que je veux :)
        System.out.print("\nSouhaitez-vous tester l'opération get() sur le tableau compressé ? (y/n) : ");
        if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
            int originalSize = data.length;
            while (true) {
                System.out.print("Entrez un indice i à tester (entre 0 et " + (originalSize - 1) + ") ou 'q' pour quitter : ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("q")) break;

                try {
                    int i = Integer.parseInt(input); //tjr pase en int
                    if (i < 0 || i >= originalSize) {
                        System.out.println("Indice hors limites. Veuillez entrer un entier entre 0 et " + (originalSize - 1) + ".");
                        continue;
                    }

                    // tjr timeGet à chaque iter
                    BitPacking.ResultWithTime<Integer> getres = compressor.timeGet(compressedArray, i);
                    System.out.println("-> Valeur récupérée à l'indice " + i + " : " + getres.result);
                    System.out.println("-> Temps d'exécution de get() : " + getres.timeNs + " ns\n");

                } catch (NumberFormatException e) {
                    System.out.println("Veuillez entrer un entier valide ou 'q' pour quitter.");
                }
            }
        }



        System.out.println("\n--- Démonstration Terminée ---"); //tadaaa
    }


    //Mode BENCHMARK -> exécute les 3 algos sur plusieurs tailles de tableaux pour obtenir des statistiques variées avec des int aléatoires entre les bornes (qu'on peux changer bien sur)
    private static void runBenchmark(Scanner scanner) {
        System.out.println("\n--- Mode BENCHMARK (Aléatoire & Interactif) ---");

        //les 3 modes de compression conformément à la factory que j'ai écrite
        String[] modes = {
                CompressionFactory.TYPE_ALIGNED,
                CompressionFactory.TYPE_OVERLAP,
                CompressionFactory.TYPE_OVERFLOW
        };

        Random rand = new Random(); //un peu de hasard

        int[] sizes = {10, 100, 1_000, 50_000, 1_000_000};

        System.out.printf("Exécution de tests complets sur %d tailles de tableaux : %s\n",
                sizes.length, Arrays.toString(sizes));

        int testNum = 1; //num du test
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            int randomMaxValue = rand.nextInt(MAX_RANDOM_MAX_VALUE - MIN_RANDOM_MAX_VALUE + 1) + MIN_RANDOM_MAX_VALUE;
            int[] data = generateRandomData(size, randomMaxValue);

            long originalBytes = (long) data.length * (INT_BITS / 8);
            double timeStd_ns = calculateTransmissionTime(originalBytes);

            System.out.printf("\n=== TEST #%d | Taille : %,d éléments | MaxVal : %,d ===\n",
                    testNum++, size, randomMaxValue);
            System.out.printf("=> Taille originale : %,d octets\n", originalBytes);
            System.out.printf("=> Temps de transmission standard : %,.0f ns\n", timeStd_ns);

            for (String type : modes) { //pour chaque mode
                BitPacking compressor = CompressionFactory.createCompressor(type);
                runDiagnostic(compressor, type, data, randomMaxValue, timeStd_ns);

                try {
                    Thread.sleep(150); //sinon ça devient vite le "bordel" pour lire
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            //seulement si c’est pas le dernier test
            if (i < sizes.length - 1) {
                System.out.printf("\n➡ Souhaitez-vous passer au test suivant (%,d éléments) ? (y/n) : ",
                        sizes[i + 1]);
                String response = scanner.next().trim().toLowerCase();
                if (!response.equals("y")) {
                    System.out.println("\n### Benchmark interrompu par l'utilisateur.");
                    break;
                }
            }
            System.out.println("\n-----------------------------------------------");
        }

        System.out.println("\n--- Benchmark Global Terminé ---");
        //retour au menu principal automatique
        System.out.println("\n Retour au menu principal...");
    }


    //exec les calculs et l'analyse de la rentabilité pour un mode en particulier choisi
    private static void runDiagnostic(BitPacking compressor, String name, int[] data, int currentMaxValue, double timeStd_ns) {
        //res + temps compress
        BitPacking.ResultWithTime<int[]> compressResult = compressor.timeCompress(data);
        int[] compressedArray = compressResult.result;
        long timeCompress = compressResult.timeNs;

        //res + temps decompress
        BitPacking.ResultWithTime<int[]> decompressResult = compressor.timeDecompress(compressedArray);
        long timeDecompress = decompressResult.timeNs;
        //res + temps get
        BitPacking.ResultWithTime<Integer> getResult = compressor.timeGet(compressedArray, data.length / 2);
        long timeGet = getResult.timeNs;
        //temps cpu + tailles avant/après
        long totalCpuTime = timeCompress + timeDecompress;
        long originalBytes = (long) data.length * (INT_BITS / 8);
        long compressedBytes = (long) compressedArray.length * (INT_BITS / 8);

        //1 octet = 8 bits
        long originalBits = originalBytes * 8;
        long compressedBits = compressedBytes * 8;

        //taux de compress
        double compressionRate = 100.0 * (1.0 - (double) compressedBytes / originalBytes);

        //temps
        double timeComp_ns = calculateTransmissionTime(compressedBytes);
        double gainTrans_ns = timeStd_ns - timeComp_ns;

        //Mon petit affichage
        System.out.println("\n=============================================");
        System.out.println("RÉSULTATS DIAGNOSTIC : " + name);
        System.out.println("=============================================");
        System.out.printf("   K : %d bits (Max Val: %,d)\n", compressor.k, currentMaxValue);
        System.out.printf("   Taille Org. : %,d octets (%,d bits)\n", originalBytes, originalBits);
        System.out.printf("   Taille Comp.: %,d octets (%,d bits)\n", compressedBytes, compressedBits);
        System.out.printf("   Taux de Compression : %.2f%%\n", compressionRate);

        if (compressor instanceof BitPackingOverflow) {
            BitPackingOverflow overflowCompressor = (BitPackingOverflow) compressor;
            System.out.printf("   [i] K' (pour les petits nombres) : %d bits\n", overflowCompressor.getKPrime());
        }

        System.out.println("\n--- Coût CPU et Rentabilité ---");
        System.out.printf("   Coût CPU Total : %,d ns\n", totalCpuTime);
        System.out.printf("   Gain Temps Réseau : %,.0f ns\n", gainTrans_ns);
        System.out.printf("   T_Get (ns) : %,d\n", timeGet);

        if (gainTrans_ns > totalCpuTime) {
            System.out.println("\n✅ RENTABLE : Le gain de temps réseau dépasse le coût du CPU.");
        } else {
            System.out.println("\n❌ NON RENTABLE : Le coût du CPU est trop élevé pour ce gain.");
        }
    }


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

    //calcul temps de transmission
    private static double calculateTransmissionTime(long bytes) {
        return (bytes * 8.0 * 1_000_000_000.0) / (NETWORK_SPEED_MBPS * 1_000_000.0);
    }

    //fct pour générer un tableau de taille  size et avec un entier max choisi
    private static int[] generateRandomData(int size, int max) {
        Random r = new Random();
        int[] tab = new int[size];
        for (int i = 0; i < size; i++) {
            tab[i] = r.nextInt(max + 1);
        }
        if (size > 0 && max > 0) {
            tab[0] = max;
        }
        return tab;
    }

    //fct pour récup le nom correspondant au type de la sous classe
    private static String getCompressorName(BitPacking compressor) {
        if (compressor instanceof BitPackingAligned) return CompressionFactory.TYPE_ALIGNED;
        if (compressor instanceof BitPackingOverlap) return CompressionFactory.TYPE_OVERLAP;
        if (compressor instanceof BitPackingOverflow) return CompressionFactory.TYPE_OVERFLOW;
        return "Inconnu";
    }

    //fct pour le choix de la méthode de compression qui utilise ma factory
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
