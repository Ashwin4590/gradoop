package org.gradoop.flink.model.impl.operators.cypher;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.id.GradoopId;
import org.gradoop.common.model.impl.pojo.Edge;
import org.gradoop.common.model.impl.pojo.GraphHead;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.common.model.impl.properties.Properties;
import org.gradoop.flink.io.impl.graph.GraphDataSource;
import org.gradoop.flink.model.api.epgm.GraphCollection;
import org.gradoop.flink.model.api.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.functions.epgm.Id;
import org.gradoop.flink.model.impl.operators.matching.common.MatchStrategy;
import org.gradoop.flink.model.impl.operators.matching.common.statistics.GraphStatistics;
import org.gradoop.flink.model.impl.operators.matching.single.cypher.CypherPatternMatching;
import org.gradoop.flink.model.impl.operators.union.Union;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ashwinramesh on 9/18/17.
 */
public class CypherExecute extends Cypher{

    public CypherExecute(String query) {
        super(query);
    }

    @Override
    protected GraphCollection executeCreate(LogicalGraph graph) {
        DataSet<String> vertexData = graph
                .getConfig()
                .getExecutionEnvironment()
                .fromElements(new String("id:000000000000000000000011,name:Ash,gender:m,label:Person"));

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

        DataSet<String> edgeData = graph
                .getConfig()
                .getExecutionEnvironment()
                .fromElements(new String("id:000000000000000001000018,label:knowsWell, sourceId:000000000000000000000011, targetId:000000000000000000000000"));

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

        DataSet<Vertex> newVertexSet = (graph.getVertices())
                .union(importVertices);

        DataSet<Edge> newEdgeSet = (graph.getEdges())
                .union(importEdges);

        return graph.getConfig().getGraphCollectionFactory()
                .fromDataSets(graph.getGraphHead(), newVertexSet, newEdgeSet);
    }

    @Override
    protected GraphCollection executeModify(LogicalGraph graph) {
        return null;
    }

    @Override
    public String getName() {
        return CypherExecute.class.getName();
    }
}
