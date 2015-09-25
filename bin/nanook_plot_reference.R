library(ggplot2)
library(scales)
library(grid)
library(gridExtra)

# Filenames
args <- commandArgs(TRUE)
sampledir <- args[1];
refid <- args[2];
format <- args[3];
maxk <- 0;

roundUp <- function(x,to=10)
{
    to*(x%/%to + as.logical(x%%to))
}

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
    textsize <- c(14)
    pointsize <- c(2)
    pointalpha <-c(0.4)
    pointshape <- c(1)
    pointwidth <- c(1)
    xvjust <- c(0.2)
    yvjust <- c(0.8)
}

# Plot GC% vs position
data_gc_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_gc.txt", sep="");
data_gc = read.table(data_gc_filename, col.name=c("Position", "Coverage"))
if (format=="png") {
    png_gc <- paste(sampledir, "/graphs/", refid, "/", refid, "_gc.png", sep="");
    png(png_gc, width=1600, height=400)
    print(ggplot(data_gc, aes(x=data_gc$Position, y=data_gc$Coverage)) + geom_line(color="black") + ggtitle("GC content") + theme(text = element_text(size=textsize)) + xlab("Position") + ylab("GC %") + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
} else {
    pdf_gc <- paste(sampledir, "/graphs/", refid, "/", refid, "_gc.pdf", sep="");
    pdf(pdf_gc, width=16, height=4)
    print(ggplot(data_gc, aes(x=data_gc$Position, y=data_gc$Coverage)) + geom_line(color="black") + ggtitle("GC content") + theme(text = element_text(size=textsize)) + xlab("Position") + ylab("GC %") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
}
garbage <- dev.off()

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    #cat(png, " ", colourcode, "\n");

    # Plot coverage vs position
    data_coverage_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_coverage.txt", sep="");
    data_coverage = read.table(data_coverage_filename, col.name=c("Position", "Coverage"))
    if (format=="png") {
        png_coverage <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_coverage.png", sep="");
        png(png_coverage, width=1600, height=400)
        print(ggplot(data_coverage, aes(x=data_coverage$Position, y=data_coverage$Coverage)) + geom_line(color=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Position") + ylab("Mean coverage") + expand_limits(y = 0) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        pdf_coverage <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_coverage.pdf", sep="");
        pdf(pdf_coverage, width=16, height=4)
        print(ggplot(data_coverage, aes(x=data_coverage$Position, y=data_coverage$Coverage)) + geom_line(color=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Position") + ylab("Mean coverage") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + expand_limits(y = 0) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Plot % reads with perfect kmer vs kmer size
    data_perfect_cumulative_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.txt", sep="");
    if (format=="png") {
        png_perfect_cumulative <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.png", sep="");
        png(png_perfect_cumulative, width=1200, height=800)
        data_perfect_cumulative = read.table(data_perfect_cumulative_filename, col.name=c("Size", "n", "Perfect"))
        print(ggplot(data_perfect_cumulative, aes(x=data_perfect_cumulative$Size, y=data_perfect_cumulative$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("kmer size") + ylab("% reads with perfect kmer") + scale_x_continuous(limits=c(0, 140)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        pdf_perfect_cumulative <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_cumulative_perfect_kmers.pdf", sep="");
        pdf(pdf_perfect_cumulative, width=6, height=4)
        data_perfect_cumulative = read.table(data_perfect_cumulative_filename, col.name=c("Size", "n", "Perfect"))
        print(ggplot(data_perfect_cumulative, aes(x=data_perfect_cumulative$Size, y=data_perfect_cumulative$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("kmer size") + ylab("% reads with perfect kmer") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 140)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Plot %reads vs best perfect kmer
    #data_perfect_best_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_best_perfect_kmers.txt", sep="");
    #if (format=="png") {
    #    png_perfect_best <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.png", sep="");
    #    png(png_perfect_best, width=1200, height=800)
    #    data_perfect_best = read.table(data_perfect_best_filename, col.name=c("Size", "n", "Perfect"))
    #    print(ggplot(data_perfect_best, aes(x=data_perfect_best$Size, y=data_perfect_best$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Best perfect kmer") + ylab("% reads") + scale_x_continuous(limits=c(0, 140)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    #} else {
    #    pdf_perfect_best <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.pdf", sep="");
    #    pdf(pdf_perfect_best, width=6, height=4)
    #    data_perfect_best = read.table(data_perfect_best_filename, col.name=c("Size", "n", "Perfect"))
    #    print(ggplot(data_perfect_best, aes(x=data_perfect_best$Size, y=data_perfect_best$Perfect)) + geom_bar(stat="identity", width=0.7, fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, 140)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    #}
    #garbage <- dev.off()

    # ========== Indels files ==========

    # Insertions
    data_insertions_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_insertions.txt", sep="");
    if (format=="png") {
        png_insertions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_insertions.png", sep="");
        png(png_insertions, width=1200, height=800)
        data_insertions = read.table(data_insertions_filename, col.name=c("Size", "Percent"))
        print(ggplot(data_insertions, aes(x=data_insertions$Size, y=data_insertions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Insertion size") + ylab("%") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        pdf_insertions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_insertions.pdf", sep="");
        pdf(pdf_insertions, width=6, height=4)
        data_insertions = read.table(data_insertions_filename, col.name=c("Size", "Percent"))
        print(ggplot(data_insertions, aes(x=data_insertions$Size, y=data_insertions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Insertion size") + ylab("%") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()
    
    # Deletions
    data_deletions_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_deletions.txt", sep="");
    if (format=="png") {
        png_deletions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_deletions.png", sep="");
        png(png_deletions, width=1200, height=800)
        data_deletions = read.table(data_deletions_filename, col.name=c("Size", "Percent"))
        print(ggplot(data_deletions, aes(x=data_deletions$Size, y=data_deletions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Deletion size") + ylab("%") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        pdf_deletions <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_deletions.pdf", sep="");
        pdf(pdf_deletions, width=6, height=4)
        data_deletions = read.table(data_deletions_filename, col.name=c("Size", "Percent"))
        print(ggplot(data_deletions, aes(x=data_deletions$Size, y=data_deletions$Percent)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Deletion size") + ylab("%") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # ========== Alignments file ==========

    input_filename <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_alignments.txt", sep="");
    data_alignments = read.table(input_filename, header=TRUE);

    # Length vs Identity histograms
    if (format=="png") {
        identity_hist_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_hist.png", sep="")
        png(identity_hist_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryPercentIdentity)) + geom_histogram(fill=colourcode) + xlab("Read identity %") +ylab("Count") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) )
    } else {
        identity_hist_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_hist.pdf", sep="")
        pdf(identity_hist_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryPercentIdentity)) + geom_histogram(fill=colourcode) + xlab("Read identity %") +ylab("Count") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()
    
    # GC histogram
    if (format=="png") {
        identity_hist_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_GC_hist.png", sep="")
        png(identity_hist_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryGC)) + geom_histogram(fill=colourcode, binwidth=1) + xlab("GC %") +ylab("Read count") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) + scale_x_continuous(limits=c(0, 100)) )
    } else {
        identity_hist_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_GC_hist.pdf", sep="")
        pdf(identity_hist_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryGC)) + geom_histogram(fill=colourcode, binwidth = 1) + xlab("GC %") +ylab("Read count") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) + scale_x_continuous(limits=c(0, 100)))
    }
    garbage <- dev.off()

    # Identity vs Length Scatter plots
    if (format=="png") {
        identity_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_scatter.png", sep="");
        png(identity_scatter_pdf, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Length") +ylab("Read identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        identity_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_length_vs_identity_scatter.pdf", sep="");
        pdf(identity_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Length") +ylab("Read identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Alignment identity vs. Fraction of read aligned scatter plots
    if (format=="png") {
        aid_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_alignment_identity_scatter.png", sep="");
        png(aid_scatter_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$AlignmentPercentIdentity)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) )
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        aid_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_alignment_identity_scatter.pdf", sep="");
        pdf(aid_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$AlignmentPercentIdentity)) + geom_point(shape=pointshape, alpha=0.4, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Query identity vs. Fraction of read aligned scatter plots
    if (format=="png") {
        qid_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_query_identity_scatter.png", sep="");
        png(qid_scatter_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        qid_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_read_fraction_vs_query_identity_scatter.pdf", sep="");
        pdf(qid_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$PercentQueryAligned, y=data_alignments$QueryPercentIdentity)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Percentage of read aligned") +ylab("Alignment identity %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 105)) + scale_y_continuous(limits=c(0, 100)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters
    if (format=="png") {
        best_perf_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_scatter.png", sep="");
        png(best_perf_scatter_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Longest perfect kmer") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        best_perf_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_scatter.pdf", sep="");
        pdf(best_perf_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Longest perfect kmer") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Best perfect sequence vs. length scatters zoomed
    if (format=="png") {
        best_perf_zoom_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_zoom_scatter.png", sep="");
        png(best_perf_zoom_scatter_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Longest perfect kmer") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 10000)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        best_perf_zoom_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_longest_perfect_vs_length_zoom_scatter.pdf", sep="");
        pdf(best_perf_zoom_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$LongestPerfectKmer)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Longest perfect kmer") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 10000)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Plot %reads vs best perfect kmer
    if (maxk == 0) {
        maxk <- max(data_alignments$LongestPerfectKmer);
        maxk <- roundUp(maxk, 10);
        message(maxk);
    }
    hdf <- hist(breaks=seq(0,maxk,by=10), x=data_alignments$LongestPerfectKmer, plot=FALSE, right=FALSE); # bins are 0-9, 10-19, 20-29 etc.
    hdf$density = hdf$counts/sum(hdf$counts)*100
    tdf <- data.frame(Pos=hdf$mids, Counts=hdf$density);

    if (format=="png") {
        png_perfect_best <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.png", sep="");
        png(png_perfect_best, width=1200, height=800)
        print(ggplot(tdf, aes(Pos, Counts)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, maxk), breaks=seq(0,maxk,by=20)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    } else {
        pdf_perfect_best <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_best_perfect_kmers.pdf", sep="");
        pdf(pdf_perfect_best, width=6, height=4)
        print(ggplot(tdf, aes(Pos, Counts)) + geom_bar(stat="identity", fill=colourcode) + ggtitle(type) + theme(text = element_text(size=textsize)) + xlab("Best perfect kmer") + ylab("% reads") + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.y=element_text(vjust=0.2)) + theme(axis.title.x=element_text(vjust=-0.2)) + scale_x_continuous(limits=c(0, maxk), breaks=seq(0,maxk,by=20)) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()


    # Number of perfect 21mers verses length scatter
    if (format=="png") {
        nk21_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_nk21_vs_length_scatter.png", sep="");
        png(nk21_scatter_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$nk21)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        nk21_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_nk21_vs_length_scatter.pdf", sep="");
        pdf(nk21_scatter_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$nk21)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Number of perfect 21mers") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()

    # Mean perfect sequence vs. length scatters
    #mean_perf_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_mean_perfect_vs_length_scatter.pdf", sep="");
    #pdf(mean_perf_scatter_pdf, height=4, width=6)
    #print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$MeanPerfectKmer), xlab="Read length") + geom_point(shape=pointshape, alpha=pointalpha) + xlab("Read length") +ylab("Mean perfect kmer") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 10000)))
    #garbage <- dev.off()

    # Percentage of read aligned vs read length
    if (format=="png") {
        output_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_percent_aligned_vs_length_scatter.png", sep="");
        png(output_png, width=1200, height=800)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$PercentQueryAligned)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Percentage of read aligned") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        output_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_percent_aligned_vs_length_scatter.pdf", sep="");
        pdf(output_pdf, width=6, height=4)
        print(ggplot(data_alignments, aes(x=data_alignments$QueryLength, y=data_alignments$PercentQueryAligned)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Read length") +ylab("Percentage of read aligned") + ggtitle(type) + theme(text = element_text(size=textsize)) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)))
    }
    garbage <- dev.off()
    
    # ========== Kmer file ==========
    
    # Kmer abundance with labels
    input_kmers <- paste(sampledir, "/analysis/", refid, "/", refid, "_",type,"_kmers.txt", sep="");
    data_kmers = read.table(input_kmers, header=TRUE);
    if (format=="png") {
        kmer_scatter_png <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_kmer_scatter.png", sep="");
        png(kmer_scatter_png, width=1200, height=1200)
        print(ggplot(data_kmers, aes(x=data_kmers$RefPc, y=data_kmers$ReadPc)) + geom_point(shape=pointshape, size=pointsize, alpha=pointalpha, color=colourcode) + xlab("Reference abundance %") +ylab("Reads abundance %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 0.3)) + scale_y_continuous(limits=c(0, 0.3)) + geom_text(aes(label=data_kmers$Kmer), size=4) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) )
        grid.edit("geom_point.points", grep = TRUE, gp = gpar(lwd = pointwidth))
    } else {
        kmer_scatter_pdf <- paste(sampledir, "/graphs/", refid, "/", refid, "_",type,"_kmer_scatter.pdf", sep="");
        pdf(kmer_scatter_pdf, width=6, height=6)
        print(ggplot(data_kmers, aes(x=data_kmers$RefPc, y=data_kmers$ReadPc)) + geom_point(shape=pointshape, alpha=pointalpha, color=colourcode) + xlab("Reference abundance %") +ylab("Reads abundance %") + ggtitle(type) + theme(text = element_text(size=textsize)) + scale_x_continuous(limits=c(0, 0.3)) + scale_y_continuous(limits=c(0, 0.3)) + geom_text(aes(label=data_kmers$Kmer), size=1) + theme(plot.margin = unit(c(0.02,0.02,0.04,0.02), "npc")) + theme(axis.title.x=element_text(vjust=-xvjust)) + theme(axis.title.y=element_text(vjust=yvjust)) )
    }
    garbage <- dev.off()
}