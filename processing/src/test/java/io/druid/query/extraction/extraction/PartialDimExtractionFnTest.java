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

package io.druid.query.extraction.extraction;

import com.google.common.collect.Sets;
import io.druid.query.extraction.DimExtractionFn;
import io.druid.query.extraction.PartialDimExtractionFn;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 */
public class PartialDimExtractionFnTest
{
  private static final String[] testStrings = {
      "Quito",
      "Calgary",
      "Tokyo",
      "Stockholm",
      "Vancouver",
      "Pretoria",
      "Wellington",
      "Ontario"
  };

  @Test
  public void testExtraction()
  {
    String regex = ".*[Tt][Oo].*";
    DimExtractionFn dimExtractionFn = new PartialDimExtractionFn(regex);
    List<String> expected = Arrays.asList("Quito", "Tokyo", "Stockholm", "Pretoria", "Wellington");
    Set<String> extracted = Sets.newHashSet();

    for (String str : testStrings) {
      String res = dimExtractionFn.apply(str);
      if (res != null) {
        extracted.add(res);
      }
    }

    Assert.assertEquals(5, extracted.size());

    for (String str : extracted) {
      Assert.assertTrue(expected.contains(str));
    }
  }
}
