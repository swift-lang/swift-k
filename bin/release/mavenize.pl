#!/usr/bin/perl
use strict;

my $dir=shift;
$|=1;
print STDERR "$dir\n";
$_=$dir;
/(.*)\-(.*)/;
my $id=$2;
print STDERR "$id\n";

open(MAVEN, ">$dir/project.xml");
print(MAVEN "<?xml version=\"1.0\"?>\n");
print(MAVEN "<project>\n");
print(MAVEN "	<pomVersion>3</pomVersion>\n");
print(MAVEN "	<id>$id</id>\n");
print(MAVEN "	<name>CoG-$id</name>\n");
print(MAVEN "	<currentVersion>0.2</currentVersion>\n");
print(MAVEN "	<dependencies>\n");

opendir(DIR, "$dir/jars");
my @jars = grep(/\.jar$/, readdir(DIR));

my($name,$jar,$version);
foreach (@jars) {
	if(/(([a-z_][a-z_0-9]*-)+)([0-9][0-9.\-]*)\.jar/) {
		$name = substr($1, 0, length($1) - 1);
		$version = $3; 
		print "Versioned: $name: $version\n";
		print(MAVEN "		<dependency>\n");
		print(MAVEN "			<groupId>cog-$id</groupId>\n");
		print(MAVEN "			<artifactId>$name</artifactId>\n");
		print(MAVEN "			<version>$version</version>\n");
		print(MAVEN "			<properties>\n");
		print(MAVEN "				<war.bundle>true</war.bundle>\n");
		print(MAVEN "			</properties>\n");
		print(MAVEN "		</dependency>\n");
		print(MAVEN "\n\n");
	}
	else {
		$name = substr($_, 0, length($_) - 4);
		$jar = $_;
		print "Unversioned: $jar\n";
		print(MAVEN "		<dependency>\n");
		print(MAVEN "			<groupId>cog-$id</groupId>\n");
		print(MAVEN "			<artifactId>$name</artifactId>\n");
		print(MAVEN "			<jar>$jar</jar>\n");
		print(MAVEN "			<properties>\n");
		print(MAVEN "				<war.bundle>true</war.bundle>\n");
		print(MAVEN "			</properties>\n");
		print(MAVEN "		</dependency>\n");
		print(MAVEN "\n\n");
	}
}

print(MAVEN "	</dependencies>\n");
print(MAVEN "</project>\n");
close(MAVEN);
