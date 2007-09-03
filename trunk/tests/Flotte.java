/**
 * 
 */
package tests;

import java.util.Vector;

import tests.Vaisseau.eClasse;

/**
 * @author Axan Classe représentant une flotte de plusieurs vaisseaux.
 */
public class Flotte
{	
	private Vector<FlotteEquivalente> m_FlottesEquivalentes = new Vector<FlotteEquivalente>();
	private String m_sNom = null;
	
	public Flotte(String sNom)
	{
		m_sNom = sNom;
		m_FlottesEquivalentes.removeAllElements();
		for(int classe = 0; classe < eClasse.nbClasses; ++classe) m_FlottesEquivalentes.add(new FlotteEquivalente(eClasse.values()[classe]));
	}
	
	static public Flotte JouerCombat(Flotte flotteA, Flotte flotteB)
	{
		Vector<Vector<FlotteEquivalente>> FlottesEquivalentes = new Vector<Vector<FlotteEquivalente>>();
		Flotte vainqueur = null;
		
		// On récupère les flottes équivalentes de chaque classe.
		for(int flotte=0; flotte < 2; ++flotte)
		{
			FlottesEquivalentes.add(new Vector<FlotteEquivalente>());
			
			for(int classe=0; classe < eClasse.nbClasses; ++classe)
			{
				FlotteEquivalente flotteEquCourante = ((flotte==0)?flotteA:flotteB).RecupererFlotteEquivalente(eClasse.getFromInt(classe));
				FlottesEquivalentes.get(flotte).add(flotteEquCourante);
			}
		}
		
		do
		{
			// On remet à zéro les données du tour courant
			for(int flotte=0; flotte < 2; ++flotte)
			{
				for(int classe=0; classe < eClasse.nbClasses; ++classe)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte).get(classe);
					flotteEquCourante.razAttaques();
				}
			}
			
			// On détermine qui meurs en premier et en combien de temps
			for(int flotte_att=0; flotte_att < 2; ++flotte_att)
			{
				for(int classe_att=0; classe_att < eClasse.nbClasses; ++classe_att)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_att).get(classe_att);
					
					// Si la flotte est détruite, on saute.
					if (flotteEquCourante.estMorte()) continue;
					
					double TempsMeilleureCible = Double.POSITIVE_INFINITY;
					FlotteEquivalente MeilleureCible = null;
					int AttaqueMeilleureCible = 0;
					
					// On détermine la meilleure cible, c'est celle la plus rapide à tuer (et suivant les rapports de forces, pas forcément la TdT)
					for(int classe_def=0; classe_def < eClasse.nbClasses; ++classe_def)
					{
						double TempsTuerCible = 0.0;
						FlotteEquivalente ciblePotentielle = FlottesEquivalentes.get((flotte_att+1) % 2).get(classe_def);
						// Si la cible est déjà détruite, on saute.
						if (ciblePotentielle.estMorte()) continue;
						
						int attaque = FlotteEquivalente.getScoreAttaque(flotteEquCourante, ciblePotentielle);
						
						if (attaque == 0)
						{
							TempsTuerCible = Double.POSITIVE_INFINITY;
						}
						else
						{
							TempsTuerCible = Double.valueOf(ciblePotentielle.getDefense() - ciblePotentielle.getDegatsAEncaisser()) / Double.valueOf(attaque);
						}
						
						
						if (TempsTuerCible < TempsMeilleureCible)
						{
							MeilleureCible = ciblePotentielle;
							TempsMeilleureCible = TempsTuerCible;
							AttaqueMeilleureCible = attaque;
						}
					}
						
					flotteEquCourante.setMeilleureCible(MeilleureCible);
					MeilleureCible.ajouterAttaque(AttaqueMeilleureCible);
				}
			}
			
			double TempsPremiereVictime = Double.POSITIVE_INFINITY;
			double TempsFlotteCourante = 0;
			for(int flotte_cib=0; flotte_cib < 2; ++flotte_cib)
			{
				for(int classe_cib=0; classe_cib < eClasse.nbClasses; ++classe_cib)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_cib).get(classe_cib);
					if (flotteEquCourante.estMorte()) continue;
					TempsFlotteCourante = Double.valueOf(flotteEquCourante.getDefense() - flotteEquCourante.getDegatsAEncaisser()) / Double.valueOf(flotteEquCourante.getTotalAttaques());
					if (TempsFlotteCourante < TempsPremiereVictime)
					{
						TempsPremiereVictime = TempsFlotteCourante;
					}
				}
			}
			
			// On joue les dégats reçu par chacun durant ce laps de temps (y compris ceux donné par la victime), c'est à dire qu'on note pour chacun la def restante
			for(int flotte_cpt=0; flotte_cpt < 2; ++flotte_cpt)
			{
				for(int classe_cpt=0; classe_cpt < eClasse.nbClasses; ++classe_cpt)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_cpt).get(classe_cpt);
					if (flotteEquCourante.estMorte()) continue;
					
					int degats = new Double(Math.ceil(TempsPremiereVictime * Double.valueOf(flotteEquCourante.getTotalAttaques()))).intValue();
					flotteEquCourante.ajouterDegats(degats);
					flotteEquCourante.EncaisserDegats(false);
				}
			}
			
			// On vérifie si les deux flottes sont survivantes
			if (flotteA.estMorte()) vainqueur = flotteB;
			if (flotteB.estMorte()) vainqueur = flotteA;
			
		}while(vainqueur == null);

		// On finalise les dégats à encaisser par le vainqueur (pour chaque flotte équivalente).
		for(int classe_fin=0; classe_fin < eClasse.nbClasses; ++classe_fin)
		{
			FlotteEquivalente flotteEquCourante = vainqueur.RecupererFlotteEquivalente(eClasse.values()[classe_fin]);
			flotteEquCourante.EncaisserDegats(true);
		}
		
		return vainqueur;
	}

	/**
	 * @return
	 */
	private boolean estMorte()
	{
		for(int classe = 0; classe < eClasse.nbClasses; ++classe)
		{
			if (!m_FlottesEquivalentes.get(classe).estMorte())
			{
				return false;
			}
		}
		
		return true;
	}

	/**
	 * @param classe
	 * @return
	 */
	private FlotteEquivalente RecupererFlotteEquivalente(eClasse classe)
	{
		return m_FlottesEquivalentes.get(classe.ordinal());
	}

	/**
	 * @param vaisseau
	 * @param quantite
	 */
	public void ajouterVaisseau(Vaisseau vaisseau, int quantite)
	{
		m_FlottesEquivalentes.get(vaisseau.Classe.ordinal()).ajouterVaisseaux(vaisseau, quantite);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<Flotte "+m_sNom+">"+AlgoTests.LINE_SEPARATOR);
		for(int classe=0; classe < eClasse.nbClasses; ++classe)
		{
			sb.append(m_FlottesEquivalentes.get(classe).toString());
		}
		sb.append("</Flotte "+m_sNom+">"+AlgoTests.LINE_SEPARATOR);
		return sb.toString();
	}
}
