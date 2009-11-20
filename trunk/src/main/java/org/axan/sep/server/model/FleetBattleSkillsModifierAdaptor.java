package org.axan.sep.server.model;

import org.axan.sep.common.eStarshipSpecializationClass;

public class FleetBattleSkillsModifierAdaptor implements IFleetBattleSkillsModifier
{

	@Override
	public int getFixedAttackBonus()
	{
		return 0;
	}

	@Override
	public int getSpcializedFixedAttackBonus(eStarshipSpecializationClass specialization)
	{
		return 0;
	}

}
