<?php
require("./fonctions.php");

$fichier = $_GET["fichier"];

$fichier = VerifCheminFils($fichier);
$ext = substr($fichier, strrpos($fichier, ".")+1);
if (($fichier === false) || ($ext == "php"))
{
	die("Erreur");
}

$pos = strrpos($fichier, "/");
$nomfichier = substr($fichier, strrpos($fichier, "/")+1);

//die("filename \"".$fichier."\"<br>filesize \"".filesize($fichier)."\"");

header("Content-disposition: attachment; filename=\"".$nomfichier."\"");
header("Content-Type: application/force-download"); // "application/octet-stream"
//header("Content-Transfer-Encoding: $type\n"); // Surtout ne pas enlever le \n
header("Content-Length: ".filesize($fichier));
header("Pragma: no-cache");
header("Cache-Control: must-revalidate, post-check=0, pre-check=0, public");
header("Expires: 0");
readfile($fichier);
?>
