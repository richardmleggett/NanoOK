#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <getopt.h>
#include <string.h>

#define VERSIONSTRING "kmer compare v0.1"

unsigned int kmer_size = 0;
unsigned int possible_kmers;
unsigned int* read_counts = 0;
unsigned int* ref_counts = 0;
char* reference_filename = 0;
char* read_filename = 0;
char* reference_id = 0;

void read_and_store_kmers(char* filename, unsigned int* counts)
{
    FILE* fp = fopen(filename, "r");
    char line[1024];
    char id[1024];
    int reading = 0;
    
    if (!fp) {
        printf("Error: can't open %s\n", filename);
        exit(1);
    }
    
    while (!feof(fp)) {
        if (fgets(line, 1024, fp)) {
            if (line[0] == '>') {
                int i = 1;
                while ((i < strlen(line)) && (line[i] > ' ')) {
                    id[i-1] = line[i];
                    i++;
                }
                id[i-1] = 0;
                printf("Got ID %s\n", id);
            }
        }
    }
    
    fclose(fp);
}

void usage(void) {
    printf("Syntax: kmercompare <-r reference> <-a reads> <-k kmer> [-i id]\n");
}

void parse_command_line(int argc, char* argv[])
{
    static struct option long_options[] = {
        {"reads", required_argument, NULL, 'a'},
        {"help", no_argument, NULL, 'h'},
        {"reference_id", required_argument, NULL, 'i'},
        {"kmer_size", required_argument, NULL, 'k'},
        {"reference", required_argument, NULL, 'r'},
        {0, 0, 0, 0}
    };
    int opt;
    int longopt_index;
    
    if (argc == 1) {
        usage();
        exit(0);
    }
    
    while ((opt = getopt_long(argc, argv, "a:hi:k:r:", long_options, &longopt_index)) > 0)
    {
        switch(opt) {
            case 'a':
                if (optarg==NULL) {
                    printf("Error: [-a | --reads] option requires an argument.\n");
                    exit(1);
                }
                read_filename = malloc(strlen(optarg) + 1);
                if (read_filename) {
                    strcpy(read_filename, optarg);
                } else {
                    printf("Error: can't allocate memory for string.\n");
                    exit(1);
                }
                break;
            case 'h':
                usage();
                exit(0);
                break;
            case 'i':
                if (optarg==NULL) {
                    printf("Error: [-i | --reference_id] option requires an argument.\n");
                    exit(1);
                }
                reference_id = malloc(strlen(optarg) + 1);
                if (reference_id) {
                    strcpy(reference_id, optarg);
                } else {
                    printf("Error: can't allocate memory for string.\n");
                    exit(1);
                }
                break;
            case 'k':
                if (optarg==NULL) {
                    printf("Error: [-k | --kmer_size] option requires an argument.\n");
                    exit(1);
                }
                kmer_size = atoi(optarg);
                break;
            case 'r':
                if (optarg==NULL) {
                    printf("Error: [-r | --reference] option requires an argument.\n");
                    exit(1);
                }
                reference_filename = malloc(strlen(optarg) + 1);
                if (reference_filename) {
                    strcpy(reference_filename, optarg);
                } else {
                    printf("Error: can't allocate memory for string.\n");
                    exit(1);
                }
                break;
            default:
                printf("Error: Unknown option %c\n", opt);
                exit(1);
                break;
        }
    }
    
    if (!reference_filename) {
        printf("Error: you must specify a reference filename\n");
        exit(1);
    }
    
    if (!read_filename) {
        printf("Error: you must specify a read filename\n");
        exit(1);
    }
    
    if ((kmer_size < 3) || (kmer_size > 11)) {
        printf("Error: kmer size out of range\n");
        exit(1);
    }
}


int main(int argc, char* argv[])
{
    parse_command_line(argc, argv);
    
    possible_kmers = (int)pow(4, kmer_size);

    printf("\n%s\n\n", VERSIONSTRING);
    printf("     kmer_size: %d\n", kmer_size);
    printf("Possible kmers: %d\n", possible_kmers);
    printf("     Reference: %s\n", reference_filename);
    printf("         Reads: %s\n", read_filename);
    
    read_counts = calloc(possible_kmers, sizeof(int));
    ref_counts = calloc(possible_kmers, sizeof(int));
    if ((!read_counts) || (!ref_counts)) {
        printf("Error: can't get memory to store counts\n");
        exit(1);
    }
    
    read_and_store_kmers(reference_filename, ref_counts);
    read_and_store_kmers(read_filename, read_counts);
    
    return 0;
}