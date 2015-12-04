package com.levenshtein;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * TODO: find out why the RollingHash signatures seem to be slightly larger than the plain has signatures. Could explain the performance difference.
 * TODO  Get material for a graph on actual compute time for files of increasing size 
 * TODO: fix the compression routines so that they always adjust to an odd number or to one of the good numbers;.
 *  
 * @author peter
 * fudge() isn't called anywhere except the TestMassCompare 
 * 
 */
	@RunWith(Suite.class)
	@Suite.SuiteClasses( 
			{ 
			TestAccuracyDriverPlain101.class,
			TestAccuracyDriverRH101.class,
			TestBasicOperations.class,
			TestCompareAccuracy.class,
			//TestMassCompare.class,
			TestRollingHash.class,
			TestPlainWRangeOfCAndN.class,
			TestRegularHash.class,
			TestRollingHash.class,
			TestSpeedForBigFiles.class,
			TestRegularVRolling101.class,
			}
	)
	public class MasterTest {

		Logger log = Logger.getLogger(MasterTest.class);
	}

