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

print "\nnanotools_extract_reads\n\n";

if (defined $help_requested) {
    print "Extract FASTA and FASTQ files from Nanopore reads.\n\n";
    print "Usage: nanotools_extract_reads.pl <-s sample> [-b directory]\n\n";
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

my $input_dir = $basedir."/".$sample."/fast5";
my $output_fastq = $basedir."/".$sample."/fastq";
my $output_fasta = $basedir."/".$sample."/fasta";
my $total_reads = 0;
my $total_2d = 0;
my $total_template = 0;
my $total_complement = 0;
my $datatype_2d = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_2D\/Fastq";
my $datatype_template = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_template\/Fastq";
my $datatype_complement = "\/Analyses\/Basecall\_2D\_000\/BaseCalled\_complement\/Fastq";

if (! -d $output_fastq) {
    mkdir($output_fastq);
    mkdir($output_fastq."\/Template");
    mkdir($output_fastq."\/Complement");
    mkdir($output_fastq."\/2D");
}

if (! -d $output_fasta) {
    mkdir($output_fasta);
    mkdir($output_fasta."\/Template");
    mkdir($output_fasta."\/Complement");
    mkdir($output_fasta."\/2D");
}

open(MYFILE, ">".$basedir."/".$sample."/extract_summary.txt") or die "Can't open extract summary\n";
opendir(DIR, $input_dir) or die $!;
while (my $file = readdir(DIR)) {
    my $got_basecalled_2d = 0;
    my $got_basecalled_complement = 0;
    my $got_basecalled_template = 0;
    
    next unless ($file =~ m/\.fast5$/);

    print "Extracting $file\n";
    
    my $pathname = "${input_dir}/${file}";
    my $dump = `h5dump -n ${pathname}`;
    
    $total_reads++;
    
    if ($dump =~ /$datatype_2d/) {
        $got_basecalled_2d = 1;
        $total_2d++;
        output_reads($pathname, $datatype_2d, $output_fastq."/2D/".$file."\_BaseCalled\_2D.fastq", $output_fasta."/2D/".$file."\_BaseCalled\_2D.fasta");
    }

    if ($dump =~ /$datatype_complement/) {
        $got_basecalled_complement = 1;
        $total_complement++;
        output_reads($pathname, $datatype_complement, $output_fastq."/Complement/".$file."\_BaseCalled\_Complement.fastq", $output_fasta."/Complement/".$file."\_BaseCalled\_Complement.fasta");
    }

    if ($dump =~ /$datatype_template/) {
        $got_basecalled_template = 1;
        output_reads($pathname, $datatype_template, $output_fastq."/Template/".$file."\_BaseCalled\_Template.fastq", $output_fasta."/Template/".$file."\_BaseCalled\_Template.fasta");
        $total_template++;
    }
    
    print MYFILE $file, "\t", $got_basecalled_template, "\t", $got_basecalled_complement, "\t", $got_basecalled_2d, "\n";
}
closedir(DIR);
close(MYFILE);

open(MYFILE, ">".$basedir."/".$sample."/summary.txt") or die "Can't open summary\n";
print MYFILE "Sample    $sample\n";
print MYFILE "\n";
print MYFILE "Total reads\t$total_reads\n";
print MYFILE "Total template\t$total_template\n";
print MYFILE "Total complement\t$total_complement\n";
print MYFILE "Total 2D\t$total_2d\n";
close(MYFILE);

print "DONE\n";



sub output_reads {
    my $pathname = $_[0];
    my $type = $_[1];
    my $out_fastq = $_[2];
    my $out_fasta = $_[3];
    
    my $data = `h5dump -d ${type} ${pathname}`;
    my $data_offset = index($data, "DATA {");
    if ($data_offset > 0) {
        my $payload = substr($data, $data_offset);
        if ($payload =~ /@(\S+)(\s*)(\S+)(\s*)\+(\s*)(\S+)/) {
            #print "Writing $out_fastq\n";
            open(OUTPUTFASTQ, ">".$out_fastq) or die "Can't open $out_fastq\n";
            print OUTPUTFASTQ "@".$1."\n";
            print OUTPUTFASTQ $3."\n";
            print OUTPUTFASTQ "+\n".$6."\n";
            close(OUTPUTFASTQ);
            #print "Writing $out_fasta\n";
            open(OUTPUTFASTA, ">".$out_fasta) or die "Can't open $out_fasta\n";
            print OUTPUTFASTA ">".$1."\n";
            print OUTPUTFASTA $3."\n";
            close(OUTPUTFASTA);
        }
    }
}
