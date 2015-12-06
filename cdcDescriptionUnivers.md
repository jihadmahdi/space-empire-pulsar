# Univers #
//"Deux choses sont infinies : l'univers et la bêtise humaine, pour l'univers, je n'en ai pas acquis la certitude absolue." A. Enstein//

## Mesure de l'univers ##
L'univers est un damier en 3 dimensions dont la taille est paramètrable en début de partie.
Ce damier est découpé en zones, repérées par leurs coordonnées (x, y, z).
Chaque zone peut être vide, occupée par un corps céleste, une flotte, ou les deux.
Au centre de l'univers se tient le soleil, seul corps céleste à occuper plusieurs zones.
Le soleil constitue un obstacle infranchissable, et une source d'énergie innépuisable pour les sondes. Son rayonnement délimite ainsi naturellement la porté des sondes et par là les limites de l'univers explorable.

On peut déterminer la distance entre deux zones en mesurant la ligne imaginaire qui les lie par un lien direct, et cette distance permet au joueur de calculer le temps que mettront ses flottes pour faire le voyage.

Une zone représente le volume minimal capable de contenir tout corps céleste (les plus volumineux étant généralement les planètes).
Elle délimite également la visibilité maximale de toute les unités (hors unités spéciales).
Ainsi, deux flottes en déplacement ne s'apercevront que si elles croisent la même zone au même moment.

## Notes sur la communication ##
L'univers est d'une étendue si immense que toute communication au delà d'une zone n'est possible qu'avec une technologie avancée et coûteuse. Cette technologie n'est donc disponible que sur les corps célestes.
Ainsi, toute flotte envoyée en déplacement n'est plus joignable avant qu'elle n'atteigne sa destination, et réussisse à s'y établir.
C'est à dire qu'une flotte qui serait envoyée pour attaquer un corps céleste ennemi n'aura pu communiquer aucune information si elle est détruite lors de l'attaque.
Le gouvernement n'aura pu que déduire son échec par l'absence de communication au jour prévu de l'attaque.
De même, une flotte qui en aurait aperçue une autre durant son voyage (c'est à dire que les deux flottes se sont croisées au même moment sur la même zone), ou bien qui aurait détruit une sonde ennemi sur son passage, ne pourra en faire le rapport qu'une fois arrivée sur un corps céleste.

Les seules exceptions étant les sondes, celles-ci sont équipées d'équipement lourd leur permettant de communiquer dès lors qu'elles se déploient et restent en stationnement.

## Phénomènes astraux ##
Les phénomènes astraux sont des évènements dont la probabilité reste mystérieuse et qui se produisent donc au hasard durant la partie.

### Vortex ###
> Les vortex ont une origine encore méconnue. Leur formation est passionnément étudiée par l'ensemble de la communauté des chercheurs.
> La brillante théorie proposée par Ivanov Pertenchevko décrivant le vortex comme une hypercourbure de l'espace-temps permettant les déplacements au-dela du point critique vitesse lumière a recemment été approuvée par l'expérience après l'envoi de la sonde "Connaissance" qui a été localisée quelques secondes a peine a un point tres éloigné de l'univers connu.

> Les vortex sont générés aléatoirement pendant la partie, et restent pour un nombre de tour également tiré au hasard.
> Un vortex permet à qui l'emprunte de se téléporter automatiquement sur un corps céleste (qu'il soit allié, neutre ou sauvage) tiré au hasard (mais fixe pour chaque vortex).
> Si un vortex apparait sur le chemin d'une flotte, celle-ci l'emprunte automatiquement lorsqu'elle le croise.
> Si un vortex choisi pour destination disparait avant que la flotte ne l'atteigne, celle-ci achève son trajet jusqu'à la zone vide, puis repart automatiquement vers sa planète d'origine.

> Si une flotte emprunte un vortex accidentellement et attérie sur un corps céleste neutre, elle tente toujours de s'y poser pacifiquement.
> Si on donne ordre à une flotte d'emprunter un vortex, on choisi "aller à ..." ou "attaquer ...".

### Pulsar ###
> Un pulsar est à l'état naturel le résultat de l'explosion d'un astre, dont résulte une étoile morte tournant rapidement sur elle-même (période de l'ordre de la seconde), et émettant un fort rayonnement électromagnétique dans la direction de son axe magnétique.
> Celui-ci n'étant chez les pulsar, comme chez de nombreux autres corps céleste, pas parfaitement aligné avec l'axe de rotation, le faisceau émis en direction de l'axe magnétique balaie en réalité un cône.
> L'astrophysicien Masanori Matsuyama (//松山正則//) fut le premier à étudier les effets de tels corps et à en explorer les utilisations possibles. Dans son article 明日は森閑　(//Demain le silence//) il attire l'attention sur les dérives millitaires qui pourraient selon lui réduire sous peu l'univers au silence.
> En effet, on observe que la zone couverte par le cône que forme le rayonnement d'un pulsar se trouve soumise à une perturbation magnétique suffisante pour rendre innutilisables tous les moyens de communication spaciaux connus (les communications locales à un corps céleste ne sont pas sujetes à ces perturbations).
> De nos jours, les craintes de M. Matsuyama sont confirmées puisque l'armée est arrivé à fabriquer articiellement de petits pulsars capables d'émèttre sur 4 ou 5 zones spatiales, et projète de construire des canons capables de les propulser n'importe où dans la galaxie.

> cf. le "Canon à pulsar" dans la liste des [cdcDescriptionConstructions|Constructions].

## Notes sur l'algorithme de génération ##
Afin de répondre au besoin de gameplay tel que défini dans le présent cahier des charges, c'est à dire :
> ¤ Voyages en mode hyper-espace possibles uniquement en lignes droites.
> ¤ Présence du soleil au centre de l'univers (qui viens rendre certaines trajectoires impossibles, car évidemment on ne peut pas passer à travers le soleil).
Il est décidé que les planètes de départ de tous les joueurs seront générées telles qu'elles soient toutes joignables entre elles.
Il en découle que si certains corps célestes pourront se trouver en position de n'être joignable que par la planète de départ de l'un des joueurs, ceux-ci resteront néanmoins potentiellement joignables pour tous dans la mesure où chacun est libre d'utiliser la force brute ou diplomatique.