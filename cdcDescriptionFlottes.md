# Flottes & vaisseaux #
Une flotte est un ensemble de vaisseaux, les caractéristiques d'une flotte dépendent donc des vaisseaux qui la compose.
Une flotte se créé sur une planète avec les vaisseaux disponibles sur celle-ci.
En stationnment sur une planète, une flotte peut être dissociée et les vaisseaux ré-affectés à de nouvelles flottes.
Le joueur a la possibilité de nommer les flottes qu'il compose.

## Classess de vaisseaux ##
Les vaisseaux sont rangés en 3 classes: Chasseurs, Destroyers, Artillerie.
Chaque classe est étudiée pour être particulièrement efficace contre une autre, et a elle même une classe prédateur.
{{
Chasseurs -> Artillerie
Artillerie -> Destroyers
Destroyers -> Chasseurs
}}
Outre ces avantages en combat, chaque classe dispose d'une capacité hors-combat, qui lui donne un attrait stratégique plus ou moins important suivant le style de jeu du joueur.
Les classes ont un score de spécialisation exprimé en pourcentage et qui se confond avec le score d'armure de la classe.

### Chasseurs ###
Les chasseurs sont des vaisseaux rapides mais peu résistants, spécialisés dans la destruction des vaisseaux d'artillerie.
En combat ils ont pour mission d'atteindre au plus vite l'artillerie ennemi et de la décimer, celle-ci étant particulièrement vulnérable en combat rapproché.
Pour ce faire ils doivent cependant traverser la ligne des destroyers ennemi dont les tirs sont puissants.

##Capacité spéciale##
> Les chasseurs ont systématiquement leur vitesse doublée par rapport à leur gabarit.
> De plus, ils sont étudiés pour pouvoir tracter (ou propulser, selon les modèles), les vaisseaux des autres classes, a condition toute fois d'être en nombre suffisant.
> Ainsi, une flotte composée de Chasseurs calcule sa vitesse suivant la formule :

> 

&lt;math&gt;

Flotte\_Vitesse = PlusLente(E\_Vitesse) **(1 + (Chasseurs\_Défense** (k + Chasseurs\_Armure) / Flotte\_Défense))

&lt;/math&gt;


> k étant une constante de réglage: 1 - Armure du plus petit gabarit, soit k = 0.5
> Dans la formule, on considère que Armure indique le taux de spécialisation de la sous flotte de Chasseurs.

### Destroyers ###
Les destroyers sont des vaisseaux blindés possédant une énorme puissance de feu à courte distance.
En combat ils ont donc pour mission de former une ligne de défense devant l'artillerie, et d'éliminer le plus de chasseurs ennemi possible.
Cependant ils sont les cibles de choix de l'artillerie ennemi, seule capable de percer efficacement leur blindage.

##Capacité spéciale##
> Les destroyers peuvent se mettre en position "défense" sur un corps céleste.
> Ils bénéficient alors d'un bonus (bonus à l'attaque, en réalité) si le corps céleste est attaqué.
> Le joueur doit mettre lui même les unités en mode "défense".
> Lorsque le joueur souhaite remobiliser ses destroyers, ceux-ci prennent un tour à ce redéployer avant d'être disponibles.

> 

&lt;math&gt;

Bonus~Attaque~des~Destroyers~fortifiés = Destroyers\_Attaque **k** Destroyers\_Armure

&lt;/math&gt;


> k étant un facteur de réglage, soit k = 1

### Artillerie ###
Les vaisseaux d'artillerie sont de lourds vaisseaux disposant de systèmes de visés longue porté et de missiles perforants.
En combat ils ont pour mission d'éliminer le plus de destroyers ennemi possible.
Cependant ils sont eux même tributaire du nombre de destroyer allié qui les protège des chasseurs ennemis contre lesquels ils ne peuvent rien.

##Capacité spéciale##
> Les unités d'artilleries, qui sont équipées de canons et d'appareils de visée longue portée, peuvent effectuer des bombardement de corps céleste à corps céleste.
> Le missile met un certain temps à arriver sur sa cible, et a un certain pourcentage de chance de faire mouche.
> Le temps de refroidissement du canon est proportionnel à la distance de tir, ainsi une artillerie doit attendre autant de temps avant de pouvoir procéder à un nouveau tir.

> 

&lt;math&gt;

Chance~de~toucher = k **Artillerie\_Défense** Artillerie\_Armure

&lt;/math&gt;


> k étant un facteur de réglage, il est tel que la flotte la plus énorme ne devrait pas avoir une chance de toucher suppérieure à 50%.

> La chance de toucher est amoindrie par l'éventuelle présence de module de défense sur le corps céleste:
> 

&lt;math&gt;

Chance~de~toucher\_finale = Chance~de~toucher - Protection

&lt;/math&gt;



> Si le missile fait mouche, un batiement choisi au hasard est détruit sur le corps céleste cible (le module gouvernemental étant toujours le dernier batiment détruit).
> Dans tous les cas, les caractéristiques et ressources du corps céleste visé sont entammé par le bombardement, suivant sa puissance :

> 

&lt;math&gt;

Puissance~missile = {k prime} **Artillerie\_Défense** Artillerie\_Armure

&lt;/math&gt;



> Le missile entame les caractéristiques de base du corps céleste avec la répartition suivante (a, b, c et d étant les constantes servant à "régler" le comportement du missile):

> 

&lt;math&gt;

Dégats = Puissance~missile = alpha **echelle\_carbone** degats_{carbone~brut} + b **echelle\_carbone** degats_{carbone~récolté} + c _echelle**{génération~pop}** degats_{génération~pop} + d **echelle\_pop** degats_{pop~actuelle}

&lt;/math&gt;_

> Note: L'assaillant n'a pas de retour sur le fait que son missile ai touché ou pas.

## Calcul des caractéristiques d'une flotte ##
Une flotte qui contient des vaisseaux représentant des 3 classes est modélisé par trois flottes équivalente de chaque classe.
Pour les déplacement, la Vitesse de la flotte est calculée suivant son effectif de chasseurs et la vitesse du plus lent de ses vaisseaux.
Pour les détails concernant le calcul des caractéristiques des flottes équivalente, [cdcReglesCombats|cf. les règles de combat].

## Calcul de la valeur d'un vaisseau ##
Afin de garantir un équilibre de prix et de puissance entre les vaisseaux, la formule suivante est proposée :
Soit 人 le coût en population.
Soit 炭 le coût en ressource (carbonne).
Soit k une constante permettant de changer l'échèlle de mesure, car on souhaite parler de ressource en milliers, mais se limiter à des nombres a deux chiffres pour les caractéristiques.
k=60



&lt;math&gt;

omega\_Attaque = omega\_Défense = 1

&lt;/math&gt;




&lt;math&gt;

omega\_Arme = omega\_Armure = 1/3

&lt;/math&gt;




&lt;math&gt;

omega\_Vitesse = 5

&lt;/math&gt;





&lt;math&gt;

2\*人 + 炭 = k **( (omega\_Attaque** Attaque) + (omega\_Défense **Défense) + (omega\_Arme** Attaque **Arme) + (omega\_Armure** Défense **Armure) + (omega\_Vitesse** Vitesse) )

&lt;/math&gt;



Voici la liste des différents vaisseaux :

### Sonde (900人/300炭) ###
La sonde est un petit vaisseau d'exploration composé d'un module de voyage et d'un module de communication lequel est largué du premier lorsque la zone destination est atteinte.
Exempt de tout dispositif d'attaque, il permet en revanche de scanner les zones voisines dans la limite de sa porté (3Z).
Elle peut être envoyé sur toute zone vide ou corps céleste et y demeurer, la limite de porté étant définie par la porté de rayonnement du soleil, rayonnement nécéssaire à l'autonomie de la sonde (module communication).
A son lancement, une zone est choisie comme destination, dès que la sonde l'atteind, elle s'y installe et ne pourra plus en bouger.
La sonde permet de voir les déplacements des flottes se trouvant a proximité (le bout de trajet, sous forme de segment, avec des informations sur la flotte aperçue).
note: Une flotte qui croiserait une sonde sur sa trajectoire peut en dénoncer la présence à son arrivée, mais ne peux pas la détruire car l'attaque est impossible en hyper-space.
note: Une sonde déployée sur la zone d'un corps céleste étranger peut à tout moment se faire détruire par celui-ci dès lors qu'il possède une flotte ou un module de défense.
{{
> Attaque: 0
> Defense: 1
> Vitesse: 1.5 Z/T
}}

### Missiles anti-sonde (0人/200炭) ###
Les missiles anti-sondes sont comme leur nom l'indique des missiles dédiés à la destruction des sondes.
Chaque missile est lancé avec pour cible une sonde localisée qu'il détruiera à son arrivée.
Dans le cas où la sonde n'est déjà plus présente à l'arrivé du missile, celui-ci est perdu (et s'auto-détruit).
Dans le cas où le missile cible une sonde présente dans la même zone qu'un corps céleste, le missile est détruit avant de pouvoir atteindre la sonde (à condition que le corps céleste dispose d'unitées de défense, vaisseau ou module de défense hostile à la destruction de la sonde).
{{
> Attaque: 1
> Defense: 1
> Vitesse: 3Z/T
}}

### Vaisseau gouvernemental (200人/200炭) ###
Le vaisseau gouvernemental ne peut être produit que sur la planète disposant du module gouvernemental et sert à transporter le gouvernement (en fuite, par exemple).
C'est un vaisseau particulièrement rapide, équipé d'armes légères en cas d'imprévu (on préfèrera cependant généralement lui adjoindre une flotte complète).
Lorsque le vaisseau gouvernemental est en déplacement, toute communication est impossible pour le joueur, c'est à dire qu'il ne peut plus donner d'ordre a ses planètes et flottes, il est contraint de passer ses tours jusqu'à ce que son gouvernement arrive à se poser.
Le joueur récupère la communication dès que le vaisseau est à l'arrêt. Cependant, si le corps sur lequel est posé le gouvernement est sous effet pulsar, seul ce corps est sous contrôle.
{{
> Attaque: A DEFINIR
> Defense: A DEFINIR
> Vitesse: 2.5 Z/T
}}

### Vaisseaux léger (A DEFINIR人/A DEFINIR炭) ###
Les vaisseaux léger de chaque classes partagent les mêmes caractéristiques de base.
On souhaite qu'un vaisseau léger BN puisse abattre 3 vaisseaux légers TdT et survivre.
{{
> Attaque: A DEFINIR
> Defense: A DEFINIR
> Arme de classe: A DEFINIR
> Defense de classe: A DEFINIR
}}

### Vaisseaux moyen (A DEFINIR人/A DEFINIR炭) ###
Les vaisseaux moyen de chaque classes partagent les mêmes caractéristiques de base.
On souhaite qu'un vaisseau moyen BN puisse abattre 4 vaisseaux moyen TdT et survivre.
On souhaite qu'un vaisseau moyen puisse abattre 10 vaisseaux Ego légers et survivre.
{{
> Attaque: A DEFINIR
> Defense: A DEFINIR
> Arme de classe: A DEFINIR
> Defense de classe: A DEFINIR
}}



### Vaisseaux lourds (A DEFINIR人/A DEFINIR炭) ###
Les vaisseaux lourds de chaque classes partagent les même caractéristiques de base.
On souhaite qu'un vaisseau lourd BN puisse abattre 5 vaisseaux lourds TdT et survivre.
On souhaite qu'un vaisseau lourd puisse abattre 10 vaisseux Ego moyens et survivre.
{{
> Attaque: A DEFINIR
> Defense: A DEFINIR
> Arme de classe: A DEFINIR
> Defense de classe: A DEFINIR
}}