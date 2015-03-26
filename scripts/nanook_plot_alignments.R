library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
refid <- args[3];

types = c("2D", "Template", "Complement");
colours = c("#68B5B9", "#CF746D", "#91A851");

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    cat(type, " ", colourcode, "\n");

    # Plot GC% vs position
    data_gc_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_gc.txt", sep="");
    pdf_gc <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_gc.pdf", sep="");
    pdf(pdf_gc, width=12, height=3)
    data_gc = read.table(data_gc_filename, col.name=c("Position", "Coverage"))
    print(ggplot(data_gc, aes(x=data_gc$Position, y=data_gc$Coverage)) + geom_line(color="black") + ggtitle("GC content") + theme(text = element_text(size=10)) + xlab("Position") + ylab("GC %") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_y_continuous(limits=c(0, 100)))
    garbage <- dev.off()

    # Plot coverage vs position
    data_coverage_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_",type,"_coverage.txt", sep="");
    pdf_coverage <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_",type,"_coverage.pdf", sep="");
    pdf(pdf_coverage, width=12, height=3)
    data_coverage = read.table(data_coverage_filename, col.name=c("Position", "Coverage"))
    print(ggplot(data_coverage, aes(x=data_coverage$Position, y=data_coverage$Coverage)) + geom_line(color=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0))
    garbage <- dev.off()

    # Plot % reads with perfect kmer vs kmer size
    data_perfect_cumulative_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.txt", sep="");
    pdf_perfect_cumulative <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.pdf", sep="");
    xlimit <- 80
    pdf(pdf_perfect_cumulative, width=4, height=3)
    data_perfect_cumulative = read.table(data_perfect_cumulative_filename, col.name=c("Size", "n", "Perfect"))
    print(ggplot(data_perfect_cumulative, aes(x=data_perfect_cumulative$Size, y=data_perfect_cumulative$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8)))
    garbage <- dev.off()

    # Plot %reads vs best perfect kmer
    data_perfect_best_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_",type,"_best_perfect_kmers.txt", sep="");
    pdf_perfect_best <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.pdf", sep="");
    pdf(pdf_perfect_best, width=4, height=3)
    data_perfect_best = read.table(data_perfect_best_filename, col.name=c("Size", "n", "Perfect"))
    print(ggplot(data_perfect_best, aes(x=data_perfect_best$Size, y=data_perfect_best$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8)))
    garbage <- dev.off()
}