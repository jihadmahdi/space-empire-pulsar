package tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.Vector;

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

	private static Vaisseau SaisirVaisseau()
	{
		for(int i=0; i<magasin_vaisseaux.size(); ++i)
		{
			System.out.println((i+1)+") "+magasin_vaisseaux.get(i));
		}
		System.out.println("0) Finir");
		
		int choix = 0;
		do
		{
			try
			{
				choix = Integer.valueOf(clavier.readLine());
			}
			catch(Exception e)
			{
				System.out.println("Erreur de saisie");
				choix = -1;
			}
		}while(choix < 0);
		
		if (choix == 0) return null;
		return magasin_vaisseaux.get(choix-1);
	}
	
	private static Flotte SaisirFlotte(String sNomFlotte)
	{
		Flotte flotte = new Flotte(sNomFlotte);
		Vaisseau vaisseau = null;
		int quantite = 0;
		do
		{
			System.out.println("Ajouter vaisseaux :");
			vaisseau = SaisirVaisseau();
			if (vaisseau == null) continue;
			
			do
			{
				System.out.print("Quantité (0 pour annuler): ");
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
			System.out.println(flotte);
			
		}while((vaisseau != null) && (quantite >= 0));
		
		return flotte;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("Test Algo de Combat");
		
		int Def = 100, Att = 20, Wp = 40, Ar = 20;
		
		magasin_vaisseaux.add(new Vaisseau("DD Leger", Def, Att, eClasse.DD, Wp, Ar));
		magasin_vaisseaux.add(new Vaisseau("TANK Leger", Def, Att, eClasse.TANK, Wp, Ar));
		magasin_vaisseaux.add(new Vaisseau("DIST Leger", Def, Att, eClasse.DIST, Wp, Ar));
		magasin_vaisseaux.add(new Vaisseau("TANK Leger Double", 40, 40, eClasse.TANK, 20, 20));
		
		// A(2DD, 1T) B(1DD, 2T)
		//clavier = new BufferedReader(new StringReader("1\n2\n2\n1\n0\n1\n1\n2\n2\n0\n"));
		
		/*
		String ligne=null;
		do
		{
			try
			{
				ligne = clavier.readLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			System.out.println("nouvelle ligne: \""+ligne+"\"");
		}while(ligne != null);
		
		if (ligne == null) return;
		*/
		
		System.out.println("Composez la flotte A");
		Flotte flotteA = SaisirFlotte("A");
		
		System.out.println("Composez la flotte B");
		Flotte flotteB = SaisirFlotte("B");
		
		System.out.println("Lancement du combat..");
		Flotte vainqueur = Flotte.JouerCombat(flotteA, flotteB);
		
		System.out.println("Vainqueur: ");
		System.out.println(vainqueur);
	}

}
