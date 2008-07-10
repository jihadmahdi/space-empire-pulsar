/**
 * @author Escallier Pierre
 * @file ConfigPartie.java
 * @date 27 févr. 08
 */
package common.metier;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Représente une config de partie, avec tout les paramètres nécéssaire à la création de la partie.
 */
public class ConfigPartie implements Serializable
{
	private static final long	serialVersionUID	= 1L;

	/** 
	 * Dimension de l'univers en X.
	 */
	private int	dimX	= 20;

	/** 
	 * Dimension de l'univers en Y.
	 */
	private int	dimY	= 20;

	/**
	 * Dimension de l'univers en Z.
	 */
	private int	dimZ	= 20;

	/**
	 * Nombre de corps célestes neutres à générer.
	 */
	private int	nbCorpsCelestesNeutres	= 0;

	/**
	 * Quantité de ressource carbone disponible sur les planètes de départ de chaque joueurs.
	 */
	private Hashtable<Class<? extends CorpsCeleste>, Integer[]>	qtCarboneDepartCorpsCelestes	= new Hashtable<Class<? extends CorpsCeleste>, Integer[]>();

	/**
	 * Quantité de slots disponibles sur les corps célestes.
	 */
	private Hashtable<Class<? extends CorpsCeleste>, Integer[]>	qtSlotsCorpsCelestes	= new Hashtable<Class<? extends CorpsCeleste>, Integer[]>();
	
	/**
	 * Condition de victoire : Victoire en alliance.
	 * Quelque soit la condition de victoire, tous les aliés du vainqueur sont vainqueurs.
	 */
	private boolean victoireEnAlliance = false;
	
	/**
	 * Condition de victoire : Régimicide.
	 * Rend disponible la les modules guvernementaux et les vaisseaux gouvernementaux qui représentent le pouvoir de chaque joueur sur son empire.
	 * Un joueur qui perd son gouvernement perds la partie, son peuple agit alors suivant la valeur de l'option "assimilerPeuplesNeutralises".
	 */
	private boolean regimicide = true;

	/**
	 * Option de la contion de victoire : Régimicide.
	 * Si l'option est activée, les peuples dont on a éliminé le gouvernement sont assimilés par le joueur responsable de cette élimination.
	 * Si l'option n'est pas activée, les peuples dont on a éliminé le gouvernement se retrouves dispersés, chaque planète est considéré comme sauvage.
	 */
	private boolean assimilerPeuplesNeutralises = false;
	
	/**
	 * Condition de victoire : Conquête Totale.
	 * Le premier joueur ayant conquis tous les corps célestes et détruit toute les flottes ennemies gagne la partie.
 	 * A noter qu'il est nécéssaire de décocher les autres conditions de victoire pour pouvoir profiter de celle-ci pleinement.
	 */
	private boolean conqueteTotale = true;
	
	/**
	 * Condition de victoire : Victoire Economique.
	 * Le premier joueur ayant atteind les seuils de ressource 資 et 人 fixées gagne la partie.
	 * [0] quantité de population à atteindre; [1] quantité de carbone à atteindre;
	 * Une valeur nulle signifie que la condition n'est pas activée.
	 */
	private Integer[] victoireEconomique = {null, null};
	
	/**
	 * Condition de victoire : Limite de temps.
	 * La partie se termine au bout d'un nombre de tours donné, le joueur ayant le meilleur score est considéré vainqueur.
	 * Une valeur nulle signifie que la condition n'est pas activée.
	 */
	private Integer victoireTempsLimite = null;
	
	/**
	 * Constructeur vide.
	 */
	public ConfigPartie()
	{
	}

	/**
	 * @return the dimX
	 */
	public int getDimX()
	{
		return dimX;
	}

	/**
	 * @param dimX the dimX to set
	 */
	public void setDimX(int dimX)
	{
		this.dimX = dimX;
	}

	/**
	 * @return the dimY
	 */
	public int getDimY()
	{
		return dimY;
	}

	/**
	 * @param dimY the dimY to set
	 */
	public void setDimY(int dimY)
	{
		this.dimY = dimY;
	}

	/**
	 * @return the dimZ
	 */
	public int getDimZ()
	{
		return dimZ;
	}

	/**
	 * @param dimZ the dimZ to set
	 */
	public void setDimZ(int dimZ)
	{
		this.dimZ = dimZ;
	}

	/**
	 * @return the nbCorpsCelestesNeutres
	 */
	public int getNbCorpsCelestesNeutres()
	{
		return nbCorpsCelestesNeutres;
	}

	/**
	 * @param nbCorpsCelestesNeutres the nbCorpsCelestesNeutres to set
	 */
	public void setNbCorpsCelestesNeutres(int nbCorpsCelestesNeutres)
	{
		this.nbCorpsCelestesNeutres = nbCorpsCelestesNeutres;
	}

	/**
	 * @return the qtCarboneDepartCorpsCelestes
	 */
	public Hashtable<Class<? extends CorpsCeleste>, Integer[]> getQtCarboneDepartCorpsCelestes()
	{
		return qtCarboneDepartCorpsCelestes;
	}

	/**
	 * @param qtCarboneDepartCorpsCelestes the qtCarboneDepartCorpsCelestes to set
	 */
	public void setQtCarboneDepartCorpsCelestes(Class<? extends CorpsCeleste> typeCorpsCeleste, int min, int max)
	{
		this.qtCarboneDepartCorpsCelestes.put(typeCorpsCeleste, new Integer[] {min, max});
	}

	/**
	 * @return the qtSlotsCorpsCelestes
	 */
	public Hashtable<Class<? extends CorpsCeleste>, Integer[]> getQtSlotsCorpsCelestes()
	{
		return qtSlotsCorpsCelestes;
	}

	/**
	 * @param qtSlotsCorpsCelestes the qtSlotsCorpsCelestes to set
	 */
	public void setQtSlotsCorpsCelestes(Class<? extends CorpsCeleste> typeCorpsCeleste, int min, int max)
	{
		this.qtSlotsCorpsCelestes.put(typeCorpsCeleste, new Integer[] {min, max});
	}

	/**
	 * @return the victoireEnAlliance
	 */
	public boolean isVictoireEnAlliance()
	{
		return victoireEnAlliance;
	}

	/**
	 * @param victoireEnAlliance the victoireEnAlliance to set
	 */
	public void setVictoireEnAlliance(boolean victoireEnAlliance)
	{
		this.victoireEnAlliance = victoireEnAlliance;
	}

	/**
	 * @return the regimicide
	 */
	public boolean isRegimicide()
	{
		return regimicide;
	}

	/**
	 * @param regimicide the regimicide to set
	 */
	public void setRegimicide(boolean regimicide)
	{
		this.regimicide = regimicide;
	}

	/**
	 * @return the assimilerPeuplesNeutralises
	 */
	public boolean isAssimilerPeuplesNeutralises()
	{
		return assimilerPeuplesNeutralises;
	}

	/**
	 * @param assimilerPeuplesNeutralises the assimilerPeuplesNeutralises to set
	 */
	public void setAssimilerPeuplesNeutralises(boolean assimilerPeuplesNeutralises)
	{
		this.assimilerPeuplesNeutralises = assimilerPeuplesNeutralises;
	}

	/**
	 * @return the conqueteTotale
	 */
	public boolean isConqueteTotale()
	{
		return conqueteTotale;
	}

	/**
	 * @param conqueteTotale the conqueteTotale to set
	 */
	public void setConqueteTotale(boolean conqueteTotale)
	{
		this.conqueteTotale = conqueteTotale;
	}

	/**
	 * @return the victoireEconomique
	 */
	public Integer[] getVictoireEconomique()
	{
		return victoireEconomique;
	}

	/**
	 * @param victoireEconomique the victoireEconomique to set
	 */
	public void setVictoireEconomique(int seuilPopulation, int seuilCarbone)
	{
		this.victoireEconomique = new Integer[] {seuilPopulation, seuilCarbone};
	}

	/**
	 * @return the victoireTempsLimite
	 */
	public Integer getVictoireTempsLimite()
	{
		return victoireTempsLimite;
	}

	/**
	 * @param victoireTempsLimite the victoireTempsLimite to set
	 */
	public void setVictoireTempsLimite(Integer victoireTempsLimite)
	{
		this.victoireTempsLimite = victoireTempsLimite;
	}
}
