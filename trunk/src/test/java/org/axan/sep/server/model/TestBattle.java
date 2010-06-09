package org.axan.sep.server.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.axan.sep.common.SEPUtils;
import org.axan.sep.common.StarshipTemplate;
import org.axan.sep.common.eStarshipSpecializationClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestBattle
{

	@Before
	public void setUp() throws Exception
	{
		conflictDiplomacy.put(chalengerName, new HashMap<String, Boolean>());
		conflictDiplomacy.get(chalengerName).put(championName, true);
		conflictDiplomacy.put(championName, new HashMap<String, Boolean>());
		conflictDiplomacy.get(championName).put(chalengerName, true);
	}

	@After
	public void tearDown() throws Exception
	{
	}

	private final Map<String, Map<String, Boolean>>	conflictDiplomacy	= new HashMap<String, Map<String, Boolean>>();
	private final String							chalengerName		= "Chalenger", championName = "Champion";

	@Test
	public void testMain() throws FileNotFoundException
	{
		File dir = new File(".");

		for(int i = 0; i < 5; ++i)
		{
			runBattleTest((int) Math.pow(10, i), dir);
		}
	}

	private void runBattleTest(int scale, File dir) throws FileNotFoundException
	{
		PrintStream out;
		
		if (dir == null)
		{
			out = System.out;
		}
		else
		{
			out = new PrintStream(new File(dir, "testBattle_scale" + scale + ".csv"));
		}

		eStarshipSpecializationClass championClass = eStarshipSpecializationClass.FIGHTER;

		for(StarshipTemplate championSizeTemplate : SEPUtils.starshipSizeTemplates)
		{
			StarshipTemplate champion = new StarshipTemplate(championSizeTemplate.getName() + " " + championClass.toString(), championSizeTemplate.getDefense(), championSizeTemplate.getAttack(), championClass, championSizeTemplate.getAttackSpecializationBonus(), championSizeTemplate.getDefenseSpecializationBonus(), championSizeTemplate.getSpeed(), championSizeTemplate.getCarbonPrice(), championSizeTemplate.getPopulationPrice());
			out.println("\"Champion Size Template: " + championSizeTemplate.getName() + "\"\t\"" + championSizeTemplate.toString() + "\"");

			for(StarshipTemplate chalengerSizeTemplate : SEPUtils.starshipSizeTemplates)
			{
				out.print("\"Chalenger Size Template: " + chalengerSizeTemplate.getName() + "\"\t\"" + chalengerSizeTemplate.toString() + "\"");

				out.print("\n\""+scale+"xEgo VS TdT\"\t\"" + doBattle(scale, champion, chalengerSizeTemplate, championClass.getTdT()) + "\"");

				out.print("\n\""+scale+"xEgo VS Ego\"\t\"" + doBattle(scale, champion, chalengerSizeTemplate, championClass) + "\"");

				out.print("\n\""+scale+"xEgo VS BN\"\t\"" + doBattle(scale, champion, chalengerSizeTemplate, championClass.getBN()) + "\"");

				out.println();
			}

			out.println();
		}
		out.println();
	}

	private String doBattle(int scale, StarshipTemplate champion, StarshipTemplate chalengerSizeTemplate, eStarshipSpecializationClass chalengerClass)
	{
		StarshipTemplate chalenger = new StarshipTemplate(chalengerSizeTemplate.getName() + " " + chalengerClass.toString(), chalengerSizeTemplate.getDefense(), chalengerSizeTemplate.getAttack(), chalengerClass, chalengerSizeTemplate.getAttackSpecializationBonus(), chalengerSizeTemplate.getDefenseSpecializationBonus(), chalengerSizeTemplate.getSpeed(), chalengerSizeTemplate.getCarbonPrice(), chalengerSizeTemplate.getPopulationPrice());

		Map<String, Fleet> forces = new HashMap<String, Fleet>(), survivors;
		Map<StarshipTemplate, Integer> championStarships = new HashMap<StarshipTemplate, Integer>();
		Map<StarshipTemplate, Integer> chalengerStarships = new HashMap<StarshipTemplate, Integer>();

		int runs = 0, maxRuns = 100000, nb = scale;
		boolean championIsWeak = false;
		boolean winCutFound = false, looseCutFound = false;

		int maxUnits = Integer.MAX_VALUE / 10000;

		Stack<Integer> drawCuts = new Stack<Integer>();
		drawCuts.push(maxUnits);
		Stack<Integer> looseCuts = new Stack<Integer>();
		looseCuts.push(maxUnits);
		int winCut = 0, drawCutMax = 0;

		String strongerName = null;

		do
		{
			++runs;

			if (runs > maxRuns) fail("Too much loop (" + (championIsWeak ? "weak" : "strong") + " champion)");

			championStarships.clear();
			championStarships.put(champion, championIsWeak ? nb : scale);
			chalengerStarships.clear();
			chalengerStarships.put(chalenger, championIsWeak ? scale : nb);
			forces.clear();
			forces.put(championName, new Fleet(null, champion.getName(), championName, null, championStarships, null, false, null, null));
			forces.put(chalengerName, new Fleet(null, chalenger.getName(), chalengerName, null, chalengerStarships, null, false, null, null));

			try
			{
				survivors = GameBoard.resolveBattle(conflictDiplomacy, forces);
			}
			catch(Error e)
			{
				e.printStackTrace();
				fail("Error thrown on " + nb + "/" + maxUnits + " units.");
				return null;
			}

			assertTrue("Unexpected survivors count (" + survivors.size() + ")", survivors.size() <= 1);
			String survivor = survivors.size() == 0 ? null : survivors.keySet().iterator().next();

			if (strongerName == null)
			{
				championIsWeak = (survivor == null || !championName.equals(survivor));
				strongerName = championIsWeak ? chalengerName : championName;
			}

			if ((survivor != null) && strongerName.equals(survivor))
			{
				// Win
				winCut = nb;
			}
			else if (survivor != null)
			{
				// Loose
				looseCuts.push(nb);
			}
			else
			{
				// Draw
				drawCuts.push(nb);
				drawCutMax = Math.max(drawCutMax, nb);
			}

			int lowerFailure = Math.min(drawCuts.peek(), looseCuts.peek());
			if (winCut + 1 != lowerFailure)
			{
				// Win cut not found yet.
				nb = (lowerFailure - winCut) / 2 + winCut;
			}
			else if (looseCuts.peek() != lowerFailure)
			{
				winCutFound = true;

				// Win cut found, looking for looseCut.
				nb = (looseCuts.peek() - drawCutMax) / 2 + drawCutMax;

				if (drawCutMax + 1 == looseCuts.peek()) looseCutFound = true;

			}
			else
			{
				winCutFound = true;
				looseCutFound = true;
			}

			if (nb >= maxUnits)
			{
				nb = Integer.MAX_VALUE;
				break;
			}

		} while(!winCutFound || !looseCutFound);

		--maxUnits;
		
		if (!championIsWeak)
		{
			return "Win from 0 to " + printVal(winCut, maxUnits) + " chalenger(s), " + (drawCuts.peek() < looseCuts.peek() ? "Draw from " + printVal(winCut + 1, maxUnits) + " to " + printVal(looseCuts.peek() - 1, maxUnits) + " chalenger(s), " : "") + "Loose from " + printVal(looseCuts.peek(), maxUnits) + " chalenger(s).";
		}
		else
		{
			return "\"\t\"Loose from 0 to " + printVal(winCut, maxUnits) + " champion(s), " + (drawCuts.peek() < looseCuts.peek() ? "Draw from " + printVal(winCut + 1, maxUnits) + " to " + printVal(looseCuts.peek() - 1, maxUnits) + ", champion(s), " : "") + "Win from " + printVal(looseCuts.peek(), maxUnits) + " champion(s).";
		}
	}

	private static String printVal(int val, int max)
	{
		return(val >= max ? "+inf" : String.valueOf(val));
	}
}
