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
	private Vector<FlotteEquivalente>	m_FlottesEquivalentes	= new Vector<FlotteEquivalente>();

	private String						m_sNom					= null;

	public Flotte(String sNom)
	{
		m_sNom = sNom;
		m_FlottesEquivalentes.removeAllElements();
		for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			m_FlottesEquivalentes.add(new FlotteEquivalente(eClasse.values()[classe]));
	}

	/**
	 * Algo de combat: Chaque round représente le laps de temps entre l'instant
	 * courant et le prochain "mort", chaque sous-flotte détermine sa meilleure
	 * cible (celle qu'elle peut tuer le plus vite), puis les attaques cumulées
	 * de chaque sous flotte sont jouées pour le laps de temps minimal
	 * entrainant la mort d'une des sous flotte.
	 * 
	 * Conclusion: Actualise trop souvent, entraine un déséquilibre entre gabarits sauvés. 
	 * 
	 * @param flotteA
	 * @param flotteB
	 * @return
	 */
	static public Flotte JouerCombatV1(Flotte flotteA, Flotte flotteB)
	{
		Vector<Vector<FlotteEquivalente>> FlottesEquivalentes = new Vector<Vector<FlotteEquivalente>>();
		Flotte vainqueur = null;

		// On récupère les flottes équivalentes de chaque classe.
		for (int flotte = 0; flotte < 2; ++flotte)
		{
			FlottesEquivalentes.add(new Vector<FlotteEquivalente>());

			for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			{
				FlotteEquivalente flotteEquCourante = ((flotte == 0) ? flotteA : flotteB)
						.RecupererFlotteEquivalente(eClasse.getFromInt(classe));
				FlottesEquivalentes.get(flotte).add(flotteEquCourante);
			}
		}

		do
		{
			// On remet à zéro les données du tour courant
			for (int flotte = 0; flotte < 2; ++flotte)
			{
				for (int classe = 0; classe < eClasse.nbClasses; ++classe)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte).get(classe);
					flotteEquCourante.razAttaques();
				}
			}

			// On détermine qui meurs en premier et en combien de temps
			for (int flotte_att = 0; flotte_att < 2; ++flotte_att)
			{
				for (int classe_att = 0; classe_att < eClasse.nbClasses; ++classe_att)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_att).get(classe_att);

					// Si la flotte est détruite, on saute.
					if (flotteEquCourante.estMorte()) continue;

					double TempsMeilleureCible = Double.POSITIVE_INFINITY;
					FlotteEquivalente MeilleureCible = null;
					int AttaqueMeilleureCible = 0;

					// On détermine la meilleure cible, c'est celle la plus
					// rapide à tuer (et suivant les rapports de forces, pas
					// forcément la TdT)
					for (int classe_def = 0; classe_def < eClasse.nbClasses; ++classe_def)
					{
						double TempsTuerCible = 0.0;
						FlotteEquivalente ciblePotentielle = FlottesEquivalentes.get((flotte_att + 1) % 2).get(
								classe_def);
						// Si la cible est déjà détruite, on saute.
						if (ciblePotentielle.estMorte()) continue;

						int Attaque = Math.max(0,flotteEquCourante.getAttaque() + FlotteEquivalente.getModifAttaque(flotteEquCourante, ciblePotentielle));

						if (Attaque == 0)
						{
							TempsTuerCible = Double.POSITIVE_INFINITY;
						}
						else
						{
							TempsTuerCible = Double.valueOf(ciblePotentielle.getDefense()
									- ciblePotentielle.getDegatsAEncaisser())
									/ Double.valueOf(Attaque);
						}

						if (TempsTuerCible < TempsMeilleureCible)
						{
							MeilleureCible = ciblePotentielle;
							TempsMeilleureCible = TempsTuerCible;
							AttaqueMeilleureCible = Attaque;
						}
					}

					flotteEquCourante.setMeilleureCible(MeilleureCible);
					if (MeilleureCible != null)
					{
						// La meilleure cible est nulle (aucune cible) dans le
						// cas où tous les ennemis sont suffisament blindé pour
						// n'encaisser aucun dégats.
						MeilleureCible.ajouterAttaque(AttaqueMeilleureCible);
					}
				}
			}

			double TempsPremiereVictime = Double.POSITIVE_INFINITY;
			double TempsFlotteCourante = 0;
			for (int flotte_cib = 0; flotte_cib < 2; ++flotte_cib)
			{
				for (int classe_cib = 0; classe_cib < eClasse.nbClasses; ++classe_cib)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_cib).get(classe_cib);
					if (flotteEquCourante.estMorte()) continue;
					TempsFlotteCourante = Double.valueOf(flotteEquCourante.getDefense()
							- flotteEquCourante.getDegatsAEncaisser())
							/ Double.valueOf(flotteEquCourante.getTotalAttaques());
					if (TempsFlotteCourante < TempsPremiereVictime)
					{
						TempsPremiereVictime = TempsFlotteCourante;
					}
				}
			}

			// On joue les dégats reçu par chacun durant ce laps de temps (y
			// compris ceux donné par la victime), c'est à dire qu'on note pour
			// chacun la def restante
			for (int flotte_cpt = 0; flotte_cpt < 2; ++flotte_cpt)
			{
				for (int classe_cpt = 0; classe_cpt < eClasse.nbClasses; ++classe_cpt)
				{
					FlotteEquivalente flotteEquCourante = FlottesEquivalentes.get(flotte_cpt).get(classe_cpt);
					if (flotteEquCourante.estMorte()) continue;

					int degats = new Double(Math.ceil(TempsPremiereVictime
							* Double.valueOf(flotteEquCourante.getTotalAttaques()))).intValue();
					flotteEquCourante.ajouterDegats(degats);
					flotteEquCourante.EncaisserDegats(false);
				}
			}

			// On vérifie si les deux flottes sont survivantes
			if (flotteA.estMorte()) vainqueur = flotteB;
			if (flotteB.estMorte()) vainqueur = flotteA;

		} while (vainqueur == null);

		// On finalise les dégats à encaisser par le vainqueur (pour chaque
		// flotte équivalente).
		for (int classe_fin = 0; classe_fin < eClasse.nbClasses; ++classe_fin)
		{
			FlotteEquivalente flotteEquCourante = vainqueur.RecupererFlotteEquivalente(eClasse.values()[classe_fin]);
			flotteEquCourante.EncaisserDegats(true);
		}

		return vainqueur;
	}

	/**
	 * Algo de combat: On joue point d'attaque par point d'attaque le combat en
	 * notant les dégats jusqu'a constater la mort d'une des deux flotte
	 * (entière). Les flottes sont considérées par leurs sous flottes, qui
	 * attaquent toujours par ordre de priorité TdT-Ego-BN. Une flotte est morte
	 * quand toutes ses sous-flottes le sont. On encaisse ainsi les dégats que
	 * pour la flotte survivante, et seulement en fin de combat, ce qui assure
	 * un équilibre des chances entre gros et petits gabarits lors du rachat des
	 * vaisseaux. A chaque "tour minimal" les dégats causé par chaque
	 * sous-flotte sont proportionnel à sa défense courante. Attaque =
	 * AttaqueMax * (DefCourante/DefMax)
	 * 
	 * Conclusion: Temps de calcul directement dépendant du score de défense, 3s de calcul pour des vaisseaux a 1000 de def.
	 * 
	 * @param flotteA
	 * @param flotteB
	 * @return
	 */
	static public Flotte JouerCombatV2(Flotte flotteA, Flotte flotteB)
	{
		Vector<Vector<FlotteEquivalente>> FlottesEquivalentes = new Vector<Vector<FlotteEquivalente>>();
		Flotte vainqueur = null;

		// On récupère les flottes équivalentes de chaque classe.
		for (int flotte = 0; flotte < 2; ++flotte)
		{
			FlottesEquivalentes.add(new Vector<FlotteEquivalente>());

			for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			{
				FlotteEquivalente flotteEquCourante = ((flotte == 0) ? flotteA : flotteB).RecupererFlotteEquivalente(eClasse.getFromInt(classe));
				FlottesEquivalentes.get(flotte).add(flotteEquCourante);
			}
		}

		FlotteEquivalente flotteEquCourante = null;
		FlotteEquivalente MeilleureCible = null;
		int flotte_adverse = 0;
		double Attaque = 0;
		double TempsFlotteCourante = Double.POSITIVE_INFINITY;
		double TempsPremierHit = Double.POSITIVE_INFINITY;
		
		do
		{
			// On remet à zéro les données du tour courant
			for (int flotte = 0; flotte < 2; ++flotte)
			{
				for (int classe = 0; classe < eClasse.nbClasses; ++classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(flotte).get(classe);
					flotteEquCourante.razAttaques();
				}
			}
			
			// On va noter les attaques de chaque flottes encore vivantes
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);

					// Si la flotte est détruite, on saute.
					if (flotteEquCourante.estMorte())
					{
						continue;
					}

					MeilleureCible = null;
					
					// On détermine la meilleure cible, c'est dans l'ordre de priorité: TdT, Ego, BN
					flotte_adverse = (it_flotte + 1) % 2;
					MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(eClasse.getTdT(flotteEquCourante.getClasse()).ordinal());
					if (MeilleureCible.estMorte())
					{
						MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(flotteEquCourante.getClasse().ordinal());
					}
					if (MeilleureCible.estMorte())
					{
						MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(eClasse.getBN(flotteEquCourante.getClasse()).ordinal());
					}
					if (MeilleureCible.estMorte())
					{
						throw new RuntimeException("Erreur, aucune cible possible, flotte déjà morte.");
					}
					
					Attaque = Math.max(0, flotteEquCourante.getAttaque() + FlotteEquivalente.getModifAttaque(flotteEquCourante, MeilleureCible));
					MeilleureCible.ajouterAttaque(Attaque);
				}
			}

			// On détermine le temps que dure ce round, c'est le temps mini qu'il faut pour qu'une des flottes encaisse 1 point de dégats, cad 1/MeilleurTotalAttaque.
			TempsFlotteCourante = Double.POSITIVE_INFINITY;
			TempsPremierHit = Double.POSITIVE_INFINITY;
			
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);
					if (flotteEquCourante.estMorte()) continue;
					TempsFlotteCourante = Double.valueOf(1) / Double.valueOf(flotteEquCourante.getTotalAttaques());
					
					if (TempsFlotteCourante < TempsPremierHit)
					{
						TempsPremierHit = TempsFlotteCourante;
					}
				}
			}

			// On joue les dégats reçu par chacun durant ce laps de temps (y
			// compris ceux donné par la victime), c'est à dire qu'on note pour
			// chacun la def restante
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);
					if (flotteEquCourante.estMorte()) continue;

					double degats = flotteEquCourante.getTotalAttaques() * TempsPremierHit;
					flotteEquCourante.ajouterDegats(degats);
				}
			}

			// On vérifie si les deux flottes sont survivantes
			if (flotteA.estMorte()) vainqueur = flotteB;
			if (flotteB.estMorte()) vainqueur = flotteA;

		} while (vainqueur == null);

		// On finalise les dégats à encaisser par le vainqueur (pour chaque
		// flotte équivalente).
		for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
		{
			flotteEquCourante = vainqueur.RecupererFlotteEquivalente(eClasse.values()[it_classe]);
			flotteEquCourante.EncaisserDegats(true);
		}

		return vainqueur;
	}

	/**
	 * Algo de combat: 
	 * Algo V2, sauf que:
	 * On joue mort / mort, en reportant les points d'attaque innutilisés.
	 * 
	 * Attention: Prendre des valeurs telles que la somme suivante ne dépasse pas Double.MAX_VALUE:
	 * SousFlotteBN.Attaque + SousFlotteBN.Arme + SousFlotteEgo.Attaque + SousFlotteTdT.Attaque (- SousFlotteCible.Armure) 
	 * @param flotteA
	 * @param flotteB
	 * @return
	 */
	static public Flotte JouerCombat(Flotte flotteA, Flotte flotteB)
	{
		Vector<Vector<FlotteEquivalente>> FlottesEquivalentes = new Vector<Vector<FlotteEquivalente>>();
		Flotte vainqueur = null;

		// On récupère les flottes équivalentes de chaque classe.
		for (int flotte = 0; flotte < 2; ++flotte)
		{
			FlottesEquivalentes.add(new Vector<FlotteEquivalente>());

			for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			{
				FlotteEquivalente flotteEquCourante = ((flotte == 0) ? flotteA : flotteB).RecupererFlotteEquivalente(eClasse.getFromInt(classe));
				FlottesEquivalentes.get(flotte).add(flotteEquCourante);
			}
		}

		FlotteEquivalente flotteEquCourante = null;
		FlotteEquivalente MeilleureCible = null;
		int flotte_adverse = 0;
		double Modif = 0;
		double TempsFlotteCourante = Double.POSITIVE_INFINITY;
		double TempsPremiereVictime = Double.POSITIVE_INFINITY;
		
		do
		{	
			// On remet à zéro les données du tour courant
			for (int flotte = 0; flotte < 2; ++flotte)
			{
				for (int classe = 0; classe < eClasse.nbClasses; ++classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(flotte).get(classe);
					flotteEquCourante.razAttaques();
				}
			}
			
			// On va noter les attaques de chaque flottes encore vivantes
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);

					// Si la flotte est détruite, on saute.
					if (flotteEquCourante.estMorte())
					{
						continue;
					}

					MeilleureCible = null;
					
					// On détermine la meilleure cible, c'est dans l'ordre de priorité: TdT, Ego, BN
					flotte_adverse = (it_flotte + 1) % 2;
					MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(eClasse.getTdT(flotteEquCourante.getClasse()).ordinal());
					if (MeilleureCible.estMorte())
					{
						MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(flotteEquCourante.getClasse().ordinal());
					}
					if (MeilleureCible.estMorte())
					{
						MeilleureCible = FlottesEquivalentes.get(flotte_adverse).get(eClasse.getBN(flotteEquCourante.getClasse()).ordinal());
					}
					if (MeilleureCible.estMorte())
					{
						throw new RuntimeException("Erreur, aucune cible possible, flotte déjà morte.");
					}
					
					Modif = FlotteEquivalente.getModifAttaque(flotteEquCourante, MeilleureCible) * (flotteEquCourante.getDefenseCourante()/flotteEquCourante.getDefense());
					MeilleureCible.ajouterAttaque(Math.max(0,flotteEquCourante.getAttaque() + Modif));
				}
			}

			// On détermine le temps que dure ce round, c'est le temps mini qu'il faut pour qu'une des flottes meure.
			TempsFlotteCourante = Double.POSITIVE_INFINITY;
			TempsPremiereVictime = Double.POSITIVE_INFINITY;
			
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);
					if (flotteEquCourante.estMorte()) continue;
					
					TempsFlotteCourante = flotteEquCourante.getDefenseCourante() / flotteEquCourante.getTotalAttaques();
					
					if (TempsFlotteCourante < TempsPremiereVictime)
					{
						TempsPremiereVictime = TempsFlotteCourante;
					}
				}
			}

			// On joue les dégats reçu par chacun durant ce laps de temps (y
			// compris ceux donné par la victime), c'est à dire qu'on note pour
			// chacun la def restante
			for (int it_flotte = 0; it_flotte < 2; ++it_flotte)
			{
				for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
				{
					flotteEquCourante = FlottesEquivalentes.get(it_flotte).get(it_classe);
					if (flotteEquCourante.estMorte()) continue;

					double degats = flotteEquCourante.getTotalAttaques() * TempsPremiereVictime;
					flotteEquCourante.ajouterDegats(degats);
				}
			}

			// On vérifie si les deux flottes sont survivantes
			if (flotteA.estMorte()) vainqueur = flotteB;
			if (flotteB.estMorte()) vainqueur = flotteA;

		} while (vainqueur == null);

		// On finalise les dégats à encaisser par le vainqueur (pour chaque
		// flotte équivalente).
		for (int it_classe = 0; it_classe < eClasse.nbClasses; ++it_classe)
		{
			flotteEquCourante = vainqueur.RecupererFlotteEquivalente(eClasse.values()[it_classe]);
			flotteEquCourante.EncaisserDegats(true);
		}

		return vainqueur;
	}
	
	/**
	 * @return
	 */
	private boolean estMorte()
	{
		for (int classe = 0; classe < eClasse.nbClasses; ++classe)
		{
			if ( !m_FlottesEquivalentes.get(classe).estMorte())
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
	public FlotteEquivalente RecupererFlotteEquivalente(eClasse classe)
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
		sb.append("<Flotte " + m_sNom + ">" + AlgoTests.LINE_SEPARATOR);
		for (int classe = 0; classe < eClasse.nbClasses; ++classe)
		{
			sb.append(m_FlottesEquivalentes.get(classe).toString());
		}
		sb.append("</Flotte " + m_sNom + ">" + AlgoTests.LINE_SEPARATOR);
		return sb.toString();
	}

	public String getM_sNom()
	{
		return m_sNom;
	}
}
