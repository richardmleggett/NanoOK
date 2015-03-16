library(ggplot2)
library(scales)

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
reference <- args[3];

filename_lengths_twod <- paste(basename, sample, "analysis", "all_2D_lengths.txt", sep="/");
filename_lengths_template <- paste(basename, sample, "analysis", "all_Template_lengths.txt", sep="/");
filename_lengths_complement <- paste(basename, sample, "analysis", "all_Complement_lengths.txt", sep="/");

filename_kmers_twod <- paste(basename, sample, "analysis", "all_2D_kmers.txt", sep="/");
filename_kmers_template <- paste(basename, sample, "analysis", "all_Template_kmers.txt", sep="/");
filename_kmers_complement <- paste(basename, sample, "analysis", "all_Complement_kmers.txt", sep="/");

lengths_twod_pdf <- paste(basename, sample, "graphs", "all_2D_lengths.pdf", sep="/");
lengths_template_pdf <- paste(basename, sample, "graphs", "all_Template_lengths.pdf", sep="/");
lengths_complement_pdf <- paste(basename, sample, "graphs", "all_Complement_lengths.pdf", sep="/");

kmers_twod_pdf <- paste(basename, sample, "graphs", "all_2D_21mers.pdf", sep="/");
kmers_template_pdf <- paste(basename, sample, "graphs", "all_Template_21mers.pdf", sep="/");
kmers_complement_pdf <- paste(basename, sample, "graphs", "all_Complement_21mers.pdf", sep="/");


pdf(lengths_twod_pdf, height=4, width=6)
data_lengths_twod = read.table(filename_lengths_twod, col.name=c("name", "length"))
ggplot(data_lengths_twod, aes(x=data_lengths_twod$length), xlab="Length") + geom_histogram(binwidth=1000, fill="#68B5B9") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("2D read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(lengths_template_pdf, height=4, width=6)
data_lengths_template = read.table(filename_lengths_template, col.name=c("name", "length"))
ggplot(data_lengths_template, aes(x=data_lengths_template$length), xlab="Length") + geom_histogram(binwidth=1000, fill="#CF746D") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Template read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(lengths_complement_pdf, height=4, width=6)
data_lengths_complement = read.table(filename_lengths_complement, col.name=c("name", "length"))
ggplot(data_lengths_complement, aes(x=data_lengths_complement$length), xlab="Length") + geom_histogram(binwidth=1000, fill="#91A851") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Complement read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()

# Number of perfect 21mers verses length scatter
pdf(kmers_twod_pdf, height=4, width=6)
data_alignments_twod = read.table(filename_kmers_twod, header=TRUE)
ggplot(data_alignments_twod, aes(x=data_alignments_twod$Length, y=data_alignments_twod$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#68B5B9") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("2D") + theme(text = element_text(size=10)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 20))
garbage <- dev.off()

# + scale_x_continuous(limits=c(0, 3000), breaks=seq(0, 20000, 2000)) + scale_y_continuous(limits=c(0, 60), breaks=seq(0, 200, 20))

pdf(kmers_template_pdf, height=4, width=6)
data_alignments_template = read.table(filename_kmers_template, header=TRUE)
ggplot(data_alignments_template, aes(x=data_alignments_template$Length, y=data_alignments_template$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#CF746D") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("Template") + theme(text = element_text(size=10)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 10))
garbage <- dev.off()

pdf(kmers_complement_pdf, height=4, width=6)
data_alignments_complement = read.table(filename_kmers_complement, header=TRUE)
ggplot(data_alignments_complement, aes(x=data_alignments_complement$Length, y=data_alignments_complement$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color="#91A851") + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle("Complement") + theme(text = element_text(size=10)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 10))
garbage <- dev.off()



#filename_lengths_twod_all <- paste(basename, "/", sample, "/analysis/", reference, "_2D_all_perfect_kmers.txt", sep="");
#lengths_twod_all_pdf <- paste(basename, "/", sample, "/graphs/", reference, "_2D_all_perfect_kmers.pdf", sep="");


#pdf(lengths_twod_all_pdf, height=4, width=6)
#data_lengths_twod = read.table(filename_lengths_twod_all, col.name=c("name", "length"))
#ggplot(data_lengths_twod, aes(x=data_lengths_twod$name,y=data_lengths_twod$length), xlab="Length") + geom_bar(stat="identity", binwidth=1, colour="black", fill="white") + xlab("Length") +ylab("Count") + ggtitle("2D read lengths") + theme(text = element_text(size=10)) + scale_y_continuous()
#garbage <- dev.off()


