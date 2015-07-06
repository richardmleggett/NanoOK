library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
samplelist <- args[1];
outdir <- args[2];
reference <- args[3];
format <- args[4];

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

# Query identity
for (t in 1:3) {
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_data <- paste(sampledir, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
        data_field = read.table(filename_data, header=TRUE);
        thisid <- data_samples[i, "SampleName"];
        message(thisid);
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Variable=data_field$QueryPercentIdentity);
        count <- count + 1;
    }
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", reference, "_", type, "_query_identity.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Variable, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("Query identity %"));
    garbage <- dev.off();
}

# Best perfect kmer
for (t in 1:3) {
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_data <- paste(sampledir, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
        data_field = read.table(filename_data, header=TRUE);
        thisid <- data_samples[i, "SampleName"];
        message(thisid);
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Variable=data_field$LongestPerfectKmer);
        count <- count + 1;
    }
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", reference, "_", type, "_best_perfect_kmer.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Variable, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("Best perfect kmer"));
    garbage <- dev.off();
}

#PercentQueryAligned
for (t in 1:3) {
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_data <- paste(sampledir, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
        data_field = read.table(filename_data, header=TRUE);
        thisid <- data_samples[i, "SampleName"];
        message(thisid);
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Variable=data_field$PercentQueryAligned);
        count <- count + 1;
    }
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", reference, "_", type, "_percent_query_aligned.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Variable, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("% query aligned"));
    garbage <- dev.off();
}

#AlignmentSize
for (t in 1:3) {
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_data <- paste(sampledir, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
        data_field = read.table(filename_data, header=TRUE);
        thisid <- data_samples[i, "SampleName"];
        message(thisid);
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Variable=data_field$AlignmentSize);
        count <- count + 1;
    }
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", reference, "_", type, "_alignment_size.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Variable, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("Alignment size"));
    garbage <- dev.off();
}

#AlignmentPercentIdentity
for (t in 1:3) {
    df <- data.frame();
    listOfDataFrames <- NULL;
    count <- c(1);
    for (i in 1:nrow(data_samples)) {
        type = types[t];
        sampledir <- data_samples[i, "SampleDir"];
        filename_data <- paste(sampledir, "/analysis/", reference, "/", reference, "_",type,"_alignments.txt", sep="");
        data_field = read.table(filename_data, header=TRUE);
        thisid <- data_samples[i, "SampleName"];
        message(thisid);
        listOfDataFrames[[count]] <- data.frame(Sample=thisid, Variable=data_field$AlignmentPercentIdentity);
        count <- count + 1;
    }
    
    df <- do.call("rbind", listOfDataFrames);
    output_file <- paste(outdir, "/graphs/", reference, "_", type, "_alignment_identity.pdf", sep="");
    message(output_file);
    imagewidth <- nrow(data_samples) * 1;
    pdf(output_file, width=imagewidth, height = 4);
    print(ggplot(df, aes(x=Sample, y=Variable, fill=Sample)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1)) + guides(fill=FALSE) + theme(text = element_text(size=textsize)) + ggtitle(types[t]) + ylab("Alignment identity %"));
    garbage <- dev.off();
}