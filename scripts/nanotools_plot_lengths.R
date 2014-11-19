library(ggplot2)
library(scales)

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
reference <- args[3];

filename_lengths_twod <- paste(basename, sample, "analysis", "all_2D_lengths.txt", sep="/");
filename_lengths_template <- paste(basename, sample, "analysis", "all_Template_lengths.txt", sep="/");
filename_lengths_complement <- paste(basename, sample, "analysis", "all_Complement_lengths.txt", sep="/");

lengths_twod_pdf <- paste(basename, sample, "graphs", "all_2D_lengths.pdf", sep="/");
lengths_template_pdf <- paste(basename, sample, "graphs", "all_Template_lengths.pdf", sep="/");
lengths_complement_pdf <- paste(basename, sample, "graphs", "all_Complement_lengths.pdf", sep="/");

pdf(lengths_twod_pdf, height=4, width=6)
data_lengths_twod = read.table(filename_lengths_twod, col.name=c("name", "length"))
ggplot(data_lengths_twod, aes(x=data_lengths_twod$length), xlab="Length") + geom_histogram(binwidth=1000, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("2D read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(lengths_template_pdf, height=4, width=6)
data_lengths_template = read.table(filename_lengths_template, col.name=c("name", "length"))
ggplot(data_lengths_template, aes(x=data_lengths_template$length), xlab="Length") + geom_histogram(binwidth=1000, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Template read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()

pdf(lengths_complement_pdf, height=4, width=6)
data_lengths_complement = read.table(filename_lengths_complement, col.name=c("name", "length"))
ggplot(data_lengths_complement, aes(x=data_lengths_complement$length), xlab="Length") + geom_histogram(binwidth=1000, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Complement read lengths") + theme(text = element_text(size=10))
garbage <- dev.off()



#filename_lengths_twod_all <- paste(basename, "/", sample, "/analysis/", reference, "_2D_all_perfect_kmers.txt", sep="");
#lengths_twod_all_pdf <- paste(basename, "/", sample, "/graphs/", reference, "_2D_all_perfect_kmers.pdf", sep="");


#pdf(lengths_twod_all_pdf, height=4, width=6)
#data_lengths_twod = read.table(filename_lengths_twod_all, col.name=c("name", "length"))
#ggplot(data_lengths_twod, aes(x=data_lengths_twod$name,y=data_lengths_twod$length), xlab="Length") + geom_bar(stat="identity", binwidth=1, colour="black", fill="white") + xlab("Length") +ylab("Count") + ggtitle("2D read lengths") + theme(text = element_text(size=10)) + scale_y_continuous()
#garbage <- dev.off()


