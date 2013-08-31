package com.rultor.base;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link Parallel}.
 * 
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public class ConcatTest {
	/**
	 * Test Concatenation of Strings
	 * 
	 * @checkstyle ExecutableStatementCount (200 lines)
	 */
	@Test
	public void testConcat() {
		List<String> listString = new ArrayList<String>();
		listString.add("test1");
		listString.add("test2");
		listString.add("test3");
		final Concat concatWithoutSeperator = new Concat(listString);
		Assert.assertEquals(concatWithoutSeperator.object(), "test1test2test3");
		Assert.assertEquals(concatWithoutSeperator.toString(),
				listString.size() + " part(s)");
		final Concat concatWithSeperator = new Concat(listString, ",");
		Assert.assertEquals(concatWithSeperator.object(), "test1,test2,test3");
		Assert.assertEquals(concatWithoutSeperator.toString(),
				listString.size() + " part(s)");

	}

	/**
	 * Test Concat when constructor parameters are null. It should throw
	 * ConstraintViolationException.
	 * 
	 * @checkstyle ExecutableStatementCount (200 lines)
	 */
	@Test(expected = ConstraintViolationException.class)
	public void testConcatWithConstructorParamNull() {
		new Concat(null, null);
	}

}
