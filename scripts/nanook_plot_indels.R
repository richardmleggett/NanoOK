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
    
    data_insertions_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_",type,"_insertions.txt", sep="");
    pdf_insertions <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_",type,"_insertions.pdf", sep="");
    pdf(pdf_insertions, height=2.5, width=4)
    data_insertions = read.table(data_insertions_filename, col.name=c("Size", "Percent"))
    print(ggplot(data_insertions, aes(x=data_insertions$Size, y=data_insertions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)))
    garbage <- dev.off()

    data_deletions_filename <- paste(basename, "/", sample, "/analysis/", refid, "/", refid, "_",type,"_deletions.txt", sep="");
    pdf_deletions <- paste(basename, "/", sample, "/graphs/", refid, "/", refid, "_",type,"_deletions.pdf", sep="");
    pdf(pdf_deletions, height=2.5, width=4)
    data_deletions = read.table(data_deletions_filename, col.name=c("Size", "Percent"))
    print(ggplot(data_deletions, aes(x=data_deletions$Size, y=data_deletions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=10)) + xlab("Size") + ylab("%") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)))
    garbage <- dev.off()
}