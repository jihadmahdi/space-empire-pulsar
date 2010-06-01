package org.axan.sep.server.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.axan.sep.common.FleetBattleSkillsModifierAdaptor;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.eStarshipSpecializationClass;
import org.axan.sep.common.SEPUtils.RealLocation;
import org.axan.sep.server.SEPServer;



class Fleet extends Unit implements Serializable
{
	public static class Key extends Unit.Key implements Serializable
	{		
		private static final long	serialVersionUID	= 1L;

		public Key(String name, String ownerName)
		{
			super(name, ownerName);
		}
	}
	
	private static final long										serialVersionUID		= 1L;

	private static final Random		rnd = new Random();
	
	private final boolean isUnassigned;
	
	private Boolean modified = true;
	
	// Variables
	private final Map<eStarshipSpecializationClass, SpecializedEquivalentFleet> specializedEquivalentFleets = new HashMap<eStarshipSpecializationClass, SpecializedEquivalentFleet>();
	private final HashSet<ISpecialUnit> specialUnits = new HashSet<ISpecialUnit>(); 
	private final Stack<org.axan.sep.common.Fleet.Move> checkpoints;
	private org.axan.sep.common.Fleet.Move currentMove;

	// Views
	PlayerDatedView<HashMap<org.axan.sep.common.StarshipTemplate, Integer>>	playersStarshipsView	= new PlayerDatedView<HashMap<org.axan.sep.common.StarshipTemplate, Integer>>();
	PlayerDatedView<HashSet<org.axan.sep.common.ISpecialUnit>> playersSpecialUnitsView = new PlayerDatedView<HashSet<org.axan.sep.common.ISpecialUnit>>();

	public Fleet(DataBase db, String name, String ownerName, RealLocation sourceLocation, Map<org.axan.sep.common.StarshipTemplate, Integer> starships, Set<ISpecialUnit> specialUnits, boolean isUnassigned)
	{
		this(db, new Key(name, ownerName), sourceLocation, starships, specialUnits, isUnassigned);		
	}
	
	public Fleet(DataBase db, Key key, RealLocation sourceLocation, Map<org.axan.sep.common.StarshipTemplate, Integer> starships, Set<ISpecialUnit> specialUnits, boolean isUnassigned)
	{
		super(db, key, sourceLocation);		
		merge(starships, specialUnits);
		this.checkpoints = new Stack<org.axan.sep.common.Fleet.Move>();
		this.isUnassigned = isUnassigned;
	}	

	@Override
	public org.axan.sep.common.Fleet getPlayerView(int date, String playerLogin, boolean isVisible)
	{
		if (isVisible)
		{
			// Updates
			playersStarshipsView.updateView(playerLogin, getStarshipsHashMap(), date);
			
			HashSet<org.axan.sep.common.ISpecialUnit> specialUnitsViews = new HashSet<org.axan.sep.common.ISpecialUnit>();
			for(ISpecialUnit specialUnit : specialUnits)
			{
				if (specialUnit.isVisibleToClients())
				{
					specialUnitsViews.add(specialUnit.getPlayerView(date, playerLogin, isVisible));
				}
			}
			playersSpecialUnitsView.updateView(playerLogin, specialUnitsViews, date);
			
		}				
		
		return new org.axan.sep.common.Fleet(isVisible, getLastObservation(date, playerLogin, isVisible), getName(), getOwnerName(), getSourceLocationView(playerLogin), getDestinationLocationView(playerLogin), getCurrentLocationView(date, playerLogin, isVisible), getTravellingProgressView(playerLogin), getSpeedView(date, playerLogin, isVisible), playersStarshipsView.getLastValue(playerLogin, null), playersSpecialUnitsView.getLastValue(playerLogin, null), (playerLogin.equals(getOwnerName()) ? currentMove : null) ,(playerLogin.equals(getOwnerName())?checkpoints:null), isUnassigned);
	}

	public boolean isGovernmentFleet()
	{
		for(ISpecialUnit specialUnit : specialUnits)
		{
			if (GovernmentStarship.class.isInstance(specialUnit))
			{
				return true;
			}
		}

		return false;
	}
	
	public void removeGovernment()
	{
		synchronized(modified)
		{
			for(ISpecialUnit specialUnit : specialUnits)
			{
				if (GovernmentStarship.class.isInstance(specialUnit))
				{
					specialUnits.remove(specialUnit);
					return;
				}
			}
			
			modified = true;
		}
	}
	
	public boolean isUnassignedFleet()
	{
		return isUnassigned;
	}
	
	private HashMap<org.axan.sep.common.StarshipTemplate, Integer> getStarshipsHashMap()
	{
		HashMap<StarshipTemplate, Integer> result = new HashMap<StarshipTemplate, Integer>();		
		for(SpecializedEquivalentFleet equivalentFleet : specializedEquivalentFleets.values())
		{
			for(StarshipTemplate starshipTemplate : equivalentFleet.starshipTemplateSet())
			{
				if (!result.containsKey(starshipTemplate)) result.put(starshipTemplate, 0);
				result.put(starshipTemplate, result.get(starshipTemplate) + equivalentFleet.getStarshipCount(starshipTemplate));
			}			
		}
		
		return result;
	}
	
	public Map<org.axan.sep.common.StarshipTemplate, Integer> getStarships()
	{
		return Collections.unmodifiableMap(getStarshipsHashMap());
	}
	
	public ISpecialUnit getSpecialUnit(String specialUnitName)
	{
		for(ISpecialUnit specialUnit : specialUnits)
		{
			if (specialUnit.getName().equals(specialUnitName)) return specialUnit;
		}
		return null;
	}
	
	public Set<ISpecialUnit> getSpecialUnits()
	{
		return Collections.unmodifiableSet(specialUnits);
	}
	
	public boolean hasNoMoreStarships()
	{
		for(SpecializedEquivalentFleet equivalentFleet : specializedEquivalentFleets.values())
		{
			if (!equivalentFleet.isEmpty()) return false;			
		}
		
		return true;
	}
	
	public boolean isDestroyed()
	{
		for(SpecializedEquivalentFleet equivalentFleet : specializedEquivalentFleets.values())
		{
			if (equivalentFleet.getDefense() > 0) return false;
		}
		return true;
	}
	
	public void rest()
	{
		for(SpecializedEquivalentFleet subFleet : specializedEquivalentFleets.values())
		{
			subFleet.rest();
		}
	}
	
	private void merge(Fleet fleet)
	{
		synchronized(modified)
		{
			for(SpecializedEquivalentFleet subFleet : fleet.specializedEquivalentFleets.values())
			{
				if (!specializedEquivalentFleets.containsKey(subFleet.specializedClass)) specializedEquivalentFleets.put(subFleet.specializedClass, new SpecializedEquivalentFleet(subFleet.specializedClass, this));
				specializedEquivalentFleets.get(subFleet.specializedClass).merge(subFleet);
			}
			
			modified = true;
		}
	}

	public void merge(Map<org.axan.sep.common.StarshipTemplate, Integer> starshipsToMerge, Set<ISpecialUnit> specialUnitsToMerge)
	{		
		if (starshipsToMerge != null) for(Map.Entry<org.axan.sep.common.StarshipTemplate, Integer> e : starshipsToMerge.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (!specializedEquivalentFleets.containsKey(e.getKey().getSpecializationClass())) specializedEquivalentFleets.put(e.getKey().getSpecializationClass(), new SpecializedEquivalentFleet(e.getKey().getSpecializationClass(), this));
			specializedEquivalentFleets.get(e.getKey().getSpecializationClass()).merge(e.getKey(), e.getValue());								
		}
	
		synchronized(modified)
		{
			// TODO: Check if it's ok.
			if (specialUnitsToMerge != null) for(ISpecialUnit u : specialUnitsToMerge)
			{
				if (u == null) continue;
				
				if (!specialUnits.contains(u))
				{
					specialUnits.add(u);
				}
			}
			
			modified = true;
		}
	}

	public void remove(Map<org.axan.sep.common.StarshipTemplate, Integer> fleetToForm, Set<ISpecialUnit> specialUnitsToForm)
	{
		if (fleetToForm != null) for(Map.Entry<org.axan.sep.common.StarshipTemplate, Integer> e : fleetToForm.entrySet())
		{
			if (e.getValue() == null || e.getValue() <= 0) continue;
			
			if (!specializedEquivalentFleets.containsKey(e.getKey().getSpecializationClass())) throw new Error("Fleet remove error (does not contain any starship specialization class '"+e.getKey().getSpecializationClass()+"'");
			specializedEquivalentFleets.get(e.getKey().getSpecializationClass()).remove(e.getKey(), e.getValue());							
		}
		
		synchronized(modified)
		{
			// TODO: Check if it's ok.
			if (specialUnitsToForm != null) for(ISpecialUnit u : specialUnitsToForm)
			{
				if (u == null) continue;
				
				specialUnits.remove(u);
			}
			
			modified = true;
		}
	}
	
	public void updateMoveOrder(Stack<org.axan.sep.common.Fleet.Move> newCheckpoints)
	{
		checkpoints.removeAllElements();
		
		if (!isMoving()) currentMove = null;
		
		for(org.axan.sep.common.Fleet.Move checkpoint : newCheckpoints)
		{
			if (checkpoints.size() > 0 && checkpoints.peek().getDestinationName().equals(checkpoint.getDestinationName()))
			{
				checkpoints.pop();
			}
			
			checkpoints.push(checkpoint);
		}
	}
	
	@Override
	public boolean startMove()
	{		
		if ((currentMove == null || currentMove.getDestinationLocation().equals(getRealLocation())) && checkpoints.size() > 0)
		{
			if (super.startMove())
			{
				currentMove = checkpoints.firstElement();
				checkpoints.removeElementAt(0);												
			}
		}
		
		if (currentMove != null)
		{
			if (currentMove.getDelay() == 0)
			{
				setDestinationLocation(db.getCelestialBody(currentMove.getDestinationName()).getLocation().asRealLocation());
				return true;
			}
			else
			{
				currentMove = currentMove.getDecreaseDelayMove();
			}
		}
		
		return false;
	}
	
	@Override
	public void endMove()
	{	
		ProductiveCelestialBody productiveCelestialBody = db.getCelestialBody(getRealLocation().asLocation(), ProductiveCelestialBody.class);
		if (productiveCelestialBody == null) throw new SEPServer.SEPImplementationException("Cannot set conflict on location '"+getRealLocation()+"', no ProductiveCelestialBody found there.");
		
		if (currentMove.isAnAttack())
		{				
			productiveCelestialBody.addConflictInititor(getOwnerName());
		}		
		
		currentMove = null; // Needed for the next startMove call.
		super.endMove();
	}
	
	@Override
	public double getSpeed()
	{
		// TODO, compute total speed from fleet composition
		return 3;
	}
	
	@Override
	public Key getKey()
	{
		return Key.class.cast(super.getKey());
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getKey().toString());
		for(SpecializedEquivalentFleet subFleet : specializedEquivalentFleets.values())
		{
			sb.append('\n');
			sb.append(subFleet.toString());
		}
		
		return sb.toString();
	}
	
	public static class SpecializedEquivalentFleet implements Serializable
	{		
		private static final long	serialVersionUID	= 1L;
		
		private final Map<StarshipTemplate, Integer> starships = new HashMap<StarshipTemplate, Integer>();
		private final eStarshipSpecializationClass specializedClass;
		
		private Boolean modified = true;
		private long totalStarshipsCount;
		private long attack;
		private long defense;
		private double attackSpecializationBonus;
		private double defenseSpecializationBonus;
		private double damage = 0;
		
		private final Fleet parentFleet;
		
		public SpecializedEquivalentFleet(eStarshipSpecializationClass specializedClass, Fleet parentFleet)
		{
			this.specializedClass = specializedClass;
			this.parentFleet = parentFleet;
		}
		
		public eStarshipSpecializationClass getSpecialization()
		{
			return specializedClass;
		}		
		
		/// < Cached caracs
		
		private void refreshCaracs()
		{
			synchronized(modified)
			{
				if (modified)
				{
					totalStarshipsCount = 0;
					attack = 0;
					defense = 0;
					double attackSpecializationBonusSum = 0;
					double defenseSpecializationBonusSum = 0;
					
					for(StarshipTemplate starshipTemplate : starships.keySet())
					{
						int nb = starships.get(starshipTemplate);
						totalStarshipsCount += nb;
						attack += starshipTemplate.attack * nb;
						defense += starshipTemplate.defense * nb;
						attackSpecializationBonusSum += starshipTemplate.attackSpecializationBonus * nb;
						defenseSpecializationBonusSum += starshipTemplate.defenseSpecializationBonus * nb;
					}
					
					attackSpecializationBonus = attackSpecializationBonusSum / totalStarshipsCount;
					defenseSpecializationBonus = defenseSpecializationBonusSum / totalStarshipsCount;
					
					modified = false;
				}
			}
		}
		
		public double getAttack()
		{
			refreshCaracs();
			return (attack * ((double) (defense - damage) / defense)) + parentFleet.getBattleSkillsModifier().getSpcializedFixedAttackBonus(specializedClass);
		}
		
		public double getDefense()
		{
			refreshCaracs();
			return (double) defense - damage;
		}
		
		public double getAttackSpecializationBonus()
		{
			refreshCaracs(); // Is this formula fair ?
			return attackSpecializationBonus * ((double) (defense - damage) / defense);
		}
		
		public double getDefenseSpecializationBonus()
		{
			refreshCaracs();
			return defenseSpecializationBonus * ((double) (defense - damage) / defense);
		}
		
		/// < Cached caracs />
		
		public Set<StarshipTemplate> starshipTemplateSet()
		{
			return starships.keySet();
		}
		
		public int getStarshipCount(StarshipTemplate starshipTemplate)
		{
			return starships.containsKey(starshipTemplate) ? starships.get(starshipTemplate) : 0;
		}
		
		public boolean isEmpty()
		{
			for(Integer i : starships.values())
			{
				if (i != null && i > 0) return false;
			}
			
			return true;
		}
		
		public void merge(SpecializedEquivalentFleet specializedFleet)
		{
			boolean isEmpty = true;
			for(StarshipTemplate starshipTemplate : specializedFleet.starships.keySet())
			{
				int nb = specializedFleet.starships.get(starshipTemplate);
				merge(starshipTemplate, nb);
				isEmpty &= (nb == 0);
			}
			
			if (!isEmpty) damage += specializedFleet.damage;
		}
		
		public void merge(StarshipTemplate starshipTemplate, int nb)
		{
			if (!specializedClass.equals(starshipTemplate.getSpecializationClass())) throw new Error("Fleet merge error: incorrect starship specialization class '"+starshipTemplate.getName()+"' ('"+starshipTemplate.getSpecializationClass()+"' != '"+specializedClass+"')");
			if (nb < 0) throw new Error("Fleet merge error: nb cannot be negative ("+nb+").");
			
			if (!starships.containsKey(starshipTemplate)) starships.put(starshipTemplate, 0);
			starships.put(starshipTemplate, starships.get(starshipTemplate) + nb);						
			
			modified = true;
		}
		
		public void remove(StarshipTemplate starshipTemplate, int nb)
		{
			if (!specializedClass.equals(starshipTemplate.getSpecializationClass())) throw new Error("Fleet remove error (incorrect starship specialization class '"+starshipTemplate.getName()+"' ('"+starshipTemplate.getSpecializationClass()+"' != '"+specializedClass+"')");
			if (!starships.containsKey(starshipTemplate)) throw new Error("Fleet remove error (does not contain starship type '"+starshipTemplate.getName()+"'");
			if (starships.get(starshipTemplate) < nb) throw new Error("Fleet remove error (does not contain enough starships of type '"+starshipTemplate.getName()+"'");
			
			starships.put(starshipTemplate, starships.get(starshipTemplate) - nb);
			
			modified = true;
		}
		
		public void takeDamage(double damage)
		{
			if (damage < 0) throw new Error("Cannot take negative damage.");
			if ((this.damage + damage) > defense)
			{
				damage = Double.valueOf(String.format((Locale) null, "%.5f", damage));
			}
			if ((this.damage + damage) > defense) throw new Error("Cannot take more damage than defense.");
			
			this.damage += damage;
		}
		
		public void rest()
		{			
			Set<StarshipTemplate> destroyables = new HashSet<StarshipTemplate>(starships.keySet());
			
			while(damage > 0 && !destroyables.isEmpty())
			{
				int i = rnd.nextInt(destroyables.size());
				StarshipTemplate starshipTemplate = destroyables.toArray(new StarshipTemplate[destroyables.size()])[i];
				double maxDmg = Math.min(damage, starshipTemplate.defense * starships.get(starshipTemplate));
				
				if (maxDmg < starshipTemplate.defense)
				{
					destroyables.remove(starshipTemplate);
					continue;
				}
				
				double dmg = (destroyables.size() == 1) ? maxDmg : Math.min(Math.max(rnd.nextDouble(), (double) starshipTemplate.defense / maxDmg), 1) * maxDmg;
								
				int destroyedUnits = (int) dmg / starshipTemplate.defense;
				dmg = destroyedUnits * starshipTemplate.defense;
				
				if (dmg < 0) throw new Error("Damage cannot be negative.");								
				
				starships.put(starshipTemplate, starships.get(starshipTemplate) - destroyedUnits);				
				damage -= dmg;
				
				if (starships.get(starshipTemplate) == 0)
				{
					destroyables.remove(starshipTemplate);
					continue;
				}
			}
			
			// FALSE: if (damage != 0) throw new Error("Damage taken are suppose to be entirely supported (damage cannot be greater than global defense).");

			modified = true;
		}
		
		@Override
		public String toString()
		{
			return this.getSpecialization()+": Attack("+this.getAttack()+"), AttackSpeBonus("+this.getAttackSpecializationBonus()+"), Defense("+this.getDefense()+"), DefenseSpeBonus("+this.getDefenseSpecializationBonus()+"), Damage("+this.damage+")";
		}
	}

	private Fleet(String defender, Set<Fleet> mergers)
	{
		this(null, "Merged attackers agains "+defender, null, null, null, null, false);
		
		for(Fleet merger : mergers)
		{
			merge(merger);
		}
		
		// Must reduce all modifier to fixed bonus ?
		// i.e. fixedDefenseBonus += startingFleet.defense * battleSkillsModifier.defenseBonusRate
		/*
		int fixedAttackBonus = 0;
		for(Fleet merger : mergers)
		{
			fixedAttackBonus += merger.getBattleSkillsModifier().getFixedAttackBonus();
			merge(merger);
		}
		
		final int bsmFixedAttackBonus = fixedAttackBonus;
		
		battleSkillsModifier = new IFleetBattleSkillsModifier()
		{
			
			@Override
			public int getFixedAttackBonus()
			{
				return bsmFixedAttackBonus;
			}
		};
		*/
	}
	
	public static Fleet computeMergedAttackers(String defender, Map<String, Map<String, Boolean>> conflictDiplomacy, Map<String, Fleet> survivors)
	{
		Set<Fleet> mergers = new HashSet<Fleet>();
		
		for(Map.Entry<String, Boolean> e : conflictDiplomacy.get(defender).entrySet())
		{
			if (e.getValue() && survivors.containsKey(e.getKey()))
			{
				Fleet attacker = survivors.get(e.getKey());
				mergers.add(attacker);
			}
		}
		
		return new Fleet(defender, mergers);
	}

	public SpecializedEquivalentFleet getSpecializedFleet(eStarshipSpecializationClass specialization)
	{
		return specializedEquivalentFleets.get(specialization);
	}
	
	public SpecializedEquivalentFleet getNextTarget(eStarshipSpecializationClass attackerSpecialization)
	{
		if (specializedEquivalentFleets.containsKey(attackerSpecialization.getTdT()) && !specializedEquivalentFleets.get(attackerSpecialization.getTdT()).isEmpty())
			return specializedEquivalentFleets.get(attackerSpecialization.getTdT());
		else if (specializedEquivalentFleets.containsKey(attackerSpecialization) && !specializedEquivalentFleets.get(attackerSpecialization).isEmpty())
			return specializedEquivalentFleets.get(attackerSpecialization);
		else if (specializedEquivalentFleets.containsKey(attackerSpecialization.getBN()) && !specializedEquivalentFleets.get(attackerSpecialization.getBN()).isEmpty())
			return specializedEquivalentFleets.get(attackerSpecialization.getBN());
		else
			return null;
	}
	
	private static class FleetBattleSkillsModifier extends FleetBattleSkillsModifierAdaptor implements Serializable
	{
		private static final long	serialVersionUID	= 1L;
		
		private int fixedAttackBonus = 0;
		private final Fleet fleet;
		
		public FleetBattleSkillsModifier(Fleet fleet)
		{
			this.fleet = fleet;
		}
		
		private void refresh()
		{
			synchronized(fleet.modified)
			{
				if (fleet.modified)
				{
					for(ISpecialUnit specialUnit : fleet.specialUnits)
					{
						if (IFleetBattleSkillsModifier.class.isInstance(specialUnit))
						{
							IFleetBattleSkillsModifier battleSkillModifier = IFleetBattleSkillsModifier.class.cast(specialUnit);
							
							fixedAttackBonus += battleSkillModifier.getFixedAttackBonus();
						}
					}
				}
				
				fleet.modified = false;
			}
		}
		
		@Override
		public int getFixedAttackBonus()
		{
			refresh();
			return fixedAttackBonus;
		}
	}
	
	private IFleetBattleSkillsModifier battleSkillsModifier = null;
	
	public IFleetBattleSkillsModifier getBattleSkillsModifier()
	{
		if (battleSkillsModifier == null)
		{
			battleSkillsModifier = new FleetBattleSkillsModifier(this);
		}
		
		return battleSkillsModifier;
	}
	
}
