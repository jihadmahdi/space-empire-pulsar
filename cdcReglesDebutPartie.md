# Début de partie #
## Options de début de partie ##
L'initiateur de la partie est celui qui choisi les options de la partie, voici la liste des options et paramètres disponibles :

### Taille de l'univers ###
Taille de chaque dimension (x, y, z) de l'univers à générer.
//Par défaut: 20x20x20//

### Nombre de corps célestes ###
Nombre de corps célestes à générer.
//Par défaut: (nombre de joueurs x 2) + 1//

### Nombre de joueurs ###
Les joueurs doivent rejoindre le jeu en se connectant au serveur lorsque celui-ci est en création de partie.
Les joueurs sont alors ajouté à la liste des joueurs et l'initiateur de la partie peut librement refuser des joueurs, ou en ajouter des artificiels (AI).
Une fois la partie lancée, le nombre de joueur est fixé et il est impossible d'en ajouter.
//Minimum: 2//

### Quantité de 炭 de départ ###
Quantité de ressource carbone disponible sur les planètes de départ de chaque joueurs.
//Par défaut: 150K//

### Le niveau de 炭 des corps célestes neutres ###
Quantité de ressource carbone disponible sur les corps célestes neutres (fourchette).
Afin de garder l'équilibre du jeu, les champs d'astéroïdes ne devraient pas fournir plus de 60% des ressources fournies par les nébuleuses.
//Par défaut://
|Nébuleuses|100K-500K|
|:---------|:--------|
|Champs d'astéroïdes|60K-300K |

### Le nombre de slots disponibles sur les corps célestes ###
Quantité de slots de construction disponibles sur chaque type de corps célestes (fourchette).
Afin de garder l'équilibre du jeu, les planètes devraient fournir plus de slots que les autres corps célestes.
//Par défaut://
|Planètes|6-15|
|:-------|:---|
|Nébuleuses|3-6 |
|Champs d'astéroïdes|4-8 |

### Avoir une flotte de départ ###
Si cette option est cochée, les joueurs disposent d'une flotte dès le départ, stationnée sur leur planète respective.

## Conditions de victoire et de défaite ##
C'est en début de partie que l'on spécifie les conditions de victoire et défaite de la partie.
Si plusieurs conditions de victoire sont choisies, c'est le premier joueur en remplissant au moins une qui remporte la partie.
De manière générale, le dernier joueur en course gagne automatiquement la partie.

### Victoire en aliance ###
Autorise ou non la victoire en aliance: quelque soit la condition de victoire, tous les aliés du vainqueur sont vainqueurs.

### Régimicide ###
Cette option rend disponibles la construction "Module gouvernemental" ainsi que le "Vaisseau gouvernemental".
Chaque joueur dispose en début de partie d'un Module gouvernemental pré-construit sur sa planète d'origine.
Si un joueur perds la planète sur laquelle est établi ce Module gouvernemental, il perds la partie (le gouvernement est mort).
Il est possible de migrer le Module gouvernemental d'une planète a une autre en créant un Vaisseau gouvernemental.
La construction d'un tel vaisseau n'est possible que sur la planète où se trouve le module gouvernemental.
Dès qu'un vaisseau gouvernemental est créé, le module gouvernemental de la planète est démonté (le gouvernement quitte les lieu pour s'établir dans le vaisseau le temps du voyage).
Le joueur peut garder le vaisseau gouvernemental en stationnement le temps de lui adjoindre une flotte, mais si ce vaisseau est perdu lors d'un assaut planétaire, le joueur perds la partie (le gouvernement est mort).

Une fois le vaisseau gouvernemental lancé, le joueur perds temporairement le contrôle de ses planètes et flottes (car le gouvernement perds la communication).
Lorsque qu'il arrive sur la planète destination choisie:
  * Soit il arrive a s'y poser et récupère la communication. Il peut établir un nouveau module gouvernemental s'il stationne sur une planète qui lui appartient.
  * Soit le corps céleste est hostile, un assaut est engagé à l'issue duquel le vaisseau gouvernemental aura réussi à se poser, ou aura été détruit.

#### Options ####
On doit définir le comportement de la population restante en cas d'élimination du gouvernement:
  * Soit toutes les unitées sont alors considérés "sauvages", elles ne produisent plus mais terminent leurs trajets, et défendent leurs positions.
  * Soit toutes les unitées tombent aux mains du joueur qui a éliminé le gouvernement.

### Conquète totale ###
Le premier joueur ayant conquis tous les corps célestes et détruit toute les flottes ennemies gagne la partie.
A noter qu'il est nécéssaire de décocher les autres conditions de victoire pour pouvoir profiter de celle-ci pleinement.

### Victoire économique ###
Le premier joueur ayant atteind les seuils de ressource 炭 et 人 fixées gagne la partie.

### Limite temps ###
Le jeu se termine au bout d'un nombre de tour fixé, le joueur ayant le plus haut score global gagne la partie.

## Calcul du score global d'un joueur ##
Le score global d'un joueur est calculé en prenant en compte toute ces possessions:
Nombre et types de corps célestes, total des ressources 人 et 炭, nombre et puissance des flottes, nombres et types de constructions.

### A DEFINIR ###
Poids de chaque paramètre dans la formule finale de calcul du score.