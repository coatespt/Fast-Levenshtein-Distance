package com.levenshtein;

//import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * TODO: find out why the RollingHash signatures seem to be slightly larger than the plain has signatures. Could explain the performance difference.
 * TODO  Get material for a graph on actual compute time for files of increasing size 
 * TODO: fix the compression routines so that they always adjust to an odd number or to one of the good numbers;.
 * TODO: put all compression tests in one file.
 * 			* RH		Compare actual is close to nominal
 * 		  	* Regular	Compare actual is close to nominal
 * 		  	* Both		Count occurences of caracters in the output. They should be pretty close.
 * 		    * Comparison: Do them side by side and ensure that they compress roughly equally
 * 			* Comparison: Compare the LD results. They should be rougly equal
 *			* Comparison: How does speed compare for the same settings.
 *
 *
 *
 *
 * @author peter
 * fudge() isn't called anywhere except the TestMassCompare 
 * 
 */
	@RunWith(Suite.class)
	@Suite.SuiteClasses( 
			{ 
			TestAccuracyDriverRH_503_11.class,
			TestBasicOperations.class,
			TestRollingHash.class
			//  TestMassCompare.class,
			//  TestPlainWRangeOfCAndN.class,
			//	TestVaryCandN.class,
			//  TestRegularVRolling101.class,
			}
	)
	public class MasterTest {

		//Logger log = Logger.getLogger(MasterTest.class);
	}

