# Multijoueur & Diplomatie #
Space Empire Pulsar est un jeu multijoueur en réseau (local ou Internet), même si des "bots" seront peut être implémentés dans le futur.

## Lancement de la partie ##
### Jouer en local ###
Un joueur doit créer la partie, il choisi alors les options de départ de celle-ci, et doit attendre que les autres joueurs joignent l'écran de création de partie avant de lancer la partie.
Les autres joueurs joignent la partie (en tapant si nécéssaire l'adresse de la machine du joueur hôte de la partie), choisissent les options les concernants puis se déclarent "prêt à commencer le jeu".
Une fois tous les joueurs prêts, l'hôte de la partie peut lancer celle-ci.

### Jouer sur Internet ###
Lors de la création de la partie, il est possible de la diffuser en ligne, de sorte qu'elle aparaisse dans la liste des "Parties sur Internet".
Sans créer de nouvelle partie, il est possible d'afficher la liste des "Parties Internet" pour en choisir une à rejoindre.
La liste des parties n'affichent que les parties en cours de création, celles ayant démarrées ne sont plus disponibles.

### Jeu tour par tour ###
Le jeu se fait en tour par tour, mais tous les joueurs jouent leur tours en même temps.
Ce n'est que lorsque le dernier joueurs valide son tour que le serveur calcule les actions et lance le tour suivant.
En début de tour, chaque joueur est informé des changements importants le concernant.

## Diplomatie ##
En jeu, la diplomatie peut tenir une part importante de la stratégie.
Ainsi, il est possible de communiquer avec les autres joueurs par Chat, en filtrant :
> ¤ A tous,
> ¤ Aux alliés seulements,
> ¤ A un joueur particulier.

Le choix est fait de laisser aux joueurs les moyens de gérer eux même la diplomatie selon leur bonnes volontés.
Ainsi toute forme de relation diplomatique (amicale, neutre, hostile) est implicitement possible hors d'une alliance, mais seul le système d'alliance offre de réelles garanties aux joueurs au prix des obligations auxquelles celà les soumets.

### Dons, échanges ###
Il est possible de procéder à des dons de carbones, ou échanges si les joueurs se mettent d'accord sur une contrepartie, cependant l'échange est non sécurisé.
Le joueur qui donne choisie le comptoir de la planète source et le montant (dans les limites du stock actuel de la planète source).
Le joueur qui reçoit choisi le comptoir de la planète destination, ce qui détermine le temps que mettra le carbone à arriver.
On considère cependant que le transporteur dépéché pour l'échange entre joueur est dédié à cette tâche.

### Diplomatie hors pacte d'alliance ###
La diplomatie hors pacte d'alliance n'offre aucune garantie aux joueurs, elle repose entièrement sur leur honnêteté, ou leur manque d'honnêteté. Elle est donc au plus prêt de la réalitée.
Chaque joueur doit prouver en permanance sa bonne foi et sa confiance a ses amis en ne les attaquant pas.
Tous les joueurs hors alliance sont considéré "neutres", bien que par le biais du Chat ils puissent constitué de réelles "alliances" fondées sur la confiance mutuelle.

#### Déplacement de flottes entre joueurs non alliés ####
Afin de ne pas ralonger le temps de jeu en demandant à chaque fois au joueur s'il souhaite "attaquer" ou "laisser en paix" les flottes étrangères qui viennent se poser sur ses corps célestes, il est nécéssaire que chaque joueur ai déjà configuré ses attitudes par défaut.
Ainsi, il y a deux sortes de comportement à définir :
> ¤ La politique interrieure qui défini le comportement de ses planètes lorsqu'une flotte étrangère pacifique souhaite se poser.
> ¤ La politique de conquète qui défini le comportement de ses propres flottes lorsqu'elles sont mêlées à un conflit multi-partites.

##### Comportement des planètes face aux nouvelles flottes étrangères #####
Le premier comportement peut être modifié à tout moment et est défini entre le joueur courant et chaque joueur non allié suivant les statuts :
> ¤ Hostile (par défaut) : Les flottes de ce joueur sont attaquées a vue même si elles viennent pacifiquement.
> ¤ Neutre : Les flottes ont l'autorisation de se poser si elle viennent pacifiquement.

##### Comportement des flottes en conflit #####
Le second comportement est défini entre le joueur courant et chaque joueur non allié suivant les status :
> ¤ Neutre (par défaut) : La flotte évitera d'attaquer les unités du joueur correspondant à moins que ceux-ci ne fassent feu.
> ¤ Hostile : La flotte attaquera les unités du joueur correspondant dans tout les cas.

##### Flottes en stationnement: Passer à l'attaque ! #####
A tout moment, le joueur peut donner ordre aux flottes stationnées pacifiquement d'attaquer. Toutes les flottes du joueur présente dans la zone sont alors automatiquement mobilisées. L'attaque se fait en fin de tour.

##### Flottes étrangères en stationnement: S'en débarasser ! #####
A tout moment, le joueur peut donner ordre aux forces de son corps céleste d'abattre les flottes étrangèes en stationnement.
Il doit définir sa politique intérieure de sorte à déclarer ennemie au moins l'une des forces présente, puis l'attaquer.
Le conflit se résoud d'abord suivant la politique intérieure du corps céleste attaquant, puis les politiques de conquètes des forces étrangères en présence.
S'il perds, le corps céleste reviens à l'une des alliance victorieuse, tirée au hasard selon la proportion de chaque joueur restant.

### Pacte d'alliance ###
Le pacte d'alliance est un lien fort permettant a plusieurs joueurs de s'allier en ayant une garantie sur l'honnêteté de leurs camarades.
En effet, trahir une alliance fait perdre définitivement les bonus lié au module gouvernemental.
Les pactes d'alliance sont donc contagieux (l'allié de mes alliés est mon allié, bref, nous sommes tous alliés).
Pour s'allier a un joueur, celui-ci doit ne faire partie d'aucune alliance, et l'alliance doit voter son adhéson à l'unanimitée (un tour pour lancer l'invitation, un tour pour voter, un tour pour le résultat).
L'alliance est crée à l'origine par l'accord des deux premiers participants (un tour pour lancer l'invitation, un tour pour accepter/refuser l'invitation, un tour pour le résultat).

Faire partie d'une alliance apporte certains droits/devoirs :

¤ Les données (cartes) de tous les membres (la position de leurs flottes, routes commerciales, sondes, etc..) sont fusionnées et visibles par tous.
¤ Possibilité de synchroniser ses flottes de façon plus simple, en sélectionnant la flotte allié comme cible avec laquelle se synchroniser.
¤ Les flottes sont toujours autorisées à se poser chez un allié.
¤ Il est impossible d'attaquer, ou de se faire attaquer par, un allié.
¤ Les flottes en garnisons sur un corps allié qui se fait attaquer participent automatiquement aux forces de défenses.
¤ Les messages privés d'un allié à un joueur ne faisant pas parti de l'alliance ont 30% de chance d'être interceptés et diffusés à toute l'alliance.
¤ Les cartes étant partagées, les échanges commerciaux d'un membre avec un joueur non membre sont visibles par tous (sans être particulièrement mis en évidence).
¤ Chaque membre peut faire par de son souhait d'exclure un autre membre en proposant un vote (le membre ciblé ne vote pas):
> ¤ Si l'exclusion est votée à l'unanimité, elle est prononcée et l'alliance ne subit aucun malus de trahison.
> ¤ Si l'exclusion est votée à plus de 50% mais pas a l'unanimité, le membre est exclu mais l'alliance subit le malus de trahison (pas le membre exclu).
> ¤ Si l'exclusion n'est pas voté à plus de 50%, elle n'est pas prononcée, et aucune mesure n'est prise même si l'on peut imaginer que celà va créer quelques tensions au sein de l'alliance.
¤ Enfin un joueur peut décider unilatéralement de quitter l'alliance, il subit alors les malus de trahison.

#### Malus de trahison ####
Le malus de trahison signifie la perte définitive des bonus de production lié à la possession d'un module gouvernemental.
De plus, un/les joueur(s) subissant le malus de trahison n'a/ont pas le droit d'attaquer ses/leur ancien(s) allié(s) avant N tours.
Le(s) joueur(s) anciennement allié(s) ont/a quant à eux/lui le droit d'attaquer le(s) traitre(s).
Si le(s) traitre(s) est/sont attaqué(s) avant les N tours, il(s) a/ont le droit de riposter immédiatement.

Note: Une flotte partie avec un ordre d'attaque qui arrive sur une planète d'un joueur avec lequel on s'est allié entre temps annule automatiquement son attaque.

### Attaques multipartites ###
Lorsqu'un corps céleste est attaqué par plusieurs joueurs en même temps, il faut un moyen de déterminer à qui revient celui-ci en cas de victoire.
¤ Si tous les joueurs vainqueurs sont alliés : Un vote est lancé au tour de la prise (durant lequel le corps céleste est "neutre"), au tour suivant l'élu prend possession du corps céleste (même s'il n'as pas participé au combat).
¤ Si tous les vainqueurs ne sont pas alliés : On regroupe les partis, et on alloue à chacun un pourcentage de chance de prendre le contrôle de la planète en fonction du poids que représente sa flotte restante sur la somme des survivants, cette prise est pacifique et tous les autres partis se retrouvent posé pacifiquement sur le corps céleste.

## Divers ##
### Option jeu rapide ###
A partir du moment où ils ont validé leur tour, et tant que le dernier joueur n'a pas validé le sien, on compte un bonus de génération de ressources pour les joueurs les plus rapides.

### Option grève à la trahison ###
En plus du malus de trahison prévu, le traitre qui quitte l'alliance perds sa production pendant un nombre de tour proportionnel au nombre de tours qu'il a passé dans l'alliance récement trahie.