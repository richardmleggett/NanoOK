library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

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
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", type, "_lengths.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Length, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]));
    garbage <- dev.off();
}
