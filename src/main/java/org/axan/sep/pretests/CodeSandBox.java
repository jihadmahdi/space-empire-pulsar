package org.axan.sep.pretests;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.axan.sep.common.Diplomacy.PlayerPolicies;
import org.axan.sep.common.Diplomacy.PlayerPolicies.eForeignPolicy;


public class CodeSandBox
{
	public static Map<String, Map<String, Boolean>> conflictDiplomacyResolution(Stack<String> initiators, Set<String> playersKeySet, String celestialBodyOwnerName, Map<String, Map<String, PlayerPolicies>> policies)
	{
		Map<String, Map<String, Boolean>> conflicts = new Hashtable<String, Map<String,Boolean>>();
		Stack<String> seenInitiators = new Stack<String>();
		
		while(!initiators.isEmpty())
		{
			boolean fought = false;
			String initiator = initiators.pop();
			if (seenInitiators.contains(initiator)) continue;
			
			for(String target : playersKeySet)
			{
				if (target.equals(initiator)) continue;
				
				boolean initiatorPolicy = false;
				
				if (initiator.equals(celestialBodyOwnerName))
				{
					initiatorPolicy = !policies.get(initiator).get(target).isAllowedToLandFleetInHomeTerritory();
				}
				else
				{
					eForeignPolicy fp = policies.get(initiator).get(target).getForeignPolicy();
					initiatorPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && target.equals(celestialBodyOwnerName));
				}
				
				boolean resultPolicy = conflicts.containsKey(target) && conflicts.get(target).containsKey(initiator) ? conflicts.get(target).get(initiator) || initiatorPolicy : initiatorPolicy;
				
				if (!conflicts.containsKey(initiator)) conflicts.put(initiator, new Hashtable<String, Boolean>());
				conflicts.get(initiator).put(target, resultPolicy);
				
				if (resultPolicy)
				{
					fought = true;
					initiators.push(target);
				}			
			}
			
			if (fought)
			{
				for(String target : playersKeySet)
				{
					if (target.equals(initiator)) continue;
					
					boolean targetPolicy = false;
					
					if (target.equals(celestialBodyOwnerName))
					{
						targetPolicy = !policies.get(target).get(initiator).isAllowedToLandFleetInHomeTerritory();
					}
					else
					{
						eForeignPolicy fp = policies.get(target).get(initiator).getForeignPolicy();
						targetPolicy = (fp == eForeignPolicy.HOSTILE || fp == eForeignPolicy.HOSTILE_IF_OWNER && initiator.equals(celestialBodyOwnerName));
					}
					
					boolean resultPolicy = conflicts.get(initiator).get(target) || targetPolicy;
					if (!conflicts.containsKey(target)) conflicts.put(target, new Hashtable<String, Boolean>());
					conflicts.get(target).put(initiator, resultPolicy);
					conflicts.get(initiator).put(target, resultPolicy);
					
					if (resultPolicy)
					{
						initiators.push(target);
					}
				}
			}
			
			seenInitiators.push(initiator);
		}
		
		return conflicts;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		Vector<Integer> vInt = new Vector<Integer>();
		Vector<String> vString = new Vector<String>();
		
		System.out.println("vInt instanceof Vector<?> == "+(vInt instanceof Vector<?>));
		System.out.println("vString instanceof Vector<?> == "+(vString instanceof Vector<?>));
		System.out.println("vInt.getClass().equals(vString.getClass()) == "+vInt.getClass().equals(vString.getClass()));
		
		String pA = "A";
		String pB = "B";
		String pC = "C";
		String pD = "D";
		String pE = "E";
		
		Stack<String> initiators = new Stack<String>();
		initiators.push(pA);
		
		Set<String> playersKeySet = new HashSet<String>();
		playersKeySet.add(pA);
		playersKeySet.add(pB);
		playersKeySet.add(pC);
		playersKeySet.add(pD);
		playersKeySet.add(pE);
		
		String celestialBodyOwnerName = pA;
		
		Map<String, Map<String, PlayerPolicies>> policies = new Hashtable<String, Map<String, PlayerPolicies>>();
		
		PlayerPolicies pAB = new PlayerPolicies(pB, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pAC = new PlayerPolicies(pC, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pAD = new PlayerPolicies(pD, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pAE = new PlayerPolicies(pE, true, eForeignPolicy.NEUTRAL);
		
		PlayerPolicies pBA = new PlayerPolicies(pA, true, eForeignPolicy.HOSTILE_IF_OWNER);
		PlayerPolicies pBC = new PlayerPolicies(pC, true, eForeignPolicy.HOSTILE_IF_OWNER);
		PlayerPolicies pBD = new PlayerPolicies(pD, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pBE = new PlayerPolicies(pE, true, eForeignPolicy.NEUTRAL);
		
		PlayerPolicies pCA = new PlayerPolicies(pA, true, eForeignPolicy.HOSTILE);
		PlayerPolicies pCB = new PlayerPolicies(pB, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pCD = new PlayerPolicies(pD, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pCE = new PlayerPolicies(pE, true, eForeignPolicy.NEUTRAL);
		
		PlayerPolicies pDA = new PlayerPolicies(pA, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pDB = new PlayerPolicies(pB, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pDC = new PlayerPolicies(pC, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pDE = new PlayerPolicies(pE, true, eForeignPolicy.NEUTRAL);
		
		PlayerPolicies pEA = new PlayerPolicies(pA, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pEB = new PlayerPolicies(pB, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pEC = new PlayerPolicies(pC, true, eForeignPolicy.NEUTRAL);
		PlayerPolicies pED = new PlayerPolicies(pD, true, eForeignPolicy.NEUTRAL);
		
		Map<String, PlayerPolicies> pAPolicies = new Hashtable<String, PlayerPolicies>();
		pAPolicies.put(pB, pAB);
		pAPolicies.put(pC, pAC);
		pAPolicies.put(pD, pAD);
		pAPolicies.put(pE, pAE);
		
		Map<String, PlayerPolicies> pBPolicies = new Hashtable<String, PlayerPolicies>();
		pBPolicies.put(pA, pBA);
		pBPolicies.put(pC, pBC);
		pBPolicies.put(pD, pBD);
		pBPolicies.put(pE, pBE);
		
		Map<String, PlayerPolicies> pCPolicies = new Hashtable<String, PlayerPolicies>();
		pCPolicies.put(pA, pCA);
		pCPolicies.put(pB, pCB);
		pCPolicies.put(pD, pCD);
		pCPolicies.put(pE, pCE);
		
		Map<String, PlayerPolicies> pDPolicies = new Hashtable<String, PlayerPolicies>();
		pDPolicies.put(pA, pDA);
		pDPolicies.put(pB, pDB);
		pDPolicies.put(pC, pDC);
		pDPolicies.put(pE, pDE);
		
		Map<String, PlayerPolicies> pEPolicies = new Hashtable<String, PlayerPolicies>();
		pEPolicies.put(pA, pEA);
		pEPolicies.put(pB, pEB);
		pEPolicies.put(pC, pEC);
		pEPolicies.put(pD, pED);
		
		policies.put(pA, pAPolicies);
		policies.put(pB, pBPolicies);
		policies.put(pC, pCPolicies);
		policies.put(pD, pDPolicies);
		policies.put(pE, pEPolicies);
		
		initiators.clear(); initiators.push(pA);
		Map<String, Map<String, Boolean>> conflictsInitA = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		printResult(conflictsInitA);
		
		initiators.clear(); initiators.push(pB);
		Map<String, Map<String, Boolean>> conflictsInitB = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		System.out.println("InitA "+(equals(conflictsInitA, conflictsInitB) ? "==" : "!=")+" InitB");
		
		initiators.clear(); initiators.push(pC);
		Map<String, Map<String, Boolean>> conflictsInitC = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		System.out.println("InitA "+(equals(conflictsInitA, conflictsInitC) ? "==" : "!=")+" InitC");
		
		initiators.clear(); initiators.push(pD);
		Map<String, Map<String, Boolean>> conflictsInitD = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		System.out.println("InitA "+(equals(conflictsInitA, conflictsInitD) ? "==" : "!=")+" InitD");
		
		initiators.clear(); initiators.push(pA); initiators.push(pD);
		Map<String, Map<String, Boolean>> conflictsInitAD = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		printResult(conflictsInitAD);
		
		initiators.clear(); initiators.push(pB); initiators.push(pD);
		Map<String, Map<String, Boolean>> conflictsInitBD = conflictDiplomacyResolution(initiators, playersKeySet, celestialBodyOwnerName, policies);
		System.out.println("InitAD "+(equals(conflictsInitAD, conflictsInitBD) ? "==" : "!=")+" InitBD");
		
		System.out.println("A est vaincu, reste B, C, D, E.");
		playersKeySet.remove(pA);
		
		System.out.println("B est élu owner.");
		initiators.clear(); initiators.push(pB);
		Map<String, Map<String, Boolean>> reConflictsInitB = conflictDiplomacyResolution(initiators, playersKeySet, pB, policies);
		printResult(reConflictsInitB);
		
		System.out.println("C est élu owner.");
		initiators.clear(); initiators.push(pB);
		Map<String, Map<String, Boolean>> reConflictsInitB2 = conflictDiplomacyResolution(initiators, playersKeySet, pC, policies);
		printResult(reConflictsInitB2);
	}

	public static void printResult(Map<String, Map<String, Boolean>> conflicts)
	{
		for(String p : conflicts.keySet())
		{
			for(String t : conflicts.get(p).keySet())
			{
				System.out.format("%s / %s : %s\n", p, t, (conflicts.get(p).get(t) ? "Hostiles" : "Neutrals"));
			}
		}
	}
	
	public static boolean equals(Map<String, Map<String, Boolean>> c1, Map<String, Map<String, Boolean>> c2)
	{
		for(String p : c1.keySet())
		{
			if (!c2.containsKey(p)) return false;
			
			for(String t : c2.get(p).keySet())
			{
				if (!c2.get(p).containsKey(t)) return false;
				
				if (c1.get(p).get(t) != c2.get(p).get(t)) return false;
			}
		}
		
		return true;
	}
}
