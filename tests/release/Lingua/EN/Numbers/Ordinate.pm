
require 5;
package Lingua::EN::Numbers::Ordinate;
use strict;
# Time-stamp: "2004-12-29 19:06:20 AST"
use vars qw(@ISA @EXPORT @EXPORT_OK $VERSION);
require Exporter;
@ISA = ('Exporter');
@EXPORT = ('ordinate');
@EXPORT_OK = ('ordsuf', 'th');
$VERSION = "1.02";

###########################################################################

=head1 NAME

Lingua::EN::Numbers::Ordinate -- go from cardinal number (3) to ordinal ("3rd")

=head1 SYNOPSIS

  use Lingua::EN::Numbers::Ordinate;
  print ordinate(4), "\n";
   # prints 4th
  print ordinate(-342), "\n";
   # prints -342nd

  # Example of actual use:
  ...
  for(my $i = 0; $i < @records; $i++) {
    unless(is_valid($record[$i]) {
      warn "The ", ordinate($i), " record is invalid!\n"; 
      next;
    }
    ...
  }

=head1 DESCRIPTION

There are two kinds of numbers in English -- cardinals (1, 2, 3...), and
ordinals (1st, 2nd, 3rd...).  This library provides functions for giving
the ordinal form of a number, given its cardinal value.

=head1 FUNCTIONS

=over

=item ordinate(SCALAR)

Returns a string consisting of that scalar's string form, plus the
appropriate ordinal suffix.  Example: C<ordinate(23)> returns "23rd".

As a special case, C<ordinate(undef)> and C<ordinate("")> return "0th",
not "th".

This function is exported by default.

=item th(SCALAR)

Merely an alias for C<ordinate>, but not exported by default.

=item ordsuf(SCALAR)

Returns just the appropriate ordinal suffix for the given scalar
numeric value.  This is what C<ordinate> uses to actually do its
work.  For example, C<ordsuf(3)> is "rd". 

Not exported by default.

=back

The above functions are all prototyped to take a scalar value,
so C<ordinate(@stuff)> is the same as C<ordinate(scalar @stuff)>.

=head1 CAVEATS

* Note that this library knows only about numbers, not number-words.
C<ordinate('seven')> might just as well be C<ordinate('superglue')>
or C<ordinate("\x1E\x9A")> -- you'll get the fallthru case of the input
string plus "th".

* As is unavoidable, C<ordinate(0256)> returns "174th" (because ordinate
sees the value 174). Similarly, C<ordinate(1E12)> returns
"1000000000000th".  Returning "trillionth" would be nice, but that's an
awfully atypical case.

* Note that this library's algorithm (as well as the basic concept
and implementation of ordinal numbers) is totally language specific.

To pick a trivial example, consider that in French, 1 ordinates
as "1ier", whereas 41 ordinates as "41ieme".

=head1 STILL NOT SATISFIED?

Bored of this...?

  use Lingua::EN::Numbers::Ordinate qw(ordinate th);
  ...
  print th($n), " entry processed...\n";
  ...

Try this bit of lunacy:

  {
    my $th_object;
    sub _th () { $th_object }

    package Lingua::EN::Numbers::Ordinate::Overloader;
    my $x; # Gotta have something to bless.
    $th_object = bless \$x; # Define the object now, which _th returns
    use Carp ();
    use Lingua::EN::Numbers::Ordinate ();
    sub overordinate {
      Carp::croak "_th should be used only as postfix!" unless $_[2];
      Lingua::EN::Numbers::Ordinate::ordinate($_[1]);
    }
    use overload '&' => \&overordinate;
  }

Then you get to do:

  print 3 & _th, "\n";
    # prints "3rd"
  
  print 1 + 2 & _th, "\n";
    # prints "3rd" too!
    # Because of the precedence of & !
  
  print _th & 3, "\n";
    # dies with: "th should be used only as postfix!"

Kooky, isn't it?  For more delightful deleria like this, see
Damian Conway's I<Object Oriented Perl> from Manning Press.

Kinda makes you like C<th(3)>, doesn't it?

=head1 COPYRIGHT

Copyright (c) 2000 Sean M. Burke.  All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the same terms as Perl itself.

=head1 AUTHOR

Sean M. Burke C<sburke@cpan.org>

=cut

###########################################################################

sub ordsuf ($) {
  return 'th' if not(defined($_[0])) or not( 0 + $_[0] );
   # 'th' for undef, 0, or anything non-number.
  my $n = abs($_[0]);  # Throw away the sign.
  return 'th' unless $n == int($n); # Best possible, I guess.
  $n %= 100;
  return 'th' if $n == 11 or $n == 12 or $n == 13;
  $n %= 10;
  return 'st' if $n == 1; 
  return 'nd' if $n == 2;
  return 'rd' if $n == 3;
  return 'th';
}

sub ordinate ($) {
  my $i = $_[0] || 0;
  return $i . ordsuf($i);
}

*th = \&ordinate; # correctly copies the prototype, too.

###########################################################################
1;

__END__
