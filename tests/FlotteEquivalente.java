/**
 * 
 */
package tests;

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

	private int								m_Defense			= 0;

	private int								m_Attaque			= 0;

	private double							m_BonusArme			= 0;

	private double							m_BonusArmure		= 0;

	/** Attributs utiles lors d'un combat */
	private double							m_DegatsAEncaisser	= 0;

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
		
		if (it.hasNext())
		{
			sb.append("<Flotte Equivalente " + m_Classe.toString() + ">" + AlgoTests.LINE_SEPARATOR);
		}
		
		while (it.hasNext())
		{
			Entry<Vaisseau, Integer> e = it.next();
			sb.append("    " + e.getValue() + " * " + e.getKey().toString() + AlgoTests.LINE_SEPARATOR);
		}

		return sb.toString();
	}

	/**
	 * @param object
	 */
	public void setMeilleureCible(FlotteEquivalente i_cible)
	{
		m_MeilleureCible = i_cible;
	}

	/**
	 * @return
	 */
	public boolean estMorte()
	{
		return ((m_Defense <= m_DegatsAEncaisser) || (m_liste_vaisseaux.isEmpty()));
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
		m_Defense = 0;
		m_Attaque = 0;
		m_BonusArme = 0;
		m_BonusArmure = 0;

		Iterator<Entry<Vaisseau, Integer>> it = m_liste_vaisseaux.entrySet().iterator();
		double quantite_totale = 0;
		while (it.hasNext())
		{
			Entry<Vaisseau, Integer> e = it.next();
			Vaisseau v = e.getKey();
			double quantite = e.getValue();
			

			if (quantite <= 0)
			{
				it.remove();
				continue;
			}

			quantite_totale += quantite;
			m_Defense += quantite * v.Defense;
			m_Attaque += quantite * v.Attaque;
			m_BonusArme += quantite * v.BonusArme;
			m_BonusArmure += quantite * v.BonusArmure;
		}
		
		m_BonusArme /= quantite_totale;
		m_BonusArmure /= quantite_totale;
	}

	/**
	 * @return
	 */
	public int getDefense()
	{
		return m_Defense;
	}
	
	public double getDefenseCourante()
	{
		return Math.max(0, Double.valueOf(m_Defense) - m_DegatsAEncaisser);
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
		return Double.valueOf(m_Attaque) * (getDefenseCourante() / Double.valueOf(m_Defense));
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
				//modif = -1 * (defenseur.getArmureMoyenne() * attaquant.getDefenseCourante());
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
		m_DegatsAEncaisser += degats;
	}

	public double getDegatsAEncaisser()
	{
		return m_DegatsAEncaisser;
	}

	/**
	 * @param degatsAEncaisser
	 */
	public void EncaisserDegats(boolean bFinaliser)
	{
		if (m_DegatsAEncaisser <= 0)
			return;

		// On détermine les points de défense globaux restant, en arrondissant à l'entier inférieur.
		int DefRestante = new Double(Math.floor(getDefenseCourante())).intValue();

		if (DefRestante <= 0)
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
				m_DegatsAEncaisser = (le_moins_cher.Defense - reste);
				reste = 0;
			}
			else
			{
				m_DegatsAEncaisser = 0;
			}
		}
		else
		{
			// Si on doit finaliser (ne pas reporter le reste pour la prochaine
			// attaque), on regarde si celui-ci permet de rachetter un dernier
			// vaisseau

			// Si on a de quoi payer la moitié de son prix avec le reste, alors
			// on le compte bon.
			if ((le_moins_cher.Defense / 2) < reste)
			{
				Integer nouvelle_qte = nouvelle_liste.get(le_moins_cher);
				if (nouvelle_qte == null)
					nouvelle_qte = 0;
				nouvelle_liste.put(le_moins_cher, nouvelle_qte + 1);
				reste = 0;
			}

			m_DegatsAEncaisser = 0;
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
	 * @param iDefRestante
	 * @param iDernierElement
	 * @param listes_possibles
	 * @param iPlusPetitReste
	 * @return Plus petit reste
	 */
	private int GenererListesPossiblesSelect(int iDefRestante, Vector<TreeMap<Vaisseau, Integer>> listes_possibles)
	{
		return GenererListesPossiblesSelect(iDefRestante, m_liste_vaisseaux.firstKey(), listes_possibles, iDefRestante);
	}

	private int GenererListesPossiblesSelect(int iDefRestante, Vaisseau iDernierElement,
			Vector<TreeMap<Vaisseau, Integer>> listes_possibles, int PlusPetitReste)
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
			int max_qte_rachette = Math.min(quantite, iDefRestante / v.Defense);

			Vaisseau v_next = m_liste_vaisseaux.higherKey(v);
			
			// On dénombre toutes les combinaisons de rachat (allant du max, a
			// 1, sauf s'il n'y a pas de prochain vaisseau en liste)
			int qte_rachette = max_qte_rachette;
			do
			{
				// On calcule la Défense qu'il resterais à dépenser si l'on
				// sauvais ce vaisseau
				int NouvelleDefRestante = (iDefRestante - (v.Defense * qte_rachette));

				// Si la nouvelle Def est plus petite que le plus petit reste
				// observé, on l'ajoute
				if (NouvelleDefRestante <= PlusPetitReste)
				{
					TreeMap<Vaisseau, Integer> nouvelle_liste = new TreeMap<Vaisseau, Integer>();
					nouvelle_liste.put(v, qte_rachette);
				
					listes_possibles.add(nouvelle_liste);

					PlusPetitReste = NouvelleDefRestante;
				}

				// On prépare une nouvelle liste de listes_possibles, que l'on
				// rempli en apellant la méthode récursivement à partir de
				// l'élement actuel + 1
				Vector<TreeMap<Vaisseau, Integer>> sous_listes_possibles = new Vector<TreeMap<Vaisseau,Integer>>();
				// On note la PlusPetitePerte (reste) rencontré dans la
				// sous-liste.
				int ppp = PlusPetitReste;
				if (v_next != null)
				{
					ppp = GenererListesPossiblesSelect(NouvelleDefRestante, v_next, sous_listes_possibles, PlusPetitReste);
				}
				
				// Sinon, c'est qu'on a bien de nouvelles listes PLUS
				// intéressantes, on vire les anciennes et on notes les
				// nouvelles
				if (ppp < PlusPetitReste)
				{
					listes_possibles.removeAllElements();
					PlusPetitReste = ppp;
				}

				// On parcours la liste des sous_listes_possibles, que l'on
				// ajoute au listes_possibles, aprés l'élément courant
				TreeMap<Vaisseau, Integer> sous_liste_courante = null;
				for (int j = 0; j < sous_listes_possibles.size(); ++j)
				{
					TreeMap<Vaisseau, Integer> nouvelle_liste = new TreeMap<Vaisseau, Integer>();
					nouvelle_liste.put(v, qte_rachette);
					sous_liste_courante = sous_listes_possibles.get(j);
					nouvelle_liste.putAll(sous_liste_courante);
					
					listes_possibles.add(nouvelle_liste);
				}
				
				--qte_rachette;
			}while((qte_rachette > 0) && (v_next != null));
			// FIN POUR
		}

		return PlusPetitReste;
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
	 * @return
	 */
	public double getTotalAttaques()
	{
		return m_totalAttaques;
	}

	/**
	 * 
	 */
	public void razAttaques()
	{
		m_totalAttaques = 0;
	}

	/**
	 * M�thode r�cursive permettant de d�nombrer toutes les combinaisons
	 * possible de rachat d'unit�e parmis les �l�ments de la flotte, avec le
	 * "porte-monnaie" D�fense disponible.
	 * 
	 * @param iDefRestante :
	 *            D�f disponible pour le rachat des �lements "rescap�s".
	 * @param iDernierElement :
	 *            Dernier �l�ment visit� dans l'arbre des possibilit�s
	 * @param listes_possibles :
	 *            Listes en cours
	 */
	/*
	 * private void GenererListesPossibles(int iDefRestante, int
	 * iDernierElement, Vector<Vector<Integer>> listes_possibles) { for (int i =
	 * iDernierElement; i < liste_elements.size(); ++i) { Vaisseau e =
	 * liste_elements.get(i);
	 * 
	 * if (e.Defense <= iDefRestante) { int NouvelleDefRestante = (iDefRestante -
	 * e.Defense);
	 * 
	 * Vector<Vector<Integer>> sous_liste_possibles = new Vector<Vector<Integer>>();
	 * GenererListesPossibles(NouvelleDefRestante, (i + 1),
	 * sous_liste_possibles);
	 * 
	 * for (int j = 0; j < sous_liste_possibles.size(); ++j) { Vector<Integer>
	 * nouvelle_liste = new Vector<Integer>(); nouvelle_liste.add(i);
	 * nouvelle_liste.addAll(sous_liste_possibles.get(j));
	 * 
	 * listes_possibles.add(nouvelle_liste); }
	 * 
	 * Vector<Integer> nouvelle_liste = new Vector<Integer>();
	 * nouvelle_liste.add(i); nouvelle_liste.add(NouvelleDefRestante);
	 * 
	 * listes_possibles.add(nouvelle_liste); } } /* ALGORITHME
	 * FLOTTE.GenererListesPossibles(DefRestante, dernier_element,
	 * listes_possibles) // On parcours la liste des �l�ments � partir du
	 * dernier d�j� vu. POUR i ALLANT DE dernier_element A
	 * Flotte.liste_elements.taille() FAIRE // On note l'�l�ment courant Element
	 * e <- Flotte.liste_element[i] // Si sa d�fense peut �tre "rachett�e" SI
	 * (e.Def <= DefRestante) ALORS // On calcule combien il reste de Defense �
	 * rachetter NouvelleDefRestante <- (DefRestante - e.Def) // On ajoute � la
	 * liste le r�sultat de la "sous-liste" des possibles ListeElements[]
	 * sous_liste_possibles <- VIDE GenererListesPossibles(NouvelleDefRestante,
	 * (i+1), sous_liste_possibles)
	 * 
	 * POUR j ALLANT DE 0 A sous_liste_possibles.taille() FAIRE // On initialise
	 * une nouvelle liste d'�l�ments. ListeElement nouvelle_liste <- VIDE
	 * nouvelle_liste.Ajouter(e)
	 * nouvelle_liste.AjouterListe(sous_listes_possibles[j])
	 * 
	 * listes_possibles.Ajouter(nouvelle_liste)
	 * 
	 * FIN POUR
	 * 
	 * ListeElement nouvelle_liste <- VIDE nouvelle_liste.Ajouter(e) // En
	 * dernier �l�ment de la liste, on met le reste.
	 * nouvelle_liste.Ajouter(DefRestante)
	 * 
	 * listes_possibles.Ajouter(nouvelle_liste) FSI
	 * 
	 * FIN POUR
	 * 
	 * FIN ALGORITHME
	 */
	// }
}
