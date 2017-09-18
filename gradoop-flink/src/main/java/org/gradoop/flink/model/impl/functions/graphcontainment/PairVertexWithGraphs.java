/**
 * Copyright © 2014 - 2017 Leipzig University (Database Research Group)
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
package org.gradoop.flink.model.impl.functions.graphcontainment;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.id.GradoopId;

/**
 * Takes a vertex and creates one tuple 2 of this vertex and a graph id per
 * graph the vertex is contained in.
 *
 * @param <V> epgm vertex type
 */
public class PairVertexWithGraphs<V extends Vertex>
  implements FlatMapFunction<V, Tuple2<V, GradoopId>> {

  @Override
  public void flatMap(V v, Collector<Tuple2<V, GradoopId>> collector) throws
    Exception {
    for (GradoopId graphId : v.getGraphIds()) {
      collector.collect(new Tuple2<>(v, graphId));
    }
  }
}
