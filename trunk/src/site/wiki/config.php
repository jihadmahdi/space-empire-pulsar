<?php
define("APP_CONFIG", true);

if (defined("CGIKI_CONFIG") === false)
{
	require_once("cgiki/config.php");
}

$ENABLE_TRACE = false;

$ENABLED_TRACE =	array(
//'external-url-functions',
						'generate_summary',
						//'gikiplugin-render-MyCWS',
						//'content',
//'gikiplugin-inc-MyCWS'
//'gikiplugin-render-MyCWS'
);

// Application config
$appDir = "sep/wiki/";

//print $wwwAbsoluteDir."; ".$appDir."<br>";
$appDir = removeCommonSubPaths($wwwAbsoluteDir, $appDir);
//die($wwwAbsoluteDir."; ".$appDir);

$appAbsoluteDir = $wwwAbsoluteDir.$appDir;
//$appAbsoluteLocation = "http://".$wwwServerName."/".$appDir;
$appAbsoluteLocation = "http://".$wwwServerName."/wiki/"; // Because of sub-domain link

// The directory data is stored in (must be world-writable and, if possible, above
// the web root). This can be a relative path, but must have the trailing slash.
//

// Local cache directory
$cacheSubDir = "cache";
$cache_dir = $appAbsoluteDir.$cacheSubDir;
define("GIKI_CACHE_VERSION", "1.0");

// E.P.

// external url for $nodedir (i.e. "http://www.myrepository.com/svn/wiki/") will work, but readonly.
$nodedir = "http://space-empire-pulsar.googlecode.com/svn/wiki/";

$APP_TITLE = "Space Empire Pulsar";

// Have to define if the nodedir is an external url or not, if it is, wiki pages will be readonly.
$nodedir_is_external_url = true;

// New option to add automaticaly <BR> to line returns (except in tables, <pre>...</pre>, <ul>...</ul> and <ol>...</ol>)
$add_br = true;

// New option to reverse links syntax between TITLE and TEXT.
$option_reverse_links = true;

// Should edit the regex used to parse the nodedir index page.
// This regex must select only *.wiki files ("name.wiki") on the match index specified by $nodedir_index_parser_match.
/*
 exemple for:
 <li><a href="files.ext">files.ext</a></li>
 <li><a href="OneNode.wiki">OneNode.wiki</a></li>
 */
$nodedir_index_parser = "/^<li><a href=\"(.*\.".$wiki_ext.")\">(.*\.".$wiki_ext.")<\/a><\/li>$/";
$nodedir_index_parser_match = 1;

// new option to disable login (disable login + guests can't post = readonly).
$disable_login = $nodedir_is_external_url || false;

// new option to disable history feature.
$disable_history = $nodedir_is_external_url || false;

// new option to disable editing
$disable_edit = $nodedir_is_external_url || false;


// Should this wiki allow guests to post?
//
$allow_guests = !$nodedir_is_external_url && true;

// Should this wiki allow file uploading?
//
$allow_uploads = !$nodedir_is_external_url && true;

// Log all attempts to login or register?
//
$remotelog = false;

// The file used as a template for the entire script
//
$template = "viewer_template.html";

// These settings control how the bar is displayed.
// There is a prefix, a separator, and a postfix, examples would be:
//  $bar_prefix = "<table border=1><tr><td>";
//  $bar_separator = "</td><td>";
//  $bar_postfix = "</td></tr></table>";
// That example would turn the bar elements into a table, but there are many other possibilities
//
$bar_prefix = "";
$bar_separator = " - ";
$bar_postfix = "";

//PDF Gen options
$PDF_DOC_TITLE = $APP_TITLE." - %DATE%";
$PDF_DOC_SUBJECT = $APP_TITLE;
$AUTHORS = array();
$AUTHORS[] = "Auriol Arnaud (aaa-auriol@club-internet.fr)";
$AUTHORS[] = "Bézille Yohann (yohann.bezille@club-internet.fr)";
$AUTHORS[] = "Cabanes Romain (romain.cabanes@laposte.net)";
$AUTHORS[] = "Escallier Pierre (p.escallier@laposte.net)";
shuffle($AUTHORS);
$PDF_DOC_AUTHOR = join(", ", $AUTHORS);
$PDF_DOC_KEYWORDS = "space, empire, pulsar, strategy, stratégie, Konquest, game, jeu, espace, tour, JAVA";
$PDF_DOC_CREATOR = "MyCustomGiki <http://cgiki.axan.org/> (TCPDF libs <http://sourceforge.net/projects/tcpdf/>)";
$PDF_FILENAME_BASE = "sep";

// Ratio de redimensionnement des images dans le PDF (augmenter le ratio diminue la taille des images).
$PDF_IMAGE_SCALE_RATIO = 2;

// Image utilisée en en-tête et en couverture.
$PDF_LOGO_IMAGE = $appAbsoluteLocation."/img/logo.png";

// Image utilisée pour la racine du menu sommaire.
$SUMMARY_ROOT_IMAGE = $appAbsoluteLocation."/img/root.gif";

// Marge horizontale à garder à coté de l'image de couvertue.
$PDF_TITLE_HORIZONTAL_MARGIN = 30;

// Fixe la hauteur (l'image est redimensioinnée en gardant ses proportions) de l'image d'en-tête.
$PDF_HEADER_IMAGE_HEIGHT = 25;

// Marge de l'en-tête.
$PDF_HEADER_MARGIN = 5;

// Marge du pied de page.
$PDF_FOOTER_MARGIN = 5;

// Marge du bas.
$PDF_BOTTOM_MARGIN = 10;

// Marge du haut (devrait être > à $PDF_HEADER_IMAGE_HEIGHT).
$PDF_TOP_MARGIN = ($PDF_HEADER_IMAGE_HEIGHT / $PDF_IMAGE_SCALE_RATIO) + $PDF_BOTTOM_MARGIN;

// Marge de gauche.
$PDF_LEFT_MARGIN = 15;

// Marge de droite.
$PDF_RIGHT_MARGIN = 15;

// Plugins
// Here you can make an array of plugins to to alter node output
// To use GWS with no HTML for example:
//  $plugins = array("noHTML", "GWS");
//
$plugins = array("MyCWS");
?>
