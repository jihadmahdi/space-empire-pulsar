# Interface #

Le jeu se jouant au tour par tour, l'interface du jeu pourrait être vue comme un menu proposant au joueur de choisir les commandes et actions à effectuer à chaque tour.
Certaines commandes devront être considérée comme "automatiquement exécutées", pour ce qui est de l'affichage des infos de l'univers par exemple.

Les paramètres de chaque commande sont ceux qui devront d'une manière où d'une autre pouvoir être spécifié par l'utilisateur (ex: sélection préalable) et l'algorithme qui suis ne concerne que les contrôles que pourraient faire l'interface cliente avant de valider la commande.

## Infos permanantes ##
  1. Afficher Univers##
> > Afficher volume et quadrillage des zones
> > Placement des corps célestes avec leur couleur
> > Placement des flottes alliées en déplacement
> > Placement des routes alliées
> > Placement des échanges alliées
> > Placement des sondes alliées, afficher leur vues

## Infos sur demande ##
X	##Mesure de distance(Zone1, Zone2)##

> ERREUR si Zone1 hors Univers
> ERREUR si Zone2 hors Univers

X	##AfficherDétails(Corps Céleste)##
> Afficher caracs de base (Corps Céleste)
> SI (EstAllié(Joueur, Corps Céleste) OU PresenceFlotteAllie(Joueur, CorpsCéleste)) ALORS
> > Afficher les caracs courants(Corps Céleste)

> FSI

X	##AfficherDétails(Flotte)##
> SI (EstAllié(Joueur, Flotte)) ALORS
> > Afficher caracs(Flotte)

> FSI

X	##AfficherDétails(Routes)##
> SI (EstAlliée(Joueur, Route)) ALORS
> > Afficher caracs(Route)

> FSI

X	##AfficherDétails(Echanges)##
> SI (EstAlliée(Joueur, Echange)) ALORS
> > Afficher caracs(Echange)

> FSI

X	##Synchroniser(Flotte, FlotteCible)##
> SI (!Possede(Joueur, Flotte)) ERREUR
> SI (EstAlliée(Joueur, FlotteCible)) ERREUR
> SI (EnDeplacement(FlotteCible)) ERREUR
> SI (EnDeplacement(Flotte)) ERREUR

## Actions ##
X	##Aller à (Flotte, Corps Céleste, Retard=0)##
> SI (!Possede(Joueur, Flotte)) ERREUR

X	##Attaquer (Flotte, Corps Céleste, Retard=0)##
> SI (!Possede(Joueur, Flotte)) ERREUR
> SI (EstAllie(Joueur, Corps Céleste)) ERREUR

X	##Construire (Corps Céleste, Construction)##
> SI (!Possede(Joueur, Corps Céleste)) ERREUR
> SI (EstPossible(Corps Céleste, Construction)) ERREUR
> SI (PeutPayer(Corps Céleste, Construction)) ERREUR
> SI (DejaConstruitACeTour(Corps Céleste)) ERREUR

X	##Détruire (Corps Céleste, Construction)##
> SI (!Possede(Joueur, Corps Céleste)) ERREUR
> SI (EstEtabli(Corps Céleste, Contruction) = 0) ERREUR

X	##Fabriquer vaisseaux (Corps Céleste, Vaisseau, Quantite)##
> SI (!Possede(Joueur, Corps Céleste)) ERREUR
> SI (EstEtabli(Corps Céleste, 'Usine Fabrication Vaisseaux') = 0) ERREUR
> SI (PeutPayer(Corps Céleste, Vaisseau, Quantite)) ERREUR
> SI (Vaisseau = 'Vaisseau Gouvernemental') ALORS
> > SI (Quantite > 1) ERREUR
> > SI (EstEtabli(Corps Céleste, 'Module Gouvernemental')) ERREUR

> FSI

X	##Construire Route Spaciale(Comptoir1, Comptoir2)##
> SI (!Possede(Joueur, Comptoir1)) ERREUR
> SI (EstAllié(Joueur, Comptoir2)) ERREUR
> SI (PeutPayerRoute(Comptoir1, Comptoir2)) ERREUR

X	##Detruire Route Spaciale(Route)##
> SI (!Possede(Joueur, Route)) ERREUR

X	##Passer commande ressource(Comptoir1, Comptoir2, Quantite)##
> SI (!Possede(Joueur, Comptoir1)) ERREUR
> SI (Possede(Joueur, Comptoir2)) ALORS
> > // Commerce intérieur, OK
> > RETOUR

> SINON
> > SI (Ennemi(Joueur, Comptoir2)) ERREUR
> > // Commerce extérieur, OK

X	##Lancer pulsar(PlaneteOrigine, Corps céleste Cible, Angle)##

> SI (!Possede(Joueur, PlaneteOrigine)) ERREUR
> SI (EstEtabli(PlaneteOrigine, 'Canon à Pulsar') = 0) ERREUR
> SI ((SousEffetPulsar(Joueur)) ET (ADesAllies(Joueur)) ERREUR

X	##Former flotte(Usine Fabrication Vaisseaux, Vaisseaux, Flotte=aucune)##
> SI (!Possede(Joueur, Usine Fabrication Vaisseaux)) ERREUR
> SI (SontDisponibles(Usine Fabrication Vaisseaux, Vaisseaux)) ERREUR
> SI ((Flotte != aucune) ET (!Possede(Joueur, Flotte))) ERREUR

X	##Lancer sonde(Sonde, Zone)##
> SI (!Possede(Joueur, Sonde)) ERREUR
> SI (Zone hors univers) ERREUR

X	##ChangerAttitudeDiplomatieGenerale(AutreJoueur, Attitude)##
> SI (EstAllié(Joueur, AutreJoueur)) ERREUR

X	##ChangerComportementDiplomatieFlotte(Flotte, Comportement)##
> SI (!Possede(Joueur, Flotte)) ERREUR

X	##AttaqueFlottesEnPresence(Corps Céleste, JoueursCibles)##
> SI (!Possede(Joueur, Corps Céleste)) ERREUR
> SI (EstAlliée(JoueurCible)) ERREUR
> SI (ForcesEnPresence(JoueursCibles, Corps Céleste) = 0) ERREUR
> SI (ForcesEnPresence(Joueur, Corps Céleste) = 0) ERREUR

## Chat ##

X	##Chatter(Msg, Filtre)##
> SI (SousEffetPulsar(Joueur)) ERREUR

## Diplomatie ##

X	##DemanderAlliance(JoueurCible)##
> SI (EstAllié(JoueurCible)) ERREUR

X	##DemanderExclusion(JoueurCible)##
> SI (EstAllié(JoueurCible)) ERREUR

X	##QuitterAlliance()##
> SI (EstEnAlliance(Joueur)) ERREUR

# Elements dépendants #
L'interface doit être capable d'afficher ces éléments en même temps.

Zone
> !> Zone, Sonde

Corps Céleste
> !> Corps Céleste, Retard, Angle, Construction, Vaisseaux, Flotte, Joueurs

Flotte
> !> Corps Céleste, Flotte, Retard, Comportement, Vaisseaux, Usine

Routes
Echanges
Sondes
> !> Zone
Constructions
> !> Corps Céleste

Vaisseaux
> !> Corps Céleste, Usine, Flotte, Quantite

Comptoir
> !> Comptoir, quantité

Usine
> !> Vaisseaux, Quantité

Attitude
> !> Joueurs

Comportement
> !> Flotte

Joueurs
> !> Corps Céleste, Msg, Attitude

Msg
> !> Joueurs