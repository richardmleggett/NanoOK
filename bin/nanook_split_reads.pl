#!/usr/bin/perl
#
# Program: nanook_split_reads
# Purpose: Split FASTA/Q file into separate files for each read
# Author:  Richard Leggett
# Contact: richard.leggett@earlham.ac.uk

use strict;
use warnings;
use Getopt::Long;

my $input_file;
my $output_dir;
my $help_requested;
my %ids;
my $count = 0;
my $input_format;
my $output_format;
my $requested_output_format;
my $reads_per_chunk = 4000;

&GetOptions(
'f|outputfmt:s' => \$requested_output_format,
'i|input:s'     => \$input_file,
'o|outputdir:s' => \$output_dir,
'h|help'        => \$help_requested
);

if (defined $help_requested) {
    print "\nnanook_split_reads\n\n";
    print "Split a multi-read FASTA into separate files.\n\n";
    print "Usage: nanotools_split_reads.pl <-i input> [-o output_dir]\n\n";
    print "Options:\n";
    print "    -i | -input      Input FASTA/Q file\n";
    print "    -o | -outputdir  Output directory\n";
    print "    -f | -outputfmt  Output format FASTA or FASTQ\n";
    print "                     (defaults to same as input)\n";
    print "\n";
    
    exit;
}

die "You must specify an input file\n" if not defined $input_file;
die "You must specify an output directory\n" if not defined $output_dir;

if ($input_file =~ /.fq$/i) {
    $input_format = "FASTQ";
} elsif ($input_file =~ /.fastq$/i) {
    $input_format = "FASTQ";
} elsif ($input_file =~ /.fa/i) {
    $input_format = "FASTA";
} elsif ($input_file =~ /.fasta$/i) {
    $input_format = "FASTA";
} else {
    die "Can't determine input file format from filename.\n";
}

if (defined $requested_output_format) {
    $output_format = uc($requested_output_format);
    
    if ($input_format eq "FASTA") {
        if ($output_format ne "FASTA") {
            $output_format = "FASTA";
            print "Defaulting to FASTA output for FASTA input\n";
        }
    } else {
        if (($output_format ne "FASTA") && ($output_format ne "FASTQ")) {
            $output_format = $input_format;
            print "Unknown output format - defaulting to ".$output_format."\n";
        }
    }
} else {
    $output_format = $input_format;
}

print " Input format: $input_format\n";
print "Output format: $output_format\n";

local $| = 1;

my $chunk = 0;

open(INPUTFILE, $input_file) or die "Can't open input ".$input_file."\n";

while(<INPUTFILE>) {
    my $header_line = $_;
    my $sequence;
    my $qual_id;
    my $qualities;
    my $read_id;

    if (($count % $reads_per_chunk) == 0) {
        mkdir($output_dir."/".$chunk);
    }
    
    if ($input_format eq "FASTA") {
        if ($header_line =~ /^>(\S+)/) {
            $read_id = $1;
        } else {
            die "Couldn't get read ID from $header_line\n";
        }
        $sequence = <INPUTFILE>;
    } else {
        if ($header_line =~ /^@(\S+)/) {
            $read_id = $1;
        } else {
            die "Couldn't get read ID from $header_line\n";
        }
        $sequence = <INPUTFILE>;
        $qual_id = <INPUTFILE>;
        $qualities = <INPUTFILE>;
    }
    
    
    if (not defined $ids{$read_id}) {
        $ids{$read_id} = 1;
    } else {
        print "\nWARNING: Repeat ID $read_id\n";
        my $i=2;
        my $newid;
        do {
            $newid = $read_id."_".$i;
            $i++;
        } while (defined $ids{$newid});
        
        print "         Changed to $newid\n";
        $read_id = $newid;
    }

    if ($output_format eq "FASTQ") {    
        if ($input_format eq "FASTA") {
            $header_line =~ s/^>/@/;
        }

        my $out_filename = $output_dir."/".$chunk."/".$read_id.".fastq";
        open(OUTFILE, ">".$out_filename) or die "Can't open output ".$out_filename."\n";
        print OUTFILE $header_line;
        print OUTFILE $sequence;
        print OUTFILE $qual_id;
        print OUTFILE $qualities;
        close(OUTFILE);
    } else {
        if ($input_format eq "FASTQ") {
            $header_line =~ s/^@/>/;
        }

        my $out_filename = $output_dir."/".$chunk."/".$read_id.".fasta";
        open(OUTFILE, ">".$out_filename) or die "Can't open output ".$out_filename."\n";
        print OUTFILE $header_line;
        print OUTFILE $sequence;
        close(OUTFILE);
    }

    $count++;
    if (($count % $reads_per_chunk) == 0) {
        $chunk++;
    }
    
    if (($count % 10) == 0) {
        print "\r$count";
    }
}

close(INPUTFILE);
