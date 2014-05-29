package com.mis.rexster.kibbles.d3;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Extension that exposes json in a format that can easily be made into
 * a d3.js graph
 * @author curtis
 */
@ExtensionNaming(name = D3Extension.EXTENSION_NAME, namespace = D3Extension.EXTENSION_NAMESPACE)
public class D3Extension extends AbstractRexsterExtension {
    
    private static final Logger logger = LoggerFactory.getLogger(D3Extension.class);
    public static final String EXTENSION_NAME = "d3graph";
    public static final String EXTENSION_NAMESPACE = "mis";
    
    @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(description = "D3 Extension for Graphs")
    public ExtensionResponse doD3WorkOnGraph(
            @RexsterContext RexsterResourceContext rexsterResourceContext,
            @RexsterContext Graph graph) {
        
        return getD3Graph(graph);
    }
    
    private ExtensionResponse getD3Graph(final Graph graph) {
        
        // process vertices
        Iterable<Vertex> vertices = graph.getVertices();
        List<Map<String, Object>> vertexList = new ArrayList<Map<String,Object>>();
        
        List<String> index = new ArrayList<String>();
        for (Vertex node : vertices) {

            // store node props
            Map<String, Object> props = new HashMap<String, Object>();

            for (String key : node.getPropertyKeys()) {
                props.put(key, node.getProperty(key));
            }
            props.put("id", node.getId()); // add the id
            // add to result object
            vertexList.add(props);

            // add to index
            index.add( ( node.getId()).toString() );
        }
        
        // process edges
        Iterable<Edge> edges = graph.getEdges();
        List<Map<String,Object>> edgeList = new ArrayList<Map<String, Object>>();
        
        for (Edge edge : edges) {
            // store node props
            Map<String, Object> props = new HashMap<String, Object>();

            for (String key : edge.getPropertyKeys()) {
                props.put(key, edge.getProperty(key));
            }
            
            Vertex domain = edge.getVertex(Direction.IN);
            Vertex range = edge.getVertex(Direction.OUT);
            int srcId = index.indexOf( (domain.getId()).toString());
            int destId = index.indexOf( (range.getId()).toString());
            props.put("source", srcId);
            props.put("target", destId);
            
            edgeList.add(props);
        }
        
        Map<String, List> result = new HashMap<String, List>();

        result.put("nodes", vertexList);
        result.put("links", edgeList);
        ExtensionResponse extensionResponse = 
                ExtensionResponse.ok(result);
        
        return extensionResponse;
    }

   
}
