package pretests;

public class Vaisseau implements Comparable<Vaisseau>
{
	public static final double FACTEUR_GLOBAL = 60;
	public static final double FACTEUR_ATTAQUE= 1;
	public static final double FACTEUR_DEFENSE= 1;
	public static final double FACTEUR_ARME   = 1/3;
	public static final double FACTEUR_ARMURE = 1/3;
	public static final double FACTEUR_VITESSE= 5;
	
	public enum eClasse
	{
		DD, TANK, DIST;
		
		static public final int nbClasses = 3;
		static public eClasse getFromInt(int entier)
		{
			return values()[entier];
		}
		
		static public eClasse getBN(eClasse classe)
		{
			switch(classe)
			{
				case DD: return TANK;
				case TANK: return DIST;
				case DIST: return DD;
				default: throw new RuntimeException("BN inconnue pour la classe \"" + classe +"\"");
			}
		}
		
		static public eClasse getTdT(eClasse classe)
		{
			switch(classe)
			{
				case DD: return DIST;
				case TANK: return DD;
				case DIST: return TANK;
				default: throw new RuntimeException("TdT inconnue pour la classe \"" + classe +"\"");
			}
		}
		
		public int comparer(eClasse classe)
		{
			if (this == classe) return 0;
			if (getTdT(this) == classe) return 1;
			if (getBN(this) == classe) return -1;
			
			throw new RuntimeException("Comparaison de classes impossible: \""+this+"\" et \""+classe+"\"");
		}
	};

	public int		Defense		= 0;

	public int		Attaque		= 0;

	/** Arme de classes */
	public double	BonusArme		= 0;

	/** Armure de classes */
	public double	BonusArmure	= 0;

	public double	Vitesse = 0;
	
	public eClasse	Classe		= null;

	public String	Nom			= null;

	public Vaisseau(Vaisseau modele) 
	{
		Nom = modele.Nom;
		Defense = modele.Defense;
		Attaque = modele.Attaque;
		Classe = modele.Classe;
		
		BonusArme = modele.BonusArme;
		BonusArmure = modele.BonusArmure;
	}
	
	public Vaisseau(String sNom, int iDef, int iAtt, eClasse iClasse, double iArme, double iArmure)
	{
		if (iArmure > 1.0) throw new NumberFormatException("Erreur: Bonus d'armure > 100%");
		Nom = sNom;
		Defense = iDef;
		Attaque = iAtt;
		Classe = iClasse;

		BonusArme = iArme;
		BonusArmure = iArmure;
	}
	
	public String toString()
	{
		return Nom+" Def:"+Defense+" / Att:"+Attaque+" / Arm:"+BonusArme+" / Bl:"+BonusArmure;
	}
	
	public double estimerValeur()
	{
		return FACTEUR_GLOBAL * ((Attaque * FACTEUR_ATTAQUE) + (Defense * FACTEUR_DEFENSE) + (BonusArme * Attaque * FACTEUR_ARME) + (BonusArmure * Defense * FACTEUR_ARMURE) + (Vitesse * FACTEUR_VITESSE) );
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Vaisseau o)
	{
		// On range les vaisseaux par classes
		if (this.Classe != o.Classe)
		{
			return (this.Classe.ordinal() - o.Classe.ordinal());
		}
		
		// A classe identique, on range par valeur
		return ((Double)(this.estimerValeur() - o.estimerValeur())).intValue();
	}
}
