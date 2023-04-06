package projet.modele;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Set;

public class Resultat implements Serializable {
  private Hashtable<BigInteger, Integer> lstPersistance;
  private int id;
  private String pseudo;
  private BigInteger nbCourant;
  private final int tailleMax = 100;

  /**
   * @param id
   */
  public Resultat(int id) {
    lstPersistance = new Hashtable<BigInteger, Integer>();
    this.id = id;

  }

  /**
   * @param persistance
   */
  public synchronized void ajoutResultat(Hashtable<BigInteger, Integer> persistance) {
    this.lstPersistance.putAll(persistance);
    // Set<BigInteger> setOfkeys = persistance.keySet();
    // for (BigInteger key : setOfkeys) {
    //   this.lstPersistance.put(key, persistance.get(key));
    // }
    System.out.println("je viens de rendre les resultats de ma tache");

  }

  public Hashtable<BigInteger, Integer> getListPersistance() {
    return this.lstPersistance;
  }

  public Boolean estPlein() {
    return (tailleMax - lstPersistance.size() <= 0);
  }

  public int taille() {
    return lstPersistance.size();
  }

}