# Combats #
Les combats peuvent opposer plusieurs factions aux intérêts divergeants.
Chaque faction sait lorsque la bataille s'engage qui sont ses ennemis.
Suivant que le combat engage une flotte en territoire étranger ou bien un corps céleste en position de défenseur on se réfèrera respectivement au tableau de politique de conquète ou à celui de politique intérieure.
Ces deux tableaux définissent à l'égard de chaque joueur non allié l'attitude à adopter : neutre ou hostile.
Ainsi, on est capable de déterminer dans le chaos de la bataille quelles sont les factions hostiles entre elles et quelles sont les factions susceptible de stopper le combat ensemble.
C'est une résolution du combat apellée "multipartite" qui permet de résoudre en une seule fois le combat en ne laissant au final que des factions non hostiles entre elles.

## Flotte équivalente ##
La flotte équivalente est le regroupement de toutes les forces disponibles sur la planète en une même flotte, celà comprend :

Les flottes en stationnement, les vaisseaux en attente dans les usines,  les module de défense et enfin les héros éventuellement présents.
Chacun de ces éléments dispose des caractéristiques (Attaque, Défense, ...)
La flotte équivalente est le regroupement des flottes moyenne de chaque classe.

### Caractéristiques des flottes équivalente de classe ###
Chaque flotte équivalente de classe se calcule suivant les formules ci-dessous:



&lt;math size=10&gt;

FlotteEquivalente\_Défense = sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{V\_Défense}

&lt;/math&gt;




&lt;math size=10&gt;

FlotteEquivalente\_Attaque = (1 + HéroEventuel\_Bonus) **sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{(V\_Attaque** V\_Défense) / FlotteEquivalente\_Défense}

&lt;/math&gt;




&lt;math size=10&gt;

FlotteEquivalente\_Arme = sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{(V\_Arme **V\_Défense) / FlotteEquivalente\_Défense}

&lt;/math&gt;**

&lt;math size=10&gt;

FlotteEquivalente\_Armure = sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{(V\_Armure **V\_Défense) / FlotteEquivalente\_Défense}

&lt;/math&gt;**

### Puissance brute ###
On apelle "puissance brute" la puissance qui ne tient pas compte des spécialisations de classes (des arme et armure).
Deux adversaires de même classe opposent leurs puissance brutes, puisqu'il n'ont aucuns avantages particulier liés à leurs classes.

On peut calculer la puissance brute d'une flotte avec la formule suivante:


&lt;math&gt;

Puissance\_Brute = Flotte\_Défense **Flotte\_Attaque** (1+HéroEventuel\_Bonus)

&lt;/math&gt;



Dans un combat opposant deux adversaires de même classe, (avant les règles de rachats) on a:


&lt;math&gt;

Puissance\_Brute(Survivant) = Puissance\_Brute(PlusFort) - Puissance\_Brute(PlusFaible)

&lt;/math&gt;



## Déroulement d'un combat ##
On détermine les sous flottes équivalentes de classes de chaque flotte combatante FA, FB, pour un total de 6 flottes maximum:
{ChasseursA, DestroyeursA, ArtillerieA} contre {ChasseursB, DestroyersB, ArtillerieB}
Toutes ne sont pas forcément représentée au départ, et ne le sont pas forcément à chaque tour de combat (elles peuvent avoir été détruites).

A chaque tour de combat on détermine donc pour chaque sous flotte la cible la plus interessante encore en jeu, dans l'ordre suivant:
Pour un attaquant Chasseur: Artillerie - Chasseurs - Destroyers
Pour un attaquant Destroyer: Chasseurs - Destroyers - Artillerie
Pour un attaquant Artillerie: Destroyers - Artillerie - Chasseurs
Et on note pour chaque cible le total des intention d'attaques.

Une fois toutes les sous flottes ayant déclarées leur cible, on détermine laquelle va être détruite en premier, et en combien de temps.
Ainsi, chacune n'encaisse que les dégats correspondant à ce laps de temps pour ce tour de jeu, qui se termine forcément sur la mort d'une sous flotte.

Si les deux opposants ont encore des sous flottes en vie, un nouveau tour s'engage.
Lorsque toutes les sous flottes d'une flotte sont détruites, on détermine combien il reste de Défense à chacune des sous flottes du vainqueur, et on recalcule les effectifs des vaisseaux qui peuvent encore les composer.

Lorsqu'on essaie de déterminer les vaisseaux que l'on peut "rachetter" avec la défense restante de chaque sous flotte, et que celle-ci ne tombe pas juste, on applique la règle des 50% aux vaisseaux restants les moins cher :
> ¤ Si la flotte était en position sur son corps céleste, le vaisseau le moins cher est automatiquement rachetté.
> ¤ Si la flotte viens de conquérir un corps céleste ennemi, le vaisseau le moins cher n'est sauvé que si les reste dépassent 50% de son prix de rachat.

### Combats multipartites ###
Avec les flottes pacifiques en stationnement sur un corps céleste, suivant les comportements définis il peut arriver que les puissances en combat soient composées de flottes de plusieurs joueurs à la fois. Le combat est cependant toujours résolu comme s'il s'agissait de 2 ennemis, et ce n'est qu'au moment des rachats des flottes qu'on redivise la flotte survivante en plusieurs flottes survivantes.

### Contraintes ###
L'algorithme de déroulement des combats peut être amené à changer, mais il doit respecter les contraintes suivantes :
> ¤ Un système juste, qui n'avantage pas une combinaison particulière de vaisseaux (sans connaissance de l'ennemi).
> ¤ Pour le combat, une flotte a toujours intéret à s'agrandir, même avec le plus faible des vaisseaux.
> ¤ Il ne doit pas y avoir d'avantage à attaquer en plusieurs vagues (ça devrait même être un léger désavantage).