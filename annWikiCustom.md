# Wiki customisé #
Toute la doc, à commencer par le cahier des charges, est rédigée dans un format texte acceptant quelques balises simples de type wiki.
L'interpréteur Wiki est développé en PHP et permet entre autres :
  * La récupération des fichiers **.wiki sur un serveur externe,
  * l'interprétation de balises de mise en forme (style, listes, tableaux),
  * l'indexation automatique dans un sommaire global,
  * l'exportation au format PDF.**

## BUGS ##
L'export PDF souffre quelques distortions :
  * Les tableaux sont mal taillés,
  * les images trop larges dépassent,
  * le texte barré n'est plus barré.