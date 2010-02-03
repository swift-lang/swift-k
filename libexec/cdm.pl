
# CDM Lookup 

sub print_hash
{
    my $hash = $_[0];
    foreach (keys %$hash) 
    {
        print "$_ : $$hash{$_} \n";
    }
}

sub cdm_lookup
{
    my $hash = $_[0];
    my $file = $_[1];

    $result = "DEFAULT";
    foreach (keys %$hash)
    {
	$pattern = $_;
	if ($file =~ /$pattern/)
	{
	    print("$$hash{$pattern}\n");
	    last;
	}
    }
}

# Command-line arguments: 
$file = $ARGV[0];

# Read fs.data off of stdin: 
%map = ();
while (<STDIN>)
{
    chomp;
    next if $_ eq "";

    @tokens = split(/ /, $_); 
    $key    = shift(@tokens);
    $rest   = join(' ', @tokens);
    $map{$key} = $rest;
}

cdm_lookup(\%map, $file);
