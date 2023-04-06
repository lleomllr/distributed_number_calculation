package projet.worker;

import java.math.*;
import java.util.*;

import projet.modele.*;

public class ThreadCalcul extends Thread {

    private boolean arret;
    private int id;
    private Hashtable<BigInteger, Integer> result;

    public ThreadCalcul(int id) {
        this.id = id;
        this.arret = false;
        result = new Hashtable<BigInteger, Integer>();
    }

    public void run() {
        // Recupere un entier chez le workeer

        // while(){
        // CalculPersistance(nb);
        // }
        // calcul de la persistance de ce nombre
        System.out.println("J'ai ete lancé threadCalcul " + id);
        while (true) {
            Tache t = null;
            synchronized (Worker.listTaches) {
                if (!Worker.listTaches.isEmpty()) {
                    t = Worker.getTache();
                }
            }
            if (t != null) {
                // effectuer le calcul de persistance pour la tâche
                for (int i = 0; i < t.getTaille(); i++) {
                    BigInteger valeur = t.getNbCourant();
                    int persistance = calculPersistance(valeur);
                    this.result.put(valeur, persistance);
                    System.out.println("Persistance de " + valeur + " : " + persistance);
                }
                // Acquérir le verrou avant d'accéder à la variable de résultat partagée
                synchronized (Worker.resultat) {
                    Worker.resultat.ajoutResultat(result);
                }

                System.out.println("Tache termineé puis ajouté avec succès");

            } else {
                try {
                    Thread.sleep(1000); // attendre 1 seconde avant de vérifier à nouveau la file d'attente
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Methode de calcul de la persistance d un nombre donné
    public int CalculPersistance(int number) {
        int persistance = 0;
        int produit = 1;
        while (number >= 10) {
            produit = 1;
            while (number != 0) {
                produit = produit * (number % 10);
                number = number / 10;
            }
            number = produit;
            persistance++;
        }
        return persistance;
    }

    public int calculPersistance(BigInteger n) {
        int persistence = 0;
        while (n.compareTo(BigInteger.TEN) >= 0) {
            BigInteger m = BigInteger.ONE;
            for (char c : n.toString().toCharArray()) {
                m = m.multiply(new BigInteger(String.valueOf(c)));
            }
            n = m;
            persistence++;
        }
        return persistence;
    }


    
}
