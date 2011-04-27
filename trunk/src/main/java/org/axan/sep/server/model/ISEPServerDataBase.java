package org.axan.sep.server.model;

import java.util.SortedSet;

import org.axan.sep.server.model.GameBoard.ATurnResolvingEvent;

public interface ISEPServerDataBase
{
	public static class SEPServerDataBaseException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public SEPServerDataBaseException(String message)
		{
			super(message);
		}
		
		public SEPServerDataBaseException(Throwable t)
		{
			super(t);
		}
		
		public SEPServerDataBaseException(String message, Throwable t)
		{
			super(message, t);
		}
	}
	
	/**
	 * Compute player view of the game board.
	 * @param playerLogin
	 * @return player view of the current game board.
	 */
	org.axan.sep.common.PlayerGameBoard getPlayerGameBoard(String playerLogin);

	/**
	 * Return sorted set of the resolving events, these events must comply with the following specification to produce valid game rules :
	 * 
	 * La résolution d'un tour génère l'évènement "le temps s'écoule d'un tour".
	 * Cet évènement engendre plusieurs effets, et l'on teste pour chaque nouvel état de l'univers si de nouveaux évènements se sont produits (avec des requetes tests).
	 * Celà permet de décomposer la résolution d'un tour en résolution plus "modulaire" par évènement/traitement que l'on organise ensuite par ordre de priorité.
	 * 
	 * Evènements (ordonnés):
	 * Nom					Description
	 * 	Effets
	 * -----------------------------------------
	 * OnTimeTick			Le temps s'écoule.
	 *	Traitements: Déplacer les unités mobiles, journal(rencontres, vortex, départs, arrivés), déclenche les conflits (qui sont lancé par des unités non mobiles à ce tour).
	 *	Lance: OnUnitArrival, OnConflict
	 *
	 * OnUnitArrival(unit)		Une unité spéciale arrive à destination.
	 * 
	 * OnUnitArrival(PulsarMissile)
	 * 	Traitements:
	 * 		Les missiles pulsar engendrent un pulsar
	 * 
	 * OnUnitArrival(AntiProbeMissile)
	 * 	Traitements:
	 * 		les missiles anti-probes explosent en détruisant éventuellement une probe
	 * 
	 * OnUnitArrival(Probe)
	 * 	Traitements:
	 * 		les probes se déploient 
	 * 
	 * OnUnitArrival(Fleet)
	 * 	Traitements:
	 * 		les flottes déclenchent un conflit, se posent, repartent, et peuvent communiquer leur journal de bord.
	 * 	Lance: OnConflict
	 * 
	 * OnUnitArrival(SpaceRoadDeliverer)
	 * 	Traitements:
	 * 		les spaceRoadDeliverer spawnent une spaceRoad, et peuvent communiquer leur journal de bord.
	 * 
	 * OnUnitArrival(CarbonCarrier)
	 * 	Traitements:
	 * 		les carbonCarrier spawn du carbone, éventuellement repartent, et peuvent communiquer leur journal de bord.
	 *	Lance: OnConflict
	 * 
	 * OnConflict			Un conflit est déclaré sur un cors céleste.
	 * 	On résoud le conflit concerné, en mettant à jour les journals de bords des flottes concernées (+ log du corps céleste champs de bataille communiqué en direct au joueur).
	 * 
	 * OnTimeTickEnd		Le temps à fini de s'écouler.
	 * 	On génère le carbone et la population pour le tour écoulé, on incrémente la date.
	 * @return
	 */
	SortedSet<ATurnResolvingEvent> getResolvingEvents();
}
