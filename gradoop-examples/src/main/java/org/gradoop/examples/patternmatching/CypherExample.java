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
package org.gradoop.examples.patternmatching;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.Properties;
import org.gradoop.flink.io.api.DataSource;
import org.gradoop.flink.io.impl.json.JSONDataSink;
import org.gradoop.flink.io.impl.json.JSONDataSource;
import org.gradoop.flink.model.api.epgm.GraphCollection;
import org.gradoop.flink.model.api.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.operators.matching.common.statistics.GraphStatistics;
import org.gradoop.flink.model.impl.operators.matching.common.statistics.GraphStatisticsLocalFSReader;
import org.gradoop.flink.util.GradoopFlinkConfig;

import java.util.Map;
import java.util.HashMap;

/**
 * A self-contained example on how to use the Cypher query engine in Gradoop.
 *
 * The example uses the graph in dev-support/social-network.pdf
 */
public class CypherExample {
  /**
   * Path to the data graph.
   */
  static final String DATA_PATH = CypherExample.class.getResource("/data/json/sna").getFile();
  /**
   * Path to the data graph statistics (computed using {@link org.gradoop.utils.statistics.StatisticsRunner}
   */
  static final String STATISTICS_PATH = DATA_PATH + "/statistics";

  static final String OUTPUT_PATH = DATA_PATH + "/output/";

  /**
   * Runs the example program on the toy graph.
   *
   * @param args arguments
   * @throws Exception in case sth goes wrong
   */
  public static void main(String[] args) throws Exception {
    // initialize Apache Flink execution environment
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    // create a Gradoop config
    GradoopFlinkConfig config = GradoopFlinkConfig.createConfig(env);
    // create a datasource
    DataSource jsonDataSource = new JSONDataSource(DATA_PATH, config);
    // load graph statistics
    GraphStatistics statistics = GraphStatisticsLocalFSReader.read(STATISTICS_PATH);

    // load graph from datasource (lazy)
    LogicalGraph socialNetwork = jsonDataSource.getLogicalGraph();

    // run a Cypher query (vertex homomorphism, edge isomorphism)
    // the result is a graph collection containing all matching subgraphs
    GraphCollection matches = socialNetwork.cypher(
      "MATCH (u1:Person)<-[:hasModerator]-(f:Forum)" +
      "(u2:Person)<-[:hasMember]-(f)" +
      "WHERE u1.name = \"Alice\"", statistics);

    // this just prints the graph heads to system out
    // alternatively, one can use a org.gradoop.flink.io.api.DataSink to store the whole collection
    // or use the result in subsequent analytical steps
    matches.getGraphHeads().print();

    DataSet<String> vertexData = env.fromElements(
            new String("id:000000000000000000000011,name:Ash,gender:m,label:Person")
    );

    DataSet<Vertex> importVertices = vertexData.map(new MapFunction<String, Vertex>() {

      @SuppressWarnings("Duplicates")
      @Override
      public Vertex map(String value) throws Exception {
        Vertex importVertex = new Vertex();
        Properties properties = Properties.create();
        Map<String, String> KeyValueStore = new HashMap<String, String>();
        String[] keyValuePairs = value.split(",");
        for(String pair : keyValuePairs)  {
          String[] entry = pair.split(":");
          KeyValueStore.put(entry[0].trim(), entry[1].trim());
        }
        for (String s : KeyValueStore.keySet()) {
          switch(s) {
            case "id": importVertex.setId(GradoopId.fromString(KeyValueStore.get(s)));
              break;
            case "label":
              importVertex.setLabel(KeyValueStore.get(s));
              break;
            default:
              properties.set(s, KeyValueStore.get(s));
          }
        }
        importVertex.setProperties(properties);
        return importVertex;
      }
    });

    DataSet<String> edgeData = env.fromElements(
            new String("id:000000000000000001000018,label:knowsWell, sourceId:000000000000000000000011, targetId:000000000000000000000000"));

    DataSet<Edge> importEdges = edgeData.map(new MapFunction<String, Edge>() {

      @SuppressWarnings("Duplicates")
      @Override
      public Edge map(String value) throws Exception {
        Edge importEdge = new Edge();
        Properties properties = Properties.create();
        Map<String, String> KeyValueStore = new HashMap<String, String>();
        String[] keyValuePairs = value.split(",");
        for(String pair : keyValuePairs)  {
          String[] entry = pair.split(":");
          KeyValueStore.put(entry[0].trim(), entry[1].trim());
        }
        for (String s : KeyValueStore.keySet()) {
          switch(s) {
            case "id": importEdge.setId(GradoopId.fromString(KeyValueStore.get(s)));
              break;
            case "label": importEdge.setLabel(KeyValueStore.get(s));
              break;
            case "sourceId": importEdge.setSourceId(GradoopId.fromString(KeyValueStore.get(s)));
              break;
            case "targetId": importEdge.setTargetId(GradoopId.fromString(KeyValueStore.get(s)));
              break;
            default: properties.set(s, KeyValueStore.get(s));
          }
        }
        importEdge.setProperties(properties);
        return importEdge;
      }
    });

    DataSet<Vertex> newVertexSet = (socialNetwork.getVertices())
            .union(importVertices);

    DataSet<Edge> newEdgeSet = (socialNetwork.getEdges())
            .union(importEdges);

    socialNetwork.getConfig().getLogicalGraphFactory().fromDataSets(newVertexSet, newEdgeSet)
            .writeTo(new JSONDataSink(
                    OUTPUT_PATH + "graphHeads.json",
                    OUTPUT_PATH + "vertices.json",
                    OUTPUT_PATH + "edges.json",
                    config));
    env.execute();
  }
}
