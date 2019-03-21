#!/usr/bin/perl -w

use Getopt::Long;

my $in_filename;
my $out_filename;
my $out_prefix;
my $removewhitespace;
my $out_dir;

&GetOptions(
    'in:s'     => \$in_filename,
    'out:s'    => \$out_prefix
);

die
    "Syntax: split_to_separate_files.pl -in <filename> -out <sample directory>\n"
    if (not defined $in_filename) || (not defined $out_prefix);

mkdir($out_prefix."/fasta");

my $file_open=0;

my $read_ctr=0;
my $chunk_ctr=0;
$out_dir=$out_prefix."/".$chunk_ctr;
print $out_dir."\n";
mkdir ($out_dir);

open(INFILE, $in_filename) or die "Can't open input file $in_filename\n";
while(<INFILE>) {
    my $line = $_;
    if ($line =~ /^>(\S+)/) {
        my $id = $1;

        if ($file_open) {
            close(OUTFILE);
            $read_ctr++;

            if ($read_ctr == 4000) {
                $chunk_ctr++;
                $read_ctr=0;
                $out_dir=$out_prefix."/".$chunk_ctr;
                print $out_dir."\n";
                mkdir($out_dir);
            }
        }

        my $out_file=$out_dir."/".$id.".fasta";
        print "Writing ".$out_file."\n";
        open(OUTFILE, ">".$out_file) or die "Can't open output file $out_file\n";
        $file_open = 1;
    }

    if ($file_open) {        
        print OUTFILE $line; 
    }
}

close(INFILE);

if ($file_open) {
    close(OUTFILE);
}
