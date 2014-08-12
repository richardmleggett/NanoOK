#!/usr/bin/perl
#
# Program: nanotools_extract_reads
# Author:  Richard Leggett
# Contact: richard.leggett@tgac.ac.uk

use strict;
use warnings;
use Getopt::Long;

my $sample;
my $help_requested;
my $basedir="/Users/leggettr/Documents/Projects/Nanopore/";
my $reference=$basedir."/references/lambdadb";

&GetOptions(
'b|basedir:s'   => \$basedir,
's|sample:s'    => \$sample,
'r|reference:s' => \$reference,
'h|help'        => \$help_requested
);

print "\nnanotools_run_last\n\n";

if (defined $help_requested) {
    print "Run LAST alignments.\n\n";
    print "Usage: nanotools_run_last.pl <-s sample> [-b directory]\n\n";
    print "Options:\n";
    print "    -s | -sample       Sample name\n";
    print "    -b | -basedir      Base directory containing all sample directories\n";
    print "    -r | reference     Path to reference\n";
    print "\n";
    
    exit;
}

die "You must specify a sample name" if not defined $sample;

print "Base directory: $basedir\n";
print "Sample: $sample\n";

if (! -d $basedir."/".$sample."/last") {
    mkdir($basedir."/".$sample."/last");
}

my @type = ("Template", "Complement", "2D");
for (my $i=0; $i<3; $i++) {
    my $input_dir=$basedir."/".$sample."/fasta/".$type[$i];
    my $output_dir=$basedir."/".$sample."/last/".$type[$i];

    if (! -d $output_dir) {
        mkdir($output_dir);
    }
    
    opendir(DIR, $input_dir) or die $!;
    while (my $file = readdir(DIR)) {
        next unless ($file =~ m/\.fasta$/);
        my $inpath = $input_dir."/".$file;
        my $outpath = $output_dir."/".$file.".maf";
        print "Aligning ".$inpath."\n";
        print "      to ".$outpath."\n";
        system("lastal -s 2 -T 0 -Q 0 -a 1 ${reference} ${inpath} > ${outpath}");
    }
    closedir(DIR);
}

print "DONE\n";