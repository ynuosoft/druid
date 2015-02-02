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

package io.druid.indexing.common;

import com.google.inject.Inject;
import io.druid.segment.loading.OmniSegmentLoader;
import io.druid.segment.loading.SegmentLoader;
import io.druid.segment.loading.SegmentLoaderConfig;
import io.druid.segment.loading.StorageLocationConfig;

import java.io.File;
import java.util.Arrays;

/**
 */
public class SegmentLoaderFactory
{
  private final OmniSegmentLoader loader;

  @Inject
  public SegmentLoaderFactory(
      OmniSegmentLoader loader
  )
  {
    this.loader = loader;
  }

  public SegmentLoader manufacturate(File storageDir)
  {
    return loader.withConfig(
        new SegmentLoaderConfig().withLocations(Arrays.asList(new StorageLocationConfig().setPath(storageDir)))
    );
  }
}
