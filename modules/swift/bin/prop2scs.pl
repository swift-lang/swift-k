#!/usr/bin/perl -w

use strict;
use warnings;
use File::Basename;

my %properties = ();		# Hash storing all swift properties
my @property_files = ();	# List of swift property files to be read
my $service = $ARGV[0];
if (!defined($service) || $service eq "-help") { 
   print STDERR "Usage: $0 service\n";
   exit 1;
}

# How to convert SCS options to environment variables
my %conversionTable = (
                   'ip'          => 'IPADDR',
                   'jobspernode' => 'JOBSPERNODE',
                   'jobthrottle' => 'JOBTHROTTLE',
                   'localport'   => 'LOCAL_PORT',
                   'mode'        => 'WORKER_MODE',
                   'serviceport' => 'SERVICE_PORT',
                   'tunnel'      => 'WORKER_TUNNEL',
                   'username'    => 'WORKER_USERNAME',
                   'workers'     => 'WORKER_HOSTS',
                   'work'        => 'WORK',
);

# Verify a property file exists and add it to the search path
sub add_property_file {
   my $property_file = $_[0];
   if( -e "$property_file" ) {
      push( @property_files, $property_file );
   }
}

# Verify Swift is found in PATH
my $swift_etc_directory = dirname(dirname(`which swift`)) . "/etc";
if( ! -d $swift_etc_directory ) { die "Unable to find a valid Swift installation"; }

# Set the search order for properties
&add_property_file("$swift_etc_directory/swift.properties");
&add_property_file("$ENV{SWIFT_SITE_CONF}/swift.properties") if defined($ENV{SWIFT_SITE_CONF});
&add_property_file("$ENV{HOME}/.swift/swift.properties");
&add_property_file("swift.properties");

# Set property values
foreach my $property_file(@property_files) {
   open(PROPERTIES, $property_file) || die "Unable to open $property_file";

   while( <PROPERTIES> ) {
      chomp;
      next if /^\s*#/ || /^(\s)*$/; # Ignore blank lines and comments
      $_ =~ s/^\s+//;               # Remove leading whitespace

      # Handle brackets 
      if( /^site\.|^service\./ && /{/ ) { 
         my $prefix = (split /\s+{/)[0];
         while( <PROPERTIES> ) {
            chomp;
            next if /^\s*#/ || /^(\s)*$/; 
            $_ =~ s/^\s+//;               
            if( /^}/ ) { last; } 
            my ($key, $value) = split('=', ($prefix . ".$_"), 2);
            if($key eq "sites") { $key = "site"; }
            $value =~ s/\$(\w+)/$ENV{$1}/g;
            $properties{ $key } = $value;
         }
      }

      else {
         my ($key, $value) = split('=', $_, 2);
         if($key eq "sites") { $key = "site"; }
         $value =~ s/\$(\w+)/$ENV{$1}/g; # Substitute environment variables
         $properties{ $key } = $value;
      }
   }
}

foreach my $key (sort keys %properties) {
   if($key =~ m/service\.$service/ ) { 
      my $val = (split /\./, $key)[-1];
      if ( defined( $conversionTable{ lc( $val )})) {
         print "export $conversionTable{lc($val)}=\"$properties{$key}\"\n";
      }
   }
}
