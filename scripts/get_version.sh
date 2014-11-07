h5dump -a /Analyses/Basecall_2D_000/version fast5/N79681_1stLambda_8kb_1745_130614_2423_1_ch100_file22_strand.fast5 | perl -nae 'if ($_ =~ /\(0\)\: "(\S+)"/) { print $1 }'
