library(ggplot2)
library(scales)
library(grid)

args <- commandArgs(TRUE)
analysisdir <- args[1];
graphsdir <- args[2];
format <- args[3];

types = c("2D", "Template", "Complement");
colours = c("#68B5B9", "#CF746D", "#91A851");

if (format=="png") {
    textsize <- c(40)
    pointsize <- c(5)
    pointalpha <- c(0.5)
    pointshape <- c(1)
    pointwidth <- c(3)
    xvjust <- c(1.2)
    yvjust <- c(1.8)
} else {
    textsize <- c(14)
    pointsize <- c(2)
    pointalpha <- c(0.4)
    pointshape <- c(1)
    pointwidth <- c(1)
    xvjust <- c(0.2)
    yvjust <- c(0.8)
}

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    #cat(type, " ", colourcode, "\n");
    
    # Count vs length
    filename_lengths <- paste(analysisdir, "/", "all_",type,"_lengths.txt", sep="");
    data_lengths = read.table(filename_lengths, col.name=c("name", "length"))

    if (format=="png") {
        lengths_png <- paste(graphsdir, "/", "all_",type,"_lengths.png", sep="");
        png(lengths_png, width=1200, height=800)
        print(ggplot(data_lengths, aes(x=data_lengths$length), xlab="Length") + geom_histogram(binwidth=1000, fill=colourcode) + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        lengths_pdf <- paste(graphsdir, "/", "all_",type,"_lengths.pdf", sep="");
        pdf(lengths_pdf, width=6, height=4)
        print(ggplot(data_lengths, aes(x=data_lengths$length), xlab="Length") + geom_histogram(binwidth=1000, fill=colourcode) + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Number of perfect 21mers verses length scatter
    filename_kmers <- paste(analysisdir, "/", "all_",type,"_kmers.txt", sep="");
    data_alignments = read.table(filename_kmers, header=TRUE)

    if (format=="png") {
        kmers_png <- paste(graphsdir, "/", "all_",type,"_21mers.png", sep="");
        png(kmers_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$Length, y=data_alignments$nk21), xlab="Read length") + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 20)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        #grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        kmers_pdf <- paste(graphsdir, "/", "all_",type,"_21mers.pdf", sep="");
        pdf(kmers_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$Length, y=data_alignments$nk21), xlab="Read length") + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(breaks=seq(0, 40000, 4000)) + scale_y_continuous(breaks=seq(0, 400, 20)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()
}
