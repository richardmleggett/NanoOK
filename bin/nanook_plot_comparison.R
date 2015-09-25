library(ggplot2)
library(scales)
library(grid)
library(gridExtra)
library(reshape2)

# Filenames
args <- commandArgs(TRUE)
samplelist <- args[1];
outdir <- args[2];
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
    textsize <- c(10)
    pointsize <- c(2)
    pointalpha <-c(0.4)
    pointshape <- c(1)
    pointwidth <- c(1)
    xvjust <- c(0.2)
    yvjust <- c(0.8)
}


data_samples = read.table(samplelist, header=TRUE);

for (t in 1:3) {
    colourcode = colours[t];
    
    # Gather data for box plots of length
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_lengths <- paste(sampledir, "/analysis/", "all_",type,"_lengths.txt", sep="");
        data_lengths = read.table(filename_lengths, col.name=c("name", "length"));
        #df$size <- data_lengths$length;
        thisid <- data_samples[i, "SampleName"];
        #paste(data_samples[i, "SampleName"], type, sep="_");
        thisid
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Length=data_lengths$length);
        count <- count + 1;
    }
 
    # Read lengths
    imagewidth <- 1 + (nrow(data_samples) * 0.5);
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", type, "_lengths.pdf", sep="");
    message(output_file);
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Length, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]));
    garbage <- dev.off();

    # Bar stacked plot of mapping
    imagewidth <- 1 + (nrow(data_samples) * 0.5) + 1.5;
    filename_maps <- paste(outdir, "/", type,"_map_summary.txt", sep="");
    #filename_maps <- c("~/temp/2D_map_summary.txt");
    data_maps = read.table(filename_maps, header=TRUE);
    df <- melt(data_maps, id.var="Sample")
    output_file <- paste(outdir, "/graphs/", type, "_maps.pdf", sep="");
    message(output_file);
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x = Sample, y = value, fill = variable)) + geom_bar(stat = "identity") + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("%"));
    garbage <- dev.off();

    imagewidth <- 1 + (nrow(data_samples) * 0.5);

    # Number of reads
    filename_comparison <- paste(outdir, "/", type,"_comparison.txt", sep="");
    data_comparison = read.table(filename_comparison, header=TRUE);
    output_file <- paste(outdir, "/graphs/", type, "_number_of_reads.pdf", sep="");
    message(output_file);
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(data_comparison, aes(x=data_comparison$Name, y=data_comparison$NumReads)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Sample") + ylab("Number of reads") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) + theme(axis.text.x = element_text(angle = 45, hjust = 1)))
    garbage <- dev.off();

    # Total bases
    data_comparison = read.table(filename_comparison, header=TRUE);
    output_file <- paste(outdir, "/graphs/", type, "_total_bases.pdf", sep="");
    message(output_file);
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(data_comparison, aes(x=data_comparison$Name, y=data_comparison$TotalBases)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Sample") + ylab("Total bases") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) + theme(axis.text.x = element_text(angle = 45, hjust = 1)))
    garbage <- dev.off();

}
