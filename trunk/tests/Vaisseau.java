package tests;

public class Vaisseau
{
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
	public int		Arme		= 0;

	/** Armure de classes */
	public int		Armure	= 0;

	public eClasse	Classe		= null;

	public String	Nom			= null;

	public Vaisseau(Vaisseau modele)
	{
		Nom = modele.Nom;
		Defense = modele.Defense;
		Attaque = modele.Attaque;
		Classe = modele.Classe;
		
		Arme = modele.Arme;
		Armure = modele.Armure;
	}

	protected Vaisseau clone()
	{
		return new Vaisseau(this);
	}
	
	public Vaisseau(String sNom, int iDef, int iAtt, eClasse iClasse, int iArme, int iArmure)
	{
		Nom = sNom;
		Defense = iDef;
		Attaque = iAtt;
		Classe = iClasse;

		Arme = iArme;
		Armure = iArmure;
	}
	
	public String toString()
	{
		return Nom+" Def:"+Defense+" / Att:"+Attaque+" / Arm:"+Arme+" / Bl:"+Armure;
	}
}