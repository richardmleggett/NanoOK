nanotools
=========

These are pre-release scripts, so please email richard.leggett@tgac.ac.uk before using.

Requires HDF5 Tools and LAST to be installed before use.

The tools expect a sample name to be provided. Samples reside within a directory inside the base directory (which can also be specified). Please put fast5 files inside a fast5 subdirectory inside the sample directory.

To extract reads:
  `nanotools_extract_reads.pl -s sample <-b basedir>`

To get read length stats:
  `nanotools_get_read_stats.pl -s sample <-b basedir>`

To run LAST on all reads:
  `nanotools_run_last.pl -s sample <-b basedir>`
  
To parse LAST output:
  `nanotools_parse_last.pl -s sample <-b basedir>`

To plot graphs:
  `Rscript nanotools_plot_graphs.R sample`
  
To run all:
  `nanotools_run_all.sh sample`
