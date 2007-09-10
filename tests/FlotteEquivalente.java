/**
 * 
 */
package tests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import tests.Vaisseau.eClasse;

/**
 * @author Axan
 * 
 */
public class FlotteEquivalente
{
	private TreeMap<Vaisseau, Integer>	m_liste_vaisseaux	= new TreeMap<Vaisseau, Integer>();

	private eClasse							m_Classe;

	private BigInteger						m_Defense			= BigInteger.ZERO;

	private int								m_Attaque			= 0;

	private double							m_BonusArme			= 0;

	private double							m_BonusArmure		= 0;

	/** Attributs utiles lors d'un combat */
	private BigDecimal						m_DegatsAEncaisser	= BigDecimal.ZERO;

	private int								m_totalAttaques		= 0;

	private FlotteEquivalente				m_MeilleureCible	= null;

	public int getNbVaisseau(Vaisseau v)
	{
		if (!m_liste_vaisseaux.containsKey(v)) return 0;
		return m_liste_vaisseaux.get(v);
	}
	
	public FlotteEquivalente(eClasse classe)
	{
		m_Classe = classe;
		m_liste_vaisseaux.clear();
		refreshCaracs();
	}

	public void ajouterVaisseaux(Vaisseau i_ModeleVaisseau, int i_Quantite)
	{
		int deja_present = 0;
		if (m_liste_vaisseaux.containsKey(i_ModeleVaisseau))
		{
			deja_present = m_liste_vaisseaux.get(i_ModeleVaisseau);
		}

		m_liste_vaisseaux.put(i_ModeleVaisseau, deja_present + i_Quantite);

		refreshCaracs();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<Vaisseau, Integer>> it = m_liste_vaisseaux.entrySet().iterator();
		String total = "";
		
		if (it.hasNext())
		{
			sb.append("<Flotte Equivalente " + m_Classe.toString() + ">" + AlgoTests.LINE_SEPARATOR);
			total = "    TOTAL Def:"+m_Defense+" / Att:"+m_Attaque+" / Arm:"+m_BonusArme+" / Bl:"+m_BonusArmure + AlgoTests.LINE_SEPARATOR;
		}
		
		while (it.hasNext())
		{
			Entry<Vaisseau, Integer> e = it.next();
			sb.append("    " + e.getValue() + " * " + e.getKey().toString() + AlgoTests.LINE_SEPARATOR);
		}
		
		sb.append(total);

		return sb.toString();
	}

	/**
	 * @param object
	 */
	public void setMeilleureCible(FlotteEquivalente i_cible)
	{
		m_MeilleureCible = i_cible;
	}

	private BigDecimal getDefenseCourane()
	{
		return new BigDecimal(m_Defense).subtract(m_DegatsAEncaisser, MathContext.DECIMAL128);
	}
	
	/**
	 * @return
	 */
	public boolean estMorte()
	{
		return ((getDefenseCourane().compareTo(BigDecimal.ZERO) <= 0) || (m_liste_vaisseaux.isEmpty()));
	}

	/**
	 * @return
	 */
	public eClasse getClasse()
	{
		return m_Classe;
	}

	private void refreshCaracs()
	{
		m_Defense = BigInteger.ZERO;
		m_Attaque = 0;
		m_BonusArme = 0;
		m_BonusArmure = 0;
		
		BigInteger somme_attaque = BigInteger.ZERO;
		BigDecimal somme_armure = BigDecimal.ZERO;
		BigDecimal somme_arme = BigDecimal.ZERO;

		Iterator<Entry<Vaisseau, Integer>> it = m_liste_vaisseaux.entrySet().iterator();
		double quantite_totale = 0;
		while (it.hasNext())
		{
			Entry<Vaisseau, Integer> e = it.next();
			Vaisseau v = e.getKey();
			long quantite = e.getValue();
			

			if (quantite <= 0)
			{
				it.remove();
				continue;
			}

			quantite_totale += quantite;
			
			m_Defense = m_Defense.add(BigInteger.valueOf(quantite * v.Defense));
			
			// On calcule la moyenne des Attaques, pondérées par la participation du vaisseau à la Def totale.
			// Ce faisant, on évite d'avoir a actualiser les caracs Vaisseau/Vaisseau lorsque ceux-ci meurent, en obtenant strictement le même résultat.
			somme_attaque = somme_attaque.add(BigInteger.valueOf((quantite * v.Defense) * v.Attaque));
			
			// On calcule la moyenne des bonus Arme et Armure, pondèrés par la participation du vaisseau à la Def totale.
			somme_arme = somme_arme.add(BigDecimal.valueOf(quantite * v.Defense * v.BonusArme));
			somme_armure = somme_armure.add(BigDecimal.valueOf(quantite * v.Defense * v.BonusArmure));
		}
		
		BigDecimal Defense = new BigDecimal(m_Defense);
		
		if (m_Defense.compareTo(BigInteger.ZERO) > 0)
		{
			m_Attaque = somme_attaque.divide(m_Defense).intValue();
			m_BonusArme = somme_arme.divide(Defense, MathContext.DECIMAL128).doubleValue();
			m_BonusArmure = somme_armure.divide(Defense, MathContext.DECIMAL128).doubleValue();
		}
		
		if ((m_Attaque >= Integer.MAX_VALUE) || (m_BonusArme >= Integer.MAX_VALUE) || (m_BonusArmure >= Integer.MAX_VALUE))
		{
			throw new RuntimeException("Les caracs sont hors-limites (overflow)");
		}
	}
		
	/**
	 * @return
	 */
	public int getAttaque()
	{
		return m_Attaque;
	}
	
	public double getAttaqueCourante()
	{
		return m_Attaque;
	}

	/**
	 * @return
	 */
	public double getBonusArme()
	{
		return m_BonusArme;
	}

	/**
	 * @return
	 */
	public double getBonusArmure()
	{
		return m_BonusArmure;
	}

	public static double getTotalAttaqueCourante(FlotteEquivalente attaquant, FlotteEquivalente defenseur)
	{
		// On calcule le temps pour tuer la cible considérée, suivant son statut
		int statut = attaquant.getClasse().comparer(defenseur.getClasse());
		double modif = 0;

		switch (statut)
		{
			case 1: // BN
			{
				modif = attaquant.getAttaqueCourante() * (1 + attaquant.getBonusArme());
				break;
			}
			case 0: // EGO
			{
				modif = attaquant.getAttaqueCourante();
				break;
			}
			case -1: // TdT
			{
				modif = attaquant.getAttaqueCourante() * (1 - defenseur.getBonusArmure());
				break;
			}
		}

		return modif;
	}

	/**
	 * @return
	 */
	public void ajouterDegats(double degats)
	{
		if (degats < 0) throw new RuntimeException("Erreur: Dégats négatifs");
		m_DegatsAEncaisser = m_DegatsAEncaisser.add(BigDecimal.valueOf(degats));
	}

	/**
	 * @param degatsAEncaisser
	 */
	public void EncaisserDegats(boolean bFinaliser)
	{
		if (m_DegatsAEncaisser.compareTo(BigDecimal.ZERO) <= 0)
			return;

		// On détermine les points de défense globaux restant, en arrondissant à l'entier inférieur.
		BigInteger DefRestante = getDefenseCourane().toBigInteger();

		if (DefRestante.compareTo(BigInteger.ZERO) <= 0)
		{
			m_liste_vaisseaux.clear();
			refreshCaracs();
			return;
		}

		Vector<TreeMap<Vaisseau, Integer>> listes_possibles = new Vector<TreeMap<Vaisseau, Integer>>();
		int reste = GenererListesPossiblesSelect(DefRestante, listes_possibles);
		Random dice = new Random();
		TreeMap<Vaisseau, Integer> nouvelle_liste = null;
		if (listes_possibles.size() >= 1)
		{
			nouvelle_liste = listes_possibles.get(dice.nextInt(listes_possibles.size()));
		}
		else
		{
			// Aucun rachat content possible, on prend une liste vide.
			nouvelle_liste = new TreeMap<Vaisseau, Integer>();
		}

		// On commencer par chercher le vaisseau le moins cher que l'on ai pas
		// déjà racheté.
		Vaisseau le_moins_cher = null;
		Iterator<Entry<Vaisseau, Integer>> it = m_liste_vaisseaux.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Vaisseau, Integer> e = it.next();
			Vaisseau v = e.getKey();
			int quantite = e.getValue();
			if (quantite <= 0)
				continue;

			if ((nouvelle_liste.get(v) == null) || (nouvelle_liste.get(v) < quantite))
			{
				if (le_moins_cher == null)
				{
					le_moins_cher = v;
					continue;
				}
				if (v.Defense < le_moins_cher.Defense)
				{
					le_moins_cher = v;
				}
			}
		}

		if ( !bFinaliser)
		{
			if (reste > 0)
			{
				// Si on ne finalise pas, on sauve forcément un dernier vaisseau
				// avec les restes, mais on reporte les dégats qu'il a subit sur
				// les dégats de la prochaine attaque
				Integer nouvelle_qte = nouvelle_liste.get(le_moins_cher);
				if (nouvelle_qte == null)
					nouvelle_qte = 0;
				nouvelle_liste.put(le_moins_cher, nouvelle_qte + 1);
				m_DegatsAEncaisser = BigDecimal.valueOf(le_moins_cher.Defense - reste);
				reste = 0;
			}
			else
			{
				m_DegatsAEncaisser = BigDecimal.ZERO;
			}
		}
		else
		{
			// Si on doit finaliser (ne pas reporter le reste pour la prochaine
			// attaque), on regarde si celui-ci permet de rachetter un dernier
			// vaisseau

			// Si on a de quoi payer la moitié de son prix avec le reste, alors
			// on le compte bon.
			if ((le_moins_cher != null) && ((le_moins_cher.Defense / 2) < reste))
			{
				Integer nouvelle_qte = nouvelle_liste.get(le_moins_cher);
				if (nouvelle_qte == null)
					nouvelle_qte = 0;
				nouvelle_liste.put(le_moins_cher, nouvelle_qte + 1);
				reste = 0;
			}

			m_DegatsAEncaisser = BigDecimal.ZERO;
		}

		m_liste_vaisseaux = nouvelle_liste;

		refreshCaracs();
	}

	/**
	 * @return
	 */
	public FlotteEquivalente getMeilleureCible()
	{
		return m_MeilleureCible;
	}

	/**
	 * Comme GenererListesPossibilites, mais filtre en ne gardant que les
	 * possibilit�es ayant le moins de "perte" (le reste le plus petit).
	 * 
	 * @param defRestante
	 * @param iDernierElement
	 * @param listes_possibles
	 * @param iPlusPetitReste
	 * @return Plus petit reste
	 */
	private int GenererListesPossiblesSelect(BigInteger defRestante, Vector<TreeMap<Vaisseau, Integer>> listes_possibles)
	{
		return GenererListesPossiblesSelect(defRestante, m_liste_vaisseaux.firstKey(), listes_possibles, defRestante).intValue();
	}

	private BigInteger GenererListesPossiblesSelect(BigInteger defRestante, Vaisseau iDernierElement,
			Vector<TreeMap<Vaisseau, Integer>> listes_possibles, BigInteger defRestante2)
	{
		// POUR CHAQUE vaisseau DE LA m_liste_vaisseaux A PARTIR DE
		// iDernierElement
		// FAIRE
		Iterator<Entry<Vaisseau, Integer>> it = m_liste_vaisseaux.tailMap(iDernierElement).entrySet().iterator();
		while (it.hasNext())
		{
			// On récupère le vaisseau à partir de son index
			Entry<Vaisseau, Integer> e = it.next();
			Vaisseau v = e.getKey();
			Integer quantite = e.getValue();
			if ((quantite == null) || (quantite <= 0))
			{
				continue;
			}

			// On calcule combien au maximum on peut "rachetter" de ce modèle de
			// vaisseau (dans la limite de la quantité d'origine)
			BigInteger max_qte_rachette = defRestante.divide(BigInteger.valueOf(v.Defense)).min(BigInteger.valueOf(quantite)); 

			Vaisseau v_next = m_liste_vaisseaux.higherKey(v);
			
			// On dénombre toutes les combinaisons de rachat (allant du max, a
			// 1, sauf s'il n'y a pas de prochain vaisseau en liste)
			BigInteger qte_rachette = max_qte_rachette;
			do
			{
				// On calcule la Défense qu'il resterais à dépenser si l'on
				// sauvais ce vaisseau
				BigInteger cout = qte_rachette.multiply(BigInteger.valueOf(v.Defense));
				BigInteger NouvelleDefRestante = defRestante.subtract(cout);

				// Si la nouvelle Def est plus petite que le plus petit reste
				// observé, on l'ajoute
				if (NouvelleDefRestante.compareTo(defRestante2) <= 0)
				{
					TreeMap<Vaisseau, Integer> nouvelle_liste = new TreeMap<Vaisseau, Integer>();
					nouvelle_liste.put(v, qte_rachette.intValue());
				
					listes_possibles.add(nouvelle_liste);

					defRestante2 = NouvelleDefRestante;
				}

				// On prépare une nouvelle liste de listes_possibles, que l'on
				// rempli en apellant la méthode récursivement à partir de
				// l'élement actuel + 1
				Vector<TreeMap<Vaisseau, Integer>> sous_listes_possibles = new Vector<TreeMap<Vaisseau,Integer>>();
				// On note la PlusPetitePerte (reste) rencontré dans la
				// sous-liste.
				BigInteger ppp = defRestante2;
				if (v_next != null)
				{
					ppp = GenererListesPossiblesSelect(NouvelleDefRestante, v_next, sous_listes_possibles, defRestante2);
				}
				
				// Sinon, c'est qu'on a bien de nouvelles listes PLUS
				// intéressantes, on vire les anciennes et on notes les
				// nouvelles
				if (ppp.compareTo(defRestante2) < 0)
				{
					listes_possibles.removeAllElements();
					defRestante2 = ppp;
				}

				// On parcours la liste des sous_listes_possibles, que l'on
				// ajoute au listes_possibles, aprés l'élément courant
				TreeMap<Vaisseau, Integer> sous_liste_courante = null;
				for (int j = 0; j < sous_listes_possibles.size(); ++j)
				{
					TreeMap<Vaisseau, Integer> nouvelle_liste = new TreeMap<Vaisseau, Integer>();
					nouvelle_liste.put(v, qte_rachette.intValue());
					sous_liste_courante = sous_listes_possibles.get(j);
					nouvelle_liste.putAll(sous_liste_courante);
					
					listes_possibles.add(nouvelle_liste);
				}
				
				qte_rachette = qte_rachette.subtract(BigInteger.ONE);
			}while((qte_rachette.compareTo(BigInteger.ZERO) > 0) && (v_next != null));
			// FIN POUR
		}

		return defRestante2;
	}

	/**
	 * @param attaqueMeilleureCible
	 */
	public void ajouterAttaque(double nouvelle_attaque)
	{
		if (nouvelle_attaque < 0) throw new RuntimeException("Erreur: Attaque négative.");
		m_totalAttaques += nouvelle_attaque;
	}

	/**
	 * 
	 */
	public void razAttaques()
	{
		m_totalAttaques = 0;
	}

	/**
	 * @return
	 */
	public double getTempsSurvieAttaque()
	{
		return getDefenseCourane().divide(BigDecimal.valueOf(m_totalAttaques), MathContext.DECIMAL128).doubleValue();
	}

	/**
	 * @return
	 */
	public double getTotalAttaques()
	{
		return m_totalAttaques;
	}

}
