/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.rultor.base;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link SecretMap}.
 * 
 * @author bharathbolisetty
 * @version $Id$
 */
public class SecretMapTest {

	/***
	 * SecretMap basic operations test.
	 * @throws Exception If some problem inside
	 */
	@Test
	public void testBasicMethods() {
		Map<String, Object> map = new HashMap<String, Object>(2);
		SecretMap secretMap = new SecretMap(map);

		MatcherAssert.assertThat(secretMap.isEmpty(), Matchers.is(true));
		MatcherAssert.assertThat(secretMap.size(), Matchers.is(0));
		MatcherAssert.assertThat(secretMap.get("a"), Matchers.nullValue());
		MatcherAssert.assertThat(secretMap.containsKey("b"), Matchers.is(false));

		map.put("a", "1");
		SecretMap secretMap1 = new SecretMap(map);
		MatcherAssert.assertThat(secretMap1.isEmpty(), Matchers.is(false));
		MatcherAssert.assertThat(secretMap1.size(), Matchers.is(1));
		MatcherAssert.assertThat(secretMap1.containsValue("1"), Matchers.is(true));
		
		secretMap1.put("b", "2");
		MatcherAssert.assertThat(secretMap1.size(), Matchers.is(2));
		MatcherAssert.assertThat(secretMap1.containsValue("2"), Matchers.is(true));
		
		secretMap1.clear();
		MatcherAssert.assertThat(secretMap1.isEmpty(), Matchers.is(true));
		
	}
	
	/***
	 * SecretMap equals and hashcode.
	 * @throws Exception If some problem inside
	 */
	@Test
	public void testEqualsHashCodeMethod(){
		Map<String, Object> map1 = new HashMap<String, Object>(2);
		map1.put("a", "1");
		map1.put("b", "2");
		SecretMap secretMap1 = new SecretMap(map1);

		Map<String, Object> map2 = new ConcurrentHashMap<String, Object>(2);
		map2.put("b", "2");
		map2.put("a", "1");
		SecretMap secretMap2 = new SecretMap(map2);

		MatcherAssert.assertThat(secretMap1, Matchers.equalTo(secretMap2));
	}

	/***
	 * SecretMap toString test.
	 * @throws Exception If some problem inside
	 */
	@Test
	public void testToString() {
		Map<String, Object> map = new HashMap<String, Object>(2);
		SecretMap secretMap = new SecretMap(map);
		MatcherAssert.assertThat(secretMap.toString(), Matchers.is("{}"));

		map.put("a", "1");
		SecretMap secretMap1 = new SecretMap(map);
		MatcherAssert.assertThat(secretMap1.toString(),
				Matchers.is("{1 pair(s)}"));

		map.put("b", "2");
		SecretMap secretMap2 = new SecretMap(map);
		MatcherAssert.assertThat(secretMap2.toString(),
				Matchers.is("{2 pair(s)}"));
	}
}
