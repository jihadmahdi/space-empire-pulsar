# Constructions #
Les constructions se font moyennant 人 et 炭 sur des corps célestes disposants de slots libres.
Chaque construction apporte un effet particulier au corps céleste sur lequel elle est batie.
Il est permi à tout moment au joueur de détruire des batiments pour libérer des slots.
En revanche, chaque corps céleste ne peut construire qu'un seul batiment par tour de jeu, le batiment n'étant disponible qu'au tour suivant.

Voici la liste des constructions possibles :

## Module d'extraction 炭 ##
0人/1000炭
Même si la technologie diffère suivant que l'on se trouve sur une planète, une nébuleuse ou un champs d'astéroïde, les modules d'extractions 炭 présentent tous les même caractéristiques:
Un module d'extraction 炭 occupe un slot et permet de générer 2500 炭 par tour.
Ils sont cumulables tant que le corps céleste dispose de slots libres, cependant le prix est exponentiel avec le nombre de module total.
En effet, si le premier module d'extraction est construit sur la première source de carbone de la planète, les modules suivant doivent exploiter des filons plus sauvages et sont donc plus couteux à mettre en place.



&lt;math&gt;

Prix_{n<sup>{ième}~Module} = Prix_{1er~Module} * e</sup>{n-1}

&lt;/math&gt;_

## Usine de production de vaisseaux ##
500人/2000炭
Les usines de production de vaisseaux ne sont disponibles que sur les planètes, car elles nécéssitent beaucoup de main d'oeuvres.
Elles ont deux fonctionnalités distinctes:
> ¤ La fabrication de nouveaux vaisseaux (moyennant le prix du vaisseau choisi), qui sont alors disponibles dans l'entrepot de l'usine, prêt à être affectés à une flotte.
> ¤ Le remaniement des flottes existantes, qui se posent sur la piste de l'usine et y reste jusqu'a être de nouveaux affecté à une flotte.

## Module de défense ##
0人/4000炭
Les modules de défense sont la première protection des corps célestes, ils octroient des bonus de +(##A DEFINIR##) en attaque et en défense au corps céleste si celui-ci est attaqué, ainsi qu'aux flottes alliées en orbite.
Les modules de défense offrent un pourcentage de protection contre les bombardements d'artillerie.
Ces modules sont cumulables tant que le corps céleste dispose de slots libres.

## Module gouvernemental ##
0人/0炭
Ce module, pré-existant sur la première planète de chaque joueur, représente la capitale de son empire, le centre de commande et d'organisation de toute ces armées.
C'est donc également un point vital qui doit être préservé à tout prix.
Si l'invasion est imminante, le joueur peu toujours créer un vaisseau gouvernemental pour changer de capitale.
Le module gouvernemental est alors démonté et sera remonté sur la planète destination du vaisseau gouvernemental, si toutefois elle est toujours libre.
A noter que si la planète destination ne possède pas de slots libre, le joueur devra obligatoirement en libérer un.
La planète où est établi le module gouvernemental reçoi un bonus de production de +50% en 人 et 炭.

## Comptoir intergalactique ##
0人/1000炭
Les comptoirs intergaliactique permettent d'acheminer les ressources entre les planètes.
Ainsi, les nébuleuse et champs d'astéroïdes qui ne peuvent produire de vaisseaux ont tout intéret à disposer d'au moins un comptoir pour pouvoir acheminer leurs ressources vers les planètes qui sont plus pauvres en carbone.
Les comptoirs ont deux fonctions pricipales : La construction de routes de l'espace, et les échanges commerciaux.

### Routes de l'espace ###
Les comptoirs permettent de construire des routes de l'espace, moyennant un prix qui dépend de la distance à couvrir (500炭/Z).
Une route se fait d'un comptoir à un autre (ceux-ci ne pouvant servir que pour une route à la fois).
Les routes de l'espace permettent à tout voyageur qui l'emprunte de substituer la vitesse de la route à sa propre vitesse, si celle-ci est inférieure. Celle-ci est très avantageuse pour la pluspart des vaisseaux.
Note: La vitesse des routes doit être telle que les Chasseurs soient toujours plus rapides même hors des routes.

#### Cas spéciaux ####
Si l'un des deux comptoir est détruit, la route est détruite. Les flottes qui seraient en train  de l'utiliser reprennent leur vitesse normale.
Si trois planètes sont alignées, on doit construire la route de l'espace étape par étape. Ex: A, B, et C alignées avec B entre A et C, il est impossible de construire une route directe entre A et C.

### Echanges commerciaux ###
Les comptoirs permettent également de procéder à des transferts réguliers de ressource carbone entre comptoirs.
Le voyage est assuré par un transporteur qui à sa propre vitesse achemine la quantité de carbone définie par le joueur (dans les limites disponibles au moment du départ, et d'un seuil maximum transportable en un trajet).
L'envoi prend le temps d'un allé simple, la planète destination encaisse le carbone reçu et le transporteur repart sur sa planète d'origine.
Il met autant de temps pour y rentrer, puis repart automatiquement s'il a une commande en attente.
Le joueur peut modifier la "commande" à tout moment, mais celle-ci n'est prise en compte que lorsque le transporteur est disponible.

#### Cas spéciaux ####
Lorsqu'un transporteur arrive sur un corps céleste ennemi (avec ou sans comptoir) : La cargaison est pillée et la route commerciale effacée (le joueur peut recréer une nouvelle route commerciale sans attendre)
Si le comptoir de destination est détruit après que le transporteur soit lancé : A l'arrivée le transporteur fait demi-tour avec la cargaison.
Si le comptoir de départ est détruit après que le transporteur soit lancé : Le transporteur stoppe ses activités.

Les routes de l'espace et les échanges de ressource sont dissociés et peuvent concerner des destinations différentes.

## Canon à pulsar ##
50000人/100000炭/2 slots détruits
Les canons à pulsar sont des bâtiments coûteux qui ont une utilisation unique.
Chaque canon à pulsar coûte en plus de son prix en 人 et 炭 un sacrifice d'au minimum 1 slot de construction définitivement perdu.
Le joueur à cependant le choix de sacrifier davantage de slots (jusqu'au maximum du corps céleste occupé) pour améliorer l'effet du pulsar qui sera produit.
Pour chaque slot consacré à la plateforme de lancement du missile à pulsar, celui-ci gagne en vitesse de lancement, et sa puissance d'effet est directement lié à sa puissance d'impact sur le soleil.
Lors du tir, le joueur choisi une direction à partir du soleil, et peux influer pour distribuer la puissance entre volume et temps.
Concrètement, il fait bouger un curseur sur un slider noté à gauche "volume", et à droite "temps". Le centre représente une distrbution optimale avec 50% volume et 50% temps. Le joueur peux cependant déséquilibrer la jauge pour obtenir plus de temps (mais il perds d'avantage de % de volume qu'il ne gagne de % de temps) ou l'inverse.
```
[v		|		|		|		t]
				50%		25%v;62,5%t				
```
Le slider permet au joueur de paramétrer une balance de bonus/malus entre le volume et le temps. L'optimisation se situe toujours à l'équilibre (100%), tandis que si l'on fait pencher de l'un ou l'autre des côté, on paie le double du bonus que l'on gagne d'un côté en malus de l'autre.
Ex: Si l'on sacrifie 25% du volume, on gagne un bonus de 12,5% de temps.
Les pourentage de bonus/malus sont appliqué à des plages fixes plus ou moins importante suivant le nombre de slots sacrifiés à l'installation de la plateforme de tir.

### L'effet "pulsar" ###
L'effet pulsar, comme le décrit l'astrophysicien M. Masanori Matsuyama (//松山正則//) se présente sous la forme de perturbations du champs magnétique, qui entrainnent l'incapacité à toute communication à travers l'espace (suivant les technologies connues à ce jour).
Concrètement, les corps célestes et flottes pris dans la zone d'effet d'un pulsar perdent toute communication avec le reste de leur empire.
Le joueur n'a plus accès aux dernières informations les concernant, et ne peut plus donner d'ordre de production, ni de déplacement aux flottes en présences.
Si le gouvernement d'un joueur est touché, alors c'est tout son empire qui est paralysé, le joueur ne peut que "passer son tour" jusqu'a ce que l'effet disparaisse.