package tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import tests.Vaisseau.eClasse;

/**
 * Classe servant a des tests d'algos divers, rapides.
 * 
 * @author Pierre ESCALLIER
 * 
 */
public class AlgoTests
{
	public static final String LINE_SEPARATOR = "\r\n";
	private static Vector<Vaisseau> magasin_vaisseaux = new Vector<Vaisseau>();
	private static BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
	
	private static SortedMap<String, Integer> map = new TreeMap<String, Integer>();

	private static Vaisseau SaisirVaisseau(PrintStream output)
	{
		for(int i=0; i<magasin_vaisseaux.size(); ++i)
		{
			output.println((i+1)+") "+magasin_vaisseaux.get(i));
		}
		output.println("0) Finir");
		
		int choix = 0;
		String ligne = null;
		do
		{
			try
			{
				ligne = clavier.readLine();
				choix = Integer.valueOf(ligne);
			}
			catch(Exception e)
			{
				output.println("Erreur de saisie");
				choix = -1;
			}
		}while(choix < 0);
		
		if (choix == 0) return null;
		return magasin_vaisseaux.get(choix-1);
	}
	
	private static Flotte SaisirFlotte(String sNomFlotte, PrintStream output)
	{
		Flotte flotte = new Flotte(sNomFlotte);
		Vaisseau vaisseau = null;
		int quantite = 0;
		do
		{
			output.println("Ajouter vaisseaux :");
			vaisseau = SaisirVaisseau(output);
			if (vaisseau == null) continue;
			
			do
			{
				output.print("Quantité (0 pour annuler): ");
				try
				{
					quantite = Integer.valueOf(clavier.readLine());
				}
				catch(Exception e)
				{
					quantite = -1;
				}
			}while(quantite < 0);
			
			flotte.ajouterVaisseau(vaisseau, quantite);
			output.println(flotte);
			
		}while((vaisseau != null) && (quantite >= 0));
		
		return flotte;
	}
	
	private static void TestIterateursBis(String dernier_element)
	{
		Iterator<Entry<String, Integer>> it = map.tailMap(dernier_element).entrySet().iterator();
		
		while(it.hasNext())
		{
			Entry<String, Integer> e = it.next();
			String k = e.getKey();
			Integer v = e.getValue();
			System.out.print("("+k+","+v+")");
			if (it.hasNext())
			{
				System.out.print(", ");
			}
		}
	}
	
	private static boolean TestIterateurs()
	{
		map.clear();
		map.put("A", 2);
		map.put("B", 1);
		map.put("C", 3);
		map.put("D", 3);
		
		Iterator<Entry<String, Integer>> it = map.entrySet().iterator();
		
		while(it.hasNext())
		{
			Entry<String, Integer> e = it.next();
			String k = e.getKey();
			System.out.print("1] "+k+" ");
			TestIterateursBis(k);
			System.out.print(AlgoTests.LINE_SEPARATOR);
		}
		
		return true;
	}
	
	public static Flotte testCombat(PrintStream output, Flotte flotteA, Flotte flotteB) throws FileNotFoundException
	{
		if (output == null)
		{
			output = new PrintStream(".\\output.txt");
		}
		
		if (flotteA == null)
		{
			output.println("Composez la flotte A");
			flotteA = SaisirFlotte("A", output);
		}
		
		if (flotteB == null)
		{
			output.println("Composez la flotte B");
			flotteB = SaisirFlotte("B", output);
		}
		
		output.println("Lancement du combat..");
		Flotte vainqueur = Flotte.JouerCombat(flotteA, flotteB);
		
		output.println("Vainqueur: ");
		output.println(vainqueur);
		return vainqueur;
	}
	
	private static Flotte jouerCombat(Vaisseau vaisseauFORT, Vaisseau vaisseauFAIBLE, int nb_FORT, int nb_FAIBLE, PrintStream output) throws FileNotFoundException
	{
		Flotte flotteForte = new Flotte("Champion");
		flotteForte.ajouterVaisseau(vaisseauFORT, nb_FORT);
		
		Flotte flotteFaible = new Flotte("Chalanger");
		flotteFaible.ajouterVaisseau(vaisseauFAIBLE, nb_FAIBLE);
		
		return testCombat(output, flotteForte, flotteFaible);
	}
	
	public static void testEquilibre(PrintStream output, Vaisseau vaisseauFORT, Vaisseau vaisseauFAIBLE, int facteur_taille, double pas, double offset) throws FileNotFoundException
	{
		pas = Math.max(1, facteur_taille*pas);
		PrintStream silent = new PrintStream(".\\output.txt");
		
		int i = 0;
		int survivant = 0;
		Flotte vainqueur = null;
		
		if (vaisseauFORT == null)
		{
			output.println("Choisissez le vaisseau Fort: ");
			vaisseauFORT = SaisirVaisseau(output);
		}
		
		if (vaisseauFAIBLE == null)
		{
			output.println("Choisissez le vaisseau Faible: ");
			vaisseauFAIBLE = SaisirVaisseau(output);
		}
		
		double valeur_courante = 0;
		int nb_FAIBLE = 0;
		int nb_FAIBLE_derniere_valeur = 0;
		int nb_FAIBLE_premier_nul = Integer.MAX_VALUE;
		int nb_FAIBLE_dernier_nul = Integer.MAX_VALUE;
		
		do
		{
			valeur_courante = (1+offset + (i*pas));
			++i;
			
			nb_FAIBLE_derniere_valeur = nb_FAIBLE;
			nb_FAIBLE = new Double(Double.valueOf(facteur_taille) * Double.valueOf(valeur_courante)).intValue();
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			
			vainqueur = jouerCombat(vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			// TODO: Trouver un moyen permettant de récupérer le vainqueur même si les deux flottes ont les même vaisseau (nom de la flotte ?)
			survivant = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			if (survivant == 0)
			{
				survivant = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
				if (survivant == 0)
				{
					nb_FAIBLE_premier_nul = Math.min(nb_FAIBLE_premier_nul, nb_FAIBLE);
					nb_FAIBLE_dernier_nul = Math.max(nb_FAIBLE_dernier_nul, nb_FAIBLE);
				}
			}
			
			output.println(survivant);
			
		}while(survivant >= 0);
		
		// Recherche du match nul
		int avant_premier_nul = Math.min(nb_FAIBLE_premier_nul, nb_FAIBLE_derniere_valeur);
		int apres_dernier_nul = Math.min(nb_FAIBLE_dernier_nul, nb_FAIBLE_derniere_valeur);
		
		// BN gagne ]0;avant_premier_nul]
		// MatchNUL/Coupure [premier_nul; dernier_nul]
		// TdT gagne [apres_dernier_nul; +inf[
		
		int score_avant_premier_nul = 0; int score_premier_nul = 0;
		int score_apres_dernier_nul = 0; int score_dernier_nul = 0;
		
		while((score_avant_premier_nul <= 0) && (avant_premier_nul > 0))
		{ 
			// Recherche arriere
			vainqueur = jouerCombat(vaisseauFORT, vaisseauFAIBLE, facteur_taille, avant_premier_nul, silent);
		
			score_avant_premier_nul = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			if (score_avant_premier_nul == 0)
			{
				score_avant_premier_nul = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
			}
			if (score_avant_premier_nul <= 0)
			{
				--avant_premier_nul;
				score_premier_nul = score_avant_premier_nul;
			}
		}

		while((score_apres_dernier_nul >= 0) && (apres_dernier_nul > 0))
		{ 
			// Recherche avant
			vainqueur = jouerCombat(vaisseauFORT, vaisseauFAIBLE, facteur_taille, apres_dernier_nul, silent);
		
			score_apres_dernier_nul = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			if (score_apres_dernier_nul == 0)
			{
				score_apres_dernier_nul = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
			}
			if (score_apres_dernier_nul >= 0)
			{
				++apres_dernier_nul;
				score_dernier_nul = score_apres_dernier_nul;
			}
		}
		
		int plage_nul = (apres_dernier_nul - avant_premier_nul);
		
		double taille = Double.valueOf(facteur_taille);
		double coupure_premier = (Double.valueOf(avant_premier_nul + 1) / taille);
		double coupure_avant_premier = (Double.valueOf(avant_premier_nul) / taille);
		double coupure_dernier = (Double.valueOf(apres_dernier_nul - 1) / taille);
		double coupure_apres_dernier = (Double.valueOf(apres_dernier_nul) / taille);
		if (plage_nul == 0)
		{	
			output.println("Victoire jusqu'à\"[1/"+coupure_avant_premier+"; "+facteur_taille+"/"+avant_premier_nul+"]: "+score_avant_premier_nul);
			output.println("Coupure en\t1/"+coupure_premier+"\t"+facteur_taille+"/"+(avant_premier_nul+1)+"]: "+score_premier_nul);
			output.println("Défaite à partir de\t1/"+coupure_apres_dernier+"\t"+facteur_taille+"/"+apres_dernier_nul+"]: "+score_apres_dernier_nul);
		}
		else
		{
			output.println("Victoire jusqu'à\t[1/"+coupure_avant_premier+"\t"+facteur_taille+"/"+avant_premier_nul+"]: "+score_avant_premier_nul);
			output.println("Match nul depuis\t[1/"+coupure_premier+"\t"+facteur_taille+"/"+(avant_premier_nul+1)+"]: "+score_premier_nul);
			output.println("Match nul jusqu'à\t[1/"+coupure_dernier+"\t"+facteur_taille+"/"+(apres_dernier_nul-1)+"]: "+score_dernier_nul);
			output.println("Défaite à partir de\t[1/"+coupure_apres_dernier+"\t"+facteur_taille+"/"+apres_dernier_nul+"]: "+score_apres_dernier_nul);
		}
				
		output.print("Il faut\t\t");
		output.println(coupure_apres_dernier+"\t*\t"+vaisseauFAIBLE);
		output.print("pour battre\t");
		output.println("1\t*\t"+vaisseauFORT);
		output.println("Etat du vainqueur: ");
		output.println(vainqueur);
	}
	
	public static void RemplirMagasin()
	{
		TreeMap<String, Integer[]> Gabarits = new TreeMap<String, Integer[]>();
		
		// Ajout d'un gabarit, nom gabarit, puis dans l'ordre "Def", "Att", "Arme", "Armure"
		Gabarits.put("Léger", new Integer[] {100,20,500,70});
		Gabarits.put("Moyen", new Integer[] {1500,200,500,80});
		Gabarits.put("Lourd", new Integer[] {70,90,90,40});
		
		/* Notes sur l'équilibre
		 * En faisant les tests avec un facteur_taille à 1 on a le nombre exact de Faible qu'il faut pour tuer un Fort.
		 * En faisant les tests avec un facteur_taille à 100, on a en plus une précision sur les dégats encaissé par le vainqueur (dégats qui sont ignoré/rachetté avec un facteur_taille 1).
		 * 
		 * FACTEUR TAILLE 1:
		 * On observe qu'a gabarit égal, le nombre de TdT qu'il faut pour tuer une BN semble obéir à la formule suivante :
		 * nb_TdT = (Armure / Attaque) + 1
		 * avec k ~= (20*10E-3) * Arme
		 * k négligeale
		 * 
		 * 
		 * Conclusion: Plus on augmente l'armure, plus on spécialise le modèle de vaisseau.
		 */
		
		Iterator<Entry<String, Integer[]>> it = Gabarits.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, Integer[]> e = it.next();
			String sGabarait = e.getKey();
			Integer[] caracs = e.getValue();
			
			for(int i=0; i < eClasse.nbClasses; ++i)
			{
				eClasse classe = eClasse.values()[i];
				magasin_vaisseaux.add(new Vaisseau(classe+" "+sGabarait, caracs[0], caracs[1], classe, Double.valueOf(caracs[2]) / 100.0, Double.valueOf(caracs[3]) / 100.0));
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Test Algo de Combat");
		
		RemplirMagasin();
		//SaisirVaisseau(System.out);

		try
		{
			testEquilibre(System.out, null, null, 1, .1, 0);
		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
