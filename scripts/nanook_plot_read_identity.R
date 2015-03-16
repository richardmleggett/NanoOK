library(ggplot2)
library(scales)

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
reference <- args[3];

alignments_twod_filename <- paste(basename, "/", sample, "/analysis/", reference, "/", reference, "_2D_alignments.txt", sep="");
alignments_template_filename <- paste(basename, "/", sample, "/analysis/", reference, "/", reference, "_Template_alignments.txt", sep="");
alignments_complement_filename <- paste(basename, "/", sample, "/analysis/", reference, "/", reference, "_Complement_alignments.txt", sep="");

identity_hist_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_length_vs_identity_hist.pdf", sep="");
identity_hist_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_length_vs_identity_hist.pdf", sep="");
identity_hist_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_length_vs_identity_hist.pdf", sep="");

identity_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_length_vs_identity_scatter.pdf", sep="");
identity_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_length_vs_identity_scatter.pdf", sep="");
identity_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_length_vs_identity_scatter.pdf", sep="");

aid_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_read_fraction_vs_alignment_identity_scatter.pdf", sep="");
aid_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_read_fraction_vs_alignment_identity_scatter.pdf", sep="");
aid_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_read_fraction_vs_alignment_identity_scatter.pdf", sep="");

qid_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_read_fraction_vs_query_identity_scatter.pdf", sep="");
qid_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_read_fraction_vs_query_identity_scatter.pdf", sep="");
qid_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_read_fraction_vs_query_identity_scatter.pdf", sep="");

best_perf_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_longest_perfect_vs_length_scatter.pdf", sep="");
best_perf_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_longest_perfect_vs_length_scatter.pdf", sep="");
best_perf_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_longest_perfect_vs_length_scatter.pdf", sep="");

best_perf_zoom_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_longest_perfect_vs_length_zoom_scatter.pdf", sep="");
best_perf_zoom_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_longest_perfect_vs_length_zoom_scatter.pdf", sep="");
best_perf_zoom_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_longest_perfect_vs_length_zoom_scatter.pdf", sep="");

mean_perf_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_mean_perfect_vs_length_scatter.pdf", sep="");
mean_perf_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_mean_perfect_vs_length_scatter.pdf", sep="");
mean_perf_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_mean_perfect_vs_length_scatter.pdf", sep="");

nk21_scatter_twod_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_2D_nk21_vs_length_scatter.pdf", sep="");
nk21_scatter_template_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Template_nk21_vs_length_scatter.pdf", sep="");
nk21_scatter_complement_pdf <- paste(basename, "/", sample, "/graphs/", reference, "/", reference, "_Complement_nk21_vs_length_scatter.pdf", sep="");


alignments_twod_filename
data_alignments_twod = read.table(alignments_twod_filename, header=TRUE)

alignments_template_filename
data_alignments_template = read.table(alignments_template_filename, header=TRUE)

alignments_complement_filename
data_alignments_complement = read.table(alignments_complement_filename, header=TRUE)

# Length vs Identity histograms
pdf(identity_hist_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryPercentIdentity), xlab="Identity") + geom_histogram(fill="#68B5B9") + xlab("Identity %") +ylab("Count") + ggtitle("2D") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(identity_hist_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$QueryPercentIdentity), xlab="Identity") + geom_histogram(fill="#CF746D") + xlab("Identity %") +ylab("Count") + ggtitle("Template") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(identity_hist_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryPercentIdentity), xlab="Identity") + geom_histogram(fill="#91A851") + xlab("Identity %") +ylab("Count") + ggtitle("Complement") + theme(text = element_text(size=10))
garbage <- dev.off()

# Identity vs Length Scatter plots
pdf(identity_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryLength, y=data_alignments_twod$QueryPercentIdentity), xlab="Identity") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Length") +ylab("Identity") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(identity_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$QueryLength, y=data_alignments_template$QueryPercentIdentity), xlab="Identity") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Length") +ylab("Identity") + ggtitle("Template") + theme(text = element_text(size=10)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(identity_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryLength, y=data_alignments_complement$QueryPercentIdentity), xlab="Identity") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Length") +ylab("Identity") + ggtitle("Complement") + theme(text = element_text(size=10)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

# Identity vs. Fraction of read aligned scatter plots
pdf(aid_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$PercentQueryAligned, y=data_alignments_twod$AlignmentPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Percentage of query aligned") +ylab("Alignment identity %") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(aid_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$PercentQueryAligned, y=data_alignments_template$AlignmentPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Percentage of query aligned") +ylab("Alignment identity %") + ggtitle("Template") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(aid_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$PercentQueryAligned, y=data_alignments_complement$AlignmentPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Percentage of query aligned") +ylab("Alignment identity %") + ggtitle("Complement") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(qid_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$PercentQueryAligned, y=data_alignments_twod$QueryPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Percentage of query aligned") +ylab("Query identity %") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(qid_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$PercentQueryAligned, y=data_alignments_template$QueryPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Percentage of query aligned") +ylab("Query identity %") + ggtitle("Template") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(qid_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$PercentQueryAligned, y=data_alignments_complement$QueryPercentIdentity), xlab="Percentage of query aligned") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Percentage of query aligned") +ylab("Query identity %") + ggtitle("Complement") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

# Best perfect sequence vs. length scatters
pdf(best_perf_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryLength, y=data_alignments_twod$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("2D") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(best_perf_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$QueryLength, y=data_alignments_template$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("Template") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(best_perf_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryLength, y=data_alignments_complement$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("Complement") + theme(text = element_text(size=10))
garbage <- dev.off()

# Best perfect sequence vs. length scatters zoomed
pdf(best_perf_zoom_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryLength, y=data_alignments_twod$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000))
garbage <- dev.off()

pdf(best_perf_zoom_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$QueryLength, y=data_alignments_template$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("Template") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000))
garbage <- dev.off()

pdf(best_perf_zoom_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryLength, y=data_alignments_complement$LongestPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Read length") +ylab("Longest perfect sequence") + ggtitle("Complement") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000))
garbage <- dev.off()

# Number of perfect 21mers verses length scatter
pdf(nk21_scatter_twod_pdf, height=4, width=6)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryLength, y=data_alignments_twod$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("2D") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(nk21_scatter_template_pdf, height=4, width=6)
ggplot(data_alignments_template, aes(x=data_alignments_template$QueryLength, y=data_alignments_template$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("Template") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(nk21_scatter_complement_pdf, height=4, width=6)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryLength, y=data_alignments_complement$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("Complement") + theme(text = element_text(size=10))
garbage <- dev.off()


# Mean perfect sequence vs. length scatters
#pdf(mean_perf_scatter_twod_pdf, height=4, width=6)
#ggplot(data_alignments_twod, aes(x=data_alignments_twod$QueryLength, y=data_alignments_twod$MeanPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4) + xlab("Read length") +ylab("Mean perfect sequence") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_x_continuous(limits=c(0, 10000))
#garbage <- dev.off()

#pdf(mean_perf_scatter_template_pdf, height=4, width=6)
#ggplot(data_alignments_template, aes(x=data_alignments_template$QueryLength, y=data_alignments_template$MeanPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4) + xlab("Read length") +ylab("Mean perfect sequence") + ggtitle("Template") + theme(text = element_text(size=10))
#garbage <- dev.off()

#pdf(mean_perf_scatter_complement_pdf, height=4, width=6)
#ggplot(data_alignments_complement, aes(x=data_alignments_complement$QueryLength, y=data_alignments_complement$MeanPerfectKmer), xlab="Read length") + geom_point(shape=1, alpha = 0.4) + xlab("Read length") +ylab("Mean perfect sequence") + ggtitle("Complement") + theme(text = element_text(size=10))
#garbage <- dev.off()