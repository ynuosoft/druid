/*
 * Druid - a distributed column store.
 * Copyright 2012 - 2015 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.druid.query.aggregation.post;

import io.druid.jackson.DefaultObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

/**
 */
public class ConstantPostAggregatorTest
{
  @Test
  public void testCompute()
  {
    ConstantPostAggregator constantPostAggregator;

    constantPostAggregator = new ConstantPostAggregator("shichi", 7, null);
    Assert.assertEquals(7, constantPostAggregator.compute(null));
    constantPostAggregator = new ConstantPostAggregator("rei", 0.0, null);
    Assert.assertEquals(0.0, constantPostAggregator.compute(null));
    constantPostAggregator = new ConstantPostAggregator("ichi", 1.0, null);
    Assert.assertNotSame(1, constantPostAggregator.compute(null));
  }

  @Test
  public void testComparator()
  {
    ConstantPostAggregator constantPostAggregator =
        new ConstantPostAggregator("thistestbasicallydoesnothing unhappyface", 1, null);
    Comparator comp = constantPostAggregator.getComparator();
    Assert.assertEquals(0, comp.compare(0, constantPostAggregator.compute(null)));
    Assert.assertEquals(0, comp.compare(0, 1));
    Assert.assertEquals(0, comp.compare(1, 0));
  }

  @Test
  public void testSerdeBackwardsCompatible() throws Exception
  {
    DefaultObjectMapper mapper = new DefaultObjectMapper();
    ConstantPostAggregator aggregator = mapper.readValue(
        "{\"type\":\"constant\",\"name\":\"thistestbasicallydoesnothing unhappyface\",\"constantValue\":1}\n",
        ConstantPostAggregator.class
    );
    Assert.assertEquals(new Integer(1), aggregator.getConstantValue());
  }

  @Test
  public void testSerde() throws Exception
  {
    DefaultObjectMapper mapper = new DefaultObjectMapper();
    ConstantPostAggregator aggregator = new ConstantPostAggregator("aggregator", 2, null);
    ConstantPostAggregator aggregator1 = mapper.readValue(
        mapper.writeValueAsString(aggregator),
        ConstantPostAggregator.class
    );
    Assert.assertEquals(aggregator, aggregator1);
  }
}
