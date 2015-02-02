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

package io.druid.segment.column;

import it.uniroma3.mat.extendedset.intset.ImmutableConciseSet;

/**
 */
public interface BitmapIndex
{
  public int getCardinality();
  public String getValue(int index);
  public boolean hasNulls();
  public ImmutableConciseSet getConciseSet(String value);
  public ImmutableConciseSet getConciseSet(int idx);
}
