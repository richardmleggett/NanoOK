library(ggplot2)
library(scales)
library(reshape)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
refid <- args[3];

filename_pdf <- paste(basename, "/", sample, "/analysis/", refid, "_alignment_plots.pdf", sep="");
filename_coverage_twod <- paste(basename, "/", sample, "/analysis/", refid, "_2D_coverage.txt", sep="");
filename_coverage_template <- paste(basename, "/", sample, "/analysis/", refid, "_Template_coverage.txt", sep="");
filename_coverage_complement <- paste(basename, "/", sample, "/analysis/", refid, "_Complement_coverage.txt", sep="");
filename_perfect_cumulative_twod <- paste(basename, "/", sample, "/analysis/", refid, "_2D_read_best_cumulative_perfect_kmers.txt", sep="");
filename_perfect_cumulative_template <- paste(basename, "/", sample, "/analysis/", refid, "_Template_read_best_cumulative_perfect_kmers.txt", sep="");
filename_perfect_cumulative_complement <- paste(basename, "/", sample, "/analysis/", refid, "_Complement_read_best_cumulative_perfect_kmers.txt", sep="");
filename_perfect_best_twod <- paste(basename, "/", sample, "/analysis/", refid, "_2D_read_best_perfect_kmers.txt", sep="");
filename_perfect_best_template <- paste(basename, "/", sample, "/analysis/", refid, "_Template_read_best_perfect_kmers.txt", sep="");
filename_perfect_best_complement <- paste(basename, "/", sample, "/analysis/", refid, "_Complement_read_best_perfect_kmers.txt", sep="");

# Create PDF

pdf(filename_pdf, paper="a4")
#par(mar = c(3, 6, 1.5, 0.7))
#par(mgp = c(1.8, 0.5, 0))

# PAGE 1 - coverage


data_coverage_twod = read.table(filename_coverage_twod, col.name=c("Position", "Coverage"))
coverage_twod <- ggplot(data_coverage_twod, aes(x=data_coverage_twod$Position, y=data_coverage_twod$Coverage)) + geom_line() + ggtitle("2D coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

data_coverage_template = read.table(filename_coverage_template, col.name=c("Position", "Coverage"))
coverage_template <- ggplot(data_coverage_template, aes(x=data_coverage_template$Position, y=data_coverage_template$Coverage)) + geom_line() + ggtitle("Template coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

data_coverage_complement = read.table(filename_coverage_complement, col.name=c("Position", "Coverage"))
coverage_complement <- ggplot(data_coverage_complement, aes(x=data_coverage_complement$Position, y=data_coverage_complement$Coverage)) + geom_line() + ggtitle("Complement coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

#multiplot(coverage_template, coverage_complement, coverage_twod, cols=1)
grid.arrange(coverage_template, coverage_complement, coverage_twod, ncol=1, main=refid);

# PAGE 2 - perfect kmers

xlimit <- 80
data_perfect_cumulative_twod = read.table(filename_perfect_cumulative_twod, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_twod <- ggplot(data_perfect_cumulative_twod, aes(x=data_perfect_cumulative_twod$Size, y=data_perfect_cumulative_twod$Perfect)) + geom_bar(stat="identity") + ggtitle("2D - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

data_perfect_cumulative_template = read.table(filename_perfect_cumulative_template, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_template <- ggplot(data_perfect_cumulative_template, aes(x=data_perfect_cumulative_template$Size, y=data_perfect_cumulative_template$Perfect)) + geom_bar(stat="identity") + ggtitle("Template - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

data_perfect_cumulative_complement = read.table(filename_perfect_cumulative_complement, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_complement <- ggplot(data_perfect_cumulative_complement, aes(x=data_perfect_cumulative_complement$Size, y=data_perfect_cumulative_complement$Perfect)) + geom_bar(stat="identity") + ggtitle("Complement - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

data_perfect_best_twod = read.table(filename_perfect_best_twod, col.name=c("Size", "n", "Perfect"))
perfect_best_twod <- ggplot(data_perfect_best_twod, aes(x=data_perfect_best_twod$Size, y=data_perfect_best_twod$Perfect)) + geom_bar(stat="identity") + ggtitle("2D - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

data_perfect_best_template = read.table(filename_perfect_best_template, col.name=c("Size", "n", "Perfect"))
perfect_best_template <- ggplot(data_perfect_best_template, aes(x=data_perfect_best_template$Size, y=data_perfect_best_template$Perfect)) + geom_bar(stat="identity") + ggtitle("Template - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

data_perfect_best_complement = read.table(filename_perfect_best_complement, col.name=c("Size", "n", "Perfect"))
perfect_best_complement <- ggplot(data_perfect_best_complement, aes(x=data_perfect_best_complement$Size, y=data_perfect_best_complement$Perfect)) + geom_bar(stat="identity") + ggtitle("Complement - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, xlimit)) + theme(text = element_text(size=8))

grid.arrange(perfect_cumulative_template, perfect_cumulative_complement, perfect_cumulative_twod, perfect_best_template, perfect_best_complement, perfect_best_twod, ncol=2, main=refid)

garbage <- dev.off()



