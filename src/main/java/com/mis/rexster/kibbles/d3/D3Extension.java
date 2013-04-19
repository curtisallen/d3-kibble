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
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.builder.ToStringBuilder;
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
    
    private static final String IDENTIFER = "mid";
    @ExtensionDefinition(extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(description = "D3 Extension for Graphs")
    public ExtensionResponse doD3WorkOnGraph(
            @RexsterContext RexsterResourceContext rexsterResourceContext,
            @RexsterContext Graph graph,
            @ExtensionRequestParameter(name="key", description="A key to search vertices for") String key,
            @ExtensionRequestParameter(name="value", description="A value to search vertices for") String value,
            @ExtensionRequestParameter(name="depth", description="The depth of graph to produce") String depth) {
        
        return getD3Graph(graph, key, value, depth);
    }
    
    private ExtensionResponse getD3Graph(final Graph graph, final String key, final String value, final String depth) {
        logger.info("Looking for vetices that have key: {}, value: {}, depth: {}", key, value, depth);
        // process vertices
        Iterable<Vertex> vertices = graph.getVertices(key, value);
//        Iterable<Vertex> vertices = graph.getVertices();
//        for(Vertex node : vertices) {
//            logger.info("Processing vertex: " + node.getProperty(IDENTIFER));
//            String[] lables = {};
//            Iterable<Vertex> adj = node.getVertices(Direction.OUT, lables);
//            for(Vertex connnectedNode : adj) {
//                logger.info("Node: " + node.getProperty(IDENTIFER) + " is connected to " + connnectedNode.getProperty(IDENTIFER));
//            }
//        }
        
        Set<Map<String, Object>> vertexList = new LinkedHashSet<Map<String,Object>>();
        Set<Map<String,Object>> edgeList = new HashSet<Map<String, Object>>();
        List<String> index = new ArrayList<String>();
        
        String[] labels = {};
        // add nodes
        for(Vertex node : vertices) {
            logger.info("Found matching node {}", node.getProperty(IDENTIFER));
            vertexList.add(processVertex(node, index)); // add root node to list
            // get the adjencnt nodes
            Iterable<Vertex> adjs = node.getVertices(Direction.OUT, labels);
            // add each adjencnt node to the nodes list
            for(Vertex adj: adjs) {
                vertexList.add(processVertex(adj, index));
                
                // add some depth
                Iterable<Vertex> slAdjs = adj.getVertices(Direction.OUT, labels);
                for(Vertex slAdj: slAdjs) {
                    vertexList.add(processVertex(slAdj, index));
                }
            }
        }
        
        logger.info("index: " + ToStringBuilder.reflectionToString(index.toArray()));
        
        // add edges
        for(Vertex node: vertices) {
            logger.info("edges: Found matching node {}", node.getProperty(IDENTIFER));
            Iterable<Edge> outEdges = node.getEdges(Direction.OUT, labels);
            for(Edge edge: outEdges) {
                logger.info("found out edge from {} -> {}",node.getProperty(IDENTIFER), edge.getVertex(Direction.IN).getProperty(IDENTIFER));
                int srcIdx = index.indexOf((node.getProperty(IDENTIFER)).toString());
                int destIdx = index.indexOf((edge.getVertex(Direction.IN).getProperty(IDENTIFER)).toString());
                // store edge props
                Map<String, Object> edgeProps = new HashMap<String, Object>();
                edgeProps.put("source", srcIdx);
                edgeProps.put("target", destIdx);
                
                edgeList.add(edgeProps);
                
                // add some depth
                Iterable<Vertex> slOutNodes = edge.getVertex(Direction.IN).getVertices(Direction.OUT, labels);
                for(Vertex slNode: slOutNodes) {
                    String sourceMid = slNode.getProperty(IDENTIFER);
                    logger.info("Found adj: {}", sourceMid);
                    if (!index.contains(sourceMid)) {
                        logger.info("new source mid! {}", sourceMid);
                    }
                }
//                Iterable<Edge> slOutEdges = edge.getVertex(Direction.OUT).getEdges(Direction.OUT, labels);
//                for(Edge slEdge: slOutEdges) {
//                    String sourceMid = slEdge.getVertex(Direction.IN).getProperty(IDENTIFER);
//                    String destMid = slEdge.getVertex(Direction.OUT).getProperty(IDENTIFER);
//                    logger.info("depth found out edge {} -> {}", sourceMid, destMid);
//                    
//                    if(!index.contains(sourceMid)) {
//                        logger.info("new source mid! {}", sourceMid);
//                    }
//                    if(!index.contains(destMid)) {
//                        logger.info("new dest mid: {}", destMid);
//                    }
//                }
                
            }
        }
        /*
        List<String> index = new ArrayList<String>();
        for (Vertex node : vertices) {
            logger.info("Processing matching node");
            // add to result object
            vertexList.add(processVertex(node, index));

            String[] lables = {};
            // process edges
            Iterable<Edge> edges = node.getEdges(Direction.OUT, lables);

            for (Edge edge : edges) {
                // store node props
                Map<String, Object> edgeProps = new HashMap<String, Object>();

                // process domain
                Vertex domain = edge.getVertex(Direction.IN);
//                vertexList.add(processVertex(domain, index));
                
                // process range
                Vertex range = edge.getVertex(Direction.IN);
                logger.info("Processing node: "+ node.getProperty(IDENTIFER) + " with range: " + range.getProperty(IDENTIFER));
                vertexList.add(processVertex(range, index));
                
                // add some depth
//                Iterable<Edge> edges2 = range.getEdges(Direction.OUT, lables);
//                for(Edge edge2: edges2) {
//                    
//                }
                
                int srcId = index.indexOf((domain.getProperty(IDENTIFER)).toString());
                int destId = index.indexOf((range.getProperty(IDENTIFER)).toString());
                edgeProps.put("source", srcId);
                edgeProps.put("target", destId);

                edgeList.add(edgeProps);
            }
        }
        
        */
        
        Map<String, Set> result = new HashMap<String, Set>();

        result.put("nodes", vertexList);
        result.put("links", edgeList);
        ExtensionResponse extensionResponse = 
                ExtensionResponse.ok(result);
        //logger.info("Index: ", ToStringBuilder.reflectionToString(index.toArray()));
        return extensionResponse;
    }

   private Map<String, Object> processVertex(Vertex node, List<String> index) {
       // store node props
       Map<String, Object> vertexProps = new HashMap<String, Object>();

       for (String propKey : node.getPropertyKeys()) {
           vertexProps.put(propKey, node.getProperty(propKey));
       }
       vertexProps.put("id", node.getId()); // add the id
       // add to index
       String mid = (String) node.getProperty(IDENTIFER);
//       logger.info("Index of mid: {} {}", mid, index.indexOf(mid));
       if(index.indexOf(mid) == -1 ) {
           logger.info("Adding index for {}", mid);
            index.add(mid);
       }
       return vertexProps;
   }
}
