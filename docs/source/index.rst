.. NanoOK documentation master file, created by
   sphinx-quickstart on Fri Sep  1 11:15:43 2017.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

NanoOK
======

.. toctree::
   :hidden:
   
   installation
   docker
   virtualbox
   running
   report
   howitworks
   parserapi
   datafiles
   commonerrors
   tutorial
   history
   nanookrt
   reporter

NanoOK (pronounced na-nook) is a tool for extraction, alignment and analysis of Nanopore reads. NanoOK will extract reads as FASTA or FASTQ files, align them (with a choice of alignment tools), then generate a comprehensive multi-page PDF report containing yield, accuracy and quality analysis. Along the way, it generates plain text files which can be used for further analysis, as well as graphs suitable for inclusion in presentations and papers.

NanoOK has a number of dependencies - Perl, LaTeX, R and an alignment tool - which means it works best on Linux and Mac OS platforms. 

Further information:

* To find out how to install NanoOK, see the :ref:`Download and installation page <installation>`.
* For further information on NanoOK RT, see :ref:`these comments <nanookrt>`.
* To find out how to run NanoOK, see the :ref:`Running NanoOK page <running>` or the :ref:`NanoOK tutorial page <tutorial>`.
* Source code is on `GitHub <https://github.com/TGAC/NanoOK>`_.
* Here's some information `about the other Nanook <http://en.wikipedia.org/wiki/Nanook>`_.

Paper
=====
Leggett RM, Heavens D, Caccamo M, Clark MD, Davey RP (2016). `NanoOK: multi-reference alignment analysis of nanopore sequencing data, quality and error profiles <https://doi.org/10.1093/bioinformatics/btv540>`_. Bioinformatics 32(1):142–144.

Talks and posters
=================
* Richard Leggett presented a poster at AGBT 2016 - `here it is <http://f1000research.com/posters/5-176>`_.
* Richard Leggett spoke at Genome Science 2015 - `here are the slides <http://f1000research.com/slides/4-717>`_.
* Richard Leggett spoke at the London Calling Nanopore conference - `here are his slides <http://documentation.tgac.ac.uk/download/attachments/7209095/RichardLeggett_LondonCalling2015.pdf?version=1&modificationDate=1431700116000&api=v2>`_.
* Robert Davey presented a poster at AGBT 2015 - `here is the PDF <http://documentation.tgac.ac.uk/download/attachments/7209095/AGBT2015_NanoOK.pdf?version=1&modificationDate=1425471330000&api=v2>`_.

Follow us
=========
You can follow NanoOK updates on twitter `@NanoOK_Software <https://twitter.com/nanook_software>`_.

Or if you would like to be on a NanoOK mailing list to receive information about updates, please email richard.leggett@earlham.ac.uk.

