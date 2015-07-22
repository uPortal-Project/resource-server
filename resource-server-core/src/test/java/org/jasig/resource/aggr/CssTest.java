/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * 
 */
package org.jasig.resource.aggr;

import org.jasig.resourceserver.aggr.om.Css;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test cases for {@link Css}, specifically the 
 * {@link Css#willAggregateWith(Css)}, {@link Css#isAbsolute()}, 
 * and {@link Css#isConditional()} methods.
 * 
 * @author Nicholas Blair, npblair@wisc.edu
 *
 */
public class CssTest {
    private ResourcesAggregatorImpl aggregatorImpl;
    
    @Before
    public void setup() {
        this.aggregatorImpl = new ResourcesAggregatorImpl();
    }
    
    @After
    public void teardown() {
        this.aggregatorImpl = null;
    }

	@Test
	public void testWillAggregateWithControl() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
		
		c1.setValue("1.css");
		c2.setValue("2.css");
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
	
	@Test
	public void testWillAggregateWithSubdirectory() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
	
		c1.setValue("subdirectory/1.css");
		c2.setValue("subdirectory/2.css");
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
	
	@Test
	public void testWillAggregateWithMultipleSubs() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
		c1.setValue("sub/sub/sub/sub/1.css");
		c2.setValue("sub/sub/sub/sub/2.css");
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
	
	@Test
	public void testWillAggregateWithOtherDir() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
		c1.setValue("1.css");
		c2.setValue("otherdirectory/2.css");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		c1.setValue("otherdirectory/1.css");
		c2.setValue("2.css");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
	
	@Test
	public void testWillAggregateWithMedia() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
		c1.setValue("1.css");
		c1.setMedia("screen");
		c2.setValue("2.css");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		c1.setMedia(null);
		c2.setMedia("screen");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		c1.setMedia("screen");
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		c2.setValue("subdir/2.css");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
	
	@Test
	public void testWillAggregateWithConditional() throws Exception {
		Css c1 = new Css();
		Css c2 = new Css();
		
		c1.setValue("subdirectory/1.css");
		c1.setConditional("condition1");
		c2.setValue("subdirectory/2.css");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		c2.setConditional("condition2");
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertFalse(this.aggregatorImpl.willAggregateWith(c2, c1));
		
		// set both to same condition, should aggregate
		c1.setConditional("condition2");
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c1, c2));
		Assert.assertTrue(this.aggregatorImpl.willAggregateWith(c2, c1));
	}
}
