#!/usr/bin/perl

use strict;
use warnings;
use Getopt::Long;

my $sample;
my $help_requested;
my $basedir="/Users/leggettr/Documents/Projects/Nanopore";
my $coverage_bin_size = 100;
my $genome_size = 50000;
my $reference = $basedir."/references/lambdadb";

my $n_reads;
my $n_reads_with_alignments;
my $n_reads_without_alignments;
my %contig_lengths;
my %ref_lengths;
my @perfect_cumulative_n;
my @perfect_best;
my @coverage;
my @correct_at_position;
my @count_at_position;

&GetOptions(
'b|basedir:s'     => \$basedir,
'c|coveragebin:i' => \$coverage_bin_size,
'g|genomesize:i'  => \$genome_size,
'r|reference:s'   => \$reference,
's|sample:s'      => \$sample,
'h|help'          => \$help_requested
);

print "\nnanotools_parse_last\n\n";

if (defined $help_requested) {
    print "Parse LAST alignments.\n\n";
    print "Usage: nanotools_parse_last.pl <-s sample> [-b directory]\n\n";
    print "Options:\n";
    print "    -s | -sample       Sample name\n";
    print "    -b | -basedir      Base directory containing all sample directories\n";
    print "    -r | reference     Path to reference\n";
    print "    -c | coveragebin   Coverage bin size (default 100)\n";
    print "    -g | genomesize    Size of genome\n";
    print "\n";
    
    exit;
}

die "You must specify a sample name" if not defined $sample;

my $sizes_file = $reference.".fasta.sizes";

print "Base directory: $basedir\n";
print "Sample: $sample\n";

my @type = ("Template", "Complement", "2D");
for (my $i=0; $i<3; $i++) {
    my $input_dir = $basedir."/".$sample."/last/".$type[$i];
    my $output_filename = $basedir."/".$sample."/".$type[$i]."_per_file_summary.txt";

    print "Parsing ".$type[$i]."\n";
    
    # Clear
    $n_reads = 0;
    $n_reads_with_alignments = 0;
    $n_reads_without_alignments = 0;
    undef %contig_lengths;
    undef @perfect_cumulative_n;
    undef @perfect_best;
    undef @coverage;
    undef @correct_at_position;
    undef @count_at_position;
    
    for (my $i=0; $i<$genome_size; $i++) {
        $coverage[$i] = 0;
    }

    open(OUTPUTFILE, ">".$output_filename) or die "Can't open $output_filename\n";

    print OUTPUTFILE "Filename\tQuery Name\tQuery Start\tQuery Bases Covered\tQuery Strand\tQuery Length\tHit Name\tHit Start\tHit Bases Covered\tHit Strand\tHit Length\tAlignment Size\tIdentical Bases\tAlignment Percent Identity\tQuery Percent Identity\tLongest Perfect Kmer\tMean Perfect Kmer\n";

    opendir(DIR, $input_dir) or die $!;
    while (my $file = readdir(DIR)) {
        next unless ($file =~ m/\.maf$/);
        my $pathname = "${input_dir}/${file}";
        my $score = 0;
        my $query_name = "";
        my $query_start = 0;
        my $query_bases = 0;
        my $query_strand = "";
        my $query_length = 0;
        my $query_alignment = "";
        my $hit_name = "";
        my $hit_start = 0;
        my $hit_bases = 0;
        my $hit_strand = "";
        my $hit_length = 0;
        my $hit_alignment = "";
        my $best_perfect_kmer = 0;
        
        open(MYFILE, $pathname) or die "Can't open $pathname\n";
        
        $n_reads++;
        
        my $n_alignments = 0;
        while(<MYFILE>) {
            chomp(my $line = $_);
            if ($line =~ /a score=(\d+)/) {
                $score = $1;
                
                chomp(my $hitline = <MYFILE>);
                chomp(my $queryline = <MYFILE>);

                if ($hitline =~ /s (\S+)(\s+)(\d+)(\s+)(\d+)(\s+)(\S+)(\s+)(\d+)(\s+)(\S+)/) {
                    $hit_name = $1;
                    $hit_start = $3;
                    $hit_bases = $5;
                    $hit_strand = $7;
                    $hit_length = $9;
                    $hit_alignment = $11;
                }
                
                if ($queryline =~ /s (\S+)(\s+)(\d+)(\s+)(\d+)(\s+)(\S+)(\s+)(\d+)(\s+)(\S+)/) {
                    $query_name = $1;
                    $query_start = $3;
                    $query_bases = $5;
                    $query_strand = $7;
                    $query_length = $9;
                    $query_alignment = $11;
                }
             
                my ($identical_bases, $longest, $average, $alignment_size) = process_matches($hit_alignment, $query_alignment);
                my $query_identity = (100 * $identical_bases) / $query_bases;
                my $alignment_identity = (100 * $identical_bases) / $alignment_size;
                
                if ($longest > $best_perfect_kmer) {
                    $best_perfect_kmer = $longest;
                }
                
                process_coverage($hit_start, $hit_bases);
                
                print OUTPUTFILE $file, "\t";
                print OUTPUTFILE $query_name, "\t", $query_start, "\t", $query_bases, "\t", $query_strand, "\t", $query_length, "\t";
                print OUTPUTFILE $hit_name, "\t", $hit_start, "\t", $hit_bases, "\t", $hit_strand, "\t", $hit_length, "\t";
                printf OUTPUTFILE "%d\t%d\t%.2f\t%.2f\t%d\t%.2f", $alignment_size, $identical_bases, $alignment_identity, $query_identity, $longest, $average;
                print OUTPUTFILE "\n";
                $n_alignments++;
            }

        }
        close(MYFILE);
        
        if ($n_alignments == 0) {
            print OUTPUTFILE $file, "\tNO ALIGNMENTS\n";
            $n_reads_without_alignments++;
        } else {
            $n_reads_with_alignments++;

            for (my $i=1; $i<=$best_perfect_kmer; $i++) {
                if (defined $perfect_cumulative_n[$i]) {
                    $perfect_cumulative_n[$i]++;
                } else {
                    $perfect_cumulative_n[$i] = 1;
                }
            }
            
            if (defined $perfect_best[$best_perfect_kmer]) {
                $perfect_best[$best_perfect_kmer]++;
            } else {
                $perfect_best[$best_perfect_kmer] = 1;
            }
        }
    }

    closedir(DIR);
    close(OUTPUTFILE);

    write_lengths_hist($basedir."/".$sample."/".$type[$i]."_last_lengths.txt");
    write_perfect_cumulative_hist($basedir."/".$sample."/".$type[$i]."_last_perfect_cumulative.txt");
    write_perfect_best_hist($basedir."/".$sample."/".$type[$i]."_last_perfect_best.txt");
    write_coverage_data($basedir."/".$sample."/".$type[$i]."_last_coverage.txt");

    open(SUMFILE, ">>".$basedir."/".$sample."/summary.txt");
    print SUMFILE "\nParse ".$type[$i]." alignments\n\n";
    print SUMFILE $type[$i]." reads: $n_reads\n";
    print SUMFILE $type[$i]." reads with alignments: $n_reads_with_alignments\n";
    print SUMFILE $type[$i]." reads without alignments: $n_reads_without_alignments\n";
}




sub write_coverage_data
{
    my $filename = $_[0];

    open(DATAFILE, ">".$filename) or die "Can't open $filename\n";

    for (my $i=0; $i<$genome_size; $i+=$coverage_bin_size) {
        my $count = 0;
        
        for (my $j=0; $j<$coverage_bin_size; $j++) {
            $count += $coverage[$i+$j];
        }
        
        printf DATAFILE "%d\t%.2f\n", $i, ($count / $coverage_bin_size);
    }
    
    close(DATAFILE);
}

sub write_lengths_hist
{
    my $filename = $_[0];

    open(HISTFILE, ">".$filename) or die "Can't open $filename\n";
    foreach my $l ( sort {$a <=> $b} keys %contig_lengths )
    {
        print HISTFILE "$l: $contig_lengths{$l}\n";
    }
    close(HISTFILE);
}

sub write_perfect_cumulative_hist
{
    my $filename = $_[0];

    open(HISTFILE, ">".$filename) or die "Can't open $filename\n";
    for (my $i=1; $i<@perfect_cumulative_n; $i++) {
        print HISTFILE $i, "\t";
        print HISTFILE $perfect_cumulative_n[$i], "\t";
        printf HISTFILE "%.2f\n", (100 * $perfect_cumulative_n[$i]) / $n_reads_with_alignments;
    }
    close(HISTFILE);
}

sub write_perfect_best_hist
{
    my $filename = $_[0];

    open(HISTFILE, ">".$filename) or die "Can't open $filename\n";
    for (my $i=0; $i<@perfect_best; $i++) {
        print HISTFILE $i, "\t";
        if (defined $perfect_best[$i]) {
            print HISTFILE $perfect_best[$i], "\t";
            printf HISTFILE "%.2f\n", (100 * $perfect_best[$i]) / $n_reads_with_alignments;
        } else {
            print HISTFILE "0\t0\n";
        }
    }
    close(HISTFILE);
}

sub process_coverage
{
    my $hit_start = $_[0];
    my $hit_size = $_[1];
    
    for (my $i=$hit_start; $i<($hit_start+$hit_size); $i++) {
        $coverage[$i]++;
    }
}

sub process_matches
{
    my $hit = $_[0];
    my $query = $_[1];
    
    my @hit_bases = split(//, $hit);
    my @query_bases = split(//, $query);
    my $size_hit = @hit_bases;
    my $size_query = @query_bases;
    my $loop_to = $size_hit <= $size_query ? $size_hit:$size_query;
    my $current_size = 0;
    my $identical_bases = 0;
    my $total = 0;
    my $count = 0;
    my $longest = 0;
    
    for (my $i=0; $i<$loop_to; $i++) {
        if ($hit_bases[$i] eq $query_bases[$i]) {
            $identical_bases++;
            $current_size++;
        } else {
            if ($current_size > 0) {
                if (defined $contig_lengths{$current_size}) {
                    $contig_lengths{$current_size}++;
                } else {
                    $contig_lengths{$current_size} = 1;
                }
                
                $total += $current_size;
                $count++;
                
                if ($current_size > $longest) {
                    $longest = $current_size;
                }
                
                $current_size = 0;
            }
        }
    }

    if ($current_size > 0) {
        if (defined $contig_lengths{$current_size}) {
            $contig_lengths{$current_size}++;
        } else {
            $contig_lengths{$current_size} = 1;
        }

        $total += $current_size;
        $count++;
        
        if ($current_size > $longest) {
            $longest = $current_size;
        }
    }
    
    return ($identical_bases, $longest, $total/$count, $loop_to);
}

sub read_sizes_file
{
    open(MYFILE, $sizes_file) or die "Can't open sizes file $sizes_file\n";
    
    while (<MYFILE>) {
        chomp(my $line = $_);
        if ($line =~ /(\S+)\t(\d+)/) {
            my $id = $1;
            my $length = $2;
            
            die "Error: Ref ID $id already defined!\n" if (defined $ref_lengths{$id});
            $ref_lengths{$id} = $length;
            print $id, "\t", $length, "\n";
        }
    }
    
    close(MYFILE);
}
