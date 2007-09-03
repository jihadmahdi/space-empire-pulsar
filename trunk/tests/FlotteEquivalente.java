/**
 * 
 */
package tests;

import java.util.Random;
import java.util.Vector;

import tests.Vaisseau.eClasse;

/**
 * @author Axan
 *
 */
public class FlotteEquivalente
{
	private Vector<Vaisseau> m_liste_vaisseaux = new Vector<Vaisseau>();
	private eClasse m_Classe;
	private int m_Defense = 0;
	private int m_Attaque = 0;
	private int m_Arme = 0;
	private int m_Armure = 0;
	
	/** Attributs utiles lors d'un combat */
	private int m_DegatsAEncaisser = 0;
	private int m_totalAttaques = 0;
	private FlotteEquivalente m_MeilleureCible = null;

	public FlotteEquivalente(eClasse classe)
	{
		m_Classe = classe;
		m_liste_vaisseaux.removeAllElements();
		refreshCaracs();
	}
	
	public void ajouterVaisseaux(Vaisseau i_ModeleVaisseau, int i_Quantite)
	{
		for(int i=0; i<i_Quantite; ++i)
		{
			m_liste_vaisseaux.add(i_ModeleVaisseau.clone());
		}
		refreshCaracs();
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<Flotte Equivalente "+m_Classe.toString()+">"+AlgoTests.LINE_SEPARATOR);
		for(int i=0; i<m_liste_vaisseaux.size(); ++i)
		{
			sb.append("    "+m_liste_vaisseaux.get(i).toString()+AlgoTests.LINE_SEPARATOR);
		}
		sb.append(AlgoTests.LINE_SEPARATOR);
		
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
		return (m_liste_vaisseaux.size() <= 0);
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
		m_Arme = 0;
		m_Armure = 0;
		
		for(int i=0; i < m_liste_vaisseaux.size(); ++i)
		{
			Vaisseau vaisseau = m_liste_vaisseaux.get(i);
			m_Defense += vaisseau.Defense;
			m_Attaque += vaisseau.Attaque;
			m_Arme += vaisseau.Arme;
			m_Armure += vaisseau.Armure;
		}
	}
	
	/**
	 * @return
	 */
	public int getDefense()
	{
		return m_Defense;
	}

	/**
	 * @return
	 */
	public int getAttaque()
	{
		return m_Attaque;
	}

	/**
	 * @return
	 */
	public int getArme()
	{
		return m_Arme;
	}

	/**
	 * @return
	 */
	public int getArmure()
	{
		return m_Armure;
	}

	public static int getScoreAttaque(FlotteEquivalente attaquant, FlotteEquivalente defenseur)
	{
//		 On calcule le temps pour tuer la cible considérée, suivant son statut
		int statut = attaquant.getClasse().comparer(defenseur.getClasse());
		int attaque = 0;
		
		switch(statut)
		{
			case 1: // BN
			{
				attaque = attaquant.getAttaque() + attaquant.getArme();
				break;
			}
			case 0: // EGO
			{
				attaque = attaquant.getAttaque();
				break;
			}
			case -1: // TdT
			{
				attaque = Math.max(0, attaquant.getAttaque() - defenseur.getArmure());
				break;
			}
		}
		
		return attaque;
	}

	/**
	 * @return
	 */
	public void ajouterDegats(int iDegats)
	{
		m_DegatsAEncaisser += iDegats;
	}
	
	public int getDegatsAEncaisser()
	{
		return m_DegatsAEncaisser;
	}

	/**
	 * @param degatsAEncaisser
	 */
	public void EncaisserDegats(boolean bFinaliser)
	{
		if (estMorte()) return;
		if (m_DegatsAEncaisser <= 0) return;
		
		// On détermine les points de défense globaux restant
		int DefRestante = m_Defense - m_DegatsAEncaisser;
		
		if (DefRestante <= 0)
		{
			m_liste_vaisseaux.removeAllElements();
			refreshCaracs();
			return;
		}
		
		Vector<Vector<Vaisseau>> listes_possibles = new Vector<Vector<Vaisseau>>();
		int reste = GenererListesPossiblesSelect(DefRestante, 0, listes_possibles, DefRestante);
		Random dice = new Random();
		Vector<Vaisseau> nouvelle_liste = null;
		if (listes_possibles.size() >= 1)
		{
			nouvelle_liste = listes_possibles.get(dice.nextInt(listes_possibles.size()));
		}
		else
		{
			// Aucun rachat content possible, on prend une liste vide.
			nouvelle_liste = new Vector<Vaisseau>();
		}
		
		// On commencer par chercher le vaisseau le moins cher que l'on ai pas déjà racheté.
		Vaisseau le_moins_cher = null;
		for(int i=0; i < m_liste_vaisseaux.size(); ++i)
		{
			Vaisseau vaisseau_courant = m_liste_vaisseaux.get(i);
			if (!nouvelle_liste.contains(vaisseau_courant))
			{
				if (le_moins_cher == null)
				{
					le_moins_cher = vaisseau_courant;
					continue;
				}
				if (vaisseau_courant.Defense < le_moins_cher.Defense)
				{
					le_moins_cher = vaisseau_courant;
				}
			}
		}
		
		if (!bFinaliser)
		{
			if (reste > 0)
			{
				// Si on ne finalise pas, on sauve forcément un dernier vaisseau avec les restes, mais on reporte les dégats qu'il a subit sur les dégats de la prochaine attaque 
				nouvelle_liste.add(le_moins_cher);
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
			// Si on doit finaliser (ne pas reporter le reste pour la prochaine attaque), on regarde si celui-ci permet de rachetter un dernier vaisseau
			
			// Si on a de quoi payer la moitié de son prix avec le reste, alors on le compte bon.
			if ((le_moins_cher.Defense / 2) < reste)
			{
				nouvelle_liste.add(le_moins_cher);
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
	private int GenererListesPossiblesSelect(int iDefRestante, int iDernierElement,
			Vector<Vector<Vaisseau>> listes_possibles, int PlusPetitReste)
	{
		// POUR CHAQUE vaisseau DE LA m_liste_vaisseaux A PARTIR DE iDernierElement
		// FAIRE
		for (int i = iDernierElement; i < m_liste_vaisseaux.size(); ++i)
		{
			// On récupère le vaisseau à partir de son index
			Vaisseau v = m_liste_vaisseaux.get(i);

			// SI le vaisseau peut être "racheté", c'est à dire qu'il reste suffisament de Défense pour que la flotte considère que le vaisseau a été sauvé. ALORS
			if (v.Defense <= iDefRestante)
			{
				// On calcule la Défense qu'il resterais à dépenser si l'on sauvais ce vaisseau
				int NouvelleDefRestante = (iDefRestante - v.Defense);

				// Si la nouvelle Def est plus petite que le plus petit reste observé, on l'ajoute 
				if (NouvelleDefRestante <= PlusPetitReste)
				{
					Vector<Vaisseau> nouvelle_liste = new Vector<Vaisseau>();
					nouvelle_liste.add(v);

					listes_possibles.add(nouvelle_liste);

					PlusPetitReste = NouvelleDefRestante;
				}

				// On prépare une nouvelle liste de listes_possibles, que l'on
				// rempli en apellant la méthode récursivement à partir de
				// l'élement actuel + 1
				Vector<Vector<Vaisseau>> sous_listes_possibles = new Vector<Vector<Vaisseau>>();
				// On note la PlusPetitePerte (reste) rencontré dans la
				// sous-liste.
				int ppp = GenererListesPossiblesSelect(NouvelleDefRestante, (i + 1), sous_listes_possibles,
						PlusPetitReste);

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
				for (int j = 0; j < sous_listes_possibles.size(); ++j)
				{
					Vector<Vaisseau> nouvelle_liste = new Vector<Vaisseau>();
					nouvelle_liste.add(v);
					nouvelle_liste.addAll(sous_listes_possibles.get(j));

					listes_possibles.add(nouvelle_liste);
				}
			}
			// FIN POUR
		}

		return PlusPetitReste;
	}

	/**
	 * @param attaqueMeilleureCible
	 */
	public void ajouterAttaque(int nouvelle_attaque)
	{
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
	private void GenererListesPossibles(int iDefRestante, int iDernierElement, Vector<Vector<Integer>> listes_possibles)
	{
		for (int i = iDernierElement; i < liste_elements.size(); ++i)
		{
			Vaisseau e = liste_elements.get(i);

			if (e.Defense <= iDefRestante)
			{
				int NouvelleDefRestante = (iDefRestante - e.Defense);

				Vector<Vector<Integer>> sous_liste_possibles = new Vector<Vector<Integer>>();
				GenererListesPossibles(NouvelleDefRestante, (i + 1), sous_liste_possibles);

				for (int j = 0; j < sous_liste_possibles.size(); ++j)
				{
					Vector<Integer> nouvelle_liste = new Vector<Integer>();
					nouvelle_liste.add(i);
					nouvelle_liste.addAll(sous_liste_possibles.get(j));

					listes_possibles.add(nouvelle_liste);
				}

				Vector<Integer> nouvelle_liste = new Vector<Integer>();
				nouvelle_liste.add(i);
				nouvelle_liste.add(NouvelleDefRestante);

				listes_possibles.add(nouvelle_liste);
			}
		}
		/*
		 * ALGORITHME FLOTTE.GenererListesPossibles(DefRestante,
		 * dernier_element, listes_possibles) // On parcours la liste des
		 * �l�ments � partir du dernier d�j� vu. POUR i ALLANT DE
		 * dernier_element A Flotte.liste_elements.taille() FAIRE // On note
		 * l'�l�ment courant Element e <- Flotte.liste_element[i] // Si sa
		 * d�fense peut �tre "rachett�e" SI (e.Def <= DefRestante) ALORS // On
		 * calcule combien il reste de Defense � rachetter NouvelleDefRestante <-
		 * (DefRestante - e.Def) // On ajoute � la liste le r�sultat de la
		 * "sous-liste" des possibles ListeElements[] sous_liste_possibles <-
		 * VIDE GenererListesPossibles(NouvelleDefRestante, (i+1),
		 * sous_liste_possibles)
		 * 
		 * POUR j ALLANT DE 0 A sous_liste_possibles.taille() FAIRE // On
		 * initialise une nouvelle liste d'�l�ments. ListeElement nouvelle_liste <-
		 * VIDE nouvelle_liste.Ajouter(e)
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
	//}
}
