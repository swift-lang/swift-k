#! /usr/bin/perl


# create 16x16

foreach (<src/images/32x32/co/*.png>) {

	$from = $_; 
	s/32/16/g;
	$to = $_; 
	printf "%s -> %s\n", $from, $to;
	
        system "convert -depth 8 -colors 32 -quality 95 -resize 16x16 $from $to";

}

#BW 16

foreach (<src/images/32x32/co/*.png>) {

	$from = $_; 
	s/32/16/g;
	$to = $_; 
	s/co/bw/g;
	$bw = $_;
	printf "%s -> %s\n", $from, $bw;
	system "convert -depth 8 -quality 95 -colors 16 -colorspace gray  $from -resize 16x16 $bw";

}


# BW 32 x32

foreach (<src/images/32x32/co/*.png>) {
	$from = $_; 
	s/co/bw/g;
	$bw = $_;
	printf "%s -> %s\n", $from, $bw;
        system "convert -depth 8 -quality 95 -colors 32 -colorspace gray  $from -resize 32x32 $bw";


}

