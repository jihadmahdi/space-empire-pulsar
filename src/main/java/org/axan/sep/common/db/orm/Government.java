package org.axan.sep.common.db.orm;

import java.security.acl.Owner;

import org.axan.eplib.orm.nosql.AVersionedGraphRelationship;
import org.axan.sep.common.Protocol.eBuildingType;
import org.axan.sep.common.db.orm.SEPCommonDB.eRelationTypes;
import org.neo4j.graphdb.Relationship;

public class Government extends AVersionedGraphRelationship<SEPCommonDB>
{
	private final String ownerName;
	private final String productiveCelestialBodyName;
	private final String unitName;
	
	/**
	 * Off-DB constructor. GovernmentModule.	
	 * @param ownerName
	 * @param productiveCelestialBodyName
	 */
	public Government(String ownerName, String productiveCelestialBodyName)
	{
		super(eRelationTypes.PlayerGovernment, "PlayerIndex", Player.getPK(ownerName), "BuildingIndex", Building.getPK(productiveCelestialBodyName, eBuildingType.GovernmentModule));
		this.ownerName = ownerName;
		this.productiveCelestialBodyName = productiveCelestialBodyName;
		this.unitName = null;
	}
	
	/**
	 * On-DB constructor.
	 * @param sepDB
	 * @param ownerName
	 * @param productiveCelestialBodyName
	 */
	public Government(SEPCommonDB sepDB, String ownerName, String productiveCelestialBodyName)
	{
		super(sepDB, eRelationTypes.PlayerGovernment, "PlayerIndex", Player.getPK(ownerName), "BuildingIndex", Building.getPK(productiveCelestialBodyName, eBuildingType.GovernmentModule));
		this.ownerName = ownerName;
		this.productiveCelestialBodyName = productiveCelestialBodyName;
		this.unitName = null;
	}
	
	@Override
	final protected void checkForDBUpdate()
	{
		super.checkForDBUpdate();
	}
	
	@Override
	protected void initializeProperties()
	{
		super.initializeProperties();
		properties.setProperty("ownerName", ownerName);
		if (productiveCelestialBodyName != null) properties.setProperty("productiveCelestialBodyName", productiveCelestialBodyName);
		if (unitName != null) properties.setProperty("unitName", unitName);
	}
	
	@Override
	final protected void register(Relationship properties)
	{
		super.register(properties);
	}
}
