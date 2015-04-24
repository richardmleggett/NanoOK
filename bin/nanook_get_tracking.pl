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
my @channels;
my @count_in_time;

&GetOptions(
'b|basedir:s' => \$basedir,
's|sample:s'  => \$sample,
'h|help'      => \$help_requested
);

print "\nnanotools_extract_reads\n\n";

if (defined $help_requested) {
    print "Get tracking information.\n\n";
    print "Usage: nanotools_get_tracking.pl <-s sample> [-b directory]\n\n";
    print "Options:\n";
    print "    -s | -sample       Sample name\n";
    print "    -b | -basedir      Base directory containing all sample directories\n";
    print "\n";
    print "Sample directories should be inside the base directory. Within each sample\n";
    print "directory, there should be a fast5 directory containing the input files.\n";
    print "\n";
    
    exit;
}

die "You must specify a sample name" if not defined $sample;

print "Base directory: $basedir\n";
print "Sample: $sample\n";

my $in_dir = $basedir."/".$sample."/fast5";
my $out_fasta;
my $out_fastq;


if ((-d $in_dir."/pass") && (-d $in_dir."/fail")) {
    print "Got pass/fail directory\n";
    process_directory($in_dir."/pass");
    #process_directory($in_dir."/fail");
} else {
    print "Got all-in-one directory\n";
    process_directory($in_dir);
}

print "\nAnalysing...\n\n";

sub process_directory {
    my $input_dir = $_[0];
    my $total_reads = 0;
    my $total_2d = 0;
    my $total_template = 0;
    my $total_complement = 0;
    my $datatype_2d = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_2D\/Fastq";
    my $datatype_template = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_template\/Fastq";
    my $datatype_complement = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_complement\/Fastq";
    
    print "Processing reads\n";
    print "       In: ", $input_dir, "\n";
    
    opendir(DIR, $input_dir) or die $!;
    while (my $file = readdir(DIR)) {
        next unless ($file =~ m/\.fast5$/);
        my $channel;
        my $template_time;
        my $complement_time;
        
        print "Extracting $file\n";
        
        if ($file =~ /_ch(\d+)_/) {
            $channel = $1;
        }
        
        my $pathname = "${input_dir}/${file}";
        my @dump = `h5dump -a /Analyses/Basecall_2D_000/BaseCalled_template/Events/start_time ${pathname}`;
        for (my $i=0; $i<@dump; $i++) {
            if ($dump[$i] =~ /\(0\)\: (\S+)/) {
                $template_time = $1;
            }
        }
        
        #@dump = `h5dump -a /Analyses/Basecall_2D_000/BaseCalled_complement/Events/start_time ${pathname}`;
        #for (my $i=0; $i<@dump; $i++) {
        #    if ($dump[$i] =~ /\(0\)\: (\S+)/) {
        #        $complement_time = $1;
        #    }
        #}
        
        #if ((defined $channel) && (defined $template_time)) {
        #    print $channel, "\t", $template_time, "\n";
        #}
        
        if (defined $channels[$channel]) {
            $channels[$channel] = $channels[$channel].",".$template_time;
        } else {
            $channels[$channel] = $template_time;
        }
        
    }
    closedir(DIR);
}

print "Channel\tTime\tDifference\tMean\n";

for (my $c=0; $c<512; $c++) {
    if (defined $channels[$c]) {
        my @times = split(/,/, $channels[$c]);
        my @sorted_times = sort {$a <=> $b} @times;
        my $total;
        my $count = 0;
        
        for (my $i=0; $i<@times; $i++) {
            my $difference = 0;
            my $mean = 0;
            
            if ($i > 0) {
                $difference = $sorted_times[$i] - $sorted_times[$i-1];
                $total += $difference;
                $mean = $total / $i;
            }
            
            if ($sorted_times[$i] < (60*60*12)) {
                $count++;
            }
            
            print $c, "\t", $sorted_times[$i], "\t", $difference, "\t", $mean, "\n";
        }
        
        $count_in_time[$c] = $count;
    }
}

print "\n";
print "Channel\tCount\n";
my $total = 0;
my $n = 0;
for (my $c=0; $c<512; $c++) {
    if (defined $channels[$c]) {
        print $c, "\t", $count_in_time[$c], "\n";
        $total += $count_in_time[$c];
        $n++;
    }
}

print "\nMean: ".($total / $n)."\n";