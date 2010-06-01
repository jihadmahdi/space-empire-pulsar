package org.axan.sep.common;

import org.axan.sep.server.model.IFleetBattleSkillsModifier;

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
