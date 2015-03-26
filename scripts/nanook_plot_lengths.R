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
    
    # Count vs length
    filename_lengths <- paste(basename, "/", sample, "/analysis/", "all_",type,"_lengths.txt", sep="");
    lengths_pdf <- paste(basename, "/", sample, "/graphs/", "all_",type,"_lengths.pdf", sep="");
    pdf(lengths_pdf, height=4, width=6)
    data_lengths = read.table(filename_lengths, col.name=c("name", "length"))
    print(ggplot(data_lengths, aes(x=data_lengths$length), xlab="Length") + geom_histogram(binwidth=1000, fill=colourcode) + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle(type) + theme(text = element_text(size=10)))
    garbage <- dev.off()

    # Number of perfect 21mers verses length scatter
    filename_kmers <- paste(basename, "/", sample, "/analysis/", "all_",type,"_kmers.txt", sep="");
    kmers_pdf <- paste(basename, "/", sample, "/graphs/", "all_",type,"_21mers.pdf", sep="");
    pdf(kmers_pdf, height=4, width=6)
    data_alignments = read.table(filename_kmers, header=TRUE)
    print(ggplot(data_alignments, aes(x=data_alignments$Length, y=data_alignments$nk21), xlab="Read length") + geom_point(shape=1, alpha = 0.4, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=10)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 20)))
    # + scale_x_continuous(limits=c(0, 3000), breaks=seq(0, 20000, 2000)) + scale_y_continuous(limits=c(0, 60), breaks=seq(0, 200, 20))
    garbage <- dev.off()
}
