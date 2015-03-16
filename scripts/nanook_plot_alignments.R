library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
refid <- args[3];

data_coverage_twod_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_2D_coverage.txt", sep="");
data_coverage_template_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Template_coverage.txt", sep="");
data_coverage_complement_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Complement_coverage.txt", sep="");
data_perfect_cumulative_twod_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_2D_cumulative_perfect_kmers.txt", sep="");
data_perfect_cumulative_template_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Template_cumulative_perfect_kmers.txt", sep="");
data_perfect_cumulative_complement_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Complement_cumulative_perfect_kmers.txt", sep="");
data_perfect_best_twod_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_2D_best_perfect_kmers.txt", sep="");
data_perfect_best_template_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Template_best_perfect_kmers.txt", sep="");
data_perfect_best_complement_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Complement_best_perfect_kmers.txt", sep="");
data_gc_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_gc.txt", sep="");

pdf_coverage_twod <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_2D_coverage.pdf", sep="");
pdf_coverage_template <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Template_coverage.pdf", sep="");
pdf_coverage_complement <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Complement_coverage.pdf", sep="");
pdf_perfect_cumulative_twod <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_2D_cumulative_perfect_kmers.pdf", sep="");
pdf_perfect_cumulative_template <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Template_cumulative_perfect_kmers.pdf", sep="");
pdf_perfect_cumulative_complement <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Complement_cumulative_perfect_kmers.pdf", sep="");
pdf_perfect_best_twod <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_2D_best_perfect_kmers.pdf", sep="");
pdf_perfect_best_template <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Template_best_perfect_kmers.pdf", sep="");
pdf_perfect_best_complement <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Complement_best_perfect_kmers.pdf", sep="");
pdf_gc <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_gc.pdf", sep="");

pdf(pdf_gc, width=12, height=3)
data_gc = read.table(data_gc_filename, col.name=c("Position", "Coverage"))
ggplot(data_gc, aes(x=data_gc$Position, y=data_gc$Coverage)) + geom_line(color="black") + ggtitle("GC content") + theme(text = element_text(size=10)) + xlab("Position") + ylab("GC %") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_y_continuous(limits=c(0, 100))
garbage <- dev.off()

pdf(pdf_coverage_twod, width=12, height=3)
data_coverage_twod = read.table(data_coverage_twod_filename, col.name=c("Position", "Coverage"))
ggplot(data_coverage_twod, aes(x=data_coverage_twod$Position, y=data_coverage_twod$Coverage)) + geom_line(color="#68B5B9") + ggtitle("2D coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0)
garbage <- dev.off()

pdf(pdf_coverage_template, width=12, height=3)
data_coverage_template = read.table(data_coverage_template_filename, col.name=c("Position", "Coverage"))
ggplot(data_coverage_template, aes(x=data_coverage_template$Position, y=data_coverage_template$Coverage)) + geom_line(color="#CF746D") + ggtitle("Template coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0)
garbage <- dev.off()

pdf(pdf_coverage_complement, width=12, height=3)
data_coverage_complement = read.table(data_coverage_complement_filename, col.name=c("Position", "Coverage"))
ggplot(data_coverage_complement, aes(x=data_coverage_complement$Position, y=data_coverage_complement$Coverage)) + geom_line(color="#91A851") + ggtitle("Complement coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0)
garbage <- dev.off()

xlimit <- 80

pdf(pdf_perfect_cumulative_twod, width=4, height=3)
data_perfect_cumulative_twod = read.table(data_perfect_cumulative_twod_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_cumulative_twod, aes(x=data_perfect_cumulative_twod$Size, y=data_perfect_cumulative_twod$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#68B5B9") + ggtitle("2D - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8))
garbage <- dev.off()

pdf(pdf_perfect_cumulative_template, width=4, height=3)
data_perfect_cumulative_template = read.table(data_perfect_cumulative_template_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_cumulative_template, aes(x=data_perfect_cumulative_template$Size, y=data_perfect_cumulative_template$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#CF746D") + ggtitle("Template - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))
garbage <- dev.off()

pdf(pdf_perfect_cumulative_complement, width=4, height=3)
data_perfect_cumulative_complement = read.table(data_perfect_cumulative_complement_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_cumulative_complement, aes(x=data_perfect_cumulative_complement$Size, y=data_perfect_cumulative_complement$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#91A851") + ggtitle("Complement - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))
garbage <- dev.off()


pdf(pdf_perfect_best_twod, width=4, height=3)
data_perfect_best_twod = read.table(data_perfect_best_twod_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_best_twod, aes(x=data_perfect_best_twod$Size, y=data_perfect_best_twod$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#68B5B9") + ggtitle("2D - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 120)) + theme(text = element_text(size=8))
garbage <- dev.off()

pdf(pdf_perfect_best_template, width=4, height=3)
data_perfect_best_template = read.table(data_perfect_best_template_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_best_template, aes(x=data_perfect_best_template$Size, y=data_perfect_best_template$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#CF746D") + ggtitle("Template - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))
garbage <- dev.off()

pdf(pdf_perfect_best_complement, width=4, height=3)
data_perfect_best_complement = read.table(data_perfect_best_complement_filename, col.name=c("Size", "n", "Perfect"))
ggplot(data_perfect_best_complement, aes(x=data_perfect_best_complement$Size, y=data_perfect_best_complement$Perfect)) + geom_bar(stat="identity", width=0.7, fill="#91A851") + ggtitle("Complement - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))
garbage <- dev.off()

