

&lt;nowiki&gt;




&lt;style type="text/css"&gt;


#quote, .quote
{
> border: 2px black dashed;
> padding: 5px;
}


&lt;/style&gt;




&lt;/nowiki&gt;


# Syntaxe #
{{
Cette page énumère les syntaxes reconnues par le moteur wiki.
  1. Ligne #summary pour indiquer la rubrique de la page dans le sommaire.
  1. Balises 

&lt;nowiki&gt;



&lt;/nowiki&gt;

 pour afficher le contenu sans modification.
  1. Balises `  ` pour mettre le texte en préformaté classe CSS "quote", sans html.
  1. Balises {{  }} pour mettre le texte en pré-formaté, sans html.
  1. Balises <QUOTE  QUOTE> pour mettre le contenu dans un div classe CSS "quote".
  1. Hors balises, remplace toutes les url par leur lien.
  1. [ ] pour créer des liens ou des images.
    1. [ext\_link|text] pour créer un lien externe si celui-ci commence par "http"
    1. [int\_link|text] pour créer un lien interne.
    1. [img|w,h] pour afficher une image (fichier terminant par .jpg, .jpeg, .png, .gif ou .bmp).
  1. Balise 

&lt;math&gt;



&lt;/math&gt;

 pour afficher des formules math avec phpmathpublisher.
  1. Balises 

&lt;nohtml&gt;



&lt;/nohtml&gt;

 pour afficher le code html tel quel.
  1. Balise // // pour mettre le texte en italique.
  1. Balise ## ## pour mettre le texte en gras.
  1. Balise   pour mettre le texte en souligné.
  1. Balise  pour mettre le texte en barré.
  1. Balises = = pour mettre le texte en titre.
  1. Hors balises, plus de 10 tirets '-' alignés sont remplacés par une ligne.
  1. Caractère ¤ indenté en début de ligne pour insérer une liste non ordonnée.
  1. Caractère # indenté en début de ligne pour insérer une liste ordonnée.
  1. Marqueur || pour dessiner des tableaux.
  1. Sauter une ligne revient à faire un nouveau paragraphe p.
  1. Revenir à la ligne revient à faire un retour chariot br.
}}

---

```
#summary nom de rubrique[ : nom de sous rubrique[...]]
```


&lt;nohtml&gt;

Est utilisé en première ligne du document pour spécifier la position de la page dans le sommaire, et n'est pas affiché sur la page.

&lt;/nohtml&gt;



---

TODO: nowiki

---

```
<nowiki>{{{</nowiki>
Contenu pouvant inclure des <html> balises </html> et autres caractères non interprétés.
	Garde la mise en forme (sauts de lignes, indentations).
<nowiki>}}}</nowiki>
```


&lt;nohtml&gt;

Hors de balises HTML, affiche le contenu comme source, en pré-formaté en appliquant la classe de style 'quote'.

&lt;/nohtml&gt;



```
Contenu pouvant inclure des <html> balises </html> et autres caractères non interprétés.
	Garde la mise en forme (sauts de lignes, indentations).
```

---

```
<nowiki>{{</nowiki>
Contenu pouvant inclure des <html> balises </html> et autres caractères non interprétés.
	Garde la mise en forme (sauts de lignes, indentations).
<nowiki>}}</nowiki>
```


&lt;nohtml&gt;

Hors de balises HTML, affiche le contenu comme source, en pré-formaté.

&lt;/nohtml&gt;



{{
Contenu pouvant inclure des 

&lt;html&gt;

 balises 

&lt;/html&gt;

 et autres caractères non interprétés.
> Garde la mise en forme (sauts de lignes, indentations).
}}

---

```
<nowiki>&lt;QUOTE</nowiki>
Contenu affiché dans un div de classe CSS "quote". Le contenu est également ##interprété## wiki, et les balises html sont <u>conservées</u>.
<nowiki>QUOTE&gt;</nowiki>
```


&lt;nohtml&gt;

Englobe le contenu dans un div de classe CSS "quote".

&lt;/nohtml&gt;



<QUOTE
Contenu affiché dans un div de classe CSS "quote". Le contenu est également ##interprété## wiki, et les balises html sont <u>conservées</u>.
QUOTE>

---

```
http://www.google.fr/
```
<nowiki nohtml>Hors de balises HTML, les liens commençant par http:// ou ftp:// sont automatiquement remplacé par leur balise <a href='http://www.google.fr'><a href='http://www.google.fr'>http://www.google.fr</a></a>.

Unknown end tag for &lt;/nowiki&gt;



http://www.google.fr/

---

```
[http://www.google.fr/|Recherche sur Google]
[http://www.google.fr]
[pageWiki|Label du lien]
[pageWiki]
[img/logo.png|100,30]
[img/logo.png]
```


&lt;nohtml&gt;

Insère un lien vers une page wiki, un url, ou une image.

&lt;/nohtml&gt;



[sur Google](http://www.google.fr/|Recherche)
http://www.google.fr
[pageWiki|Label du lien]
[pageWiki](pageWiki.md)
[img/logo.png|100,30]
[img/logo.png]

---

```
<math size=10>FlotteEquivalente_Défense = sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{V_Défense}</math>
```


&lt;nohtml&gt;

Génère l'image de la formule écrite, à la taille donnée, cf. la doc de la lib utilisée (phpmathpublisher).

&lt;/nohtml&gt;





&lt;math size=10&gt;

FlotteEquivalente\_Défense = sum{pour~chaque~vaisseau~V}{dernier~vaisseau}{V\_Défense}

&lt;/math&gt;



---

```
<nohtml>
Je peux <b>écrire</b> des balises <s>HTML</s> qui seront affichées <u>comme</u> source.
</nohtml>
```


&lt;nohtml&gt;

Tout les caractères html contenu entre les balises nohtml seront converti en code caractère, et seront donc affiché comme le source.

&lt;/nohtml&gt;





&lt;nohtml&gt;

Je peux <b>écrire</b> des balises <s>HTML</s> qui seront affichées <u>comme</u> source.

&lt;/nohtml&gt;



---

```
//Texte en italique//
```


&lt;nohtml&gt;

Hors de balises HTML, est remplacé par <i>Texte en italique</i>.

&lt;/nohtml&gt;



//Texte en italique//

---

```
##Texte en gras##
```


&lt;nohtml&gt;

Hors de balises HTML, est remplacé par <b>Texte en gras</b>.

&lt;/nohtml&gt;



##Texte en gras##

---

```
__Texte souligné__
```


&lt;nohtml&gt;

Hors de balises HTML, est remplacé par <u>Texte souligné</u>.

&lt;/nohtml&gt;



Texte souligné

---

```
~~Texte barré~~
```


&lt;nohtml&gt;

Hors de balises HTML, est remplacé par <s>Texte barré</s>.

&lt;/nohtml&gt;



~~Texte barré~~

---

```
==Titre de niveau 2==
```


&lt;nohtml&gt;

Les lignes dont le premier caractère est = et dont le même nombre de caractère = encadrent un texte sont changé en titre du niveau correspondant au nombre de caractère = utilisé.
Le titre est également automatiquement encadré d'une ancre <a name='<?php urlencode('Titre de niveau 2');?>', et suivi d'un retour chariot <p>.<br>
<br>
Unknown end tag for </nohtml><br>
<br>
<br>
<br>
<h2>Titre de niveau 2</h2>
<hr />
<pre><code>----------<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Hors de balises HTML, une suite de 10 tiret '-' ou plus est remplacée par une ligne horizontale, <br>
<br>
<hr><br>
<br>
.<br>
<br>
Unknown end tag for </nohtml><br>
<br>
<br>
<br>
<hr />
<hr />
<pre><code>¤ Liste non ordonné 1<br>
	¤ Liste non ordonnée 1.1<br>
	¤ Liste non ordonnée 1.2<br>
		¤ Liste non ordonnée 1.2.1<br>
	¤ Liste non ordonnée 1.3<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Hors de balises HTML, les lignes dont le premier caractère est ¤ sont changé en liste non ordonnée, du rang donné par leur indentation (tab).<br>
<br>
</nohtml><br>
<br>
<br>
<br>
¤ Liste non ordonné 1<br>
<blockquote>¤ Liste non ordonnée 1.1<br>
¤ Liste non ordonnée 1.2<br>
<blockquote>¤ Liste non ordonnée 1.2.1<br>
</blockquote>¤ Liste non ordonnée 1.3<br>
<hr />
<pre><code># Liste ordonné 1<br>
	# Liste ordonnée 1.1<br>
	# Liste ordonnée 1.2<br>
		# Liste ordonnée 1.2.1<br>
	# Liste ordonnée 1.3<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Hors de balises HTML, les lignes dont le premier caractère est # sont changé en liste ordonnée, du rang donné par leur indentation (tab).<br>
<br>
</nohtml><br>
<br>
</blockquote>

# Liste ordonné 1<br>
<ol><li>Liste ordonnée 1.1<br>
</li><li>Liste ordonnée 1.2<br>
<ol><li>Liste ordonnée 1.2.1<br>
</li></ol></li><li>Liste ordonnée 1.3<br>
<hr />
<pre><code>||Colonne 1 Ligne 1||Colonne 2 Ligne 1||<br>
||Colonne 1 Ligne 2|| Colonne 2 Ligne 2||<br>
||Colonne 1 Ligne 3||<br>
||		   ||Colonne 2 Ligne 4||<br>
||||Colonne 2 Ligne 5||<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Hors de balises HTML, le marqueur || est utilisé pour dessiner des tableaux.<br>
<br>
</nohtml><br>
<br>
</li></ol>

<table><thead><th>Colonne 1 Ligne 1</th><th>Colonne 2 Ligne 1</th></thead><tbody>
<tr><td>Colonne 1 Ligne 2</td><td> Colonne 2 Ligne 2</td></tr>
<tr><td>Colonne 1 Ligne 3</td></tr>
<tr><td>		               </td><td>Colonne 2 Ligne 4</td></tr>
<tr><td>Colonne 2 Ligne 5</td><td> </td></tr>
<hr />
<pre><code>Pour séparer deux paragraphes, je laisse une ligne vide entre ceux-ci.<br>
<br>
Ici débute mon second paragraphe.<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Insère un saut de ligne <p> à la place de chaque ligne vide.<br>
<br>
Unknown end tag for </nohtml><br>
<br>
</tbody></table>

Pour séparer deux paragraphes, je laisse une ligne vide entre ceux-ci.<br>
<br>
Ici débute mon second paragraphe.<br>
<hr />
<pre><code>Je peut utiliser le retour à la ligne normalement, et couper mon paragraphe après ce point.<br>
De sorte qu'il continue sans saut de ligne à la ligne suivante.<br>
</code></pre>
<br>
<br>
<nohtml><br>
<br>
Insère un saut de ligne <br /> à chaque retour à la ligne.<br>
<br>
</nohtml><br>
<br>
<br>
<br>
Je peut utiliser le retour à la ligne normalement, et couper mon paragraphe après ce point.<br>
De sorte qu'il continue sans saut de ligne à la ligne suivante.<br>
<hr />