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
package org.gradoop.flink.model.impl.operators.aggregation.functions;

import org.apache.flink.api.common.functions.GroupCombineFunction;
import org.apache.flink.util.Collector;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.PropertyValue;
import org.gradoop.flink.model.api.functions.VertexAggregateFunction;

/**
 * vertex,.. => aggregateValue
 */
public class AggregateVertices
  implements GroupCombineFunction<Vertex, PropertyValue> {

  /**
   * Aggregate function.
   */
  private final VertexAggregateFunction aggFunc;

  /**
   * Constructor.
   *
   * @param aggFunc aggregate function
   */
  public AggregateVertices(VertexAggregateFunction aggFunc) {
    this.aggFunc = aggFunc;
  }

  @Override
  public void combine(
    Iterable<Vertex> vertices, Collector<PropertyValue> out) throws Exception {
    PropertyValue aggregate = null;

    for (Vertex vertex : vertices) {
      PropertyValue increment = aggFunc.getVertexIncrement(vertex);
      if (increment != null) {
        if (aggregate == null) {
          aggregate = increment;
        } else {
          aggregate = aggFunc.aggregate(aggregate, increment);
        }
      }
    }

    if (aggregate != null) {
      out.collect(aggregate);
    }
  }
}
