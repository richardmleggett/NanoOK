library(ggplot2)
library(scales)

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
reference <- args[3];

types = c("2D", "Template", "Complement");
colours = c("#68B5B9", "#CF746D", "#91A851");

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    cat(type, " ", colourcode, "\n");

    input_filename <- paste(basename, "/", sample, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
    data_alignments = read.table(input_filename, header=TRUE);

    # Length vs Identity histograms
    identity_hist_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_length_vs_identity_hist.pdf", sep="")
    pdf(identity_hist_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryPercentIdentity), xlab="Identity") + geom_histogram(fill=colourcode) + xlab("Identity %") +ylab("Count") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()
    
    # Identity vs Length Scatter plots
    identity_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_length_vs_identity_scatter.pdf", sep="");
    pdf(identity_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$QueryPercentIdentity), xlab="Identity") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Length") +ylab("Identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Alignment identity vs. Fraction of read aligned scatter plots
    aid_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_read_fraction_vs_alignment_identity_scatter.pdf", sep="");
    pdf(aid_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$AlignmentPercentIdentity), xlab="Percentage of read aligned") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Percentage of read aligned") +ylab("Identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Query identity vs. Fraction of read aligned scatter plots
    qid_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_read_fraction_vs_query_identity_scatter.pdf", sep="");
    pdf(qid_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$QueryPercentIdentity), xlab="Percentage of read aligned") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Percentage of read aligned") +ylab("Identity %") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters
    best_perf_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_longest_perfect_vs_length_scatter.pdf", sep="");
    pdf(best_perf_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters zoomed
    best_perf_zoom_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_longest_perfect_vs_length_zoom_scatter.pdf", sep="");
    pdf(best_perf_zoom_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000)))
    garbage <- dev.off()

    # Number of perfect 21mers verses length scatter
    nk21_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_nk21_vs_length_scatter.pdf", sep="");
    pdf(nk21_scatter_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Mean perfect sequence vs. length scatters
    #mean_perf_scatter_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_mean_perfect_vs_length_scatter.pdf", sep="");
    #pdf(mean_perf_scatter_pdf, height=4, width=6)
    #print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$MeanPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4) + xlab("Read length") +ylab("Mean perfect sequence") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000)))
    #garbage <- dev.off()

    # Percentage of read aligned vs read length
    output_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_",type,"_percent_aligned_vs_length_scatter.pdf", sep="");
    pdf(output_pdf, height=4, width=6)
    print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$PercentQueryAligned), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Percentage of read aligned") + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()
}