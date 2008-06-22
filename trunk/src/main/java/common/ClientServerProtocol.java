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
		DemandeListeParties,
		CreerNouvellePartie,
		JoindreNouvellePartie,
		CommandeChat,
		AnoncerPret,
		LancerPartie
	};
	
	static enum eTransitions
	{
	
	};
}
