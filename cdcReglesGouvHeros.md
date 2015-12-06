# Gouvernement & Héros #
## Gouvernement ##
Le gourvenement représente l'élite militaire de votre empire, celle par qui tout votre empire s'organise et tient.
C'est donc une entité particulière qu'il vous faut protéger et sans laquelle la partie est perdue.
En d'autres termes, le gouvernement représente le joueur.

Concrètement, le gouvernement peut prendre deux formes, une seule pouvant exister à la fois et en un seul exemplaire par joueur:
> ¤ Celle de la construction "Module gouvernemental", établi sur une planète à laquelle il apporte des bonus de production de 人 et 炭.
> ¤ Celle d'un "Vaisseau gouvernemental", qui n'apporte aucun bonus en stationnement, et qui perds toute communication lorsqu'il est en déplacement.

Le vaisseau gouvernemental ne peut être créé que sur la planète disposant du module gouvernemental.
Lorsque le vaisseau gouvernemental est créé, le module gouvernemental est démonté.
Le vaisseau gouvernemental peut être laissé en stationnement sur la planète, mais le joueur perds alors le bénéfice des bonus de productions que lui aurait octroyé le module gouvernemental.
Lorsqu'il arrive a destination, plusieurs cas de figure se présentent:
> ¤ La destination est une planète du joueur: Le vaisseau gouvernemental se pose et le joueur récupère la communication.
> ¤ La destination est une planète alliée: Le vaisseau gouvernemental se pose et retrouve ses capacités de communication.
> ¤ La destination est une planète neutre, dont le comportement est "neutre": Le vaisseau gouvernemental se pose pacifiquement et retrouve ses capacités de communication, mais il est susceptible de se faire attaquer a tout moment par son hôte.
> ¤ La destination est une planète sauvage, ou neutre dont le comportement est "ennemi": Le vaisseau gouernemental est attaqué, soit il est détruit, soit il survit et se pose sur la planète et récupère la communication.
Le gouvernement peut rétablir un module gouvernemental lorsqu'il est en stationnement sur une planète appartenant au joueur.

Durant son voyage, le gouvernement est coupé du reste de l'empire (communication impossible), le joueur perds donc l'actualisation des informations de son empire, et ne peut plus passer d'ordres, il n'a pas non plus de rapport sur les éventuelles attaques subies par ses planètes ou flottes.
Le joueur perds aussi le chat multijoueur.
Dès que son gouvernement arrive à s'établir sur un corps céleste, il retrouve les informations à jour et le log des informations passées.
Bien qu'il soit le centre de décision, le vaisseau gouvernemental ne peut pas changer de direction durant un trajet car comme toute autre flotte il voyage en hyper-space.

## Héros ##
Les héros sont des chefs millitaires exceptionnels, sous le commandemant desquels toute flotte arrive à accroitre son potentiel.
Les héros n'ont donc pas de vaisseaux propres, ils doivent être attaché à une flotte ou demeurer sur la planète à défendre, ils peuvent être dissocié et réaffecter à une nouvelle flotte comme n'importe quel vaisseau lorsqu'ils sont sur une planète.
Ils gagnent de l'expérience grace à laquelle les bonus qu'ils octroient augmentent.
Chaque joueur ne peut avoir qu'un seul héro, mais si celui-ci meurs, un nouveau peut être créé (avec 0XP).

Le héro reçoit l'expérience des combats qu'il mène, celle-ci ne prend en compte que la puissance brute de la flotte abattue :



&lt;math&gt;

Experience~reçue = Puissance\_Brute(Ennemi) = Ennemi\_Défense **Ennemi\_Attaque** (1+HéroEnnemiEventuel\_Bonus)

&lt;/math&gt;



Le héro applique son bonus à l'attaque de chaque sous flotte de la façon suivante :



&lt;math&gt;

SousFlotte\_Attaque = (1 + Héro\_Bonus) **sum{pour chaque vaisseau V}{dernier vaisseau}{(V\_Attaque** V\_Défense) / SousFlotte\_Défense}

&lt;/math&gt;



### Réglage de la courbe d'XP ###


&lt;math&gt;

Héro\_XP = échelle **((K<sup>(Héro_Bonus)) - K) / K</sup>BonusMax

&lt;/math&gt;


K = 2 semble avoir une bonne alure.
echelle = 5120.10<sup>3 = echelle nécéssaire dosé en fonction de l'XP souhaitée pour avoir un Bonus de 2. Pour 10K d'XP, il faut une échelle de 5120.10</sup>3.
BonusMax = 10 = Valeur de Bonus à partir de laquelle la progression en XP nécéssaire est jugée "impossible", une fois ce bonus atteind, le Héro continue de progresser mais à une vitesse très faible.**

Soit:


&lt;math&gt;

Héro\_Bonus = {ln( ( (Héro\_XP 