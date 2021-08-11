This is a git project that demonstrates a heuristic for fast estimates of 
Levenshtein Distance (LD) of large strings. Typical speedup w.r.t actually 
computing the LD might be 10,000-20,000 X in realistic cases. 

The chief limitation is it works only on fairly large strings--several KB and up.

You can read about it in http://hadoopoopadoop.com/2015/11/08/super-fast-estimates-of-levenshtein-distance/

This is an Eclipse java project that can be built with maven on the command line as well.

The demo assumes you have pulled down the git repo with some test files. If you want an up to date copy, 

Go to your usual working directory where you keep projects. e.g. /home/peter/workingdir

Execute the following on the command line.

git clone git@github.com:coatespt/Fast-Levenshtein-Distance.git

This will build /home/peter/workingdir/Fast-Levenshtein-Distance

Building and/or executing the demo assume you are in <yourworkingdir>/Fast-Levenshtein-Distance because the config directory and
data files are relative to that directory, but you can set it up otherways if you want.

To build an executable jar go to Fast-Levenshtein-Distance and execute:
mvn -Dmaven.test.skip=true assembly:assembly

Running like this will execute the tests, but they run a long time:
mvn -Dmaven.test.skip=false assembly:assembly

To run the jar go to Fast-Levenshtein-Distance and execute:
java -Xmx4000M -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar

To run with different configuration, e.g., other file sets, clone the props file and run like this:
java -Xmx4000M -jar target/Fast-Levenshtein-Distance-0.0.1-SNAPSHOT-jar-with-dependencies.jar somefile.properties

Or just load the project into Eclipse and run it.  You'll want to set -Xmx=4000m  -Xms=2000m.  The heuristic uses relatively little memory
but the tests and demo execute the real LD on large files (27KB), which is quite large for a quadratic algorthm. The heuristic uses 
a tiny fraction as much memory.

Beware that this is just a demo---nothing here is suitable for production.  But if you feel like working on that, please let me know first 
so we can coordinate in advance to avoid pain.

The Demo class gives a comprehensible result--try running that to see what it does.  Other test 
classes may or many not give useful results pending a cleanup.

See https://github.com/coatespt/Fast-Levenshtein-Distance/tree/gh-pages for code
See http://hadoopoopadoop.com/2015/11/08/super-fast-estimates-of-levenshtein-distance/#more-535 for Wordpress website
 http://coatespt.github.io/Fast-Levenshtein-Distance

Note, all combinations of values do not work equally well, and some combinations work badly, particularly for certain C's. 
n=6 works well, and c=101, 199, and 307, rather than 100, 200, and 300.



