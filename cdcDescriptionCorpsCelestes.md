# Corps célestes #
Les corps célestes sont des points stratégiques dont toutes les caractéristiques de début de partie (人/tour, 人 max, 炭　max de départ, nombre de slots) sont visibles par tous les joueurs.
Ils fournissent les ressources nécessaires à la construction des flottes et des batiments.
Les quantités de carbone associées aux corps celestes sont déterminées aléatoirement en début de partie, et s'épuisent à mesure que le joueur les exploite.
Tout corps celeste est coloré suivant son appartenance: Sauvage ou couleur du joueur propriétaire.

## Caractéristiques ##
Les corps célestes sont définis par plusieurs caractéristiques, pouvant fluctuer durant le jeu :

|	##人 par tour##	|	Production de population à chaque tour de jeu.	|
|:---------------|:-----------------------------------------------|
|	##人 max##		    |	Valeur maximale de population autorisée sur la planète, au delà la production s'arrète, jusqu'a ce que la population soit redevenue inférieure au maximum autorisé. |
|	##炭 max##		    |	Réserve en ressource carbone disponible sur le corps céleste, cette valeur diminue à chaque tour passé a extraire le carbone, jusqu'a ce que le corps céleste soit stérile. |
|	##Nb. slots##	 |	Nombre d'emplacement disponibles sur le corps céleste pour la construction de modules. |

### Seuil naturel minimal de 炭 ###
Tout corps céleste surlequel le carbone n'est pas extrait par des modules d'extraction fournie tout de même une faible quantité de 炭 par tour, jusqu'a un certain seuil, apellé seuil naturel minimal.
Généralement, ce seuil permet tout juste la fabrication d'un module d'extraction.
Même un corps céleste sauvage produit cette quantité naturelle de carbone par tour, jusqu'a atteindre le seuil.
La production naturelle par tour est cependant très faible, inférieure à la production d'un module d'extraction, si bien qu'il est préférable de construire un module d'extraction dès que celà est possible.

## Types de corps célestes ##
Il existe différents types de corps célestes dont voici la liste:

### Planète ###
Les planètes sont les seuls corps célestes offrant les deux types de ressources existantes: population 人 et carbonne 炭.
De ce fait, certaines constructions ne sont possibles que sur des planètes.
Elles sont un point stratégique important, même si leurs ressources carbone sont très inférieures aux autres corps célestes desquels elles doivent s'approvisionner.
Les planètes sauvages disposent d'une armée de défense fixé à la génération de l'univers suivant leurs taille.
{{
> 人 par tour: [2500-7500]
> 人 max: [50K-150K]
> 炭 max: [50K-100K]
> Nb. slots: [4-10]
}}
Constructions autorisées:	Exploitation de ressources, Usine de vaisseaux, Module de défense, Module gouvernemental, Comptoir, Canon à pulsar.
En tant que corps célestes neutres, les planètes ont 30% de chance d'être générées.

### Champ d'astéroïde ###
Les champs d'astéroïdes sont des lieux impropre à la vie, mais propice à la construction de batiments millitaires, et riche en carbonne.
Ainsi il est possible de construire la pluspart des batiments, sauf ceux dont la production nécéssite de la population.
{{
> 人 par tour: 0
> 人 max: 0
> 炭 max: [60K-300K]
> Nb. slots: [3-6]
}}
Constructions autorisées:	Exploitation de ressources, ~~Usine de vaisseaux~~, Module de défense, ~~Module gouvernemental~~, Comptoir, ~~Canon à pulsar~~.
En tant que corps célestes neutres, les champs d'astéroïdes ont 50% de chance d'être générés.

### Nébuleuse ###
La nébuleuse est un corps celeste formé de gaz et de poussières.
Sa structure mouvante ne permet pas l'accueuil de construction de batiments autre que les modules d'extraction et les comptoirs nécéssaires à l'acheminement du carbonne.
{{
> 人 par tour: 0
> 人 max: 0
> 炭 max: [100K-500K]
> Nb. slots: [2-4]
}}
Constructions autorisées:	Exploitation de ressources,~~Usine de vaisseau~~, ~~Module de défense~~, ~~Module gouvernemental~~, Comptoir, ~~Canon à pulsar~~.
en tant que corps célestes neutres, les nébuleuses ont 20% de chance d'être générées.