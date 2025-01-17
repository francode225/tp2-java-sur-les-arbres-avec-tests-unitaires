package ci.miage.prg.list.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import ci.miage.prg.list.MySet;
import ci.miage.prg.list.SubSet;
import ci.miage.prg.list_util.Iterator;
import ci.miage.prg.list_util.OperationCounter;
import ci.miage.prg.list_util.SmallSet;

import static org.junit.Assert.assertTrue;

/**
 * @author Mickaël Foursov <foursov@univ-rennes1.fr>
 * @author Vincent Drevelle
 * @version 4.0
 * @since 2023-10-08
 * 
 *        Classe contenant les tests unitaires pour la classe MySet.
 */

public class TestMySet {
	@Rule
	public Timeout globalTimeout = Timeout.millis(1000);

	public static final String ENS0 = "f0.ens";
	public static final String ENS1 = "f1.ens";
	public static final String ENS3 = "f3.ens";
	public static final String TEST_U01 = "test-u01.ens";
	public static final String TEST_D01 = "test-d01.ens";
	public static final String TEST_S01 = "test-s01.ens";
	public static final String TEST_D03 = "test-d03.ens";
	public static final String TEST_I03 = "test-i03.ens";
	public static final String TEST_U03 = "test-u03.ens";
	public static final String BAD_COMPLEXITY = "bad complexity";
	public static final int OP_COUNT_ADD = 1;

	/**
	 * @param l1 premier ensemble
	 * @param l2 deuxième ensemble
	 * @return true si les ensembles l1 et l2 sont égaux, false sinon
	 */
	public static boolean compareMySets(MySet l1, MySet l2) {
		Iterator<SubSet> it1 = l1.iterator();
		Iterator<SubSet> it2 = l2.iterator();
		boolean bool = true;
		while (!it1.isOnFlag() && bool) {
			SubSet s1 = it1.getValue();
			SubSet s2 = it2.getValue();
			if (!compareSubSets(s1, s2)) {
				bool = false;
			}
			it1.goForward();
			it2.goForward();
		}
		return bool && it1.isOnFlag() && it2.isOnFlag();
	}

	public static boolean compareSubSets(SubSet s1, SubSet s2) {
		return s1.rank == s2.rank && compareSmallSets(s1.set, s2.set);
	}

	public static boolean compareSmallSets(SmallSet s1, SmallSet s2) {
		return !(s1.size() == 0 || s2.size() == 0) && s1.toString().equals(s2.toString());
	}

	/**
	 * Get the number of operations performed by object obj if it is an
	 * OperationCounter
	 * 
	 * @param obj The object to monitor
	 * @return The number of operations since resetOperationCount, 0 if not an
	 *         OperationCounter
	 */
	private static int getOperationsCount(Object obj) {
		if (!(obj instanceof OperationCounter)) {
			return 0;
		}
		OperationCounter counter = (OperationCounter) obj;
		return counter.getCount();
	}

	/**
	 * Reset the number of operations performed by object obj if it is an
	 * OperationCounter
	 * 
	 * @param obj The object to reset, do nothing if not an OperationCounter
	 */
	private static void resetOperationsCount(Object obj) {
		if (!(obj instanceof OperationCounter)) {
			return;
		}
		OperationCounter counter = (OperationCounter) obj;
		counter.startCount();
	}

	/**
	 * Get the sum of operations performed by all objects that implements
	 * OperationCounter
	 * 
	 * @param objects The objects to monitor
	 * @return The sum of the number of operations permormed by all OperationCounter
	 *         in objects
	 */
	private static int getAllOperationsCount(Object... objects) {
		int count = 0;
		for (Object o : objects) {
			count += getOperationsCount(o);
		}
		return count;
	}

	/**
	 * Reset the number of operations performed by all objects obj if they are
	 * OperationCounter
	 * 
	 * @param objects The objects to reset
	 */
	private static void resetAllOperationsCount(Object... objects) {
		for (Object o : objects) {
			resetOperationsCount(o);
		}
	}

	/**
	 * @param mySet ensemble à tester
	 * @return true si mySet est bien un ensemble creux
	 */
	public static boolean testSparsity(MySet mySet) {
		Iterator<SubSet> it = mySet.iterator();
		while (!it.isOnFlag() && it.getValue().set.size() != 0) {
			it.goForward();
		}
		return it.isOnFlag();
	}

	public static MySet readFileToMySet(String fileName) {
		MySet set = new MySet();
		try {
			InputStream is = null;
			is = new FileInputStream(fileName);
			set.addAllFromStream(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return set;
	}

	/**
	 * Reload set from referenceFile and ensure it is still equal.
	 * 
	 * @param test          The name of the test
	 * @param set           The set to check
	 * @param referenceFile The reference file containing set elements
	 */
	private static void assertConstness(String test, MySet set, String referenceFile) {
		assertTrue(test + " is const", compareMySets(set, readFileToMySet(referenceFile)));
	}

	@Test
	public void testSetCreation() {
		MySet mySet1 = readFileToMySet("test-desordre.ens");
		MySet mySet2 = readFileToMySet(ENS0);
		assertTrue("set creation in disorder", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testContainment1() {
		MySet mySet = readFileToMySet(ENS0);
		boolean bool1 = mySet.containsValue(128);
		boolean bool2 = mySet.containsValue(129);
		boolean bool3 = mySet.containsValue(32767);
		boolean bool4 = mySet.containsValue(22222);
		assertTrue("appartenance 1", bool1 && !bool2 && bool3 && !bool4);
		assertConstness("appartenance 1", mySet, ENS0);
	}

	@Test
	public void testContainment2() {
		MySet mySet = readFileToMySet(ENS0);
		boolean bool = mySet.containsValue(32511);
		assertTrue("appartenance 2", !bool);
		assertConstness("appartenance 2", mySet, ENS0);
	}

	@Test
	public void testSetAddition() throws FileNotFoundException {
		MySet mySet1 = readFileToMySet(ENS0);
		InputStream is = new FileInputStream(ENS1);
		MySet mySet2 = readFileToMySet(TEST_U01);
		mySet1.addAllFromStream(is);
		assertTrue("set addition f0 f1", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testRemoval1() {
		MySet mySet1 = readFileToMySet(ENS0);
		mySet1.removeNumber(64);
		mySet1.removeNumber(32767);
		MySet mySet2 = readFileToMySet("test-d05.ens");
		assertTrue("deletion sparsity 1", testSparsity(mySet1));
		assertTrue("deletion 1", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testRemoval2() {
		MySet mySet1 = new MySet();
		mySet1.addNumber(0);
		mySet1.addNumber(512);
		MySet mySet2 = new MySet();
		mySet2.addNumber(0);
		mySet2.addNumber(512);

		mySet1.removeNumber(256);
		assertTrue("deletion sparsity 2", testSparsity(mySet1));
		assertTrue("deletion 2", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testRemoval3() {
		MySet mySet1 = new MySet();
		mySet1.addNumber(64);
		mySet1.removeNumber(64);
		assertTrue("deletion sparsity 3", testSparsity(mySet1));
		assertTrue("deletion 3", mySet1.isEmpty());
	}

	@Test
	public void testRemoval4() {
		MySet mySet1 = new MySet();
		mySet1.addNumber(64);
		mySet1.addNumber(3333);
		mySet1.removeNumber(64);
		mySet1.removeNumber(3333);
		assertTrue("deletion sparsity 4", testSparsity(mySet1));
		assertTrue("deletion 4", mySet1.isEmpty());
	}

	@Test
	public void testRemoval5() throws FileNotFoundException {
		MySet mySet1 = readFileToMySet(ENS0);
		mySet1.removeAllFromStream(new FileInputStream(new File(ENS1)));
		MySet mySet2 = readFileToMySet(TEST_D01);
		assertTrue("deletion sparsity 5", testSparsity(mySet1));
		assertTrue("deletion 5", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testRemoval6() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet1.removeNumber(4744);
		assertTrue("deletion sparsity 6", testSparsity(mySet1));
		assertTrue("deletion 6", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testRemoval7() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet1.removeNumber(1030 - 256);
		assertTrue("deletion sparsity 7", testSparsity(mySet1));
		assertTrue("deletion 7", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testSize1() {
		MySet mySet = readFileToMySet(ENS0);
		int size = mySet.size();
		assertTrue("size", size == 14);
		assertConstness("size", mySet, ENS0);
	}

	@Test
	public void testSize2() {
		MySet mySet = new MySet();
		mySet.iterator().getValue().set.add(22);
		int size = mySet.size();
		assertTrue("size", size == 0);
	}

	@Test
	public void testSize3() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		mySet1.union(mySet2);
		int size = mySet1.size();
		assertTrue("size", size == 23);
	}

	@Test
	public void testDifference1() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		MySet mySet3 = readFileToMySet(TEST_D03);
		resetAllOperationsCount(mySet1, mySet2);
		mySet1.difference(mySet2);
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertTrue("difference f0 and f3", compareMySets(mySet1, mySet3));
		assertConstness("difference f0 and f3", mySet2, ENS3);
		assertTrue(BAD_COMPLEXITY, complexity <= 10 + OP_COUNT_ADD);
	}

	@Test
	public void testDifference2() {
		MySet mySet1 = readFileToMySet(ENS3);
		MySet mySet2 = readFileToMySet(ENS0);
		MySet mySet3 = readFileToMySet("test-d30.ens");
		mySet1.difference(mySet2);
		assertTrue("difference f3 and f0", compareMySets(mySet1, mySet3));
		assertConstness("difference f3 and f0", mySet2, ENS0);
	}

	@Test
	public void testDifference3() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS1);
		MySet mySet3 = readFileToMySet(TEST_D01);
		mySet1.difference(mySet2);
		assertTrue("difference f0 and f1", compareMySets(mySet1, mySet3));
		assertConstness("difference f0 and f1", mySet2, ENS1);
	}

	@Test
	public void testDifference4() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(100);

		mySet3.addNumber(300);

		mySet1.difference(mySet2);
		assertTrue("difference 100+300 and 100", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testDifference5() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(301);
		mySet2.addNumber(100);

		mySet1.difference(mySet2);
		assertTrue("difference 100 and 100+301", mySet1.isEmpty());
	}

	@Test
	public void testDifference6() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet1.difference(mySet2);
		assertTrue("difference f0 and f0 : version 1", mySet1.isEmpty());
		assertConstness("difference f0 and f0 : version 1", mySet2, ENS0);
	}

	@Test
	public void testDifference7() {
		MySet mySet1 = readFileToMySet(ENS0);
		mySet1.difference(mySet1);
		assertTrue("difference f0 and f0 : version 2", mySet1.isEmpty());
	}

	@Test
	public void testIntersection1() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		MySet mySet3 = readFileToMySet(TEST_I03);
		resetAllOperationsCount(mySet1, mySet2);
		mySet1.intersection(mySet2);
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertTrue("intersection f0 and f3", compareMySets(mySet1, mySet3));
		assertConstness("intersection f0 and f3", mySet2, ENS3);
		assertTrue(BAD_COMPLEXITY, complexity <= 11 + OP_COUNT_ADD);
	}

	@Test
	public void testIntersection2() {
		MySet mySet1 = readFileToMySet(ENS3);
		MySet mySet2 = readFileToMySet(ENS0);
		MySet mySet3 = readFileToMySet(TEST_I03);
		mySet1.intersection(mySet2);
		assertTrue("intersection f3 and f0", compareMySets(mySet1, mySet3));
		assertConstness("intersection f3 and f0", mySet2, ENS0);
	}

	@Test
	public void testIntersection3() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(100);
		mySet2.addNumber(301);

		mySet3.addNumber(100);

		mySet1.intersection(mySet2);
		assertTrue("intersection 100+300 and 100+301", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testIntersection4() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(100);

		mySet1.intersection(mySet2);
		assertTrue("intersection 100+300 and 100", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testIntersection5() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(301);

		mySet3.addNumber(100);

		mySet1.intersection(mySet2);
		assertTrue("intersection 100 and 100+301", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testIntersection6() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(999);

		mySet2.addNumber(200);
		mySet2.addNumber(301);

		mySet1.intersection(mySet2);
		assertTrue("intersection 100+999 and 200+301", mySet1.isEmpty());
	}

	@Test
	public void testIntersection7() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(999);

		mySet2.addNumber(100);
		mySet2.addNumber(301);

		mySet3.addNumber(100);
		mySet3.addNumber(301);

		mySet1.intersection(mySet2);
		assertTrue("intersection 100+999 and 100+301", compareMySets(mySet2, mySet3));
	}

	@Test
	public void testUnion1() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		MySet mySet3 = readFileToMySet(TEST_U03);
		resetAllOperationsCount(mySet1, mySet2);
		mySet1.union(mySet2);
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertTrue("union f0 and f3", compareMySets(mySet1, mySet3));
		assertConstness("union f0 and f3", mySet2, ENS3);
		assertTrue(BAD_COMPLEXITY, complexity <= 16 + OP_COUNT_ADD);
		}

	@Test
	public void testUnion2() {
		MySet mySet1 = readFileToMySet(ENS3);
		MySet mySet2 = readFileToMySet(ENS0);
		MySet mySet3 = readFileToMySet(TEST_U03);
		mySet1.union(mySet2);
		assertTrue("union f3 and f0", compareMySets(mySet1, mySet3));
		assertConstness("union f3 and f0", mySet2, ENS0);
	}

	@Test
	public void testUnion3() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(100);

		mySet3.addNumber(100);
		mySet3.addNumber(300);

		mySet1.union(mySet2);
		assertTrue("union 100+300 and 100", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testUnion4() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(301);

		mySet1.union(mySet2);
		assertTrue("union 100 and 100+301", compareMySets(mySet1, mySet2));
	}

	@Test
	public void testUnion5() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		MySet mySet3 = readFileToMySet(TEST_U03);
		mySet1.union(mySet2);
		mySet2.addNumber(8201);
		assertTrue("union f0 and f3 (bis)", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testUnionVD1() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(101);
		mySet2.addNumber(400);
		mySet2.addNumber(800);

		mySet3.addNumber(100);
		mySet3.addNumber(101);
		mySet3.addNumber(400);
		mySet3.addNumber(800);

		mySet1.union(mySet2);
		assertTrue("union 100 and 100+101+400+800", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testSymmetricDifference1() {
		MySet mySet1 = readFileToMySet(ENS1);
		MySet mySet2 = readFileToMySet(ENS0);
		MySet mySet3 = readFileToMySet(TEST_S01);
		resetAllOperationsCount(mySet1, mySet2);
		mySet1.symmetricDifference(mySet2);
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertTrue("symmetric difference f1 and f0", compareMySets(mySet1, mySet3));
		assertConstness("symmetric difference f1 and f0", mySet2, ENS0);
		assertTrue(BAD_COMPLEXITY, complexity <= 15 + OP_COUNT_ADD);
	}

	@Test
	public void testSymmetricDifference2() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS1);
		MySet mySet3 = readFileToMySet(TEST_S01);
		mySet1.symmetricDifference(mySet2);
		assertTrue("symmetric difference f0 and f1", compareMySets(mySet1, mySet3));
		assertConstness("symmetric difference f0 and f1", mySet2, ENS1);
	}

	@Test
	public void testSymmetricDifference3() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(100);

		mySet3.addNumber(300);

		mySet1.symmetricDifference(mySet2);
		assertTrue("symmetric difference 100+300 and 100", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testSymmetricDifference4() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(301);

		mySet3.addNumber(301);

		mySet1.symmetricDifference(mySet2);
		assertTrue("symmetric difference 100 and 100+301", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testSymmetricDifference5() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet1.symmetricDifference(mySet2);
		assertTrue("symmetric difference f0 and f0 :version 1", mySet1.isEmpty());
		assertConstness("symmetric difference f0 and f0 :version 1", mySet2, ENS0);
	}

	@Test
	public void testSymmetricDifference6() {
		MySet mySet1 = readFileToMySet(ENS0);
		mySet1.symmetricDifference(mySet1);
		assertTrue("symmetric difference f0 and f0 : version 2", mySet1.isEmpty());
	}

	@Test
	public void testSymmetricDifference7() {
		MySet mySet1 = readFileToMySet(ENS1);
		MySet mySet2 = readFileToMySet(ENS0);
		MySet mySet3 = readFileToMySet(TEST_S01);
		mySet1.symmetricDifference(mySet2);
		mySet2.addNumber(5001);
		assertTrue("symmetric difference f1 and f0 (bis)", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testSymmetricDifference8() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();
		mySet1.addNumber(100);
		mySet1.addNumber(300);

		mySet2.addNumber(150);
		mySet2.addNumber(800);

		mySet3.addNumber(100);
		mySet3.addNumber(150);
		mySet3.addNumber(300);
		mySet3.addNumber(800);

		mySet1.symmetricDifference(mySet2);

		assertTrue("symmetric difference 100+300 and 150+800", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testSymmetricDifferenceVD1() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		MySet mySet3 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(101);
		mySet2.addNumber(400);
		mySet2.addNumber(800);

		mySet3.addNumber(101);
		mySet3.addNumber(400);
		mySet3.addNumber(800);

		mySet1.symmetricDifference(mySet2);
		assertTrue("symmetric difference 100 and 100+101+400+800", compareMySets(mySet1, mySet3));
	}

	@Test
	public void testEquality1() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		resetAllOperationsCount(mySet1, mySet2);
		assertTrue("equality f0 == f0", mySet1.equals(mySet2));
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertConstness("equals (this)", mySet1, ENS0);
		assertConstness("equals (other)", mySet2, ENS0);
		assertTrue(BAD_COMPLEXITY, complexity <= 12 + OP_COUNT_ADD);
		}

	@Test
	public void testEquality2() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(8888);
		assertTrue("equality f0 == f0 + 8888", !mySet1.equals(mySet2));
	}

	@Test
	public void testEquality3() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(5001);
		assertTrue("equality f0 == f0 + 5001", !mySet1.equals(mySet2));
	}

	@Test
	public void testEquality4() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(8888);
		assertTrue("equality f0 + 8888 == f0", !mySet2.equals(mySet1));
	}

	@Test
	public void testEquality5() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(5001);
		assertTrue("equality f0 + 5001 == f0", !mySet2.equals(mySet1));
	}

	@Test
	public void testEquality6() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(300);

		assertTrue("equality 100+300 == 100", !mySet2.equals(mySet1));
	}

	@Test
	public void testEquality7() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);

		mySet2.addNumber(100);
		mySet2.addNumber(300);

		assertTrue("equality 100 == 100+300", !mySet1.equals(mySet2));
	}

	@Test
	public void testEquality8() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(0);
		mySet1.addNumber(1000);

		mySet2.addNumber(256);
		mySet2.addNumber(1000);

		assertTrue("equality 0+1000 == 256+1000", !mySet1.equals(mySet2));
	}

	@Test
	public void testEquality9() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(0);
		mySet1.addNumber(1000);

		mySet2.addNumber(0);
		mySet2.addNumber(256);

		assertTrue("equality 0+1000 == 0+256", !mySet1.equals(mySet2));
	}

	@Test
	public void testInclusion1() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		mySet1.union(mySet2);
		resetAllOperationsCount(mySet1, mySet2);
		assertTrue("inclusion f3 in f0 u f3", mySet2.isIncludedIn(mySet1));
		int complexity = getAllOperationsCount(mySet1, mySet2);
		assertTrue(BAD_COMPLEXITY, complexity <= 13 + OP_COUNT_ADD);
	}

	@Test
	public void testInclusion2() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS3);
		assertTrue("inclusion f3 in f0", !mySet2.isIncludedIn(mySet1));
	}

	@Test
	public void testInclusion3() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(101);
		mySet1.addNumber(300);

		mySet2.addNumber(100);
		mySet2.addNumber(300);

		assertTrue("inclusion 3", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion4() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(101);
		mySet1.addNumber(300);

		mySet2.addNumber(300);

		assertTrue("inclusion 4", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion5() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(100);
		mySet1.addNumber(200);
		mySet1.addNumber(300);

		mySet2.addNumber(100);
		mySet2.addNumber(200);

		assertTrue("inclusion 5", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion6() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(10);

		mySet2.addNumber(1034);
		mySet2.addNumber(5555);

		assertTrue("inclusion 6", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion7() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(11);
		mySet1.addNumber(1000);

		mySet2.addNumber(1000);
		mySet2.addNumber(11);

		assertTrue("inclusion 7", mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion8() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(11);
		mySet1.addNumber(1000);

		mySet2.addNumber(1000);
		mySet2.addNumber(2222);

		assertTrue("inclusion 8", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion9() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(11);
		mySet1.addNumber(1000);

		mySet2.addNumber(11);
		mySet2.addNumber(2000);

		assertTrue("inclusion 9", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusion10() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();

		mySet1.addNumber(11);
		mySet1.addNumber(111);
		mySet1.addNumber(1000);

		mySet2.addNumber(11);
		mySet2.addNumber(555);
		mySet2.addNumber(1000);
		assertTrue("inclusion 10", !mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusionVD101() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(5001);
		assertTrue("inclusion f0 in f0 U 5001", mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusionVD102() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(5001);
		assertTrue("non-inclusion f0 U 5001 in f0", !mySet2.isIncludedIn(mySet1));
	}

	@Test
	public void testInclusionVD103() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(5001);
		mySet1.isIncludedIn(mySet2);
		assertConstness("inclusion: this", mySet1, ENS0);
		mySet2.isIncludedIn(mySet1);
		assertConstness("inclusion: arg", mySet1, ENS0);
	}

	@Test
	public void testInclusionVD201() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(6001);
		assertTrue("inclusion f0 in f0 U 6001", mySet1.isIncludedIn(mySet2));
	}

	@Test
	public void testInclusionVD202() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(6001);
		assertTrue("non-inclusion f0 U 6001 in f0", !mySet2.isIncludedIn(mySet1));
	}

	@Test
	public void testInclusionVD203() {
		MySet mySet1 = readFileToMySet(ENS0);
		MySet mySet2 = readFileToMySet(ENS0);
		mySet2.addNumber(6001);
		mySet2.isIncludedIn(mySet1);
		assertConstness("inclusion: arg", mySet1, ENS0);
		mySet1.isIncludedIn(mySet2);
		assertConstness("inclusion: this", mySet1, ENS0);
	}

	@Test
	public void testAddTail() {
		MySet mySet1 = new MySet();
		MySet mySet2 = new MySet();
		int bigValue = 32000;

		mySet1.addNumber(100);
		mySet1.addNumber(10000);
		mySet1.addNumber(bigValue);

		mySet2.addNumber(100);
		mySet2.addNumber(10000);

		SmallSet smallSet = new SmallSet();
		smallSet.add(bigValue % 256);
		SubSet subset = new SubSet(bigValue / 256, smallSet);
		mySet2.addTail(subset);
		assertTrue("addTail", compareMySets(mySet1, mySet2));
	}

}