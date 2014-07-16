#!/usr/bin/perl

use File::Copy;
#Available variables (work in toc and main list)
# @JNLP@ - a ref to the jnlp
# @PROJECT@ - the project name
# @M.NAME@ - the short module name
# @M.LONGNAME@ - the long module name
# @M.VERSION@ - the module version
# @M.DESCRIPTION@ - the module description (if there is one)
# @M.ICON@ - the module icon
# @M.SCREENSHOT@ - a module screenshot (if any)
# @M.SCREENSHOTTHM@ - the thumbnail of the above
# @L.NAME@ - short launcher name
# @L.LONGNAME@ - long launcher name
# @L.DESCRIPTION@ - launcher description
# @L.ICON@ - the launcher icon
# @L.SCREENSHOT@ - a screenshot
# @L.SCREENSHOTTHM@ - a thumbnail of the above

#the structure goes like this:
#	head
#		toc-head
#			toc-module-head-{i}
#				toc-launcher-item-{i}-{j}
#			toc-module-tail-{i}
#		toc-tail
#		main-head
#			main-module-head-{i}
#				main-launcher-item-{i}-{j}
#			main-module-tail-{i}
#		main-tail
#	tail


$cogdir="../..";
open(VER, "$cogdir/VERSION");
@ver=<VER>;
chomp(@ver);
close(VER);
@vera=split(/\=/,$ver[0]);
$version=$vera[1];
$version =~ s/[ \t]*//g;
print("Version: $version\n");
$webstartdir="$cogdir/dist/cog-$version/webstart";
$index="$webstartdir/index.html";
$project="Java CoG Kit";
$thumbnailheight = 100;
open(INDEX, ">$index");

sub filter {
	print "$fname\n";
	$fname=shift;
	open(FILE, $fname);
	@lines=<FILE>;
	foreach $line (@lines){
		if ($line =~ /(.*)\@M\.DESCRIPTION\@(.*)/){
			$line = "$1$mdescription$2";
		}
		if ($line =~ /(.*)\@L\.DESCRIPTION\@(.*)/){
			$line = "$1$ldescription$2";
		}
		$line =~ s/\@JNLP\@/$jnlp/;
		$line =~ s/\@PROJECT\@/$project/;
		$line =~ s/\@M\.NAME\@/$mname/;
		$line =~ s/\@M\.LONGNAME\@/$mlongname/;
		$line =~ s/\@M\.VERSION\@/$mversion/;
		$line =~ s/\@M\.ICON\@/$micon/;
		$line =~ s/\@M\.SCREENSHOT\@/$mscreenshot/;
		$line =~ s/\@M\.SCREENSHOTTHM\@/$mscreenshotthm/;
		$line =~ s/\@L\.NAME\@/$lname/;
		$line =~ s/\@L\.LONGNAME\@/$llongname/;
		$line =~ s/\@L\.ICON\@/$licon/;
		$line =~ s/\@L\.SCREENSHOT\@/$lscreenshot/;
		$line =~ s/\@L\.SCREENSHOTTHM\@/$lscreenshotthm/;
		print INDEX $line;
		
	}
	close(FILE);
}

sub processline{
	$endmodule = 0;
	$endlauncher = 0;
	$tline = shift;
	chomp($tline);
	($name, $value) = split(/:/, $tline);
	$name =~ s/[\t\s]*//;
	$value =~ s/[\t\s]*//;
	if ($name eq ""){
		return;
	}
	if (index($value, "\$") == 0){
		$value = "";
	}
	if ($name eq "module"){
		$module = 1;
		$launchers = 0;
		$mname = $value;
	}
	elsif ($name eq "launcher"){
		$module=0;
		$launchers = $launchers+1;
		$lname=$value;
	}
	elsif ($module == 1){
		if ($name eq "project"){
			$project = $value;
		}
		elsif ($name eq "long-name"){
			$mlongname = $value;
		}
		elsif ($name eq "version"){
			$mversion = $value;
		}
		elsif ($name eq "endmodule"){
			$endmodule = 1;
		}
		else{
			print "Unrecognized property: $name\n";
		}
	}
	else{
		if ($name eq "long-name"){
			$llongname = $value;
		}
		elsif ($name eq "version"){
			if ($value eq ""){
				$lversion = $mversion;
			}
			else{
				$lversion = $value;
			}
		}
		elsif ($name eq "jnlp"){
			$jnlp = $value;
		}
		elsif ($name eq "endlauncher"){
			$endlauncher = 1;
			$module = 1;
		}
		else{
			print "Unrecognized property: $name\n";
		}
	}
}

sub copyandthumbnail {
	$sourcedir = shift;
	$source = shift;
	$target = shift;
	$targetthm = shift;
	if (copy("$sourcedir/$source","$webstartdir/$target")){
		not system("convert", 
			"$webstartdir/$target", 
			"-resize",
			"x$thumbnailheight", 
			"$webstartdir/$targetthm") or die("Could not create thumbnail. Is ImageMagick installed?");
		return 1;
	}
	else{
		return 0;
	}
}

sub resolvemodulemeta {
	$metadir = "$cogdir/modules/$mname/meta";
	$mdescription = "";
	if (open(DESC, "$metadir/description.txt")){
		while(<DESC>){
			$mdescription = "$mdescription$_";
		}
		close(DESC);
	}
	if (copy("$metadir/icon.png","$webstartdir/$mname-icon.png")){
		$micon = "$mname-icon.png";
	}
	else{
		$micon = "na-icon.png";
	}
	if (copyandthumbnail("$metadir", "screenshot.png", "$mname-screenshot.png", "$mname-screenshot-thm.png")){
		$mscreenshot = "$mname-screenshot.png";
		$mscreenshotthm = "$mname-screenshot-thm.png";
	}
	elsif (copyandthumbnail("$metadir", "screenshot.jpg", "$mname-screenshot.jpg", "$mname-screenshot-thm.jpg")){
		$mscreenshot = "$mname-screenshot.jpg";
		$mscreenshotthm = "$mname-screenshot-thm.jpg";
	}
	else{
		$mscreenshot = "";
		$mscreenshotthm = "na-thm.png";
	}
}

sub resolvelaunchermeta {
	$metadir = "$cogdir/modules/$mname/meta/$lname";
	$ldescription = "";
	if (open(DESC, "$metadir/description.txt")){
		while(<DESC>){
			$ldescription = "$ldescription$_";
		}
		close(DESC);
	}
	if (copy("$metadir/icon.png","$webstartdir/$mname-$lname-icon.png")){
		$licon = "$mname-$lname-icon.png";
	}
	else{
		$licon = $micon;
	}
	if (copyandthumbnail("$metadir", "screenshot.png", "$mname-$lname-screenshot.png", "$mname-$lname-screenshot-thm.png")){
		$lscreenshot = "$mname-$lname-screenshot.png";
		$lscreenshotthm = "$mname-$lname-screenshot-thm.png";
	}
	elsif (copyandthumbnail("$metadir", "screenshot.jpg", "$mname-$lname-screenshot.jpg", "$mname-$lname-screenshot-thm.jpg")){
		$lscreenshot = "$mname-$lname-screenshot.jpg";
		$lscreenshotthm = "$mname-$lname-screenshot-thm.jpg";
	}
	else{
		$lscreenshot = "";
		$lscreenshotthm = "na-thm.png";
	}
}

copy("na-icon.png", "$webstartdir/na-icon.png");
copy("na-thm.png", "$webstartdir/na-thm.png");
filter("head.html");
filter("toc-head.html");
open(TREE, "<$webstartdir/launcher-tree.txt") or die("Could not open $webstartdir/launcher-tree.txt");
@tlines = <TREE>;
for $tline (@tlines){
	processline($tline);
	if ($endmodule){
		if ($launchers != 0){
			filter("toc-module-tail.html");
		}
	}
	if ($endlauncher){
		if ($launchers == 1){
			resolvemodulemeta();
			filter("toc-module-head.html");
		}
		resolvelaunchermeta();
		filter("toc-launcher-item.html");
	}
}
close(TREE);
filter("toc-tail.html");
filter("main-head.html");
open(TREE, "<$webstartdir/launcher-tree.txt") or die("Could not open $webstartdir/launcher-tree.txt");
@tlines = <TREE>;
for $tline (@tlines){
	processline($tline);
	if ($endmodule){
		if ($launchers != 0){
			filter("main-module-tail.html");
		}
	}
	if ($endlauncher){
		if ($launchers == 1){
			resolvemodulemeta();
			filter("main-module-head.html");
		}
		resolvelaunchermeta();
		filter("main-launcher-item.html");
	}
}
close(TREE);
filter("main-tail.html");
filter("tail.html");
close(INDEX);