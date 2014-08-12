#!/usr/bin/perl -w

# Script:  get_contig_stats.pl 
# Purpose: Calculate mean, N50, average etc. stats for a FASTA file 
# Author:  Richard Leggett

use warnings;
use strict;
use Getopt::Long;

my $inputfile;
my $longerthan;
my %contig_lengths;
my $type = 0;
my $id = "";
my $contig_length = 0;
my $total_length = 0;
my $shortest;
my $longest;
my $cumulative = 0;
my $counter = 0;
my $n50;
my $n50count;
my $n90;
my $n90count;
my @lengths;
my @length_counts;
my $help_requested;
my $is_fasta;
my $is_fastq;
my $histogram;
my $length_file;
my $length_fh;
my %hist_counts;

&GetOptions(
'i|input:s'       => \$inputfile,
'g|histogram:s'   => \$histogram,
'h|help'          => \$help_requested,
'l|longerthan:s'  => \$longerthan,
'a|fasta'         => \$is_fasta,
'q|fastq'         => \$is_fastq,
'r|lengthfile:s'    => \$length_file
);

if (defined $help_requested) {
    print "\nGet contig stats on a FASTA file.\n\n";
    print "Usage: get_contig_stats.pl <-f filename> [-l lengths]\n\n";
    print "Options:\n";
    print "    -i | -input        input file\n";
    print "    -a | -fasta        input file is FASTA (default)\n";
    print "    -g | -histogram    filename to output length histogram\n";
    print "    -q | -fastq        input file is FASTQ\n";
    print "    -r | -lengthfile   filename to output lengths\n";
    print "    -l | -longerthan   list of comma separated lengths for which you wish\n";
    print "                       to know number of contigs >= to - eg. 76,151\n\n"; 

    exit;
}

die "You must specify -input or -i\n" if not defined $inputfile;

$is_fasta = 1 if not defined $is_fastq;

if (defined $length_file) {
    open($length_fh, ">".$length_file) or die "Can't open $length_file\n";
}

if (defined $longerthan) {
    @lengths = split(/,/, $longerthan);
    for (my $i=0; $i<@lengths; $i++) {
        $length_counts[$i] = 0;
    }
}

if (defined $is_fastq) {
    read_fastq($inputfile);
} else {
    read_fasta($inputfile);
}

if (defined $length_fh) {
    close($length_fh);
}

foreach $id (sort {$contig_lengths{$b} <=> $contig_lengths{$a}} keys %contig_lengths)
{
    my $contig_length = $contig_lengths{$id};
    $cumulative += $contig_length;
    $counter++;
    
    if (not defined $n50) {
        if ($cumulative >= ($total_length  * 0.5)) {
            $n50 = $contig_length;
            $n50count = $counter;
        }
    }
    
    if (not defined $n90) {
        if ($cumulative >= ($total_length  * 0.9)) {
            $n90 = $contig_length;
            $n90count = $counter;
        }
    }
}

my $mean = $cumulative / $counter;

my $header_string="NumContigs\tTotalSum\tMeanLength\tShortest\tLongest\tN50Length\tN50Count\tN90Length\tN90Count";

print "NumContigs:\t", $counter, "\n";
print "TotalSum:\t", $cumulative, "\n";
printf "MeanLength:\t%.2f\n", $mean;
print "Shortest:\t", $shortest, "\n";
print "Longest:\t", $longest, "\n";
print "N50Length:\t", $n50, "\n";
print "N50Count:\t", $n50count, "\n";
print "N90Length:\t", $n90, "\n";
print "N90Count:\t", $n90count, "\n";

if (defined $longerthan) {
    for (my $i=0; $i<@lengths; $i++) {
        $header_string = $header_string."\tGE".$lengths[$i]."Count";
        print "GE",$lengths[$i],"Count:\t", $length_counts[$i], "\n";
    }
}

print "Headings:\t", $header_string, "\n";
print "AllFields:\t", $counter, "\t", $cumulative, "\t";
printf "%.2f\t", $mean;
print $shortest, "\t", $longest, "\t", $n50, "\t", $n50count, "\t", $n90, "\t", $n90count;

if (defined $longerthan) {
    for (my $i=0; $i<@lengths; $i++) {
        print "\t", $length_counts[$i];
    }
}

print "\n";

if (defined $histogram) {
    output_histogram();
}


sub read_fasta
{
    my $filename = $_[0];
    open(INPUTFILE, $filename) or die "Can't open $filename\n";
    
    
    while(<INPUTFILE>) {
        chomp(my $line = $_);
        
        if ($line =~ /^>(\S+)/) {
            if ($contig_length > 0) {
                store_length($id, $contig_length);
            }
            
            $contig_length = 0;
            $id = $1;
        } else {
            $contig_length += length($line);
        }
    }
    
    if ($contig_length > 0) {
        store_length($id, $contig_length);
    }
    
    close(INPUTFILE);
}

sub read_fastq
{
    my $filename = $_[0];
    open(INPUTFILE, $filename) or die "Can't open $filename\n";
    
    while(<INPUTFILE>) {
        chomp(my $seq_header = $_);
        chomp(my $sequence = <INPUTFILE>);
        chomp(my $qual_header = <INPUTFILE>);
        chomp(my $qualities = <INPUTFILE>);
        my @fields = split(/ /, $seq_header);
        my $id = substr $fields[0], 1;
        my $contig_length = length($sequence);
        
        store_length($id, $contig_length);
    }
    
    close(INPUTFILE);
}

sub store_length
{
    my $id = $_[0];
    my $contig_length = $_[1];
    
    if (defined $contig_lengths{$id}) {
        my $new_id = $id;
        my $counter = 0;
        do {
            $counter++;
            $new_id = $id."_duplicate_".$counter;
        } while (defined $contig_lengths{$new_id});
        $id = $new_id;
        print "Found duplicate ID - used new ID $id\n";
    }

    $contig_lengths{$id} = $contig_length;
    $total_length += $contig_length;
    
    if ((not defined $longest) || ($contig_length > $longest)) {
        $longest = $contig_length;
    }
    
    if ((not defined $shortest) || ($contig_length < $shortest)) {
        $shortest = $contig_length;
    }
    
    if (defined $longerthan) {
        for (my $i=0; $i<@lengths; $i++) {
            if ($contig_length >= $lengths[$i]) {
                $length_counts[$i]++;
            }
        }
    }
    
    if (defined $hist_counts{$contig_length}) {
        $hist_counts{$contig_length}++;
    } else {
        $hist_counts{$contig_length}=1;
    }
    
    if (defined $length_fh) {
        print $length_fh $id, "\t", $contig_length, "\n";
    }
}

sub output_histogram
{
    open(my $output_fh, ">".$histogram) or die "Can't open $histogram\n";
    
    for (my $i=1; $i<=$longest; $i++) {
        if (defined $hist_counts{$i}) {
            print $output_fh $i, "\t", $hist_counts{$i}, "\n";            
        } else {
            print $output_fh $i, "\t0\n";
        }
    }
    
    close($output_fh);
}
