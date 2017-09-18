package org.gradoop.flink.model.impl.operators.cypher;

import org.gradoop.flink.model.api.epgm.GraphCollection;
import org.gradoop.flink.model.api.epgm.LogicalGraph;
import org.gradoop.flink.model.api.operators.UnaryGraphToCollectionOperator;

/**
 * Created by ashwinramesh on 9/18/17.
 */
public abstract class Cypher implements UnaryGraphToCollectionOperator {

    private final String query;

    public Cypher(String query) {
        this.query = query;
    }

    @Override
    public GraphCollection execute(LogicalGraph graph) {
        GraphCollection result;

        // Parse the Cypher query
        // Identify Operation
        // Identify Parameters
        // Call the operation by passing the parameters

        return executeCreate(graph);
    }

    protected abstract GraphCollection executeCreate(LogicalGraph graph);

    protected abstract GraphCollection executeModify(LogicalGraph graph);

    protected String getQuery() {
        return query;
    }

}
