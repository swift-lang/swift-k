#!/bin/perl
if (opendir(DIR, "lib")) {	
	
	$output_file=">tmp";
	open(OUTPUT_FILE, $output_file);
	@entries = readdir(DIR);
	$scalar = @entries;
	print $scalar;
	closedir(DIR);
	foreach (@entries) {	
		if ($_ ne "." && $_ ne "..") {
			$entry = "<jar href=\""."lib/".$_."\"/>";			
			print OUTPUT_FILE $entry;
			print OUTPUT_FILE "\n";
		}		
	}
	close(OUTPUT_FILE);
	
	
}