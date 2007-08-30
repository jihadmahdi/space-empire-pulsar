package tests;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Classe servant a des tests d'algos divers, rapides.
 * @author Pierre ESCALLIER
 *
 */
public class AlgoTests {

	/**
	 * Classe représentant un élément d'une flotte (juste le nom, et le score de def), utilisé pour les tests GenererListesPossibles.
	 * @author Axan
	 *
	 */
	static public class Element
	{
		public int Def=0;
		public String Nom=null;
		
		public Element() {}
		
		public Element(String sNom, int iDef)
		{
			Nom=sNom;
			Def = iDef;
		}
	};
	
	/** Liste des éléments de la flotte (test) */
	private Vector<Element> liste_elements = new Vector<Element>();
	
	/**
	 * Comme GenererListesPossibilites, mais filtre en ne gardant que les possibilitées ayant le moins de "perte" (le reste le plus petit).
	 * @param iDefRestante
	 * @param iDernierElement
	 * @param listes_possibles
	 * @param iPlusPetitReste
	 * @return Plus petit reste
	 */
	private int GenererListesPossiblesSelect(int iDefRestante, int iDernierElement, Vector<Vector<Integer>> listes_possibles, int PlusPetitReste)
	{
		// POUR CHAQUE element DE LA liste_elements A PARTIR DE iDernierElement FAIRE
		for(int i=iDernierElement; i < liste_elements.size(); ++i)
		{
			// On récupère l'élément à partir de son index
			Element e = liste_elements.get(i);
			
			// SI l'élément peut être "rachetté", c'est à dire qu'il reste suffisament de Défense à la flotte pour qu'elle considère que l'élément a été sauvé. ALORS
			if (e.Def <= iDefRestante)
			{
				// On calcule la Défense qu'il resterais à dépenser si l'on "sauve" cet élément
				int NouvelleDefRestante = (iDefRestante - e.Def);
				
				// Si la nouvelle Def est plus petite que le plus petit reste observé, on ajoute l'élément courant et on note.
				if (NouvelleDefRestante <= PlusPetitReste)
				{
					Vector<Integer> nouvelle_liste = new Vector<Integer>();
					nouvelle_liste.add(i);
					nouvelle_liste.add(NouvelleDefRestante);
					
					listes_possibles.add(nouvelle_liste);
					
					PlusPetitReste = NouvelleDefRestante;
				}
				
				// On prépare une nouvelle liste de listes_possibles, que l'on rempli en apellant la méthode récursivement à partir de l'élément actuel+1
				Vector<Vector<Integer>> sous_listes_possibles = new Vector<Vector<Integer>>();
				// On note la PlusPetitePerte (reste) rencontré dans la sous-liste.
				int ppp = GenererListesPossiblesSelect(NouvelleDefRestante, (i+1), sous_listes_possibles, PlusPetitReste);

				// Sinon, c'est qu'on a bien de nouvelles listes PLUS intéressantes, on vire les anciennes et on notes les nouvelles
				if (ppp < PlusPetitReste)
				{
					listes_possibles.removeAllElements();
					PlusPetitReste = ppp;
				}
				
				// On parcours la liste des sous_listes_possibles, que l'on ajoute au listes_possibles, après l'élément courant
				for(int j=0; j < sous_listes_possibles.size(); ++j)
				{
					Vector<Integer> nouvelle_liste = new Vector<Integer>();
					nouvelle_liste.add(i);
					nouvelle_liste.addAll(sous_listes_possibles.get(j));
					
					listes_possibles.add(nouvelle_liste);
				}
			}
		// FIN POUR
		}
		
		return PlusPetitReste;
	}
	
	/**
	 * Méthode récursive permettant de dénombrer toutes les combinaisons possible de rachat d'unitée parmis les éléments de la flotte, avec le "porte-monnaie" Défense disponible.
	 * @param iDefRestante : Déf disponible pour le rachat des élements "rescapés".
	 * @param iDernierElement : Dernier élément visité dans l'arbre des possibilités
	 * @param listes_possibles : Listes en cours
	 */
	private void GenererListesPossibles(int iDefRestante, int iDernierElement, Vector<Vector<Integer>> listes_possibles)
	{
		for(int i=iDernierElement; i < liste_elements.size(); ++i)
		{
			Element e = liste_elements.get(i);
			
			if (e.Def <= iDefRestante)
			{
				int NouvelleDefRestante = (iDefRestante - e.Def);
				
				Vector<Vector<Integer>> sous_liste_possibles = new Vector<Vector<Integer>>();
				GenererListesPossibles(NouvelleDefRestante, (i+1), sous_liste_possibles);
				
				for(int j=0; j < sous_liste_possibles.size(); ++j)
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
	ALGORITHME FLOTTE.GenererListesPossibles(DefRestante, dernier_element, listes_possibles)
	
		// On parcours la liste des éléments à partir du dernier déjà vu.
		POUR i ALLANT DE dernier_element A Flotte.liste_elements.taille() FAIRE
				
			// On note l'élément courant
			Element e <- Flotte.liste_element[i]
			
			// Si sa défense peut être "rachettée"
			SI (e.Def <= DefRestante) ALORS
				
				// On calcule combien il reste de Defense à rachetter
				NouvelleDefRestante <- (DefRestante - e.Def)
				
				// On ajoute à la liste le résultat de la "sous-liste" des possibles
				ListeElements[] sous_liste_possibles <- VIDE
				GenererListesPossibles(NouvelleDefRestante, (i+1), sous_liste_possibles)
				
				POUR j ALLANT DE 0 A sous_liste_possibles.taille() FAIRE
				
					// On initialise une nouvelle liste d'éléments.
					ListeElement nouvelle_liste <- VIDE
					nouvelle_liste.Ajouter(e)
					nouvelle_liste.AjouterListe(sous_listes_possibles[j])
					
					listes_possibles.Ajouter(nouvelle_liste)
				
				FIN POUR
				
				ListeElement nouvelle_liste <- VIDE
				nouvelle_liste.Ajouter(e)
				
				// En dernier élément de la liste, on met le reste.
				nouvelle_liste.Ajouter(DefRestante)
			
				listes_possibles.Ajouter(nouvelle_liste)
			FSI
		
		FIN POUR

	FIN ALGORITHME
*/
	}
	
	private static void AffListesPossible(Vector<Vector<Integer>> listes_possibles, AlgoTests flotte)
	{
		for(int i=0; i<listes_possibles.size(); ++i)
		{
			System.out.print("[");
			for(int j=0; j<(listes_possibles.get(i).size()-1); ++j)
			{
				if (j>0) System.out.print(", ");
				System.out.print(flotte.liste_elements.get(listes_possibles.get(i).get(j)).Nom);
			}
			System.out.print("]");
			System.out.println(" "+listes_possibles.get(i).get(listes_possibles.get(i).size()-1));
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AlgoTests flotte = new AlgoTests();
		flotte.liste_elements.add(new Element("A",4));
		flotte.liste_elements.add(new Element("B",3));
		flotte.liste_elements.add(new Element("C",9));
		flotte.liste_elements.add(new Element("D",5));
		flotte.liste_elements.add(new Element("E",7));
		flotte.liste_elements.add(new Element("F",4));
		flotte.liste_elements.add(new Element("G",2));
		
		Vector<Vector<Integer>> listes_possibles = new Vector<Vector<Integer>>();
		flotte.GenererListesPossibles(10, 0, listes_possibles);
		AffListesPossible(listes_possibles, flotte);
		
		System.out.println("SELECTION");
		listes_possibles.removeAllElements();
		flotte.GenererListesPossiblesSelect(10, 0, listes_possibles, 10);
		AffListesPossible(listes_possibles, flotte);
	}

}
