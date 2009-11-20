package org.axan.sep.server.model;

import org.axan.sep.common.eStarshipSpecializationClass;

public interface IFleetBattleSkillsModifier
{
	/**
	 * Attack bonus applied to the whole fleet, neutral attack.
	 * @return
	 */
	int getFixedAttackBonus();
	
	/**
	 * Attack bonus applied to a specialized sub-fleet.
	 * @param specialization
	 * @return
	 */
	int getSpcializedFixedAttackBonus(eStarshipSpecializationClass specialization);
}
