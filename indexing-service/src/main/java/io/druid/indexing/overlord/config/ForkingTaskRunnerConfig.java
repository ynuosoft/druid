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

package io.druid.indexing.overlord.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class ForkingTaskRunnerConfig
{
  @JsonProperty
  @NotNull
  private String javaCommand = "java";

  /**
   * This is intended for setting -X parameters on the underlying java.  It is used by first splitting on whitespace,
   * so it cannot handle properties that have whitespace in the value.  Those properties should be set via a
   * druid.indexer.fork.property. property instead.
   */
  @JsonProperty
  @NotNull
  private String javaOpts = "";

  @JsonProperty
  @NotNull
  private String classpath = System.getProperty("java.class.path");

  @JsonProperty
  @Min(1024)
  @Max(65535)
  private int startPort = 8081;

  @JsonProperty
  @NotNull
  List<String> allowedPrefixes = Lists.newArrayList(
      "com.metamx",
      "druid",
      "io.druid",
      "user.timezone",
      "file.encoding",
      "java.io.tmpdir"
  );

  public String getJavaCommand()
  {
    return javaCommand;
  }

  public String getJavaOpts()
  {
    return javaOpts;
  }

  public String getClasspath()
  {
    return classpath;
  }

  public int getStartPort()
  {
    return startPort;
  }

  public List<String> getAllowedPrefixes()
  {
    return allowedPrefixes;
  }
}
