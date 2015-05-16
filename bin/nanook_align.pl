#!/usr/bin/perl
#
# Program: nanook_align.pl
# Author:  Richard Leggett
# Contact: richard.leggett@tgac.ac.uk

use strict;
use warnings;
use Getopt::Long;

my $sample;
my $help_requested;
my $basedir=".";
my $reference;
my $scheduler="NONE";
my $queue="Test128";
my $aligner="last";
my $num_threads=1;
my $alignment_dir;
my $read_dir;
my $aligner_params;
my $n_types = 0;
my $no_template;
my $no_complement;
my $pass_only;
my $fail_only;
my $no_2d;
my @type;

&GetOptions(
'a|aligner:s'    => \$aligner,
'alignmentdir:s' => \$alignment_dir,
'b|basedir:s'    => \$basedir,
'p|params:s'     => \$aligner_params,
's|sample:s'     => \$sample,
'r|reference:s'  => \$reference,
'q|queue:s'      => \$queue,
't|threads:i'    => \$num_threads,
'x|scheduler:s'  => \$scheduler,
'passonly'       => \$pass_only,
'failonly'       => \$fail_only,
'no2d'           => \$no_2d,
'nocomplement'   => \$no_complement,
'notemplate'     => \$no_template,
'h|help'         => \$help_requested
);

print "\nnanook_align\n\n";
print "This tool has been replaced with the command nanook align <options>\n";
exit;

if (defined $help_requested) {
    print "Create alignments.\n\n";
    print "Usage: nanook_align.pl <-a aligner> <-s sample> [-b directory] [options]\n\n";
    print "Options:\n";
    print "    -a | -aligner      Aligner (default last)\n";
    print "    -alignmentdir      Subdirectory for alignments (defaults to aligner name)\n";
    print "    -b | -basedir      Base directory containing all sample directories\n";
    print "    -no2d              Don't align 2D reads\n";
    print "    -notemplate        Don't align Template reads\n";
    print "    -nocomplement      Don't align Complement reads\n";
    print "    -numthreads        Number of threasd per alignment\n";
    print "    -passonly          Only pass reads\n";
    print "    -failonly          Only fail reads\n";
    print "    -p | -params       Extra parameters for aligner\n";
    print "    -q | -queue        Scheduler queue (if running)\n";
    print "    -r | -reference    Path to reference\n";
    print "    -s | -sample       Sample name\n";
    print "    -x | -scheduler    Work scheduler (LSF, NONE)\n";
    print "\n";
    
    exit;
}

$type[$n_types++] = "2D" if not defined $no_2d;
$type[$n_types++] = "Template" if not defined $no_template;
$type[$n_types++] = "Complement" if not defined $no_complement;

die "You must specify a sample name" if not defined $sample;
die "You must specify a reference." if not defined $reference;
die "Base directory must be specified." if not defined $basedir;

if (($reference =~ /.fa$/) || ($reference =~ /.fasta$/)) {
    if (($aligner eq "last") || ($aligner eq "bwa")) {
        $reference =~ s/.fa$//;
        $reference =~ s/.fasta//;
    }
} else {
    die "Error: Reference must be path to the .fa or .fasta file.\n";
}

print $reference, "\n";

$basedir =~ s/\/$//;

if (not defined $alignment_dir) {
    $alignment_dir = $aligner;
}

if (not defined $aligner_params) {
    if ($aligner eq "last") {
        $aligner_params = "-s 2 -T 0 -Q 0 -a 1";
        $read_dir = $basedir."/".$sample."/fasta";
    } elsif ($aligner eq "bwa") {
        $aligner_params = "-x ont2d";
        $read_dir = $basedir."/".$sample."/fasta";
    } elsif ($aligner eq "marginalign") {
        $aligner_params = "";
        $read_dir = $basedir."/".$sample."/fastq";
    } elsif ($aligner eq "blasr") {
        $aligner_params = "";
        $read_dir = $basedir."/".$sample."/fasta";
    } else {
        die "Aligner not yet supported\n";
    }
}

print "Base directory: $basedir\n";
print "        Sample: $sample\n";
print "       Aligner: $aligner\n";
print "Aligner params: $aligner_params\n";
print "\n";

if (! -d $basedir."/".$sample."/".$alignment_dir) {
    mkdir($basedir."/".$sample."/".$alignment_dir);
}

if (! -d $basedir."/".$sample."/logs") {
    mkdir($basedir."/".$sample."/logs");
}

if (! -d $basedir."/".$sample."/logs/".$alignment_dir) {
    mkdir($basedir."/".$sample."/logs/".$alignment_dir);
}

if ((-d $read_dir."/pass") && (-d $read_dir."/fail")) {
    print "Got pass/fail directory\n";
    if (not defined $fail_only) {
        process_directory($read_dir."/pass", $basedir."/".$sample."/".$alignment_dir."/pass", $basedir."/".$sample."/logs/".$alignment_dir."/pass");
    }
    if (not defined $pass_only) {
        process_directory($read_dir."/fail", $basedir."/".$sample."/".$alignment_dir."/fail", $basedir."/".$sample."/logs/".$alignment_dir."/fail");
    }
} else {
    print "Got all-in-one directory\n";
    process_directory($read_dir, $basedir."/".$sample."/".$alignment_dir, $basedir."/".$sample."/logs/".$alignment_dir);
}

print "DONE\n";

sub process_directory {
    my $reads_dir = $_[0];
    my $align_dir = $_[1];
    my $log_dir = $_[2];
 
    print "Processing directory\n";
    print "Input files: $reads_dir\n";
    print "Alignment files: $align_dir\n";
    
    if (! -d $align_dir) {
        mkdir($align_dir);
    }

    if (! -d $log_dir) {
        mkdir($log_dir);
    }
    
    for (my $i=0; $i<$n_types; $i++) {
        my $input_dir=$reads_dir."/".$type[$i];
        my $output_dir=$align_dir."/".$type[$i];

        print " Input dir: $input_dir\n";
        print "Output dir: $output_dir\n";
        
        if (! -d $output_dir) {
            mkdir($output_dir);
        }
        
        opendir(DIR, $input_dir) or die $!;
        while (my $file = readdir(DIR)) {
            next unless (($file =~ m/\.fasta$/) || ($file =~ m/\.fa$/) || ($file =~ m/\.fastq$/) || ($file =~ m/\.fq$/));
            my $inpath = $input_dir."/".$file;
            my $outpath = $output_dir."/".$file;
            my $logfile = $log_dir."/".$file.".log";
            my $command;
            
            if ($aligner eq "last") {
                $outpath = $outpath.".maf";
                $command = get_last_command($inpath, $outpath, $reference);
            } elsif ($aligner eq "blastn") {
                $outpath = $outpath.".txt";
                $command = get_blast_command($inpath, $outpath, $reference);
            } elsif ($aligner eq "bwa") {
                $outpath = $outpath.".sam";
                $command = get_bwa_command($inpath, $outpath, $reference);
            } elsif ($aligner eq "marginalign") {
                $outpath = $outpath.".sam";
                $command = get_marginalign_command($inpath, $outpath, $reference);
            } elsif ($aligner eq "blasr") {
                $outpath = $outpath.".sam";
                $command = get_blasr_command($inpath, $outpath, $reference);
            } else {
                print "Error: aligner $aligner not known!\n";
                exit;
            }

            print "Aligning ".$inpath."\n";
            print "      to ".$outpath."\n";
            
            if ($scheduler eq "LSF") {
                system("bsub -n ${num_threads} -q ${queue} -oo ${logfile} -R \"rusage[mem=8000] span[hosts=1]\" \"${command}\"");
            } elsif ($scheduler eq "PBS") {
                print("Error: PBS not yet implemented.");
            } elsif ($scheduler eq "screen") {
                print $command, "\n";
            } else {
                system($command);
                open(COMMAND, $command." |");
                while(<COMMAND>) {
                    print $_;
                }
            }
        }
        closedir(DIR);
    }
}

sub get_last_command {
    my $query = $_[0];
    my $output_file = $_[1];
    my $reference = $_[2];
    my $command = "lastal ".$aligner_params." ".$reference." ".$query." > ".$output_file;

    return $command;
}

sub get_bwa_command {
    my $query = $_[0];
    my $output_file = $_[1];
    my $reference = $_[2];
    my $command = "bwa mem ".$aligner_params." ".$reference." ".$query." > ".$output_file;
    
    return $command;
}

sub get_blasr_command {
    my $query = $_[0];
    my $output_file = $_[1];
    my $reference = $_[2];
    my $command = "blasr ".$query." ".$reference." -sam -out ".$output_file;
    
    if (length($aligner_params) > 0) {
        $command = $command." ".$aligner_params;
    }
    
    return $command;
}

sub get_marginalign_command {
    my $query = $_[0];
    my $output_file = $_[1];
    my $jobtree = $_[1].".jobTree";
    my $reference = $_[2];
    my $command = "rm -rf ".$jobtree." ; marginAlign ";
    
    if (length($aligner_params) > 0) {
        $command = $command." ".$aligner_params." ";
    }

    $command = $command.$query." ".$reference." ".$output_file." --jobTree ".$jobtree;
    
    return $command;
}

sub get_blast_command {
    my $query = $_[0];
    my $output_file = $_[1];
    my $reference = $_[2];
    my $command = "blastn ";
    
    if (defined $aligner_params) {
        $command = $command." ".$aligner_params;
    }
    
    $command = $command."-num_threads ".$num_threads." -db ".$reference." -query ".$query." -out ".$output_file.".txt";
    
    return $command;
}
