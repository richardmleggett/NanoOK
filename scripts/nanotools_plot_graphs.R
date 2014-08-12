library(ggplot2)
library(scales)
library(reshape)
library(grid)
library(gridExtra)

# Multiple plot function
#
# ggplot objects can be passed in ..., or to plotlist (as a list of ggplot objects)
# - cols:   Number of columns in layout
# - layout: A matrix specifying the layout. If present, 'cols' is ignored.
#
# If the layout is something like matrix(c(1,2,3,3), nrow=2, byrow=TRUE),
# then plot 1 will go in the upper left, 2 will go in the upper right, and
# 3 will go all the way across the bottom.
#
multiplot <- function(..., plotlist=NULL, file, cols=1, layout=NULL) {
    require(grid)
    
    # Make a list from the ... arguments and plotlist
    plots <- c(list(...), plotlist)
    
    numPlots = length(plots)
    
    # If layout is NULL, then use 'cols' to determine layout
    if (is.null(layout)) {
        # Make the panel
        # ncol: Number of columns of plots
        # nrow: Number of rows needed, calculated from # of cols
        layout <- matrix(seq(1, cols * ceiling(numPlots/cols)),
        ncol = cols, nrow = ceiling(numPlots/cols))
    }
    
    if (numPlots==1) {
        print(plots[[1]])
        
    } else {
        # Set up the page
        grid.newpage()
        pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
        
        # Make each plot, in the correct location
        for (i in 1:numPlots) {
            # Get the i,j matrix positions of the regions that contain this subplot
            matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
            
            print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
            layout.pos.col = matchidx$col))
        }
    }
}

# Filenames

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];

filename_pdf <- paste(basename, "/", sample, "/", sample, "_plots.pdf", sep="");
filename_lengths_twod <- paste(basename, sample, "all_2D_lengths.txt", sep="/");
filename_lengths_template <- paste(basename, sample, "all_Template_lengths.txt", sep="/");
filename_lengths_complement <- paste(basename, sample, "all_Complement_lengths.txt", sep="/");
filename_coverage_twod <- paste(basename, sample, "2D_last_coverage.txt", sep="/");
filename_coverage_template <- paste(basename, sample, "Template_last_coverage.txt", sep="/");
filename_coverage_complement <- paste(basename, sample, "Complement_last_coverage.txt", sep="/");
filename_perfect_cumulative_twod <- paste(basename, sample, "2D_last_perfect_cumulative.txt", sep="/");
filename_perfect_cumulative_template <- paste(basename, sample, "Template_last_perfect_cumulative.txt", sep="/");
filename_perfect_cumulative_complement <- paste(basename, sample, "Complement_last_perfect_cumulative.txt", sep="/");
filename_perfect_best_twod <- paste(basename, sample, "2D_last_perfect_best.txt", sep="/");
filename_perfect_best_template <- paste(basename, sample, "Template_last_perfect_best.txt", sep="/");
filename_perfect_best_complement <- paste(basename, sample, "Complement_last_perfect_best.txt", sep="/");

# Create PDF

pdf(filename_pdf, paper="a4")
#par(mar = c(3, 6, 1.5, 0.7))
#par(mgp = c(1.8, 0.5, 0))

# PAGE 1 - lengths

data_lengths_twod = read.table(filename_lengths_twod, col.name=c("name", "length"))
lengths_twod <- ggplot(data_lengths_twod, aes(x=data_lengths_twod$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("2D read lengths") + theme(text = element_text(size=10))

data_lengths_template = read.table(filename_lengths_template, col.name=c("name", "length"))
lengths_template <- ggplot(data_lengths_template, aes(x=data_lengths_template$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Template read lengths") + theme(text = element_text(size=10))

data_lengths_complement = read.table(filename_lengths_complement, col.name=c("name", "length"))
lengths_complement <- ggplot(data_lengths_complement, aes(x=data_lengths_complement$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Complement read lengths") + theme(text = element_text(size=10))

multiplot(lengths_template, lengths_complement, lengths_twod, cols=1)

# PAGE 2 - coverage

data_coverage_twod = read.table(filename_coverage_twod, col.name=c("Position", "Coverage"))
coverage_twod <- ggplot(data_coverage_twod, aes(x=data_coverage_twod$Position, y=data_coverage_twod$Coverage)) + geom_line() + ggtitle("2D coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

data_coverage_template = read.table(filename_coverage_template, col.name=c("Position", "Coverage"))
coverage_template <- ggplot(data_coverage_template, aes(x=data_coverage_template$Position, y=data_coverage_template$Coverage)) + geom_line() + ggtitle("Template coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

data_coverage_complement = read.table(filename_coverage_complement, col.name=c("Position", "Coverage"))
coverage_complement <- ggplot(data_coverage_complement, aes(x=data_coverage_complement$Position, y=data_coverage_complement$Coverage)) + geom_line() + ggtitle("Complement coverage") + theme(text = element_text(size=10)) + xlab("Position") + ylab("Mean coverage for bin") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2))

multiplot(coverage_template, coverage_complement, coverage_twod, cols=1)

# PAGE 3 - perfect kmers

data_perfect_cumulative_twod = read.table(filename_perfect_cumulative_twod, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_twod <- ggplot(data_perfect_cumulative_twod, aes(x=data_perfect_cumulative_twod$Size, y=data_perfect_cumulative_twod$Perfect)) + geom_bar(stat="identity") + ggtitle("2D - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

data_perfect_cumulative_template = read.table(filename_perfect_cumulative_template, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_template <- ggplot(data_perfect_cumulative_template, aes(x=data_perfect_cumulative_template$Size, y=data_perfect_cumulative_template$Perfect)) + geom_bar(stat="identity") + ggtitle("Template - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

data_perfect_cumulative_complement = read.table(filename_perfect_cumulative_complement, col.name=c("Size", "n", "Perfect"))
perfect_cumulative_complement <- ggplot(data_perfect_cumulative_complement, aes(x=data_perfect_cumulative_complement$Size, y=data_perfect_cumulative_complement$Perfect)) + geom_bar(stat="identity") + ggtitle("Complement - cumulative perfect kmers") + theme(text = element_text(size=10)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

data_perfect_best_twod = read.table(filename_perfect_best_twod, col.name=c("Size", "n", "Perfect"))
perfect_best_twod <- ggplot(data_perfect_best_twod, aes(x=data_perfect_best_twod$Size, y=data_perfect_best_twod$Perfect)) + geom_bar(stat="identity") + ggtitle("2D - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

data_perfect_best_template = read.table(filename_perfect_best_template, col.name=c("Size", "n", "Perfect"))
perfect_best_template <- ggplot(data_perfect_best_template, aes(x=data_perfect_best_template$Size, y=data_perfect_best_template$Perfect)) + geom_bar(stat="identity") + ggtitle("Template - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

data_perfect_best_complement = read.table(filename_perfect_best_complement, col.name=c("Size", "n", "Perfect"))
perfect_best_complement <- ggplot(data_perfect_best_complement, aes(x=data_perfect_best_complement$Size, y=data_perfect_best_complement$Perfect)) + geom_bar(stat="identity") + ggtitle("Complement - best perfect kmer") + theme(text = element_text(size=10)) + xlab("best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.2,0.2,0.2,0.2), "cm")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 60)) + theme(text = element_text(size=8))

multiplot(perfect_cumulative_template, perfect_cumulative_complement, perfect_cumulative_twod, perfect_best_template, perfect_best_complement, perfect_best_twod, cols=2)

garbage <- dev.off()



