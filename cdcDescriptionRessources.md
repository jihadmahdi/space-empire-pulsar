# Ressources #
Il existe deux types de ressources:

## La population, notée 人 ##
Elle représente le nombre de personnes disponibles sur chaque planète, elle n'est donc disponible que sur les planètes.
La quantité de 人 générée par tour par une planète dépend de la taille de cette planète et peut donc varier d'une planète a une autre.
Pour des raisons de gameplay, les planètes de départ de chaque joueur sont considérées de même taille et génèrent donc la même quantité de 人 par tour.

Une planète possède une production de 人 par tour, définie aléatoirement à la création de l'univers.
Une planète possède également un plafond maximum de 人 au delà duquel elle ne produit plus (tant que l'on ne dépense pas de 人), fixé lui aussi à la création de l'univers, et qui représente de façon abstraite sa taille (espace habitable).

Attention: La production de 人 par tour peut diminuer (de façon permanante) si la planète est victime de bombardements.

## La ressource carbone, notée 炭 ##
Elle représente la réserve en carbone de chaque planète, elle est disponible sur tout [cdcDescriptionCorpsCelestes|corps céleste].
Elle se trouve à l'état naturel jusqu'a un certain seuil, puis grace aux [cdcDescriptionConstructions|modules d'extraction 炭].

Un corps céleste possède une production de 炭 par tour, dépendant du nombre de [cdcDescriptionConstructions|modules d'extraction 炭] construits (ou production naturelle en l'absence de tout module d'extraction).
Un corps céleste possède une réserve maximale de 炭 au delà de laquelle il devient définitivement stérile et ne peut plus produire.