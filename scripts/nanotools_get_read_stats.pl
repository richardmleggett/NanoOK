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

&GetOptions(
'b|basedir:s' => \$basedir,
's|sample:s'  => \$sample,
'h|help'      => \$help_requested
);

print "\nnanotools_get_read_stats\n\n";

if (defined $help_requested) {
    print "Get stats on read lengths.\n\n";
    print "Usage: nanotools_get_read_stats.pl <-s sample> [-b directory]\n\n";
    print "Options:\n";
    print "    -s | -sample       Sample name\n";
    print "    -b | -basedir      Base directory containing all sample directories\n";
    print "\n";
    
    exit;
}

die "You must specify a sample name" if not defined $sample;

print "Base directory: $basedir\n";
print "Sample: $sample\n";

print "Merging template reads\n";
system("find ${basedir}/${sample}/fasta/Template -name '*BaseCalled_Template.fasta' | xargs cat > ${basedir}/${sample}/fasta/all_Template.fasta");
print "Merging complement reads\n";
system("find ${basedir}/${sample}/fasta/Complement -name '*BaseCalled_Complement.fasta' | xargs cat > ${basedir}/${sample}/fasta/all_Complement.fasta");
print "Merging 2D reads\n";
system("find ${basedir}/${sample}/fasta/2D -name '*BaseCalled_2D.fasta' | xargs cat > ${basedir}/${sample}/fasta/all_2D.fasta");

print "Generating stats for template reads\n";
system("get_contig_stats.pl -i ${basedir}/${sample}/fasta/all_Template.fasta -a -g ${basedir}/${sample}/analysis/all_Template_fasta_hist.txt -r ${basedir}/${sample}/analysis/all_Template_lengths.txt -l 500,1000,1500,2000,2500,3000,3500,4000,4500,5000 > ${basedir}/${sample}/analysis/all_Template_stats.txt");
print "Generating stats for complement reads\n";
system("get_contig_stats.pl -i ${basedir}/${sample}/fasta/all_Complement.fasta -a -g ${basedir}/${sample}/analysis/all_Complement_fasta_hist.txt -r ${basedir}/${sample}/analysis/all_Complement_lengths.txt -l 500,1000,1500,2000,2500,3000,3500,4000,4500,5000 > ${basedir}/${sample}/analysis/all_Complement_stats.txt");
print "Generating stats for 2D reads\n";
system("get_contig_stats.pl -i ${basedir}/${sample}/fasta/all_2D.fasta -a -g ${basedir}/${sample}/analysis/all_2D_fasta_hist.txt -r ${basedir}/${sample}/analysis/all_2D_lengths.txt -l 500,1000,1500,2000,2500,3000,3500,4000,4500,5000 > ${basedir}/${sample}/analysis/all_2D_stats.txt");

system("echo \"\" >> ${basedir}/${sample}/summary.txt");
system("cat ${basedir}/${sample}/all_Template_stats.txt | grep 'Headings:' | sed 's/Headings:/  ReadType/' >> ${basedir}/${sample}/summary.txt");
system("cat ${basedir}/${sample}/all_Template_stats.txt | grep 'AllFields:' | sed 's/AllFields:/  Template/' >> ${basedir}/${sample}/summary.txt");
system("cat ${basedir}/${sample}/all_Complement_stats.txt | grep 'AllFields:' | sed 's/AllFields:/Complement/' >> ${basedir}/${sample}/summary.txt");
system("cat ${basedir}/${sample}/all_2D_stats.txt | grep 'AllFields:' | sed 's/AllFields:/        2D/' >> ${basedir}/${sample}/summary.txt");

print "DONE\n";