This is a git project that demonstrates a heuristic for fast estimates of 
Levenshtein Distance (LD) of large strings. Typical speedup w.r.t actually 
computing the LD might be 10,000-20,000 X in realistic cases. 

The chief limitation is it works only on fairly large strings--several KB and up.

You can read about it in http://hadoopoopadoop.com/2015/11/08/super-fast-estimates-of-levenshtein-distance/

Sample command line for invoking the CLI:
 -p ./config/config.properties -f ./data/testcompress.csv -c 251 -n 11 -ld true -ft ./data/targets.csv -x 0.08

-p is a java properties file. A sample is included.
-f  is an input list of files
-c and -n are the nominal compression rate and the neighborhood size.
-ld is false means do compression of the files in the input list. If it is true
    it means match each of the files to a target list that must be declared
    either in the properties or as a command line argument.
-ft is the list of target signatures.  Note that these are not filenames. It is
    CSV output from running compression with -ld false.
-x is a floating point number used to specify how significant you want an LD
    comparison to be before printing it out.

To build an executable jar file, execute the following from the
project directory. It will build a jar in ./target. If any tests fail
  the jar will not be built. If you set -Dmaven.test.skip=false it will not run
  the tests.
Depending upon which entry point you want to use you need to adjust the
entry point under "manifest" in the pom.xml file. Both Demo and Cli have entries
but one of them must be commented out.

 mvn assembly:assembly -DdescriptorId=jar-with-dependencies -Dmaven.test.skip=true

Sample command line to run Deno from the jar file.

The main() in the jar is Demo.java which reads the properties file in the config directory.
    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar

The demo assumes you have the test files in the right place.
You can adjust the choice of properties file by adding the one you want as a
n argument at then end. It defaults to ./config/config.properties.

To use the CLI from a jar file you must build it with the correct entry
point in the pom file's manifest section.  See above for how.

Sample command line to run the jar for the CLI:
    java -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar  -p ./config/config.properties -f ./data/search.csv -c 251 -n 11 -ld true -ft ./data/target.csv -x 0.1


To get the code

* Go to your usual working directory where you keep projects. e.g. /home/peter/workingdir
* Execute the following on the command line.
    git clone git@github.com:coatespt/Fast-Levenshtein-Distance.git

This will build /home/project-dir/workingdir/Fast-Levenshtein-Distance

Building and/or executing the demo assume you are in
<yourworkingdir>/Fast-Levenshtein-Distance because the config directory and
data files are relative to that directory, but you can set it up other
ways if you want.

Instructions for running are above.

To run the Demo with different configuration, e.g., other file sets, clone the
props file, modify to suit, and run like this:

java -Xmx4000M -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar somefile.properties

Alternatively, you can load the project into Eclipse and run it.
You'll want to set -Xmx=4000m  -Xms=2000m.  The heuristic uses relatively little memory
but the tests and demo execute the real LD on large files (27KB), which is
quite large for a quadratic algorthm. The heuristic uses a tiny fraction as much memory.

The Demo class gives a comprehensible result--try running that to see what it does.
Be aware that Demo does real LD computations on the original file input so that
it has something to judge the quality of the estimates.

Tests are undergoing a purge and cleanup as of Augutst 2021. The test set is filled
with junk that accumulated during development of the idea.

See https://github.com/coatespt/Fast-Levenshtein-Distance/tree/gh-pages for code
See http://hadoopoopadoop.com/2015/11/08/super-fast-estimates-of-levenshtein-distance/#more-535 for Wordpress website
 http://coatespt.github.io/Fast-Levenshtein-Distance

Note, all combinations of values do not work equally well, and some combinations work badly, particularly for certain C's. 
n=6 works well, and c=101, 199, and 307, rather than 100, 200, and 300.



