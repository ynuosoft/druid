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

package io.druid.guice;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.druid.jackson.JacksonModule;

import java.util.List;

/**
 */
public class GuiceInjectors
{
  public static Injector makeStartupInjector()
  {
    return Guice.createInjector(
        new DruidGuiceExtensions(),
        new JacksonModule(),
        new PropertiesModule("runtime.properties"),
        new ConfigModule(),
        new Module()
        {
          @Override
          public void configure(Binder binder)
          {
            binder.bind(DruidSecondaryModule.class);
            JsonConfigProvider.bind(binder, "druid.extensions", ExtensionsConfig.class);
          }
        }
    );
  }

  public static Injector makeStartupInjectorWithModules(Iterable<Module> modules)
  {
    List<Module> theModules = Lists.newArrayList();
    theModules.add(new DruidGuiceExtensions());
    theModules.add(new JacksonModule());
    theModules.add(new PropertiesModule("runtime.properties"));
    theModules.add(new ConfigModule());
    theModules.add(
        new Module()
        {
          @Override
          public void configure(Binder binder)
          {
            binder.bind(DruidSecondaryModule.class);
            JsonConfigProvider.bind(binder, "druid.extensions", ExtensionsConfig.class);
          }
        }
    );
    for (Module theModule : modules) {
      theModules.add(theModule);
    }


    return Guice.createInjector(theModules);
  }
}
