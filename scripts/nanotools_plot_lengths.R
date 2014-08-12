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

filename_pdf <- paste(basename, "/", sample, "/analysis/", sample, "_length_plots.pdf", sep="");
filename_lengths_twod <- paste(basename, sample, "analysis", "all_2D_lengths.txt", sep="/");
filename_lengths_template <- paste(basename, sample, "analysis", "all_Template_lengths.txt", sep="/");
filename_lengths_complement <- paste(basename, sample, "analysis", "all_Complement_lengths.txt", sep="/");

# Create PDF

pdf(filename_pdf, paper="a4")
#par(mar = c(3, 6, 1.5, 0.7))
#par(mgp = c(1.8, 0.5, 0))

data_lengths_twod = read.table(filename_lengths_twod, col.name=c("name", "length"))
lengths_twod <- ggplot(data_lengths_twod, aes(x=data_lengths_twod$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("2D read lengths") + theme(text = element_text(size=10))

data_lengths_template = read.table(filename_lengths_template, col.name=c("name", "length"))
lengths_template <- ggplot(data_lengths_template, aes(x=data_lengths_template$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Template read lengths") + theme(text = element_text(size=10))

data_lengths_complement = read.table(filename_lengths_complement, col.name=c("name", "length"))
lengths_complement <- ggplot(data_lengths_complement, aes(x=data_lengths_complement$length), xlab="Length") + geom_histogram(binwidth=500, colour="black", fill="white") + xlab("Length") +ylab("Count") + scale_x_continuous(limits=c(0, 35000)) + ggtitle("Complement read lengths") + theme(text = element_text(size=10))

multiplot(lengths_template, lengths_complement, lengths_twod, cols=1)

garbage <- dev.off()



