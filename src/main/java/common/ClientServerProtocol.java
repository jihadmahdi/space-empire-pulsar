package common;
/**
 * @author Escallier Pierre
 * @file ClientServerProtocol.java
 * @date 20 juin 08
 */

/**
 * 
 */
public interface ClientServerProtocol
{
	static enum eEtats
	{
		AttenteCommande,
		AttenteCreationPartie,
		PartieEnCours;
	};
	
	static enum eEvenements
	{
		// AttenteCommande
		DemandeListeParties,
		CreerNouvellePartie,
		JoindreNouvellePartie,
		
		// AttenteCreationPartie
		QuitterNouvellePartie,
		CommandeChat,
		ModifConfigPartie,
		ModifJoueur,
		LancerPartie
		
		// PartieEnCours
	};
	
}
