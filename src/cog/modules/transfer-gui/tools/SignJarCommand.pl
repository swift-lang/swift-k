#!/bin/perl
if (opendir(DIR, "lib")) {	
	
	$output_file=">tmp";
	open(OUTPUT_FILE, $output_file);
	@entries = readdir(DIR);
	closedir(DIR);
	$alias = "mykey";
	$keystore="mykeystore";
	$storepass="mcs123";
	`keytool -genkey -alias $alias -keystore $keystore -keypass mcs123 -storepass $storepass`;
	$sign_jar_cmd_base = "jarsigner -keystore $keystore -storepass $storepass ";
	$sign_jar_cmd = $sign_jar_cmd_base."gui.jar ".$alias;
	`$sign_jar_cmd`;
	foreach (@entries) {	
		if ($_ ne "." && $_ ne "..") {
			$entry = "lib/".$_;			
		  $sign_jar_cmd = $sign_jar_cmd_base.$entry." ".$alias;
		  print $sign_jar_cmd;
		  print "\n";
		  `$sign_jar_cmd`;
		  
		}		
	}
	
	close(OUTPUT_FILE);
	
	
}