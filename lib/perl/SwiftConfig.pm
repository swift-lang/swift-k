package SwiftConfig;

use strict;
use warnings;

use File::Copy;
use File::Path;
use Pod::Usage;
use FindBin qw($Bin);
use lib "$FindBin::Bin/../lib/perl";   # Use libraries in $swift_home/lib/perl
use SwiftConfig;
use Getopt::Long;
use File::Which qw(which where);
use XML::Simple;

require Exporter;
our @ISA = qw(Exporter);
our %EXPORT_TAGS = ( 'all' => [ qw() ] );
our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );
our @EXPORT = qw(create_directory get_entry add_ssh remove_ssh 
update_tc_hostname update_xml write_file print_directory strip_directory
copy_file cat_file add_application edit_application remove_application
list_applications initialize_swiftconfig update_site_applications import_tc
import_xml edit_profile add_to_group list_group remove_from_group
);
our $VERSION = '0.01';

# Prepare data
my $xml = new XML::Simple();
my @execution_providers =
    ( "gt4", "local", "pbs", "condor", "ssh", "coaster", );

# Create a new directory if it does not exist
sub create_directory {
    my $directory = $_[0];
    if ( !-d "$directory" ) {
        mkdir "$directory"
            or die "Unable to create directory $directory\n";
    }
}

# Process keyboard input
sub get_entry {
    my ( $entry_description, $entry_value, $use_default, @allowable_values, ) = @_;

    # If use_default is specified, automatically plug in the default value
    if($use_default) {
        return $entry_value;
    }
    
   if(!@allowable_values) {
        print "$entry_description [$entry_value]: ";
    }
    else {
        print "$entry_description [";
        my $counter=0;
        foreach my $allowable(@allowable_values) {
            if($counter != 0) {
                print " ";
            }
            if($allowable eq $entry_value) {
                print "*$allowable";
            }
            else {
                print "$allowable";
           }
           $counter++;
        }
        print "]: ";
   }
    my $new_value = <STDIN>;
    chomp($new_value);
    if ($new_value) {
        $entry_value = $new_value;
    }

    # Check if value entered is valid (if valid values were passed)
    my $is_valid = 0;
    if (@allowable_values) {
        foreach my $allowable (@allowable_values) {
            if ( $allowable eq $entry_value ) {
                $is_valid = 1;
            }
        }
        if ( !$is_valid ) {
            my $error_message = q{};
            foreach my $allowable (@allowable_values) {
                $error_message = $error_message . $allowable . ' ';
            }
            print 'Invalid value selected. Please select from: '
                . "$error_message\n\n";
            $entry_value = get_entry( $entry_description, $entry_value, 0, 
                @allowable_values, );
        }
    }
    return $entry_value;
}

# Add new entry to auths.default
sub add_ssh {
    my ( $ssh_site, $ssh_url) = @_;
        
    # Open authfile
    my $auth_file = "$ENV{'HOME'}/.ssh/auth.defaults";
    if( -e "$auth_file") {
        open( AUTH_FILE, "<$auth_file")
            || die "Unable to open $auth_file for reading\n";
    }
    else {
        open( AUTH_FILE, "+>$auth_file" )
            || die "Unable to open $auth_file for read/write!\n";
    }
    my @auth_data = <AUTH_FILE>;
    close(AUTH_FILE);

    # Get existing values and modify if found
    my $ssh_username;
    my $ssh_password;
    my $found = 0;
    foreach (@auth_data) {
        if (/username/i && /^$ssh_url/i) {
            ( my $setting, $ssh_username, ) = split( '=', $_ );
            chomp($ssh_username);
            $ssh_username = get_entry( 'Username',   $ssh_username, 0);
            $_ = "$ssh_url.username=$ssh_username\n";
            $found = 1;
        }
        if (/\.password/i && /^$ssh_url/i) {
            ( my $setting, $ssh_password, ) = split( '=', $_ );
            chomp($ssh_password);
            $ssh_password = get_entry( 'Password', $ssh_password, 0);
            $_ = "$ssh_url.password=$ssh_password\n";
            $found = 1;
        }
    }

    # Add new entry if needed
    if(!$found) {
        $ssh_username = getlogin();
        $ssh_username = get_entry( 'Username',   $ssh_username, 0 );
        $ssh_password = get_entry( 'Password', '', 0);

        # Password based authentication
        push( @auth_data, "$ssh_url.type=password\n" );
        push( @auth_data, "$ssh_url.username=$ssh_username\n" );
        push( @auth_data, "$ssh_url.password=$ssh_password\n" );
    }
    
    # Write data
    write_file( $auth_file, @auth_data );
}

# Remove an entry from auth.defaults
sub remove_ssh {
    my ( $ssh_url, ) = @_;
    
    # Open auth.defaults
    my $auth_file = "$ENV{'HOME'}/.ssh/auth.defaults";
    if( -e "$auth_file") {
        open( AUTH_FILE, "<$auth_file")
            || die "Unable to open $auth_file for reading\n";
    }
    else {
        return;
    }
    my @auth_data = <AUTH_FILE>;
    close(AUTH_FILE);
    
    # Remove 
    foreach (@auth_data) {
        if (/^$ssh_url/i) {
            $_ = '';
        }
    }
    
    # Write data
    write_file( $auth_file, @auth_data );
}  

# Update TC with correct hostname
sub update_tc_hostname {
    my ( $tc_filename, $tc_host ) = @_;

    # Store TC data
    open( TC_FILESTREAM, "$tc_filename" )
        or die("Unable to open tc file $tc_filename!");
    my @tc_data = <TC_FILESTREAM>;
    close(TC_FILESTREAM);

    foreach my $line (@tc_data) {

        # Ignore comments
        my $first_character = substr( $line, 0, 1 );
        if ( $first_character eq '#' ) {
            next;
        }

        # Replace old entry with new entry
        my ($line_tc_host,   $line_tc_name,     $line_tc_path,
            $line_tc_status, $line_tc_platform, $line_tc_profile,
        ) = split( /\s+/, $line );
        $line =
              "$tc_host\t"
            . "$line_tc_name\t"
            . "$line_tc_path\t"
            . "$line_tc_status\t"
            . "$line_tc_platform\t"
            . "$line_tc_profile\n";
    }

    write_file( $tc_filename, @tc_data );
}

# Update XML hash with values from command line
sub update_xml {

    # Set up data
    my ($xml_filename, $edit_mode, $option_default) = @_;
    # Values for edit mode
    # 0 - Edit all
    # 1 - All but name
    # 2 - Customized.. to do
    
    if ( !-e $xml_filename ) {
        die "Unable to update xml file $xml_filename\n";
    }

    my $xml_ref = $xml->XMLin(
        $xml_filename,
        ForceArray => [qw(workdirectory profile)],
        KeyAttr    => [],
    );

    # Handle
    if($edit_mode == 0) {
        $xml_ref->{handle} = get_entry( 'Site Entry Name', $xml_ref->{handle}, $option_default);
    }
    
    # Execution
    my $initial_exprovider = $xml_ref->{execution}{provider};
    $xml_ref->{execution}{provider} = get_entry(
        'Execution Provider',
        $xml_ref->{execution}{provider},
        $option_default,
        @execution_providers,
    );
    my $current_exprovider = $xml_ref->{execution}{provider};
    
    # Handle changes in execution provider
    if( $initial_exprovider ne $current_exprovider ) {
        # Add SSH fields
        if( $current_exprovider eq 'ssh' ) {
            if( !$xml_ref->{execution}{url} ) {
                $xml_ref->{execution}{url} = 'unknown';
            }
        }
        # Add coaster fields
        if( $current_exprovider eq 'coaster' ) {
            if( !$xml_ref->{execution}{jobmanager} ) {
                $xml_ref->{execution}{jobmanager} = 'unknown';
            }
            if( !$xml_ref->{execution}{url} ){ 
                $xml_ref->{execution}{url} = 'unknown';
            }
        }
    }
    
    # Job manager
    if ( $xml_ref->{execution}{jobmanager}) {
        $xml_ref->{execution}{jobmanager} =
                get_entry( 'Execution Job Manager',
                $xml_ref->{execution}{jobmanager},
                $option_default
             );
    }
    
    # Execution URL
    if ( $xml_ref->{execution}{url} ) {
            $xml_ref->{execution}{url} = 
                get_entry( 'Execution URL', 
                            $xml_ref->{execution}{url}, 
                            $option_default 
                );
    }
        
    # Grid FTP
    if ( $xml_ref->{gridftp} ) {
        $xml_ref->{gridftp}{url} =
            get_entry( 'GridFTP URL', $xml_ref->{gridftp}{url}, $option_default);
    }

    # Work directory
    if ( $xml_ref->{workdirectory} ) {
        $xml_ref->{workdirectory}[0] =~ s/\$HOME/$ENV{'HOME'}/;
	$xml_ref->{workdirectory}[0] =~ s/\$USER/$ENV{'USER'}/;
        $xml_ref->{workdirectory} =
            [ get_entry( 'Work Directory', $xml_ref->{workdirectory}[0], $option_default) ];
    }

    # Job manager
    if ( $xml_ref->{jobmanager} ) {
        if ( $xml_ref->{jobmanager}{universe} ) {
            $xml_ref->{jobmanager}{universe} =
                get_entry( 'Job Universe', $xml_ref->{jobmanager}{universe}, $option_default);
        }
        if ( $xml_ref->{jobmanager}{url} ) {
            $xml_ref->{jobmanager}{url} =
                get_entry( 'Job Manager URL', $xml_ref->{jobmanager}{url}, $option_default);
        }
        if ( $xml_ref->{jobmanager}{major} ) {
            $xml_ref->{jobmanager}{major} =
                get_entry( 'Job Major Number',
                $xml_ref->{jobmanager}{major}, $option_default);
        }
        if ( $xml_ref->{jobmanager}{minor} ) {
            $xml_ref->{jobmanager}{minor} =
                get_entry( 'Job Minor Number',
                $xml_ref->{jobmanager}{minor}, $option_default);
        }
    }

    # Filesystem
    if ( $xml_ref->{filesystem} ) {
        if ( $xml_ref->{filesystem}{provider} ) {
            $xml_ref->{filesystem}{provider} = get_entry(
                'Filesystem Provider',
                $xml_ref->{filesystem}{provider},
                $option_default,
            );
        }
        if ( $xml_ref->{filesystem}{url} ) {
            $xml_ref->{filesystem}{url} =
                get_entry( 'Filesystem URL', $xml_ref->{filesystem}{url}, $option_default);
        }
    }

    # Profiles
    foreach my $profile ( @{ $xml_ref->{profile} } ) {
        $profile->{content} =
            get_entry( $profile->{key}, $profile->{content}, $option_default);
    }
    return $xml_ref;
}

# Write a file given variable and filename
sub write_file {
    my ( $filename, @data ) = @_;
    open( TEMPFILESTREAM, ">$filename" )
        or die("Unable to open $filename!\n");
    print TEMPFILESTREAM @data;
    close(TEMPFILESTREAM);
}

# Print all files in a directory
sub print_directory {
    my ($directory) = @_;
    chdir($directory)
        || die "Unable to change directories to $directory\n";
    my @files = <*>;
    foreach my $file (@files) {
        ( my $basename, my $ext ) = split( /\./, $file );
        my @path = split( '/', $basename );
        print "$basename\n";
    }
}

# Append one file to another
sub cat_file {
    my ( $src, $dst ) = @_;
    if ( -e $dst ) {
        open( DSTFILE, ">>$dst" ) || die "Unable to open $dst for append\n";
    }
    else {
        open( DSTFILE, ">$dst" )
            or die "Unable to open $dst for writing\n";
    }
    open( SRCFILE, $src ) || die "Unable to open $src for reading\n";
    foreach my $line (<SRCFILE>) {
        print DSTFILE $line;
    }
}

# Copy a file to a new location
sub copy_file {
    my ( $src, $dst ) = @_;
    if ( !-e $src ) { die "Unable to access $src\n"; }
    if ( !-e $dst ) { die "Unable to access $dst\n"; }
    copy( $src, $dst ) or die "Unable to copy $src to $dst\n";
}

# Strip directory name out of a string
sub strip_directory {
    my ($fullpath) = @_;
    my @path = split( '/', $fullpath );
    my $filename = $path[$#path];
    return $filename;
}

# Add an application
sub add_application {
    my ($app_filename) = @_;
    open(APPFILE, "+<$app_filename") || die "Unable to open application file $app_filename for writing\n";

    print "Enter name of new application: ";
    my $app_name = <STDIN>;
    chomp($app_name);
    foreach(<APPFILE>){ 
        my ($site, $name, $path, $status, $platform, $profile) = split(/\s+/);
        if($name eq $app_name) {
            print "$name already exists\n";
            return;
        }
    }
    my $app_path_guess = which("$app_name");
    if(!$app_path_guess) {
        $app_path_guess = "/path/to/$app_name";
    }
    my $app_path = get_entry("Path", $app_path_guess, 0);
    my $app_time = get_entry("Walltime (hh:mm:ss)", "00:20:00", 0);
    while($app_time !~ m/(\d+:\d+:\d+)/ ) {
        print "Error: incorrect format\n";
        $app_time = get_entry("Walltime", $1, 0);
    }
    print APPFILE "SWIFTCONFIGURATION\t$app_name\t$app_path\tINSTALLED\tINTEL32::LINUX\tGLOBUS::maxwalltime=\"$app_time\"\n";
    close(APPFILE);
    print "Added $app_name\n";
}
            
# Remove an application
sub remove_application {
    my ($app_filename) = @_;
    open(APPFILE, "$app_filename") || die "Unable to open application file $app_filename\n";
    my $app_to_remove = get_entry("Name of application to remove", '', 0);
    my @app_data;
    my $found=0;
    foreach my $line (<APPFILE>){
        my ($site, $name, $path, $status, $platform, $profile) = split(/\s+/, $line);
        if($name eq $app_to_remove) {
            $found=1;
        }
        else {
            push(@app_data, $line);
        }    
    }
    close(APPFILE);
    if(!$found) {
        print "Unable to find application named $app_to_remove\n";
        return;
    }
    else {
        write_file($app_filename, @app_data);
        print "Removed $app_to_remove\n";
    }
}

# List applications
sub list_applications {
    my ($app_filename) = @_;
    open(APPFILE, $app_filename) || die "Unable to open application file $app_filename\n";
    my @app_data = <APPFILE>;
    close(APPFILE);
    my ($site, $name, $path, $status, $platform, $profile);
    my $counter=0;
    my $header = sprintf("\n%-15s %-30s %-35s\n", 'Command Name', 'Path', 'Profile');
    $header .= sprintf("%-15s %-30s %-35s\n",    '------------', '----', '-------');
    print $header;
    foreach(@app_data) {
        ($site, $name, $path, $status, $platform, $profile) = split(/\s+/);
        if($name) {
            my $nicely_formatted = sprintf("%-15s %-30s %-35s", $name, $path, $profile);
            print "$nicely_formatted\n";
        }
    }

}

# Edit application
sub edit_application {
    my ($app_filename) = @_;
    open(APPFILE, $app_filename) || die "Unable to open application file $app_filename\n";
    my @app_data = <APPFILE>;
    close(APPFILE);
    print "Application to edit: ";
    my $app_to_edit = <STDIN>;
    chomp($app_to_edit);

    my $found=0;
    foreach(@app_data) {
        my ($site, $name, $path, $status, $platform, $profile) = split(/\s+/);
        if( $name eq $app_to_edit) {
                $found=1;
                my $previous_name = $name;
                # App name
		        $name = get_entry("New application name", $name, 0);
                if($name ne $previous_name) {
                    $path = which("$name");
                }
                # Path
                if(!$path) {
                    $path = "/path/app";
                }
                $path = get_entry("New path", $path, 0);
                # Time
                my $time = '';
                my $wallfound=0;
                my $counter=0;
                my @profiles = split(/;/, $profile);
                foreach (@profiles) {
                    if( m/maxwalltime=\"(\d+:\d+:\d+)\"/ig ) {
                        $wallfound=1;
                        my $default_time = $1;
                        $time = get_entry("Walltime", $default_time, 0);
                        while($time !~ m/(\d+:\d+:\d+)/ ) {
                            print "Error: incorrect format\n";
                            $time = get_entry("Walltime", $default_time, 0);
                        }
                        s/(\d+:\d+:\d+)/$time/g;
                    }
                }
                if( !$wallfound ) {
                    $time = get_entry("Time", "00:20:00", 0);
                    push(@profiles, "GLOBUS::maxwalltime=\"$time\"");
                }
                $profile = join(';', @profiles);            
                $_ = "$site\t$name\t$path\t$status\t$platform\t$profile\n";
       }
   }

    if(!$found) {
        print "Unable to find application named $app_to_edit\n";
        return;
    }
    else {
        print "$app_to_edit updated\n";
    }
    open(APPFILE, ">$app_filename") || die "Unable to open application file $app_filename for writing\n";
    print APPFILE @app_data;
    close(APPFILE);
}

# Initialize swiftconfig. Prepare things it needs for first run
sub initialize_swiftconfig {
    create_directory("$ENV{'HOME'}/.swift");
    create_directory("$ENV{'HOME'}/.swift/sites");
    create_directory("$ENV{'HOME'}/.swift/groups");
    if(!-e "$ENV{'HOME'}/.swift/apps") {
        create_directory("$ENV{'HOME'}/.swift/apps");
        my @app_files = glob("$FindBin::Bin/../etc/apps/*.apps");
        foreach my $app_file(@app_files) {
            copy_file($app_file, "$ENV{'HOME'}/.swift/apps");
        }
    }
}

# Associate application sets with a configuration
sub update_site_applications {
    my ($site, $use_defaults) = @_;
    my $apps = '';
    
    if(! -d "$ENV{'HOME'}/.swift/sites/$site") {
        print "Unable to find configuration for $site\n";
        return;
    }
    
    if(-e "$ENV{'HOME'}/.swift/sites/$site/apps") {
        open(APPLIST, "$ENV{'HOME'}/.swift/sites/$site/apps")
            || die "Unable to open app list $ENV{'HOME'}/.swift/sites/$site/apps";
        my @apps_file_data = <APPLIST>;
        close(APPLIST);
        foreach my $current_app (@apps_file_data) {
            chomp($current_app);
            if($apps) { 
                $apps = "$apps $current_app";
            }
            else {
                $apps = "$current_app";
            }
       }       
    }
    
    if(!$use_defaults) {
        my @all_available_appsets = glob("$ENV{'HOME'}/.swift/apps/*.apps");
        print "Available application sets: ";
        foreach(@all_available_appsets) {
           $_ = strip_directory($_);
           ($_, my $junk) = split('\.');
            print "$_ ";
        }
        print "\n";
    }
    
    if(!$apps) {
        $apps = "linux";
    }
    
    $apps = get_entry("Select application sets for $site", $apps, $use_defaults);    
    my @all_list = split(/ /, $apps);
    open(APPFILE, ">$ENV{'HOME'}/.swift/sites/$site/apps")
        || die "Unable to open $ENV{'HOME'}/.swift/sites/$site/apps for writing!\n";
        
    foreach(@all_list) {
        print APPFILE "$_\n";
    }
    close(APPFILE);
    
    if(!$use_defaults) {
        print "Successfully saved application sets for $site\n\n";
    }
}

# Import existing XML file into swiftconfig style format
sub import_xml {
    my ($xml_filename) = @_;
    if(! -e $xml_filename) {
        die "Unable to find file to import: $xml_filename\n";
    }
    
    my $xml_ref = $xml->XMLin(
        $xml_filename,
        ForceArray => [qw(workdirectory profile pool)],
        KeyAttr    => "pool",
    );
    
    foreach my $pool ( @{$xml_ref->{pool}} ) {
        if(-d "$ENV{'HOME'}/.swift/sites/$pool->{handle}") {
            print "Configuration for $pool->{handle} already exists. Skipping\n";
        }
        else {
            create_directory("$ENV{'HOME'}/.swift/sites/$pool->{handle}");
            my $xml_out_ref = $xml->XMLout(
                $pool,
                RootName      => 'pool',
                SuppressEmpty => 1,
            );
            write_file("$ENV{'HOME'}/.swift/sites/$pool->{handle}/sites.xml", 
                        $xml_out_ref );
            update_site_applications($pool->{handle});
        }
    }
}

# Import tc.data files into swiftconfig application set
sub import_tc {
    my ($tc_filename) = @_;
    if(! -e $tc_filename) {
        die "Unable to find file to import: $tc_filename\n";
    }
    open(TCFILE, $tc_filename) || die "Unable to open $tc_filename for reading\n";
    my @tc_data = <TCFILE>;
    close(TCFILE);
    foreach(@tc_data) {
        my ($site, $name, $path, $status, $platform, $profile) = split(/\s+/);
        $site = "SWIFTCONFIGURATION";
        if(!$name) { 
            $_ = ''; 
            next; 
        }
        my $first_character = substr( $_, 0, 1 );
        if ( $first_character eq '#' ) {
            $_ = '';
            next;
        }
        $_ = "$site\t$name\t$path\t$status\t$platform\t$profile\n";
   }
   print "Enter name to call this application set: ";
   my $set_name = <STDIN>;
   chomp($set_name);
   
   if(-e "$ENV{'HOME'}/.swift/apps/$set_name.apps") {
       print "Set named $set_name already exists\n";
       my $yesno = get_entry("Overwrite $set_name?", "No", 0, ("yes", "no"));
       if($yesno eq "no") {
           return;
       }
   }

   write_file("$ENV{'HOME'}/.swift/apps/$set_name.apps", @tc_data);
   print "Successfully imported application set named $set_name\n";
}

sub edit_profile {
    my ($xml_filename, @profiles_to_edit) = @_;
    if(! -e $xml_filename) {
	print "Unable to edit profile on $xml_filename\n";
	return;
    }

    my $xml_ref = $xml->XMLin(
        $xml_filename,
        ForceArray => [qw(workdirectory profile)],
        KeyAttr    => [],      
    );

    
    foreach my $profile ( @{ $xml_ref->{profile} } ) {
	foreach my $profile_to_edit(@profiles_to_edit) {
		if(lc($profile->{key}) eq lc($profile_to_edit)) {
        		$profile->{content} =
            			get_entry( $profile->{key}, $profile->{content}, 0);
        	}
    	}
    }

    return $xml_ref;
}

sub add_to_group {
    my ($group_filename) = @_;
    if(!-e $group_filename) {
        print "Unable to find file $group_filename for editing\n";
        return;
    }
    open(GROUPFILE, ">>$group_filename") || die "Error opening $group_filename\n";
    my @all_sites = glob("$ENV{'HOME'}/.swift/sites/*");
    foreach(@all_sites) {
        $_ = strip_directory($_);
    }
    my $new_site = get_entry("Enter name of site to add", '', 0, @all_sites);
    
    print GROUPFILE "$new_site\n";
    close(GROUPFILE);    
}

sub list_group {
    my ($group_filename) = @_;
    if(!-e $group_filename) {
        print "Unable to find $group_filename for editing\n";
        return;
    }
    system("cat $group_filename");
}

sub remove_from_group {
    my ($group_filename) = @_;
    if(!-e $group_filename) {
        print "Unable to find $group_filename for editing\n";
        return;
    } 
    
    open(GROUPFILE, $group_filename) || die "Error opening $group_filename\n";
    my @group_data = <GROUPFILE>;
    close(GROUPFILE);
    
    my $remove_group = get_entry("Enter name of site to remove", '', 0);
    $remove_group .= "\n";
    
    my $found=0;
    foreach(@group_data) {
        if($_ eq $remove_group)
        {
            $found=1;
            $_ = '';
        }
    }
    if(!$found) {
        print "Unable to find site $remove_group. Use 'l' to get a list\n";
    }
    write_file($group_filename, @group_data);
}
1;
__END__

=head1 SwiftConfig

SwiftConfig - Perl module for swiftconfig and swiftrun applications

=head1 SYNOPSIS

  use SwiftConfig;

=head1 DESCRIPTION

The SwiftConfig module provides a set of standard routines needed to handle
Swift Configuration.

create_directory(directory_name)
    Creates a directory, handles errors

get_entry($entry_description, $entry_value, $use_default, @allowable_values)
    get_entry is used to get input from the user
    $entry_description is used to describe the data to the user
    $entry_value is the default value. If the user hits enter, it will be used
    $use_default, if true, will set input to $entry_value without prompting
    @allowable_values is a list of all available options for that input

add_ssh($ssh_site, $ssh_url)
    Add an entry to ~/.ssh/auth.defaults
    $ssh_site is the swift configuration name
    $ssh_url is the network name/address to connect to

remove_ssh($ssh_url)
    Remove an entry from ~/.ssh/auth.defaults
    $ssh_url is the network name/address to remove
    
update_tc_hostname($filename, $tc_hostname)
    Change the site name in a tc.data file
    $filename is the filename of the tc.data
    $tc_hostname is the value of the new hostname
    
update_xml($xml_filename, $edit_mode, $option_default)
    Edit and update a sites.xml file
    $xml_filename filename to edit
    There are currently two edit modes: 0=edit everything, 1=everything but name
    $option_default, if true, will automatically use defaults
    
write_file($filename, @data)
    Write to $filename with @data. Handle errors
    
print_directory($directory)
    List all files within a directory without paths   
    
cat_file($src, $dst)
    Concatenate $src to $dst, handle errors
    
copy_file($src, $dst)
    Copy file $src to $dst, handle errors
    
strip_directory($fullpath)
    Given a string like "/foo/blah/file.txt", return "file.txt"
    
add_application($filename)
    Add an application to an application set
    
remove_application($filename)
    Remove an application from an application set
    
list_applications($filename)
    List all applications contained in $filename

edit_application($filename)
    Edit an application in $filename

initialize_swiftconfig()
    Set up swiftconig directory structure and copy required files

update_site_applications($site, $use_defaults)
    Associate application sets with a configuration
    $site is the name of the configuration
    If $use_defaults > 0, it will save without prompting
    
import_xml($filename)
    Import existing site configuration(s) from $filename
    
import_tc($filename)
    Import an application set from an existing tc.data file
    
=cut
