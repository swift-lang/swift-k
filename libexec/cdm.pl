
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

sub cdm_property
{
    my $hash = $_[0];
    my $name = $_[1];

    $result = "UNSET";
    foreach (keys %$hash)
    {
	$property = $_;
	if ($name eq $property)
	{
	    $result = "$$hash{$property}\n";
	    last;
	}
    }
    print "$result\n";
}

# Command-line arguments:
$task = $ARGV[0];
$arg  = $ARGV[1];

# Read fs.data off of stdin:
@keys = ();
%map  = ();
%properties = ();
while (<STDIN>)
{
    chomp;

    my $comment = index $_, "#";
    if ($comment >= 0) {
	$_ = substr($_, 0, $comment);
    }

    next if $_ eq "";

    @tokens = split(/[ \t]+/, $_);
    $type   = shift(@tokens);
    if ($type eq "rule")
    {
	$key = shift(@tokens);
	if (! defined $map{$key})
	{
	    $rest = join(' ', @tokens);
	    @keys = (@keys, $key);
	    $map{$key} = $rest;
	}
    }
    elsif ($type eq "property")
    {
	$property = shift(@tokens);
	$rest   = join(' ', @tokens);
	$properties{$property} = $rest;
    }
    else
    {
	die "Unknown directive: $type";
    }
}

# Do the user task:
if ($task eq "lookup")
{
    cdm_lookup(\@keys, \%map, $arg);
}
elsif ($task eq "property")
{
    cdm_property(\%properties, $arg);
}
else
{
    die "Unknown task: $task";
}
