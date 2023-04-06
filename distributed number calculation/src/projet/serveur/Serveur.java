package projet.serveur;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import projet.modele.Tache;

public class Serveur {
	static int port1 = 8080;
	static int port2 = 8081;
	static final int maxClients = 50;
	static final int maxWorker = 30;

	/*
	 * Creation du fichier pour stocker les données
	 */
	
	

	static PrintWriter pw[];
	static int workerCoeurDispo[]; // Liste des worker dispo avec leur nombre de coeur de leur processeur
	static ArrayBlockingQueue<Tache> listeTache; // Liste des taches dont dispose le serveur
	static Hashtable<BigInteger, Integer> stockageCourant; // stocker les resultat que va envoyer le worker
	static Hashtable<BigInteger, Integer> stockageIntermed;
	static Hashtable<BigInteger, Integer> stockageLibre; // Pour pouvoir faire des echanges lorsque le prmierest plein
	static Hashtable<BigInteger, Hashtable<BigInteger, Integer>> MemoireStockage;
	static final int MaxtailleHashtable = 800000;
	static ConcurrentHashMap<BigInteger, String> references ;

	static int numClient = 0;
	static int numWorker = 0;

	// Pour utiliser un autre port pour le serveur, l'exécuter avec la commande :
	// java ServeurMC 8081
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		if (args.length > 1) {

			port1 = Integer.parseInt(args[0]);
			port2 = Integer.parseInt(args[1]);
		}
		/*
		 * Initialisation des variable
		 */
		pw = new PrintWriter[maxClients]; // Pour stocker les clients
		workerCoeurDispo = new int[maxWorker]; // Pour stocker l'id des workers avec
		listeTache = new ArrayBlockingQueue<Tache>(8000000);
		stockageCourant = new Hashtable<BigInteger, Integer>();
		stockageIntermed = new Hashtable<BigInteger, Integer>();
		stockageLibre = new Hashtable<BigInteger, Integer>();
		MemoireStockage = new Hashtable<BigInteger, Hashtable<BigInteger, Integer>>();
		references = new ConcurrentHashMap<BigInteger,String>();
		/*
		 * Creation des threaads et leur lancement sachant qu on a un thread qui
		 * s'occupe de la
		 * production des nombres et les deux autres sont les threads serveur un pour
		 * clients et
		 * l'autre pour le workers
		 */
		ThreadProductionTaches ProduitTache = new ThreadProductionTaches(listeTache);
		ProduitTache.start();
		ServeurClient clientserveur = new ServeurClient(port1);
		clientserveur.start();
		ServeurWorker workerserveur = new ServeurWorker(port2);
		workerserveur.start();

		/*
		 * PARTIE POUR TESTER SI MON PROGRAMME MARCHE BIEN
		 */
		
		
		// while (true) {
		// System.out.println(listeTache.size());
		// System.out.println(listeTache.take().getTaille());
		// }

	} 


	/*
	 * Methode qui ajoute les elements  dans le fichier 
	 */
	public void ajoutHashtableFichier(Hashtable<BigInteger, Integer> ht1, BigInteger bd) {
        ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("Projet4B/Projet4B/src/projet/serveur/Stockage/Fichier.dat"));
			oos.writeObject(ht1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
    }


	/**
	 * Methode pour simplifier l'ajout des resultats dans la hashtable des resultats
	 */
	public synchronized static void addResultat(Hashtable<BigInteger, Integer> res) {

		//stockageCourant.putAll(res);
		Set<BigInteger> setOfkeys = res.keySet();
			for (BigInteger key : setOfkeys) {
				
				try {
					stockageCourant.put(key, res.get(key));				
				} catch (OutOfMemoryError e) {
					//Gestion de l'exception
					System.out.println("Hashtable plein, ajout dans la memoire " );
					//MemoireStockage.put(stockageCourant.keySet().iterator().next(), stockageCourant);
					MemoireStockage.put(getFirstElement(stockageCourant), stockageCourant);
					stockageCourant = stockageIntermed;
					stockageIntermed = new Hashtable<BigInteger, Integer>();
					// Code pour libérer de la mémoire ou pour effectuer une autre action
				
				}
			}

		

	}

	/**
	 * @param nombre
	 * @return
	 *         Methode pour retrouver la persistance d un nombre qu on le donne en
	 *         parametre
	 */
	public synchronized static int RetrouvePersistance(BigInteger nombre) {
		if(stockageCourant.containsKey(nombre)){
			System.out.println("la clé se trouve dans la hashtable");
			return (Integer)stockageCourant.get(nombre);
		}
		//else if(!MemoireStockage.isEmpty())
		else{
			BigInteger clePrecedente = BigInteger.ONE;
			ArrayList<BigInteger> setOfkeys  = new ArrayList<BigInteger>(MemoireStockage.keySet());
			//List<BigInteger> setOfkeys = (List<BigInteger>) MemoireStockage.keys();
			Collections.sort((List<BigInteger>) setOfkeys);
			for (BigInteger key : setOfkeys) {
				if (nombre.compareTo(key) < 0) {
					for (BigInteger cle :setOfkeys) {
						if (cle.compareTo(key) == 0) {
						// cle précédente est la clé recherchée (sauf si c'était la première)
							break;
						}
						clePrecedente = cle;
					}
					stockageLibre=(Hashtable<BigInteger, Integer>) MemoireStockage.get(clePrecedente).clone();
					if(stockageLibre.containsKey(nombre)){
						System.out.println("la clé se trouve dans la hashtable");
						return (Integer)stockageLibre.get(nombre);
					}
					break;
				}
			
			}
			// System.out.println("Elle etait VIDE");
			// stockageLibre=(Hashtable<BigInteger, Integer>) MemoireStockage.get(clePrecedente).clone();
			// System.out.println("Elle n est pas vide");
			// //return stockageLibre.get(nombre);
			
			
			
			System.out.println("la clé ne se trouve pas dans la hashtable");
				
			
		}
		return -1;
		//return stockageCourant.size();


	}

	/*
	 * Methode pour verifier si la liste des taches est pleine pour faire attendre
	 * le threadProductionTaches
	 */
	public static boolean listTachePlein() {
		return (MaxtailleHashtable - listeTache.size()) == 0;
	}

	/*
	 * Methode de calcul de la moyenne
	 */

	public synchronized static double calculMoyenne(){
		double moyenne;
		if(!MemoireStockage.isEmpty()){
			double somme =0;
			for(BigInteger key : MemoireStockage.keySet()){
				somme +=MoyenneHashtable(MemoireStockage.get(key));
			}
			moyenne = (double) somme / MemoireStockage.size();
			return moyenne;
		}
		else{
			moyenne=MoyenneHashtable(stockageCourant);
			return moyenne;
		}
		
	}

	public static  double MoyenneHashtable(Hashtable<BigInteger,Integer> hash){
		int somme = 0;
		for (Integer valeur : hash.values()) {
    		somme += valeur;
		}
		double moyenne = (double) somme / hash.size();
		return moyenne;
	}


	 /*
	  * Methode de calcul de la mediane 
	  */

	public synchronized  static double calculMediane(){
		double mediane;
		if(!MemoireStockage.isEmpty()){
			
			List<Double> valeurs = new ArrayList<Double>();


		
			for(BigInteger key : MemoireStockage.keySet()){
				double med =MedianeHashtable(MemoireStockage.get(key));
				valeurs.add(med);
			}
				// Tri de la liste
			Collections.sort(valeurs);
			
			int taille = valeurs.size();
			if (taille % 2 == 0) {
				mediane = (double) (valeurs.get(taille/2) + valeurs.get((taille/2)-1)) / 2;
			} else {
				mediane = (double) valeurs.get(taille/2);
			}	
			return mediane;
		}
		else{
			mediane = MedianeHashtable(stockageCourant);
			return mediane;
		}
		
	}

	public static double MedianeHashtable(Hashtable<BigInteger,Integer> hash){
		List<Integer> valeurs = new ArrayList<>(hash.values());

		// Tri de la liste
		Collections.sort(valeurs);

		// Calcul de la médiane
		double mediane;
		int taille = valeurs.size();
		if (taille % 2 == 0) {
			mediane = (double) (valeurs.get(taille/2) + valeurs.get((taille/2)-1)) / 2;
		} else {
			mediane = (double) valeurs.get(taille/2);
		}
		return mediane;
	}
	/*
	 * Methode pour calculer le nombre d'occcurence 
	 */

	public synchronized static int calculOccurrence(int valeurRecherchee){
		int  somme =0;
		if(!MemoireStockage.isEmpty()){
			
			for(BigInteger key : MemoireStockage.keySet()){
				somme +=OccurrenceHashtable(MemoireStockage.get(key),valeurRecherchee);
			}
			
			return somme;
		}
		else{
			somme=OccurrenceHashtable(stockageCourant, valeurRecherchee);
			return somme;
		}
		
	}

	public static int OccurrenceHashtable(Hashtable<BigInteger,Integer> hash,int valeurRecherchee){
		
		int nbOccurrences = 0;

		for (int valeur : hash.values()) {
			if (valeur == valeurRecherchee) {
				nbOccurrences++;
			}
		
		}
		return nbOccurrences;

	}

	/*
	 * Calcul de maximum d une hashtable
	 */
	public synchronized  static List<BigInteger> listePersistanceMax(){
		int max =0;
		List<BigInteger> liste = new ArrayList<BigInteger>() ;
		if(!MemoireStockage.isEmpty()){
			
			for(BigInteger key : MemoireStockage.keySet()){
				if(max<MaxHashtable(MemoireStockage.get(key))){
					max=MaxHashtable(MemoireStockage.get(key));
				}			
			}
			for(BigInteger key : MemoireStockage.keySet()){
				liste.addAll(getKeysByValue(MemoireStockage.get(key),max));	
			}
			return liste;

		}
		else{
			max=MaxHashtable(stockageCourant);
			liste=getKeysByValue(stockageCourant,max);
			return liste;
		}
	}

	/*
	 * FONCTION POUR TROUVER LES CLE DANS UNE HASHTABLE QUI ONT TOUS LA VALEUR QUI EST PASSE EN PARAMETRE 
	 */

	public static List<BigInteger> getKeysByValue(Hashtable<BigInteger, Integer> hashtable, int value) {
		List<BigInteger> keys = new ArrayList<BigInteger>();
		for (BigInteger key : hashtable.keySet()) {
			if (hashtable.get(key)==(value)) {
				keys.add(key);
			}
		}
		return keys;
	  }
	
	/*
	 * FONCTION QUI DETERMINE LE MAXIMUM D UNE HASHTABLE
	 */

	public static int MaxHashtable(Hashtable<BigInteger,Integer> hash){
		int max =0;
		for (int valeur : hash.values()) {
			if (valeur >max) {
				max =valeur;
			}
		
		}
		return max;
	}

	/*
	 * CALCUL DU MAXIMUN DES PERSISTANCES
	 */

	public synchronized static int calculMaxPersistance(){
		int max =0;
		List<BigInteger> liste = new ArrayList<BigInteger>() ;
		if(!MemoireStockage.isEmpty()){
			
			for(BigInteger key : MemoireStockage.keySet()){
				if(max<MaxHashtable(MemoireStockage.get(key))){
					max=MaxHashtable(MemoireStockage.get(key));
				}			
			}

		}
		else{
			max=MaxHashtable(stockageCourant);
			
		
		}
		return max;
	}


	//// Methode pour recuperer le premier element d une hashtable 
	public static BigInteger getFirstElement(Hashtable<BigInteger,Integer> hash){

		ArrayList<BigInteger> keysList = new ArrayList<BigInteger>(hash.keySet());

		// Trier les clés par ordre alphabétique
		Collections.sort(keysList);

		// Récupérer la première clé triée
		BigInteger firstKey = keysList.get(0);
		return firstKey;
	}

}
