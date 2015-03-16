library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
refid <- args[3];

data_insertions_twod_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_2D_insertions.txt", sep="");
data_insertions_template_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Template_insertions.txt", sep="");
data_insertions_complement_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Complement_insertions.txt", sep="");
data_deletions_twod_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_2D_deletions.txt", sep="");
data_deletions_template_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Template_deletions.txt", sep="");
data_deletions_complement_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_Complement_deletions.txt", sep="");

pdf_insertions_twod <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_2D_insertions.pdf", sep="");
pdf_insertions_template <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Template_insertions.pdf", sep="");
pdf_insertions_complement <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Complement_insertions.pdf", sep="");
pdf_deletions_twod <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_2D_deletions.pdf", sep="");
pdf_deletions_template <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Template_deletions.pdf", sep="");
pdf_deletions_complement <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_Complement_deletions.pdf", sep="");

pdf(pdf_insertions_twod, height=2.5, width=4)
data_insertions_twod = read.table(data_insertions_twod_filename, col.name=c("Size", "Percent"))
ggplot(data_insertions_twod, aes(x=data_insertions_twod$Size, y=data_insertions_twod$Percent)) + geom_bar(stat="identity", fill="#68B5B9") + ggtitle("2D insertion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()

pdf(pdf_insertions_template, height=2.5, width=4)
data_insertions_template = read.table(data_insertions_template_filename, col.name=c("Size", "Percent"))
ggplot(data_insertions_template, aes(x=data_insertions_template$Size, y=data_insertions_template$Percent)) + geom_bar(stat="identity", fill="#CF746D") + ggtitle("Template insertion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()

pdf(pdf_insertions_complement, height=2.5, width=4)
data_insertions_complement = read.table(data_insertions_complement_filename, col.name=c("Size", "Percent"))
ggplot(data_insertions_complement, aes(x=data_insertions_complement$Size, y=data_insertions_complement$Percent)) + geom_bar(stat="identity", fill="#91A851") + ggtitle("Complement insertion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()

pdf(pdf_deletions_twod, height=2.5, width=4)
data_deletions_twod = read.table(data_deletions_twod_filename, col.name=c("Size", "Percent"))
ggplot(data_deletions_twod, aes(x=data_deletions_twod$Size, y=data_deletions_twod$Percent)) + geom_bar(stat="identity", fill="#68B5B9") + ggtitle("2D deletion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()

pdf(pdf_deletions_template, height=2.5, width=4)
data_deletions_template = read.table(data_deletions_template_filename, col.name=c("Size", "Percent"))
ggplot(data_deletions_template, aes(x=data_deletions_template$Size, y=data_deletions_template$Percent)) + geom_bar(stat="identity", fill="#CF746D") + ggtitle("Template deletion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()

pdf(pdf_deletions_complement, height=2.5, width=4)
data_deletions_complement = read.table(data_deletions_complement_filename, col.name=c("Size", "Percent"))
ggplot(data_deletions_complement, aes(x=data_deletions_complement$Size, y=data_deletions_complement$Percent)) + geom_bar(stat="identity", fill="#91A851") + ggtitle("Complement deletion size") + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))
garbage <- dev.off()
