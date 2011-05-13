PRAGMA foreign_keys=1;

DROP TABLE IF EXISTS GovSt;
DROP TABLE IF EXISTS Fleet;
DROP TABLE IF EXISTS Entity;

CREATE TABLE Entity (
	id INT NOT NULL,
	turn INT NOT NULL,
	PRIMARY KEY (id, turn)
);

CREATE TABLE Fleet (
	id INT NOT NULL,
	player TEXT NOT NULL,
	fleet TEXT NOT NULL,
	turn INT NOT NULL,
	PRIMARY KEY (player, fleet, turn),
	FOREIGN KEY (id, turn) REFERENCES Entity (id, turn)
);

CREATE TABLE GovSt (
	id INT NOT NULL,
	player TEXT NOT NULL,
	fleet TEXT,
	turn INT NOT NULL,
	PRIMARY KEY (player, turn),
	FOREIGN KEY (id, turn) REFERENCES Entity (id, turn)
	-- CHECK ( fleet ISNULL OR EXISTS ( SELECT turn FROM Fleet F WHERE F.player = player AND F.fleet = fleet ) )
);

DROP TRIGGER IF EXISTS GovStUpdate;
CREATE TRIGGER GovStUpdate
UPDATE ON GovSt
FOR EACH ROW WHEN
(
		NEW.fleet IS NOT NULL
	AND	NOT EXISTS ( SELECT turn FROM Fleet F WHERE F.player = NEW.player AND F.fleet = NEW.fleet )
)
BEGIN
	SELECT RAISE(FAIL,'fleet must be null or (player,fleet) must exists in Fleet');
END;

DROP TRIGGER IF EXISTS GovStInsert;
CREATE TRIGGER GovStInsert
INSERT ON GovSt
FOR EACH ROW WHEN
(
		NEW.fleet IS NOT NULL
	AND	NOT EXISTS ( SELECT turn FROM Fleet F WHERE F.player = NEW.player AND F.fleet = NEW.fleet )
)
BEGIN
	SELECT RAISE(FAIL,'fleet must be null or (player,fleet) must exists in Fleet');
END;