# Déplacements #
## Rappel sur l'Univers ##
L'univers est si immense que malgré le niveau avancé de la technologie actuelle, toute communication est impossible a plus d'une Zone de distance.
Ainsi les voyages se font "à l'aveuglette", les trajets affichés ne sont que des prévisions, et seule les sondes sont capables de donner une information actualisé sur les Zones qu'elles observent.

## Départ et arrivé ##
Le départ d'une flotte est nécéssairement un corps céleste.
Et exception faite des Sondes, le point d'arrivé est également un corps céleste.
Le lancement d'une flotte se fait en sélectionnant le corps céleste sur lequel elle stationne et en lui assignant une destination et un ordre: "se poser pacifiquement" ou "attaquer".
A l'arrivée sur la destination, plusieurs cas de figure sont possibles :
> ¤ La destination est alliée, la flotte y stationne alors en renfort quelque soit l'ordre donné au départ.
> ¤ La destination est sauvage ou neutre, la flotte a reçue ordre de se poser pacifiquement, elle se fera tout de même attaquer si le comportement défini par le maître de la destination est "ennemi".
> ¤ La destination est neutre, la flotte a reçu ordre de se poser pacifiquement, elle se pose sans encombre si le comportement défini par le maître de la destination est "neutre".
> ¤ La destination est sauvage ou neutre, la flotte a reçue ordre d'attaquer, elle attaque en arrivant sur le corps céleste.

## Trajet ##
Les voyages dans l'espace se font en hyper-space. Ce mode déplacement permet un gain de vitesse considérable. Celle-ci reste cependant proportionelle à la vitesse de base de la flotte (en effet, l'hyper-space à un effet d'amplification).
Le déplacement de base se fait toujours en ligne droite d'un corps céleste vers la destination.
Le mode hyper-space nécéssite pour que la flotte puisse s'arrêter qu'une masse importante fasse obstacle au mouvement, c'est pourquoi les flottes ne peuvent s'arrêter que sur les corps célestes (et ne peuvent pas prendre pour cible les sondes).
Le mode hyper-space interdit aussi toute communication même si l'on passe à proximité d'une sonde.
Du point de vue du gameplay, le joueur à la possibilité de définir des trajets en plusieurs étapes (utile pour pouvoir joindre deux corps célestes non joignable en ligne droite à cause du soleil).
Le joueur peut choisir lors de l'ordre de déplacement multi-étape le comportement suivant :
> ¤ Soit toujours s'arrêter sur les corps célestes étapes, il perd le reste de son déplacement pour le tour mais est averti qu'il peut dérouter la flotte à ce tour ci.
> ¤ Soit ne pas s'arrêter, la flotte continue son trajet suivant les ordres qu'elle actualise cependant à chaque checkpoint.
Dans les deux cas, à tout moment le joueur peut changer la feuille de route de la flotte, celle-ci ne l'actualisant qu'au checkpoint suivant.

Le temps de voyage est estimé en mesurant la distance du trajet et en la comparant a la vitesse de la flotte.
A chaque tour, la flotte parcours le pourcentage du trajet que sa vitesse lui permet; Toutes les zones traversées a ce moment là sont considérée comme **visitées**.
Si durant son trajet une flotte en croise une autre, c'est à dire que les deux empruntent la même case en même temps, chacun des joueurs recevra (lorsque la flotte arrivera à destination) un rapport (simplement la position et la date, aucune déduction de direction ni de sens) sur la flotte ennemie aperçue, mais aucun combat n'est engagé.

## Retarder un départ ##
Il est possible d'assigner un trajet à une flotte en lui donnant ordre d'arriver à une date précise (suppérieure ou égale à la date minimale à pleine vitesse).
Cette flotte calcule alors le nombre de tour qu'elle doit attendre avant de partir, et part automatiquement au tour où elle le doit.
Tant que la flotte n'est pas partie, le joueur peut très bien annuler et donner un nouvel ordre à sa flotte.
Cette fonctionnalitée permet entre autre de synchroniser l'arrivée de flottes de plusieurs joueurs sur une même destination (attaques groupées par exemple).

## Synchronisation de flottes entre alliés ##
Entre joueurs alliés, qui partagent la même vue, il est possible de synchroniser une de ses flottes avec une flotte allié déjà en route, à condition que le temps de trajet de celle-ci soit suppérieur au trajet que mettrais la flotte à synchroniser.
Dans ce cas, le joueur choisi pour destination la flotte alliée avec laquelle se synchroniser, et la flotte calcule d'elle même le retard qu'il lui faut appliquer pour être certaine d'arriver à destination en même temps que la flotte alliée.

## Cas d'une destination incertaine, politique de rapatriement des flottes (OBSOLETE, cf. Missile anti-sonde)) ##
Nous apellons destination incertaine une sonde que l'on prend pour cible pour la détruire, ou un vortex sur lequel on se rend mais qui peut très bien disparaître avant que l'on y arrive.
Lorsqu'on envoie une flotte sur ce type de destination, on est libre de spéficier le corps céleste sur lequel doit se rendre la flotte après avoir visité la zone occupée par la sonde / le vortex.
En l'absence de choix du joueur, la flotte est rapatriée par défaut sur le corps céleste allié le plus proche (tel que connu au moment de l'envoi).

## Cas particulier des Sondes (OBSOLETE: cf. Missile anti-sonde) ##
La sonde est la seule unité à pouvoir stationner sur une zone vide.
Après sa fabrication, la sonde doit être lancée sur un zone, pouvant être un corps céleste, ou vide.
Une fois sa destination atteinte, la sonde se déploie, elle n'a alors plus la capacité de se déplacer mais permet de voir ce qui se passe autour d'elle (jusqu'a une certaine limite).
Les sondes sont invisible pour l'ennemi (comme tout autre vaisseau), a moins que celui-ci n'est lui même intallé une sonde à porté.
Dès lors, une sonde ennemi constitue pour toute flotte de combat une "destination valide".
De plus, lors d'un voyage normal, si la flotte passe par une case occupée par une sonde ennemie (zone considérée comme //visitée//), celle-ci est automatiquement détruite.