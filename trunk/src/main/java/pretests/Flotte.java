/**
 * 
 */
package pretests;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Vector;

import pretests.Vaisseau.eClasse;

/**
 * @author Axan Classe représentant une flotte de plusieurs vaisseaux.
 */
public class Flotte
{
	private Vector<FlotteEquivalente>	m_FlottesEquivalentes	= new Vector<FlotteEquivalente>();

	private String						m_sNom					= null;
	private BigDecimal					m_Defense				= BigDecimal.ZERO;
	private BigDecimal					m_Attaque				= BigDecimal.ZERO;
	private BigDecimal					m_Arme					= BigDecimal.ZERO;
	private BigDecimal					m_Armure				= BigDecimal.ZERO;

	public Flotte(String sNom)
	{
		m_sNom = sNom;
		m_FlottesEquivalentes.removeAllElements();
		for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			m_FlottesEquivalentes.add(new FlotteEquivalente(eClasse.values()[classe]));
	}

	static public void refreshCaracsCombat(Flotte flotteA, Flotte flotteB)
	{
		BigDecimal somme_arme[] = {BigDecimal.ZERO, BigDecimal.ZERO};
		BigDecimal somme_armure[] = {BigDecimal.ZERO, BigDecimal.ZERO};
		for (int flotte = 0; flotte < 2; ++flotte)
		{
			Flotte flotteCourante = ((flotte == 0) ? flotteA : flotteB);
			Flotte flotteEnnemie = ((flotte == 0) ? flotteB : flotteA);
			BigDecimal somme_attaque = BigDecimal.ZERO;
			
			flotteCourante.m_Defense = BigDecimal.ZERO;
			for (int classe = 0; classe < eClasse.nbClasses; ++classe)
			{
				FlotteEquivalente flotteEquCourante = flotteCourante.RecupererFlotteEquivalente(eClasse.getFromInt(classe));
				FlotteEquivalente flotteEquTdT = flotteEnnemie.RecupererFlotteEquivalente(eClasse.getTdT(eClasse.getFromInt(classe)));
				
				BigDecimal Defense = flotteEquCourante.getDefense();
				BigDecimal DefTdT = flotteEquTdT.getDefense();
				
				flotteCourante.m_Defense = flotteCourante.m_Defense.add(flotteEquCourante.getDefense());
				somme_attaque = somme_attaque.add(Defense.multiply(BigDecimal.valueOf(flotteEquCourante.getAttaque())));
				somme_arme[flotte] = somme_arme[flotte].add(Defense.multiply(DefTdT.multiply(BigDecimal.valueOf(flotteEquCourante.getBonusArme()))));
				somme_armure[flotte]=somme_armure[flotte].add(Defense.multiply(DefTdT.multiply(BigDecimal.valueOf(flotteEquCourante.getBonusArmure()))));
			}
			flotteCourante.m_Attaque = somme_attaque.divide(flotteCourante.m_Defense, MathContext.DECIMAL128);
		}
		
		BigDecimal DefDef = flotteA.m_Defense.multiply(flotteB.m_Defense);
		flotteA.m_Arme = somme_arme[0].divide(DefDef, MathContext.DECIMAL128);
		flotteA.m_Armure = somme_armure[0].divide(DefDef, MathContext.DECIMAL128);
		flotteB.m_Arme = somme_arme[1].divide(DefDef, MathContext.DECIMAL128);
		flotteB.m_Armure = somme_armure[1].divide(DefDef, MathContext.DECIMAL128);
	}
	
	static public Flotte JouerCombat(Flotte flotteA, Flotte flotteB)
	{
		return JouerCombatAlgoFlottesMoyenne(flotteA, flotteB);
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
	static public Flotte JouerCombatAlgoFlottesEquivalentes(Flotte flotteA, Flotte flotteB)
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
		double AttaqueCoup = 0;
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
					
					AttaqueCoup = FlotteEquivalente.getTotalAttaqueCourante(flotteEquCourante, MeilleureCible);
					MeilleureCible.ajouterAttaque(Math.max(0,AttaqueCoup));
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
					
					TempsFlotteCourante = flotteEquCourante.getTempsSurvieAttaque();
					
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
	
	static public Flotte JouerCombatAlgoFlottesMoyenne(Flotte flotteA, Flotte flotteB)
	{
		Flotte vainqueur = null;
		
		refreshCaracsCombat(flotteA, flotteB);
		BigDecimal flotteA_attaque_totale = flotteA.m_Attaque.multiply(flotteA.m_Arme.add(BigDecimal.ONE).subtract(flotteB.m_Armure));
		BigDecimal flotteB_attaque_totale = flotteB.m_Attaque.multiply(flotteB.m_Arme.add(BigDecimal.ONE).subtract(flotteA.m_Armure));
		
		BigDecimal TempsFAtueFB = flotteB.m_Defense.divide(flotteA_attaque_totale, MathContext.DECIMAL128);
		BigDecimal TempsFBtueFA = flotteA.m_Defense.divide(flotteB_attaque_totale, MathContext.DECIMAL128);
		
		if (TempsFAtueFB.compareTo(TempsFBtueFA) < 0)
		{
			flotteA.encaisserDegats(TempsFAtueFB.multiply(flotteB_attaque_totale));
			flotteB.detruire();
			
			vainqueur = flotteA;
		}
		else
		{
			flotteB.encaisserDegats(TempsFBtueFA.multiply(flotteA_attaque_totale));
			flotteA.detruire();
			
			vainqueur = flotteB;
		}

		return vainqueur;
	}
	
	/**
	 * 
	 */
	private void detruire()
	{
		encaisserDegats(m_Defense);
	}

	/**
	 * @param decimal
	 */
	private void encaisserDegats(BigDecimal degats)
	{
		for (int classe = 0; classe < eClasse.nbClasses; ++classe)
		{
			FlotteEquivalente flotteEquCourante = RecupererFlotteEquivalente(eClasse.getFromInt(classe));
			flotteEquCourante.ajouterDegats(degats.multiply(flotteEquCourante.getDefense()).divide(m_Defense, MathContext.DECIMAL128).doubleValue());
			flotteEquCourante.EncaisserDegats(true);
		}
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
