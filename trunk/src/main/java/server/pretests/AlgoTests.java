package server.pretests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import server.pretests.Vaisseau.eClasse;



/**
 * Classe servant a des tests d'algos divers, rapides.
 * 
 * @author Pierre ESCALLIER
 * 
 */
public class AlgoTests
{
	public static final String LINE_SEPARATOR = "\r\n";
	private static TreeMap<Vaisseau, String> Gabarits = new TreeMap<Vaisseau, String>();
	private static TreeSet<Vaisseau> magasin_vaisseaux = new TreeSet<Vaisseau>();
	private static BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
	
	private static Vaisseau SaisirVaisseau(PrintStream output)
	{
		Iterator<Vaisseau> it = magasin_vaisseaux.iterator();
		Vaisseau[] vaisseaux = new Vaisseau[magasin_vaisseaux.size()+1]; 
		magasin_vaisseaux.toArray(vaisseaux);
		
		int i = 0;
		while(it.hasNext())
		{
			output.println((i+1)+") "+it.next());
			++i;
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
		}while((choix < 0) || (choix > i));
		
		if (choix == 0) return null;
		return vaisseaux[choix-1];
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
	
	public static Flotte testCombat(PrintStream output, Flotte flotteA, Flotte flotteB) throws FileNotFoundException
	{
		if (output == null)
		{
			output = new PrintStream("."+File.separatorChar+"output.txt");
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
	
	private static Flotte jouerCombat(String sNomFORT, String sNomFAIBLE, Vaisseau vaisseauFORT, Vaisseau vaisseauFAIBLE, int nb_FORT, int nb_FAIBLE, PrintStream output) throws FileNotFoundException
	{
		Flotte flotteForte = new Flotte(sNomFORT);
		flotteForte.ajouterVaisseau(vaisseauFORT, nb_FORT);
		
		Flotte flotteFaible = new Flotte(sNomFAIBLE);
		flotteFaible.ajouterVaisseau(vaisseauFAIBLE, nb_FAIBLE);
		
		return testCombat(output, flotteForte, flotteFaible);
	}
	
	public static Double[] testEquilibre(PrintStream output, Vaisseau vaisseauFORT, Vaisseau vaisseauFAIBLE, int facteur_taille, double pas, double offset) throws FileNotFoundException
	{
		Double[] resultats = new Double[4];
		
		pas = Math.max(1, facteur_taille*pas);
		PrintStream silent = new PrintStream("."+File.separatorChar+"output.txt");
		
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
		// On recherche deux valeurs bornes (Victoire, Défaite), et le premier nul rencontré s'il y'en a un.
		int borne_victoires = 0;
		int borne_defaites = Integer.MAX_VALUE;
		int borne_premier_nul = Integer.MAX_VALUE;
		int borne_dernier_nul = 0;
		
		int score_derniere_victoire = facteur_taille;
		int score_premiere_defaite = 0;
		
		do
		{
			++i;
			nb_FAIBLE = ((borne_defaites - borne_victoires) / 2) + borne_victoires;
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getM_sNom().compareTo("Champion") == 0)
			{
				survivant = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
			}
			
			// Si le chalenger gagne
			if (survivant < 0)
			{
				// On retente dans la moitié inférieure
				borne_defaites = nb_FAIBLE;
				score_premiere_defaite = survivant;
			}
			// Si le champion gagne
			else if (survivant > 0)
			{
				// On retente dans la moitié suppérieure
				borne_victoires = nb_FAIBLE;
				score_derniere_victoire = survivant;
			}
			// Match nul
			else
			{
				borne_premier_nul = Math.min(borne_premier_nul, nb_FAIBLE);
				borne_dernier_nul = Math.max(borne_dernier_nul, nb_FAIBLE);
			}
		}while((borne_dernier_nul < borne_premier_nul) && (borne_victoires < borne_defaites));
		
		// Recherche du premier nul
		while(borne_premier_nul > (borne_victoires +1))
		{
			++i;
			nb_FAIBLE = ((borne_premier_nul - borne_victoires) / 2) + borne_victoires;
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getM_sNom().compareTo("Champion") == 0)
			{
				survivant = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
			}
			
			// Si le champion gagne
			if (survivant > 0)
			{
				// On retente dans la moitié suppérieure
				borne_victoires = nb_FAIBLE;
				score_derniere_victoire = survivant;
			}
			// Match nul
			else if (survivant == 0)
			{
				// On retente dans la moitié inférieure
				borne_premier_nul = nb_FAIBLE;
			}
			else
			{
				throw new RuntimeException("Champion n'est pas sensé perdre..");
			}
		}
		
		// Recherche du dernier nul
		while(borne_dernier_nul < (borne_defaites-1))
		{
			++i;
			nb_FAIBLE = ((borne_defaites - borne_dernier_nul) / 2) + borne_dernier_nul;
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getM_sNom().compareTo("Champion") == 0)
			{
				survivant = vainqueur.RecupererFlotteEquivalente(vaisseauFORT.Classe).getNbVaisseau(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.RecupererFlotteEquivalente(vaisseauFAIBLE.Classe).getNbVaisseau(vaisseauFAIBLE);
			}
			
			// Si le chalanger gagne
			if (survivant < 0)
			{
				// On retente dans la moitié inférieure
				borne_defaites = nb_FAIBLE;
				score_premiere_defaite = survivant;
			}
			// Match nul
			else if (survivant == 0)
			{
				// On retente dans la moitié inférieure
				borne_dernier_nul = nb_FAIBLE;
			}
			else
			{
				throw new RuntimeException("Champion n'est pas sensé gagner..");
			}
		}
		
		int plage_nul = (borne_defaites - borne_premier_nul);
		
		double taille = Double.valueOf(facteur_taille);
		double coupure_premier = (Double.valueOf(borne_premier_nul) / taille);
		double coupure_avant_premier = (Double.valueOf(borne_victoires) / taille);
		double coupure_dernier = (Double.valueOf(borne_dernier_nul) / taille);
		double coupure_apres_dernier = (Double.valueOf(borne_defaites) / taille);
		if (plage_nul == 0)
		{	
			output.println("Victoire jusqu'à\"[1/"+coupure_avant_premier+"; "+facteur_taille+"/"+borne_victoires+"]: "+score_derniere_victoire);
			output.println("Défaite à partir de\t1/"+coupure_apres_dernier+"\t"+facteur_taille+"/"+borne_defaites+"]: "+score_premiere_defaite);
		}
		else
		{
			output.println("Victoire jusqu'à\t[1/"+coupure_avant_premier+"\t"+facteur_taille+"/"+borne_victoires+"]: "+score_derniere_victoire);
			output.println("Match nul depuis\t[1/"+coupure_premier+"\t"+facteur_taille+"/"+borne_premier_nul+"]: 0");
			output.println("Match nul jusqu'à\t[1/"+coupure_dernier+"\t"+facteur_taille+"/"+borne_dernier_nul+"]: 0");
			output.println("Défaite à partir de\t[1/"+coupure_apres_dernier+"\t"+facteur_taille+"/"+borne_defaites+"]: "+score_premiere_defaite);
		}
		
		// Victoire jusqu'a
		resultats[0] = Double.valueOf(borne_victoires);
		resultats[1] = coupure_avant_premier;
		// Défaite à partir de
		resultats[2] = Double.valueOf(borne_defaites);
		resultats[3] = coupure_apres_dernier;
		
		output.print("Il faut\t\t");
		output.println(coupure_apres_dernier+"\t*\t"+vaisseauFAIBLE);
		output.print("pour battre\t");
		output.println("1\t*\t"+vaisseauFORT);
		output.println("Etat du vainqueur: ");
		output.println(vainqueur);

		return resultats;
	}
	
	public static void EcrireSortie(PrintStream output, String sChampion, String sChalanger, double borneVictoire, double borneDefaite)
	{
		output.println("\""+sChampion+" v "+sChalanger+"\"\t\""+borneVictoire+"\"\t\""+borneDefaite+"\"");
	}
	
	public static void TesterRapports(PrintStream output, int facteur_taille, eClasse Ego) throws FileNotFoundException
	{
		PrintStream silent = new PrintStream("."+File.separatorChar+"output.txt");
		
		Double[] resultat = null;
		Vaisseau[] vaisseaux = new Vaisseau[magasin_vaisseaux.size()+1];
		String[] gabarits = new String[Gabarits.keySet().size()];
		Gabarits.values().toArray(gabarits);
		magasin_vaisseaux.toArray(vaisseaux);
		eClasse BN = eClasse.getBN(Ego);
		eClasse TdT = eClasse.getTdT(Ego);
		Vaisseau champion = null;
		Vaisseau chalenger = null;
		double pas = 1;
		
		String liste_titre_gabarits = "";
		String liste_gabarits = "";
		for(int gabarit = 0; gabarit < Gabarits.size(); ++gabarit)
		{
			if (!liste_titre_gabarits.isEmpty())
			{
				liste_titre_gabarits += "\t";
				liste_gabarits += "\t";
			}
			liste_titre_gabarits += gabarits[gabarit];
			champion = vaisseaux[Ego.ordinal()*Gabarits.size() + gabarit];
			liste_gabarits += "Def:"+champion.Defense+" / Att:"+champion.Attaque+" / Arm:"+champion.BonusArme+" / Bl:"+champion.BonusArmure;
		}
		output.println("Facteur taille\t"+liste_titre_gabarits);
		output.println(facteur_taille+"\t"+liste_gabarits);
		output.println("");
		output.println("Champion v Chalanger\tVictoire jusqu'à\tDéfaite à partir de");
		
		for(int gabarit_champion = 0; gabarit_champion < Gabarits.size(); ++gabarit_champion)
		{
			champion = vaisseaux[Ego.ordinal()*Gabarits.size() + gabarit_champion];
			
			for(int gabarit_chalenger = 0; gabarit_chalenger <= gabarit_champion; ++gabarit_chalenger)
			{
				chalenger = vaisseaux[TdT.ordinal()*Gabarits.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, facteur_taille, pas, 0);
				
				EcrireSortie(output, gabarits[gabarit_champion]+" BN", gabarits[gabarit_chalenger]+" TdT", resultat[0], resultat[2]);
			}
			for(int gabarit_chalenger = 0; gabarit_chalenger < gabarit_champion; ++gabarit_chalenger)
			{
				chalenger = vaisseaux[Ego.ordinal()*Gabarits.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, facteur_taille, pas, 0);
				
				EcrireSortie(output, gabarits[gabarit_champion]+" Ego", gabarits[gabarit_chalenger]+" Ego", resultat[0], resultat[2]);
			}
			for(int gabarit_chalenger = 0; gabarit_chalenger < gabarit_champion; ++gabarit_chalenger)
			{
				chalenger = vaisseaux[BN.ordinal()*Gabarits.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, facteur_taille, pas, 0);
				
				EcrireSortie(output, gabarits[gabarit_champion]+" TdT", gabarits[gabarit_chalenger]+" BN", resultat[0], resultat[2]);
			}
		}
		output.println("");
	}
	
	public static void RemplirMagasin()
	{
		Gabarits.clear();
		
		// Ajout d'un gabarit, nom gabarit, puis dans l'ordre "Def", "Att", "Arme", "Armure"
		
		// Les gabarits doivent forcément être ajoutés du plus petit au plus grand, pour garder compatibilité avec testerRapports()
		
		// LégerBN vs LégerTdT : 3/9
		Gabarits.put(new Vaisseau("Modèle_Léger", 100, 20, eClasse.DD, 3.0, 0.5), "Léger");
		// MoyenBN vs MoyenTdT : 5/12
		// MoyenBN vs LégerTdT : 140/282
		// MoyenEgo vs LégerEgo: 12/26
		Gabarits.put(new Vaisseau("Modèle_Moyen", 500, 20, eClasse.DD, 3.5, 0.6), "Moyen");
		// LourdBN vs LourdTdT : 8/18
		// LourdBN vs MoyenTdT : 208/418
		// LourdBN vs LégerTdT : 5204/10418
		// LourdEgo vs MoyenEgo: 12/26
		// LourdEgo vs LégerEgo: 312/626
		Gabarits.put(new Vaisseau("Modèle_Lourd", 2500, 500, eClasse.DD, 4.0, 0.7), "Lourd");
	
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
		
		Iterator<Entry<Vaisseau, String>> it = Gabarits.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<Vaisseau, String> e = it.next();
			String sGabarait = e.getValue();
			Vaisseau modele = e.getKey();
			
			for(int i=0; i < eClasse.nbClasses; ++i)
			{
				eClasse classe = eClasse.values()[i];
				Vaisseau nouveau = new Vaisseau(classe+" "+sGabarait, modele.Defense, modele.Attaque, classe, modele.BonusArme, modele.BonusArmure);
				magasin_vaisseaux.add(nouveau);
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		File dir = new File(".");
		
		if (args.length > 0)
		{
				File test = new File(args[0]);
				if (test.isDirectory())
				{
					if (!test.exists())
					{
						test.mkdirs();
					}
					dir = test;
				}
		}
		
		System.out.println("Test Algo de Combat");
		
		RemplirMagasin();

		try
		{
			//testEquilibre(System.out, null, null, 10, .1, 0);
			for(int i=0; i<=5; ++i)
			{
				int facteur_taille = new Double(Math.pow(10, i)).intValue();
				String file = dir.getAbsolutePath()+File.separatorChar+"rapport_combats_"+facteur_taille+".csv";
				System.out.println("creating file : "+file);
				PrintStream ps = new PrintStream(file);
				TesterRapports(ps, facteur_taille, eClasse.DD);
				
				if (!ps.equals(System.out))
				{
					ps.close();
				}
			}
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
	}

}
