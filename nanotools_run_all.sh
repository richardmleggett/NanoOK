#!/bin/bash

basedir="/Users/leggettr/Documents/Projects/Nanopore/"
sample=$1

nanotools_extract_reads.pl -s ${sample} -b ${basedir}
nanotools_get_read_stats.pl -s ${sample} -b ${basedir}
nanotools_run_last.pl -s ${sample} -b ${basedir}
nanotools_parse_last.pl -s ${sample} -b ${basedir}
Rscript /Users/leggettr/Documents/github/nanotools/nanotools_plot_graphs.R ${basedir} ${sample}