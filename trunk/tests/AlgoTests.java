package tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
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
	
	public static Flotte testCombat(PrintStream output) throws FileNotFoundException
	{
		if (output == null)
		{
			output = new PrintStream(".\\output.txt");
		}
		
		output.println("Composez la flotte A");
		Flotte flotteA = SaisirFlotte("A", output);
		
		output.println("Composez la flotte B");
		Flotte flotteB = SaisirFlotte("B", output);
		
		output.println("Lancement du combat..");
		Flotte vainqueur = Flotte.JouerCombat(flotteA, flotteB);
		
		output.println("Vainqueur: ");
		output.println(vainqueur);
		return vainqueur;
	}
	
	public static void testEquilibre(PrintStream output, double pas, double offset) throws FileNotFoundException
	{
		int facteur_taille = 1000000;
		PrintStream silent = new PrintStream(".\\output.txt");
		
		int i = 0;
		int survivant = 0;
		Flotte vainqueur = null;
		
		String sBN = "7\n";
		clavier = new BufferedReader(new StringReader(sBN));
		Vaisseau vaisseauBN= SaisirVaisseau(silent);
		
		String sTdT = "6\n";
		clavier = new BufferedReader(new StringReader(sTdT));
		Vaisseau vaisseauTdT = SaisirVaisseau(silent);
		double valeur_courante = 0;
		
		do
		{
			valeur_courante = (1+offset + (i*pas));
			++i;
			output.print("Test n°"+i+": 1/"+valeur_courante+"\t\t");
			
			int nb_tdt = new Double(Double.valueOf(facteur_taille) * Double.valueOf(valeur_courante)).intValue();
			String sEntree = sBN+(facteur_taille)+"\n0\n"+sTdT+nb_tdt+"\n0\n";
			clavier = new BufferedReader(new StringReader(sEntree));
			vainqueur = testCombat(silent);
			
			survivant = vainqueur.RecupererFlotteEquivalente(eClasse.DD).getNbVaisseau(vaisseauBN);
			if (survivant == 0) survivant = -1 * vainqueur.RecupererFlotteEquivalente(eClasse.DIST).getNbVaisseau(vaisseauTdT);
			
			output.println(survivant);
			
		}while(survivant > 0);
		
		output.println("L'équilibre est renversé à "+valeur_courante);
		output.println("Il faut");
		output.println(valeur_courante+" * "+vaisseauTdT);
		output.println("pour battre");
		output.println("1 * "+vaisseauBN);
	}
	
	public static void RemplirMagasin()
	{
		TreeMap<String, Integer[]> Gabarits = new TreeMap<String, Integer[]>();
		
		// Ajout d'un gabarit, nom gabarit, puis dans l'ordre "Def", "Att", "Arme", "Armure"
		Gabarits.put("Léger", new Integer[] {10,20,20,10});
		Gabarits.put("Moyen", new Integer[] {30,40,40,30});
		Gabarits.put("Lourd", new Integer[] {70,90,90,40});
		
		Iterator<Entry<String, Integer[]>> it = Gabarits.entrySet().iterator();
		while(it.hasNext())
		{
			Entry<String, Integer[]> e = it.next();
			String sGabarait = e.getKey();
			Integer[] caracs = e.getValue();
			
			for(int i=0; i < eClasse.nbClasses; ++i)
			{
				eClasse classe = eClasse.values()[i];
				magasin_vaisseaux.add(new Vaisseau(classe+" "+sGabarait, caracs[0], caracs[1], classe, caracs[2], caracs[3]));
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
			testEquilibre(System.out, .001, 0);
		}
		catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
