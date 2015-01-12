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
my $scheduler="NONE";
my $queue="Test128";

&GetOptions(
'b|basedir:s'   => \$basedir,
's|sample:s'    => \$sample,
'r|reference:s' => \$reference,
'q|queue:s'     => \$queue,
'x|scheduler:s' => \$scheduler,
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

if (! -d $basedir."/".$sample."/logs") {
    mkdir($basedir."/".$sample."/logs");
}

if (! -d $basedir."/".$sample."/logs/last") {
    mkdir($basedir."/".$sample."/logs/last");
}


my $in_dir = $basedir."/".$sample."/fasta";

if ((-d $in_dir."/pass") && (-d $in_dir."/fail")) {
    print "Got pass/fail directory\n";
    process_directory($basedir."/".$sample."/fasta/pass", $basedir."/".$sample."/last/pass", $basedir."/".$sample."/logs/last/pass");
    process_directory($basedir."/".$sample."/fasta/fail", $basedir."/".$sample."/last/fail", $basedir."/".$sample."/logs/last/fail");
} else {
    print "Got all-in-one directory\n";
    process_directory($basedir."/".$sample."/fasta", $basedir."/".$sample."/last", $basedir."/".$sample."/logs/last");
}

print "DONE\n";

sub process_directory {
    my $fasta_dir = $_[0];
    my $last_dir = $_[1];
    my $log_dir = $_[2];
    my @type = ("Template", "Complement", "2D");
 
    print "Processing directory\n";
    print "FASTA files: $fasta_dir\n";
    print " LAST files: $last_dir\n";
    
    if (! -d $last_dir) {
        mkdir($last_dir);
    }

    if (! -d $log_dir) {
        mkdir($log_dir);
    }
    
    for (my $i=0; $i<3; $i++) {
        my $input_dir=$fasta_dir."/".$type[$i];
        my $output_dir=$last_dir."/".$type[$i];

        if (! -d $output_dir) {
            mkdir($output_dir);
        }
        
        opendir(DIR, $input_dir) or die $!;
        while (my $file = readdir(DIR)) {
            next unless ($file =~ m/\.fasta$/);
            my $inpath = $input_dir."/".$file;
            my $outpath = $output_dir."/".$file.".maf";
            my $logpath = $log_dir."/".$file.".log";
            my $last_command = "lastal -s 2 -T 0 -Q 0 -a 1 ${reference} ${inpath} > ${outpath}";
            
            print "Aligning ".$inpath."\n";
            print "      to ".$outpath."\n";
            
            if ($scheduler eq "LSF") {
                system("bsub -q ${queue} -oo ${logpath} \"${last_command}\"");
            } elsif ($scheduler eq "PBS") {
                print("Error: PBS not yet implemented.");
            } else {
                system($last_command);
            }
        }
        closedir(DIR);
    }
}
