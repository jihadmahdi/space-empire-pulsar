package org.axan.sep.server.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.db.orm.Fleet;
import org.axan.sep.common.db.orm.StarshipTemplate;

public class BattleReporter
{
	private static Set<StarshipTemplate> Templates = new TreeSet<StarshipTemplate>();
	private static Set<StarshipTemplate> starshipFactory = new TreeSet<StarshipTemplate>();
	
	public static void FullfillStarshipFactory()
	{
		Templates.clear();
		
		// Ajout d'un gabarit, nom gabarit, puis dans l'ordre "Def", "Att", "Arme", "Armure"
		
		// Les gabarits doivent forcément être ajoutés du plus petit au plus grand, pour garder compatibilité avec testerRapports()
		
		// LégerBN vs LégerTdT : 3/9
		Templates.add(new StarshipTemplate("Léger", 100, 20, null, 3.0, 0.5, 3, 0, 0));
		// MoyenBN vs MoyenTdT : 5/12
		// MoyenBN vs LégerTdT : 140/282
		// MoyenEgo vs LégerEgo: 12/26
		Templates.add(new StarshipTemplate("Moyen", 500, 20, null, 3.5, 0.6, 3, 0, 0));
		// LourdBN vs LourdTdT : 8/18
		// LourdBN vs MoyenTdT : 208/418
		// LourdBN vs LégerTdT : 5204/10418
		// LourdEgo vs MoyenEgo: 12/26
		// LourdEgo vs LégerEgo: 312/626
		Templates.add(new StarshipTemplate("Lourd", 2500, 500, null, 4.0, 0.7, 3, 0, 0));
		
		Templates = SEPUtils.starshipSizeTemplates;
	
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
		
		for(StarshipTemplate starshipTemplate : Templates)
		{			
			for(eStarshipSpecializationClass specialization : eStarshipSpecializationClass.values())
			{
				StarshipTemplate realTemplate = new StarshipTemplate(specialization+" "+starshipTemplate.getName(), starshipTemplate.getDefense(), starshipTemplate.getAttack(), specialization, starshipTemplate.getAttackSpecializationBonus(), starshipTemplate.getDefenseSpecializationBonus(), starshipTemplate.getSpeed(), starshipTemplate.getCarbonPrice(), starshipTemplate.getPopulationPrice());
				starshipFactory.add(realTemplate);				
			}
		}
	}
	
	public static Fleet testCombat(PrintStream output, Fleet flotteA, Fleet flotteB) throws FileNotFoundException
	{
		if (output == null)
		{
			output = new PrintStream("."+File.separatorChar+"output.txt");
		}
		
		/*
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
		*/
		
		Map<String, Map<String, Boolean>> conflictDiplomacy = new HashMap<String, Map<String,Boolean>>();
		conflictDiplomacy.put(flotteA.getOwnerName(), new HashMap<String, Boolean>());
		conflictDiplomacy.get(flotteA.getOwnerName()).put(flotteB.getOwnerName(), true);
		conflictDiplomacy.put(flotteB.getOwnerName(), new HashMap<String, Boolean>());
		conflictDiplomacy.get(flotteB.getOwnerName()).put(flotteA.getOwnerName(), true);
		
		Map<String, Fleet> forces = new HashMap<String, Fleet>();
		forces.put(flotteA.getOwnerName(), flotteA);
		forces.put(flotteB.getOwnerName(), flotteB);		
		
		output.println("Lancement du combat..");
		Map<String, Fleet> survivors = GameBoard.resolveBattle(conflictDiplomacy, forces);
		
		if (survivors.size() > 1) throw new Error("Only one (or no) survivors expected.");
		
		Fleet vainqueur = survivors.get(survivors.keySet().iterator().next());
		
		output.println("Vainqueur: ");
		output.println(vainqueur);
		return vainqueur;
	}
	
	private static Fleet jouerCombat(String sNomFORT, String sNomFAIBLE, StarshipTemplate vaisseauFORT, StarshipTemplate vaisseauFAIBLE, int nb_FORT, int nb_FAIBLE, PrintStream output) throws FileNotFoundException
	{
		Map<StarshipTemplate, Integer> starships = new HashMap<StarshipTemplate, Integer>();
		starships.put(vaisseauFORT, nb_FORT);
		Fleet flotteForte = new Fleet(null, sNomFORT, "fort", null, starships, null, false, null, null);
				
		starships.clear();
		starships.put(vaisseauFAIBLE, nb_FAIBLE);
		Fleet flotteFaible = new Fleet(null, sNomFAIBLE, "faible", null, starships, null, false, null, null);
		
		return testCombat(output, flotteForte, flotteFaible);
	}
	
	public static Double[] testEquilibre(PrintStream output, StarshipTemplate vaisseauFORT, StarshipTemplate vaisseauFAIBLE, int facteur_taille, double pas, double offset) throws FileNotFoundException
	{
		Double[] resultats = new Double[4];
		
		pas = Math.max(1, facteur_taille*pas);
		PrintStream silent = new PrintStream("."+File.separatorChar+"output.txt");
		
		int i = 0;
		int survivant = 0;
		Fleet vainqueur = null;
		
		/*
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
		*/
		
		double valeur_courante = 0;
		int nb_FAIBLE = 0;
		// On recherche deux valeurs bornes (Victoire, Défaite), et le premier nul rencontré s'il y'en a un.
		int borne_victoires = 0;
		int borne_defaites = Integer.MAX_VALUE;
		int borne_premier_nul = Integer.MAX_VALUE;
		int borne_dernier_nul = 0;
		
		int max_nb = 100000;
		
		int score_derniere_victoire = facteur_taille;
		int score_premiere_defaite = 0;
		
		boolean nb_FAIBLE_cut = false;
		
		do
		{
			++i;
			nb_FAIBLE = ((borne_defaites - borne_victoires) / 2) + borne_victoires;
			
			if (nb_FAIBLE > max_nb)
			{
				nb_FAIBLE_cut = true;
				break;
			}
			
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getName().compareTo("Champion") == 0)
			{
				survivant = vainqueur.getSpecializedFleet(vaisseauFORT.getSpecializationClass()).getStarshipCount(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.getSpecializedFleet(vaisseauFAIBLE.getSpecializationClass()).getStarshipCount(vaisseauFAIBLE);
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
		while(!nb_FAIBLE_cut && borne_premier_nul > (borne_victoires +1))
		{
			++i;
			nb_FAIBLE = ((borne_premier_nul - borne_victoires) / 2) + borne_victoires;
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getName().compareTo("Champion") == 0)
			{
				survivant = vainqueur.getSpecializedFleet(vaisseauFORT.getSpecializationClass()).getStarshipCount(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.getSpecializedFleet(vaisseauFAIBLE.getSpecializationClass()).getStarshipCount(vaisseauFAIBLE);
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
		while(!nb_FAIBLE_cut && borne_dernier_nul < (borne_defaites-1))
		{
			++i;
			nb_FAIBLE = ((borne_defaites - borne_dernier_nul) / 2) + borne_dernier_nul;
			valeur_courante = Double.valueOf(nb_FAIBLE) / Double.valueOf(facteur_taille);
			
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t"+facteur_taille+"/"+nb_FAIBLE+"\t");
			vainqueur = jouerCombat("Champion", "Chalanger", vaisseauFORT, vaisseauFAIBLE, facteur_taille, nb_FAIBLE, silent);
			
			if (vainqueur.getName().compareTo("Champion") == 0)
			{
				survivant = vainqueur.getSpecializedFleet(vaisseauFORT.getSpecializationClass()).getStarshipCount(vaisseauFORT);
			}
			else
			{
				survivant = -1 * vainqueur.getSpecializedFleet(vaisseauFAIBLE.getSpecializationClass()).getStarshipCount(vaisseauFAIBLE);
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
		if (nb_FAIBLE_cut)
		{
			output.println("Victoire jusqu'à\"[+INF]\"");
		}
		else if (plage_nul == 0)
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
	
	public static void FactorsTest(PrintStream output, int sizeFactor, eStarshipSpecializationClass ego) throws FileNotFoundException
	{
		PrintStream silent = new PrintStream("."+File.separatorChar+"output.txt");
		
		Double[] resultat = null;	
		StarshipTemplate[] vaisseaux = new StarshipTemplate[starshipFactory.size()+1];
		starshipFactory.toArray(vaisseaux);
		eStarshipSpecializationClass BN = ego.getBN();
		eStarshipSpecializationClass TdT = ego.getTdT();
		StarshipTemplate champion = null;
		StarshipTemplate chalenger = null;
		double pas = 1;
		
		String liste_titre_gabarits = "";
		String liste_gabarits = "";
		int i = 0;
		for(StarshipTemplate sizeTemplate : Templates)
		{
			if (!liste_titre_gabarits.isEmpty())
			{
				liste_titre_gabarits += "\t";
				liste_gabarits += "\t";
			}
			liste_titre_gabarits += sizeTemplate.getName();
			champion = vaisseaux[ego.ordinal()*Templates.size() + i];			
			liste_gabarits += "Def:"+champion.getDefense()+" / Att:"+champion.getAttack()+" / Arm:"+champion.getAttackSpecializationBonus()+" / Bl:"+champion.getDefenseSpecializationBonus();
			++i;
		}
		output.println("Facteur taille\t"+liste_titre_gabarits);
		output.println(sizeFactor+"\t"+liste_gabarits);
		output.println("");
		output.println("Champion v Chalanger\tVictoire jusqu'à\tDéfaite à partir de");
		
		int gabarit_champion = 0;
		for(StarshipTemplate championSizeTemplate : Templates)
		{
			champion = vaisseaux[ego.ordinal()*Templates.size() + gabarit_champion];
			
			int gabarit_chalenger = 0;			
			for(StarshipTemplate chalengerSizeTemplate : Templates)			
			{
				if (gabarit_chalenger > gabarit_champion) break;
				
				chalenger = vaisseaux[TdT.ordinal()*Templates.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, sizeFactor, pas, 0);
				
				EcrireSortie(output,championSizeTemplate.getName()+" BN", chalengerSizeTemplate.getName()+" TdT", resultat[0], resultat[2]);
				++gabarit_chalenger;				
			}
			
			gabarit_chalenger = 0;
			for(StarshipTemplate chalengerSizeTemplate : Templates)
			{
				if (gabarit_chalenger >= gabarit_champion) break;
				
				chalenger = vaisseaux[ego.ordinal()*Templates.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, sizeFactor, pas, 0);
				
				EcrireSortie(output, championSizeTemplate.getName()+" Ego", chalengerSizeTemplate.getName()+" Ego", resultat[0], resultat[2]);
				++gabarit_chalenger;
			}
			
			gabarit_chalenger = 0;
			for(StarshipTemplate chalengerSizeTemplate : Templates)
			{
				if (gabarit_chalenger >= gabarit_champion) break;
				
				chalenger = vaisseaux[BN.ordinal()*Templates.size() + gabarit_chalenger];
				resultat = testEquilibre(silent, champion, chalenger, sizeFactor, pas, 0);
				
				EcrireSortie(output, championSizeTemplate.getName()+" TdT", chalengerSizeTemplate.getName()+" BN", resultat[0], resultat[2]);
				++gabarit_chalenger;
			}
			
			++gabarit_champion;
		}
		output.println("");
	}
	
	public static void EcrireSortie(PrintStream output, String sChampion, String sChalanger, double borneVictoire, double borneDefaite)
	{
		output.println("\""+sChampion+" v "+sChalanger+"\"\t\""+borneVictoire+"\"\t\""+borneDefaite+"\"");
	}
	
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
		
		FullfillStarshipFactory();

		try
		{
			//testEquilibre(System.out, null, null, 10, .1, 0);
			for(int i=0; i<=5; ++i)
			{
				int facteur_taille = new Double(Math.pow(10, i)).intValue();
				String file = dir.getAbsolutePath()+File.separatorChar+"rapport_combats_"+facteur_taille+".csv";
				System.out.println("creating file : "+file);
				PrintStream ps = new PrintStream(file);
				FactorsTest(ps, facteur_taille, eStarshipSpecializationClass.FIGHTER);
				
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
