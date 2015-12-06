# Modélisation UML #
Le projet étant en JAVA sous Eclipse, le choix a été fait d'utiliser de préférence des plugins Eclipse lorsque c'est possible.
Celà permet entre autre de profiter de la gestion en configuration (projet partagé en équipe sur un dépot SVN) de façon transparante quelque soit l'élément sur lequel on travaille.

Cette page liste les plugins UML disponibles pour Eclipse, seuls ceux compatible avec la version Europa ont été testé.
Les fonctionnalités souhaitées sont :
##Must##
> ¤ Modéliser des diagrammes de classes graphiquement.
> ¤ Synchronisation Diagramme/Code, ou support de la génération et du reverse.
> ¤ Compatible projets partagés (SVN).
##Should##
> ¤ Robuste, fiable, léger.
> ¤ Modéliser les autres diagrammes UML: diagrammes de séquence, diagrammes d'états-transitions.
##Can##
> ¤ Modéliser les autres diagrammes UML.

## Liste des plugins testés ##
|http://www.eclipse-plugins.info/eclipse/redirect_do.jsp?id=1598|Apollo	|Shareware.|
|:----------------------------------------------------------------------|:---------|
|[Studio](http://www.eclipse-plugins.info/eclipse/redirect_do.jsp?id=1540|eUML2)	|La version evaluation Studio propose la synchro diagrammes/code, les seuls bridages sont à l'exportation image, et la licence (non-commerciale).|
|<b><a href='http://www.eclipse-plugins.info/eclipse/redirect_do.jsp?id=1540|eUML2'>Free</a></b>|La version FREE offre déjà l'essentiel par rappot à la studio, et semble plus "légère": Synchro diagramme/code (gen/reverse), restrictions d'export et de licence en moins, on perds juste la génération de diagramme de séquence par rapport à la version Studio (le reste étant superflu).|
|http://www.eclipse-plugins.info/eclipse/redirect_do.jsp?id=1727|IdafeUML|Gratuit et léger, mais ne supporte que le reverse, pas la génération.|
|http://www.eclipse-plugins.info/eclipse/redirect_do.jsp?id=1760|ObjectAid|Gratuit, mais ne supporte que la synchro code->diagramme, pas l'inverse.|

## Choix ##
##eUML2 Free Edition## pour la conception de diagrammes avec synchronisation modèles/code.
[& Installation](http://www.soyatec.com/euml2/installation/|Téléchargement)