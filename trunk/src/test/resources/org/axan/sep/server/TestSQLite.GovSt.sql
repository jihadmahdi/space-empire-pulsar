SELECT IFNULL((LastGovSt.fleet='fl' AND (LastGovSt.turn >= GlobalFleet.lastTurn OR LastGovSt.turn >= GlobalFleet.nextToLastTurn)),0) AS isGovSt FROM
(SELECT IFNULL(MAX(turn),-1) AS turn, fleet FROM GovSt WHERE player='pl') LastGovSt,
(
(SELECT MAX(IFNULL((SELECT MAX(turn) FROM Fleet WHERE player='pl' AND fleet='fl' AND turn < (SELECT MAX(turn) FROM Fleet WHERE player='pl' AND fleet='fl')),0),IFNULL((SELECT MAX(turn) FROM GovSt WHERE player='pl' AND fleet='fl' AND turn < (SELECT MAX(turn) FROM GovSt WHERE player='pl' AND fleet='fl')),0)) AS lastTurn),
(SELECT MAX(IFNULL((SELECT MAX(turn) FROM Fleet WHERE player='pl' AND fleet='fl'),0),IFNULL((SELECT MAX(turn) FROM GovSt WHERE player='pl' AND fleet='fl'),0)) AS nextToLastTurn)
) GlobalFleet;