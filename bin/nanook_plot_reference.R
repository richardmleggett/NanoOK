library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
sampledir <- args[1];
refid <- args[2];

types = c("2D", "Template", "Complement");
colours = c("#68B5B9", "#CF746D", "#91A851");

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    cat(type, " ", colourcode, "\n");

    # Plot GC% vs position
    data_gc_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_gc.txt", sep="");
    pdf_gc <- paste(sampledir, "/graphs/", refid, "/", refid, "_gc.pdf", sep="");
    pdf(pdf_gc, width=12, height=3)
    data_gc = read.table(data_gc_filename, col.name=c("Position", "Coverage"))
    print(ggplot(data_gc, aes(x=data_gc$Position, y=data_gc$Coverage)) + geom_line(color="black") + ggtitle("GC content") + theme(text = element_text(size=10)) + xlab("Position") + ylab("GC %") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Plot coverage vs position
    data_coverage_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_coverage.txt", sep="");
    pdf_coverage <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_coverage.pdf", sep="");
    pdf(pdf_coverage, width=12, height=3)
    data_coverage = read.table(data_coverage_filename, col.name=c("Position", "Coverage"))
    print(ggplot(data_coverage, aes(x=data_coverage$Position, y=data_coverage$Coverage)) + geom_line(color=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0))
    garbage <- dev.off()

    # Plot % reads with perfect kmer vs kmer size
    data_perfect_cumulative_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.txt", sep="");
    pdf_perfect_cumulative <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.pdf", sep="");
    xlimit <- 80
    pdf(pdf_perfect_cumulative, width=4, height=3)
    data_perfect_cumulative = read.table(data_perfect_cumulative_filename, col.name=c("Size", "n", "Perfect"))
    print(ggplot(data_perfect_cumulative, aes(x=data_perfect_cumulative$Size, y=data_perfect_cumulative$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8)))
    garbage <- dev.off()

    # Plot %reads vs best perfect kmer
    data_perfect_best_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_best_perfect_kmers.txt", sep="");
    pdf_perfect_best <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.pdf", sep="");
    pdf(pdf_perfect_best, width=4, height=3)
    data_perfect_best = read.table(data_perfect_best_filename, col.name=c("Size", "n", "Perfect"))
    print(ggplot(data_perfect_best, aes(x=data_perfect_best$Size, y=data_perfect_best$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8)))
    garbage <- dev.off()

    # ========== Indels files ==========

    # Insertions
    data_insertions_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_insertions.txt", sep="");
    pdf_insertions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_insertions.pdf", sep="");
    pdf(pdf_insertions, height=2.5, width=4)
    data_insertions = read.table(data_insertions_filename, col.name=c("Size", "Percent"))
    print(ggplot(data_insertions, aes(x=data_insertions$Size, y=data_insertions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Insertion size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)))
    garbage <- dev.off()
    
    # Deletions
    data_deletions_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_deletions.txt", sep="");
    pdf_deletions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_deletions.pdf", sep="");
    pdf(pdf_deletions, height=2.5, width=4)
    data_deletions = read.table(data_deletions_filename, col.name=c("Size", "Percent"))
    print(ggplot(data_deletions, aes(x=data_deletions$Size, y=data_deletions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Deletion size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)))
    garbage <- dev.off()

    # ========== Alignments file ==========

    input_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_alignments.txt", sep="");
    data_alignments = read.table(input_filename, header=TRUE);

    # Length vs Identity histograms
    identity_hist_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_hist.pdf", sep="")
    pdf(identity_hist_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryPercentIdentity)) + geom_histogram(fill=colourcode) + xlab("Read identity %") +ylab("Count") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Identity vs Length Scatter plots
    identity_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_scatter.pdf", sep="");
    pdf(identity_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Length") +ylab("Read identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Alignment identity vs. Fraction of read aligned scatter plots
    aid_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_alignment_identity_scatter.pdf", sep="");
    pdf(aid_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$AlignmentPercentIdentity)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Query identity vs. Fraction of read aligned scatter plots
    qid_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_query_identity_scatter.pdf", sep="");
    pdf(qid_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters
    best_perf_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_scatter.pdf", sep="");
    pdf(best_perf_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters zoomed
    best_perf_zoom_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_zoom_scatter.pdf", sep="");
    pdf(best_perf_zoom_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000)))
    garbage <- dev.off()

    # Number of perfect 21mers verses length scatter
    nk21_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_nk21_vs_length_scatter.pdf", sep="");
    pdf(nk21_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$nk21)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Mean perfect sequence vs. length scatters
    #mean_perf_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_mean_perfect_vs_length_scatter.pdf", sep="");
    #pdf(mean_perf_scatter_pdf, height=4, width=6)
    #print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$MeanPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4) + xlab("Read length") +ylab("Mean perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000)))
    #garbage <- dev.off()

    # Percentage of read aligned vs read length
    output_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_percent_aligned_vs_length_scatter.pdf", sep="");
    pdf(output_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$PercentQueryAligned)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Percentage of read aligned") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()
    
    # ========== Kmer file ==========
    
    # Kmer abundance with labels
    input_kmers <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_kmers.txt", sep="");
    data_kmers = read.table(input_kmers, header=TRUE);
    kmer_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_kmer_scatter.pdf", sep="");
    pdf(kmer_scatter_pdf, height=6, width=6)
    print(ggplot(data_kmers, aes(x=data_kmers$RefPc, y=data_kmers$ReadPc)) + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Reference abundance") +ylab("Reads abundance") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 0.3)) + scale_y_continuous(limits=c(0, 0.3)) + geom_text(aes(label=data_kmers$Kmer), size=1) )
    garbage <- dev.off()


}