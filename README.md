# Projet Génie Logiciel 2025 — BitPacking 

**Auteur :** OUAHRANI KHALDI Sofiane

---
## Description

J'ai réalisé ce projet dans le cadre de la matière **Génie Logiciel**, suivie en première année de M1 Informatique (Université Côte d'Azur).

Il implémente trois approches de **compression binaire** (*Bit Packing*) afin d’optimiser la taille mémoire des tableaux d’entiers.

Les trois méthodes disponibles sont :

* **BitPackingAligned** : compression alignée sans chevauchement.
* **BitPackingOverlap** : compression avec chevauchement de bits.
* **BitPackingOverflow** : compression avec chevauchement de bits et une séparation des grandes valeurs dans une zone *overflow*.

--- 

## Structure générale du projet

```
Projet_Genie_Logiciel/
│
├── src/main/java/gl/bp/
│   ├── BitPacking.java
│   ├── BitPackingAligned.java
│   ├── BitPackingOverlap.java
│   ├── BitPackingOverflow.java
│   ├── CompressionFactory.java
│   └── Main.java
│
├── RAPPORT_BitPacking_OUAHRANI_Sofiane.pdf
├── Software Engineering Project 2025.txt
└── README.md
```

---

## Compilation et exécution

### Prérequis

* Java 17 ou version ultérieure.

### Étapes d’exécution

Depuis la racine du projet :

```bash
cd src/main/java
javac gl/bp/*.java
java gl.bp.Main
```

---

## Sélection du mode de compression

Au lancement du programme, un **menu interactif** vous permet de choisir le mode à tester. Le choix se fait en deux étapes :

1. **Sélection du type de mode** :

    * `1` → Mode DÉMONSTRATION (permet de tester la compression/décompression sur vos propres tableaux)
    * `2` → Mode BENCHMARK (effectue des tests automatiques et affiche des statistiques globales)
    * `q` → Quitter le programme

2. **Choix de l’algorithme de compression pour le Mode DÉMONSTRATION** :
   Une fois dans le mode DÉMONSTRATION, le programme demande quel algorithme utiliser. Il suffit d’entrer l’une des options suivantes :

    * `1` → active la compression alignée (`BitPackingAligned`)
    * `2` → active la compression avec chevauchement (`BitPackingOverlap`)
    * `3` → active la compression avec zone de débordement (`BitPackingOverflow`)

Le programme affichera ensuite :
* Une **vérification** qui indique si le tableau est identique à la fin du cycle.
* Les **tailles** des tableaux originaux et compressés et **le taux de compression**.
* Le **temps CPU** des différentes opérations (compression, décompression et get).
* Les différents **Temps de transmission** et la **Rentabilité** ou non de la compression.


> Vous pouvez également tester l’opération `get(i)` autant de fois que vous le voulez pour accéder à une valeur compressée spécifique.

---

## Remerciements

Merci d’avoir pris le temps de lire ce document et d’évaluer mon travail.
J’espère que ce projet reflète pleinement mon engagement et ma compréhension des principes abordés en Génie Logiciel.




