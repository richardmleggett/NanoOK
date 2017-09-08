NanoOK tutorial
===============

#. Follow the installation instructions to install NanoOK on your
   system. Docker and a VM are also options.

#. Download the `example E. coli
   dataset <http://opendata.tgac.ac.uk/nanook/nanook_ecoli_500.tar.gz>`__
   from http://opendata.tgac.ac.uk/nanook/ and uncompress::

     tar -xvf nanook_ecoli_500.tar.gz

#. Change into the directory::

     cd nanook_ecoli_500

#. Index the reference with LAST::

     cd references
     lastdb -Q 0 ecoli_dh10b_cs ecoli_dh10b_cs.fasta
     cd ..

#. Extract FASTA::

     nanook extract -s N79596_dh10b_8kb_11022015

#. Perform alignments::

     nanook align -s N79596_dh10b_8kb_11022015 -r references/ecoli_dh10b_cs.fasta

#. Generate a report::

     nanook analyse -s N79596_dh10b_8kb_11022015 -r references/ecoli_dh10b_cs.fasta -passonly

#. View the PDF file inside ``N79596_dh10b_8kb_11022015/latex``
