library(ggplot2)
library(scales)

args <- commandArgs(TRUE)
basename <- args[1];
sample <-args[2];
reference <- args[3];

types = c("2D", "Template", "Complement");
colours = c("#68B5B9", "#CF746D", "#91A851");

for (t in 1:3) {
    type = types[t];
    colourcode = colours[t];
    cat(type, " ", colourcode, "\n");


}