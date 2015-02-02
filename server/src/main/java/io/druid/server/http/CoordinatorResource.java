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

package io.druid.server.http;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.druid.server.coordinator.DruidCoordinator;
import io.druid.server.coordinator.LoadQueuePeon;
import io.druid.timeline.DataSegment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 */
@Path("/druid/coordinator/v1")
public class CoordinatorResource
{
  private final DruidCoordinator coordinator;

  @Inject
  public CoordinatorResource(
      DruidCoordinator coordinator
  )
  {
    this.coordinator = coordinator;
  }

  @GET
  @Path("/leader")
  @Produces("application/json")
  public Response getLeader()
  {
    return Response.ok(coordinator.getCurrentLeader()).build();
  }

  @GET
  @Path("/loadstatus")
  @Produces("application/json")
  public Response getLoadStatus(
      @QueryParam("simple") String simple,
      @QueryParam("full") String full
  )
  {
    if (simple != null) {
      return Response.ok(coordinator.getSegmentAvailability()).build();
    }

    if (full != null) {
      return Response.ok(coordinator.getReplicationStatus()).build();
    }
    return Response.ok(coordinator.getLoadStatus()).build();
  }

  @GET
  @Path("/loadqueue")
  @Produces("application/json")
  public Response getLoadQueue(
      @QueryParam("simple") String simple,
      @QueryParam("full") String full
  )
  {
    if (simple != null) {
      return Response.ok(
          Maps.transformValues(
              coordinator.getLoadManagementPeons(),
              new Function<LoadQueuePeon, Object>()
              {
                @Override
                public Object apply(LoadQueuePeon input)
                {
                  long loadSize = 0;
                  for (DataSegment dataSegment : input.getSegmentsToLoad()) {
                    loadSize += dataSegment.getSize();
                  }

                  long dropSize = 0;
                  for (DataSegment dataSegment : input.getSegmentsToDrop()) {
                    dropSize += dataSegment.getSize();
                  }

                  return new ImmutableMap.Builder<>()
                      .put("segmentsToLoad", input.getSegmentsToLoad().size())
                      .put("segmentsToDrop", input.getSegmentsToDrop().size())
                      .put("segmentsToLoadSize", loadSize)
                      .put("segmentsToDropSize", dropSize)
                      .build();
                }
              }
          )
      ).build();
    }

    if (full != null) {
      return Response.ok(coordinator.getLoadManagementPeons()).build();
    }

    return Response.ok(
        Maps.transformValues(
            coordinator.getLoadManagementPeons(),
            new Function<LoadQueuePeon, Object>()
            {
              @Override
              public Object apply(LoadQueuePeon input)
              {
                return new ImmutableMap.Builder<>()
                    .put(
                        "segmentsToLoad",
                        Collections2.transform(
                            input.getSegmentsToLoad(),
                            new Function<DataSegment, Object>()
                            {
                              @Override
                              public String apply(DataSegment segment)
                              {
                                return segment.getIdentifier();
                              }
                            }
                        )
                    )
                    .put(
                        "segmentsToDrop", Collections2.transform(
                        input.getSegmentsToDrop(),
                        new Function<DataSegment, Object>()
                        {
                          @Override
                          public String apply(DataSegment segment)
                          {
                            return segment.getIdentifier();
                          }
                        }
                    )
                    )
                    .build();
              }
            }
        )
    ).build();
  }
}
