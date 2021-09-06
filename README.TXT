This is a git project that demonstrates a heuristic for making fast estimates of
Levenshtein Distance (LD) of large strings. You compress the strings to be compared
to a high degree, e.g., 250-1500x to produce thumbnail versions and extrapolate
the LD of the full size strings from the LD of the thumbnails.

Because LD is quadratic, shrinking results in a speedup that is inversely quadratic
to the compression rate. Or the other way around, if LD is practical for strings
of up to size X, you can estimate LD on strings uf up to X time sthe compression
rate.

The chief limitation is it works only on fairly large strings--several KB and up.

Sample command line for invoking the CLI:
 -p ./config/config.properties -f ./data/testcompress.csv -c 251 -n 11 -ld true -ft ./data/targets.csv -x 0.08

Arguments

-p is a java properties file. A sample is included.
-f  is an input list of files
-c and -n are the nominal compression rate and the neighborhood size.
-ld is false it means do compression of the files named in the input list. If it is true
    it means match each of the files in the intput list to the signatures in a target list
    that must be declared either in the properties or as a command line argument.
-ft is the list of target signatures.  Note that these are not filenames. Contents of this
    file is the CSV output from running compression with -ld false.
-x is a floating point number used to specify how significant you want an LD
    comparison to be before printing it out.

It is useful to direct the output of compression into a CSV file named target_<comp-rate>_<n>.csv

Building

To build an executable jar file, execute the following from the project directory.
It will build a jar in ./target. By default Maven runs your JUnit tests. If any tests fail
the jar will not be built. To avoid this, you can set the command line option:

    -Dmaven.test.skip=false

Depending upon which entry point you want to use you need to adjust the entry point under
"manifest" in the pom.xml file. Both Demo and Cli have entries but one of them must
be commented out.

If you don't give maven Plenty of memory or use -Dmaven.test.skip=false you may fail to
build with OOM errors. The memory settings for Maven can be done with an environment
variable as follows. You can put this in your .profile or just run it by hand.

    export MAVEN_OPTS="-Xmx4g -XX:MaxPermSize=500m"

Maven is touchy so you may find it easier to do the tests in your IDE and skip them in
the maven jar file generation as follows.

    mvn assembly:assembly -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true

Another Maven gotcha is that the build will fail if you have any file that starts with
"Test" that does not have a test declared inside. There's probably some obscure setting
to solve this, but it's easier to just put a do-nothing test in any such file.

Sample command line to run Demo from the jar file.

The main() in the jar is Demo.java which reads the properties file in the config directory.
    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar

The demo assumes you have the test files in the right place.
You can adjust the choice of properties file by adding the one you want as a
n argument at then end. It defaults to ./config/config.properties.

To use the CLI from a jar file you must build it with the correct entry
point in the pom file's manifest section.  See above for how.

Sample command line to run the jar for the CLI:

    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ./config/config.properties -f ./data/search.csv -c 251 -n 11 -ld true -ft ./data/target.csv -x 0.1

Example running jar to make signatures from a list of files. Compression 1009 and neighborhood 11. Note
that the output filename encodes those parameters.

    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar  -p ./config/config.properties -f ./data/allfiles.txt -c 1009 -n 11 -ld false > targets_1009_11.csv

We search for our ten files among the results of the compression stesp

    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar -p ./config/config.properties -f ./data/search.csv -c 1009 -n 11 -ld true -ft targets_1009_11.csv -x 0.4

The options can all be put in a properties file (by default config.properties but you can change that
on the command line.) Any properties you put on the command line will override the ones in the config file.

        -f <a file> input data would be a list of files names
        -c <an integer> 	compression rate.
        -n <an integer> 	neighborhhod size.;
        -ch <char-string>	output-char-set in the form of a string.
        -ld true/false estimate mode.
        -f <input-file-spec> is a csv file of the form output by the compression step.
        -i <input-file-spec> is a csv file of one or more rows you are tyring to match.
        -t is how different from the expected value the estimated LD has to be.
        -v true/false suppress the informational ouput written to stderr.
        -h true/false print the output file header.
        -ol 1 to 3, Higher number gives fewer output fields.

To get the code:

* Go to your usual working directory where you keep projects. e.g. /home/peter/workingdir
* Execute the following on the command line.
    git clone git@github.com:coatespt/Fast-Levenshtein-Distance.git

This will build /home/project-dir/workingdir/Fast-Levenshtein-Distance

Building and/or executing the demo assume you are in <yourworkingdir>/Fast-Levenshtein-Distance
because the config directory and data files are relative to that directory, but you can set it
up other ways if you want.

To run the Demo with different configuration, e.g., other file sets, clone the
props file, modify to suit, and run like this:

java -Xmx4000M -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar somefile.properties

Alternatively, you can load the project into Eclipse or some other IDE and run it there.

You'll want to set -Xmx=4000m  -Xms=2000m.

The heuristic uses relatively little memory but the tests and demo execute the real LD on large
files (27KB), which is quite large for a quadratic algorthm. The heuristic uses a tiny fraction as
much memory.

The Demo class gives a comprehensible result--try running that to see what it does. Be aware that Demo
does real LD computations on the original file input so that it has something to judge the quality
of the estimates.

See https://github.com/coatespt/Fast-Levenshtein-Distance/tree/gh-pages for code See
http://hadoopoopadoop.com/2015/11/08/super-fast-estimates-of-levenshtein-distance/#more-535
for Wordpress website or http://coatespt.github.io/Fast-Levenshtein-Distance on github.

Note, not all N and C work equally well. Smaller n give less pseudo-randomized signatures, hence
worse performance, but larger n will tend to wash out fine grained differences. It's application
dependent. Increasing C gives MUCH faster runtime but coarser results. for a few 10's of K you can use
C in the low hundreds or even less than 100, but for hundreds of K to megabytes you will probably
want 1000<c<2000.

Primes seem to give better results for both N and C. Neighborhood n=11 or 17 work well.



