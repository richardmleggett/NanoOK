#!/usr/bin/perl

use strict;
use warnings;
use Getopt::Long;

my $help_requested;
my $basedir="/Users/leggettr/Documents/Projects/Nanopore";
my $reference;
my $getname;

&GetOptions(
'n|getname'       => \$getname,
'r|reference:s'   => \$reference,
'h|help'          => \$help_requested
);

print "\nnanotools_index_reference\n\n";

if (defined $help_requested) {
    print "Index reference sequences.\n\n";
    print "Usage: nanotools_index_reference.pl <-r reference>\n\n";
    print "Options:\n";
    print "    -r | reference     Path to reference FASTA\n";
    print "\n";
    
    exit;
}

die "You must specify a reference\n" if not defined $reference;

open(REFERENCE, $reference) or die "Can't open reference.\n";
open(OUTFILE, ">".$reference.".sizes") or die "Can't open output file.\n";

my $current_length = 0;
my $id = "";
my $name = "";

while(<REFERENCE>) {
    chomp(my $line = $_);
    
    if ($line =~ /^>(\S+)/) {
        if ($id ne "") {
            print OUTFILE $id, "\t", $current_length, "\t", $name, "\n";
        }
        $id = $1;
        $name = $id;
        $current_length = 0;
        
        if (defined $getname) {
            if ($line =~ /^>(\S+) (\S+) (\S+)/) {
                $name=$2."_".$3;
            }
        }
        
    } else {
        $current_length += length($line);
    }
}

if ($id ne "") {
    print OUTFILE $id, "\t", $current_length, "\t", $name, "\n";
}

close(OUTFILE);
close(REFERENCE);
