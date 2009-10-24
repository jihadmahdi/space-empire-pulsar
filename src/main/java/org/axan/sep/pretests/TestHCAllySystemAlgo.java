/**
 * @author Escallier Pierre
 * @file TestHCAllySystemAlgo.java
 * @date 8 mars 08
 */
package org.axan.sep.pretests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

/**
 * 
 */
public class TestHCAllySystemAlgo
{

	public static class Joueur
	{

		/** R�f�rence vers la table des variables */
		static private TreeMap<String, Joueur>	sm_tableVariables	= new TreeMap<String, Joueur>();

		public String							nom					= "sans nom";

		public SortedMap<Integer, Joueur>		alliésSouhaités		= new TreeMap<Integer, Joueur>();

		private SortedMap<Integer, Joueur>		alliésPossibles		= null;
		
		static public void CalculerVariables()
		{
			Iterator<Joueur> it = sm_tableVariables.values().iterator();
			
			while(it.hasNext())
			{
				Joueur var = (Joueur) it.next();
				var.listerAlliés();
			}
		}
		
		static public Joueur getVariableFromName(String i_sNomVar) throws Exception
		{
			if ( !sm_tableVariables.containsKey(i_sNomVar))
			{
				throw new Exception("Variable \"" + i_sNomVar + "\" non d�finie.");
			}

			return (Joueur) sm_tableVariables.get(i_sNomVar);
		}

		public synchronized SortedMap<Integer, Joueur> getAlliésPossibles()
		{
			if (alliésPossibles == null)
			{
				alliésPossibles = new TreeMap<Integer, Joueur>();
			}

			if (alliésPossibles.isEmpty())
			{
				alliésPossibles.putAll(alliésSouhaités);
			}

			return alliésPossibles;
		}

		public Joueur(String nom)
		{
			this.nom = nom;
			sm_tableVariables.put(nom, this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString()
		{
			return nom;
		}

		public void ajouterAlliéSouhaité(Joueur alliéSouhaité, int poids)
		{
			if (alliéSouhaité == this) return;

			alliésSouhaités.put(poids, alliéSouhaité);
		}

		private boolean			m_bValide					= false;

		private boolean			m_bErreur					= true;

		/** Table des noms des variables utilis�es pour d�finir celle-ci */
		private Vector<String>	m_vVariablesDontJeDepents	= new Vector<String>();

		/** Table des noms des variables qui utilise celle-ci */
		private Vector<String>	m_vVariablesDependantes		= new Vector<String>();

		/**
		 * Flag indiquant si l'�quation a �t� analys�e et les abonnements aux autres variables effectu�s correctement
		 * 
		 * @uml.property name="m_bEquationOK"
		 */
		private boolean			m_bEquationOK				= false;
		
		private Set<Joueur> m_dValeur = new HashSet<Joueur>();

		public boolean estValide()
		{
			return m_bValide;
		}

		public boolean enErreur()
		{
			// On ne peut pas interroger l'état d'erreur d'une variable invalide.
			if ( !m_bValide)
			{
				throw new RuntimeException(nom + ".enErreur() incorrect car variable invalide");
			}

			return m_bErreur;
		}

		public void AbonnerVariableDependante(String i_sNom)
		{
			if ( !m_vVariablesDependantes.contains(i_sNom))
			{
				m_vVariablesDependantes.add(i_sNom);
			}
		}

		public void DesabonnerVariableDependante(String i_sNom)
		{
			if (m_vVariablesDependantes.contains(i_sNom))
			{
				m_vVariablesDependantes.remove(i_sNom);
			}
		}

		private void AnalyserEquation(boolean i_bForcerAnalyse)
		{
			// Si l'�quation a d�j� �t� analys�e correctement et que l'on ne force pas une nouvelle analyse, on passe.
			if (m_bEquationOK && !i_bForcerAnalyse) return;

			// On commence par se d�sabonner des variables de l'ancienne �quation
			Iterator<String> it = m_vVariablesDontJeDepents.iterator();
			while (it.hasNext())
			{
				String sNomVar = (String) it.next();
				if (sm_tableVariables.containsKey(sNomVar))
				{
					Joueur var = (Joueur) sm_tableVariables.get(sNomVar);
					var.DesabonnerVariableDependante(nom);
				}
			}

			m_vVariablesDontJeDepents.removeAllElements();

			// On analyse l'�quation
			// AnalyserFragmentEquation(m_sEquation, false);
			Iterator<Entry<Integer, Joueur>> itAlliés = alliésSouhaités.entrySet().iterator();
			while (itAlliés.hasNext())
			{
				Joueur alliéSouhaité = itAlliés.next().getValue();
				alliéSouhaité.AbonnerVariableDependante(nom);
				m_vVariablesDontJeDepents.add(alliéSouhaité.nom);
			}
			/*
			 * catch (ExceptionVarInconnue e) { m_bEquationOK = false; return; }
			 */

			m_bEquationOK = true;
		}

		private void MarquerErreur()
		{
			m_bErreur = true;
		}

		/** Valide la variable */
		private void Valider()
		{
			m_bValide = true;
			// m_bErreur = false;
		}

		private Joueur getVarFromName(String i_sNomVar)
		{
			Joueur var = null;
			try
			{
				var = getVariableFromName(i_sNomVar);
			}
			catch (Exception e)
			{
				MarquerErreur();
				Valider();
				throw new RuntimeException(nom + "d�pend d'une variable non d�finie: \"" + i_sNomVar + "\"");
			}

			return var;
		}

		public Set<Joueur> getValeur()
		{
			// On ne peut pas r�cup�rer la valeur d'une variable invalide
			if (!m_bValide)
			{
				throw new RuntimeException(nom + ".getValeur() incorrect car variable invalide");
			}
			
			// On ne peut pas r�cup�rer la valeur d'une variable en erreur
			if (m_bErreur)
			{
				throw new RuntimeException(nom + ".getValeur() incorrect car variable en erreur");
			}
			
			return m_dValeur;
		}
		
		public boolean listerAlliés()
		{
			return listerAlliés(new Vector<Joueur>());
		}
		
		public boolean listerAlliés(Vector<Joueur> i_vPileVariables)
		{
			if (estValide())
			{
				return enErreur();
			}

			m_bErreur = false;

			AnalyserEquation(false);

			// On parcours la liste des variables n�c�ssaires et on d�termine pour chacune leur valeur
			Iterator<String> it = m_vVariablesDontJeDepents.iterator();
			while (it.hasNext())
			{
				String sNomVar = (String) it.next();

				Joueur var = getVarFromName(sNomVar);

				// Si la variable est valide, on peut directement r�cup�rer sa valeur
				if (var.estValide())
				{
					if (var.enErreur())
					{
						MarquerErreur();
						Valider();
						return false;
					}
					else
					{
						// TODO: Utiliser var.getValeur
					}
				}
				// Sinon
				else
				{
					// Si la variable est d�j� dans la pile de calcul, c'est que l'on tourne en rond, on ne pourra pas d�terminer la variable courante.
					if (i_vPileVariables.contains(var))
					{
						MarquerErreur();
						Valider();
						return false;
					}
					// Sinon
					else
					{
						// On ajoute la variable courante a la pile de calcul
						i_vPileVariables.add(this);

						if ( !var.listerAlliés(i_vPileVariables))
						{
							MarquerErreur();
							Valider();
							return false;
						}
					}
				}
			}

				m_dValeur.clear();
				
				Iterator<Entry<Integer, Joueur>> itAlliésSouhaités = alliésSouhaités.entrySet().iterator();
				while(itAlliésSouhaités.hasNext())
				{
					Joueur alliéSouhaité = itAlliésSouhaités.next().getValue();
					if (alliéSouhaité.getValeur().contains(this))
					{
						m_dValeur.add(alliéSouhaité);
					}
				}
				/*
	POUR CHAQUE alliésSouhaités => alliéSouhaité FAIRE
		SI (alliéSouhaité.alliésSouhaités.contient(joueur)) ALORS
			SI (alliéSouhaité.ListAllyOf().contient(joueur)) ALORS
				resultat.ajouter(alliéSouhaité)
			SINON
				alliésSouhaités.retirer(alliéSouhaité)
			FSI
		FSI
	FPOUR
				 */
			/*
			catch (ExceptionVarInconnue e)
			{
				MarquerErreur();
				Valider();
				return false;
			}
			*/

			Valider();
			return true;

			/*
			 * synchronized (this) { if (partiel && (alliésPossibles != null)) { return new HashSet<Joueur>(alliésPossibles.values()); }
			 * 
			 * if (alliésPossibles == null) { alliésPossibles = new TreeMap<Integer, Joueur>(); }
			 * 
			 * alliésPossibles.clear(); alliésPossibles.putAll(alliésSouhaités); }
			 * 
			 * Set<Joueur> resultat = new HashSet<Joueur>();
			 * 
			 * Iterator<Entry<Integer, Joueur>> it = alliésPossibles.entrySet().iterator(); while (it.hasNext()) { Joueur alliéSouhaité = it.next().getValue(); if ((alliéSouhaité.getAlliésPossibles().containsValue(this)) && (alliéSouhaité.listerAlliés(true).contains(this))) { resultat.add(alliéSouhaité); } else { it.remove(); } }
			 */

			/*
			 * ALGO Joueur::ListAllyOf() : Joueur[] allies
			 * 
			 * Joueur[] resultat <= []
			 * 
			 * POUR CHAQUE alliésSouhaités => alliéSouhaité FAIRE SI (alliéSouhaité.alliésSouhaités.contient(joueur)) ALORS SI (alliéSouhaité.ListAllyOf().contient(joueur)) ALORS resultat.ajouter(alliéSouhaité) SINON alliésSouhaités.retirer(alliéSouhaité) FSI FSI FPOUR
			 * 
			 * RETOURNER resultat FIN
			 */
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		/*
		 * A [B, C, D] B [D, A] C [A, B, D] D [C, A]
		 */
		Joueur A = new Joueur("A");
		Joueur B = new Joueur("B");
		Joueur C = new Joueur("C");
		Joueur D = new Joueur("D");

		A.ajouterAlliéSouhaité(B, 1);
		A.ajouterAlliéSouhaité(C, 2);
		A.ajouterAlliéSouhaité(D, 3);

		B.ajouterAlliéSouhaité(D, 1);
		B.ajouterAlliéSouhaité(A, 2);

		C.ajouterAlliéSouhaité(A, 1);
		C.ajouterAlliéSouhaité(B, 2);
		C.ajouterAlliéSouhaité(D, 3);

		D.ajouterAlliéSouhaité(C, 1);
		D.ajouterAlliéSouhaité(A, 2);

		Joueur.CalculerVariables();
		
		Set<Joueur> alliésA = A.getValeur();
		Set<Joueur> alliésB = B.getValeur();
		Set<Joueur> alliésC = C.getValeur();
		Set<Joueur> alliésD = D.getValeur();

		System.out.println("A : " + alliésA);
		System.out.println("B : " + alliésB);
		System.out.println("C : " + alliésC);
		System.out.println("D : " + alliésD);
	}

}
