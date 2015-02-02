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

package io.druid.query;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.metamx.common.ISE;
import com.metamx.common.guava.BaseSequence;
import com.metamx.common.guava.MergeIterable;
import com.metamx.common.guava.Sequence;
import com.metamx.common.guava.Sequences;
import com.metamx.common.logger.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A QueryRunner that combines a list of other QueryRunners and executes them in parallel on an executor.
 * <p/>
 * When using this, it is important to make sure that the list of QueryRunners provided is fully flattened.
 * If, for example, you were to pass a list of a Chained QueryRunner (A) and a non-chained QueryRunner (B).  Imagine
 * A has 2 QueryRunner chained together (Aa and Ab), the fact that the Queryables are run in parallel on an
 * executor would mean that the Queryables are actually processed in the order
 * <p/>
 * <pre>A -&gt; B -&gt; Aa -&gt; Ab</pre>
 * <p/>
 * That is, the two sub queryables for A would run *after* B is run, effectively meaning that the results for B
 * must be fully cached in memory before the results for Aa and Ab are computed.
 */
public class ChainedExecutionQueryRunner<T> implements QueryRunner<T>
{
  private static final Logger log = new Logger(ChainedExecutionQueryRunner.class);

  private final Iterable<QueryRunner<T>> queryables;
  private final ListeningExecutorService exec;
  private final Ordering<T> ordering;
  private final QueryWatcher queryWatcher;

  public ChainedExecutionQueryRunner(
      ExecutorService exec,
      Ordering<T> ordering,
      QueryWatcher queryWatcher,
      QueryRunner<T>... queryables
  )
  {
    this(exec, ordering, queryWatcher, Arrays.asList(queryables));
  }

  public ChainedExecutionQueryRunner(
      ExecutorService exec,
      Ordering<T> ordering,
      QueryWatcher queryWatcher,
      Iterable<QueryRunner<T>> queryables
  )
  {
    // listeningDecorator will leave PrioritizedExecutorService unchanged,
    // since it already implements ListeningExecutorService
    this.exec = MoreExecutors.listeningDecorator(exec);
    this.ordering = ordering;
    this.queryables = Iterables.unmodifiableIterable(Iterables.filter(queryables, Predicates.notNull()));
    this.queryWatcher = queryWatcher;
  }

  @Override
  public Sequence<T> run(final Query<T> query)
  {
    final int priority = query.getContextPriority(0);

    return new BaseSequence<T, Iterator<T>>(
        new BaseSequence.IteratorMaker<T, Iterator<T>>()
        {
          @Override
          public Iterator<T> make()
          {
            // Make it a List<> to materialize all of the values (so that it will submit everything to the executor)
            ListenableFuture<List<Iterable<T>>> futures = Futures.allAsList(
                Lists.newArrayList(
                    Iterables.transform(
                        queryables,
                        new Function<QueryRunner<T>, ListenableFuture<Iterable<T>>>()
                        {
                          @Override
                          public ListenableFuture<Iterable<T>> apply(final QueryRunner<T> input)
                          {
                            if (input == null) {
                              throw new ISE("Null queryRunner! Looks to be some segment unmapping action happening");
                            }

                            return exec.submit(
                                new AbstractPrioritizedCallable<Iterable<T>>(priority)
                                {
                                  @Override
                                  public Iterable<T> call() throws Exception
                                  {
                                    try {
                                      Sequence<T> result = input.run(query);
                                      if (result == null) {
                                        throw new ISE("Got a null result! Segments are missing!");
                                      }

                                      List<T> retVal = Sequences.toList(result, Lists.<T>newArrayList());
                                      if (retVal == null) {
                                        throw new ISE("Got a null list of results! WTF?!");
                                      }

                                      return retVal;
                                    }
                                    catch (QueryInterruptedException e) {
                                      throw Throwables.propagate(e);
                                    }
                                    catch (Exception e) {
                                      log.error(e, "Exception with one of the sequences!");
                                      throw Throwables.propagate(e);
                                    }
                                  }
                                }
                            );
                          }
                        }
                    )
                )
            );

            queryWatcher.registerQuery(query, futures);

            try {
              final Number timeout = query.getContextValue("timeout", (Number) null);
              return new MergeIterable<>(
                  ordering.nullsFirst(),
                  timeout == null ?
                  futures.get() :
                  futures.get(timeout.longValue(), TimeUnit.MILLISECONDS)
              ).iterator();
            }
            catch (InterruptedException e) {
              log.warn(e, "Query interrupted, cancelling pending results, query id [%s]", query.getId());
              futures.cancel(true);
              throw new QueryInterruptedException("Query interrupted");
            }
            catch (CancellationException e) {
              throw new QueryInterruptedException("Query cancelled");
            }
            catch (TimeoutException e) {
              log.info("Query timeout, cancelling pending results for query id [%s]", query.getId());
              futures.cancel(true);
              throw new QueryInterruptedException("Query timeout");
            }
            catch (ExecutionException e) {
              throw Throwables.propagate(e.getCause());
            }
          }

          @Override
          public void cleanup(Iterator<T> tIterator)
          {

          }
        }
    );
  }
}
