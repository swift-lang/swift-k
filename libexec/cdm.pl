
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
    my $keys = $_[0];
    my $hash = $_[1];
    my $file = $_[2];

    $result = "DEFAULT";
    foreach (@$keys)
    {
	$pattern = $_;
	if ($file =~ /$pattern/)
	{
	    $result = "$$hash{$pattern}\n";
	    last;
	}
    }
    print "$result\n";
}

# Command-line arguments: 
$file = $ARGV[0];

# Read fs.data off of stdin: 
@keys = ();
%map  = ();
while (<STDIN>)
{
    chomp;
    next if $_ eq "";

    @tokens = split(/ /, $_); 
    $key    = shift(@tokens);
    $rest   = join(' ', @tokens);
    @keys   = (@keys, $key); 
    $map{$key} = $rest;
}

cdm_lookup(\@keys, \%map, $file);
