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
        String[] lables = {};
        // process vertices
        Iterable<Vertex> vertices = graph.getVertices(key, value);
//        Iterable<Vertex> vertices = graph.getVertices();
//        for(Vertex node : vertices) {
//            
//            String name = node.getProperty("name");
//            if(name == null) {
//                continue;
//            }
//            Iterable<Edge> nOutEdges = node.getEdges(Direction.OUT, lables);
//            Iterable<Edge> nInEdges = node.getEdges(Direction.IN, lables);
//            int nOutEdgesCount = 0;
//            for(Edge nOutEdge: nOutEdges) {
//                nOutEdgesCount++;
//            }
////            logger.info("Out Edges: {}", nOutEdgesCount);
//            int nInEdgesCount = 0;
//            for(Edge nInEdge: nInEdges) {
//                nInEdgesCount++;
//            }
////            logger.info("In Edges: {}", nInEdgesCount);
//            
//            
//            Iterable<Vertex> adj = node.getVertices(Direction.OUT, lables);
//            int adjCount = 0;
//            for(Vertex connnectedNode : adj) {
//                //logger.info("Node: " + node.getProperty(IDENTIFER) + " is connected to " + connnectedNode.getProperty(IDENTIFER));
//                adjCount++;
//            }
////            logger.info("Adj count: {}", adjCount);
//            if( name != null && nOutEdgesCount > 2 && nInEdgesCount >= 1)
//                logger.info("Processing vertex: {} name: {}", node.getProperty(IDENTIFER), name); //nOutEdgesCount, nInEdgesCount);
//        }
        
        Set<Map<String, Object>> vertexList = new LinkedHashSet<Map<String,Object>>();
        Set<Map<String,Object>> edgeList = new HashSet<Map<String, Object>>();
        List<String> index = new ArrayList<String>();
        
        
        // add nodes
        for(Vertex node : vertices) {
            logger.info("Found matching node {}", node.getProperty(IDENTIFER));
            vertexList.add(processVertex(node, index)); // add root node to list
            // get the adjencnt nodes
            Iterable<Vertex> adjs = node.getVertices(Direction.OUT, lables);
            // add each adjencnt node to the nodes list
            for(Vertex adj: adjs) {
                vertexList.add(processVertex(adj, index));
                
                // add some depth
//                Iterable<Vertex> slAdjs = adj.getVertices(Direction.OUT, lables);
//                for(Vertex slAdj: slAdjs) {
//                    vertexList.add(processVertex(slAdj, index));
//                }
            }
        }
        
        if(depth != null) {
            // get adjencenies of depth and add them to graph
            Set<Map<String, Object>> depthVertices = new LinkedHashSet<Map<String,Object>>();
            for (Map<String, Object> processedVertex : vertexList) {
                Vertex rawNode = (Vertex) processedVertex.get("_raw");
                if(rawNode.getProperty(IDENTIFER).toString().equals(depth)) {
                    logger.info("Adding some depth");
                    Iterable<Vertex> depthAdjs = rawNode.getVertices(Direction.OUT, lables);
                    for(Vertex depthAdj: depthAdjs) {
                        logger.info("Added depth: {}", depthAdj.getProperty(IDENTIFER));
                        depthVertices.add(processVertex(depthAdj, index));
                    }
                }
            }
            // now merge
            vertexList.addAll(depthVertices);
            
        }
        
        logger.info("index: " + ToStringBuilder.reflectionToString(index.toArray()));
        
        // make links
        for(Map<String,Object> processedVertex : vertexList) {
            Vertex rawNode = (Vertex) processedVertex.get("_raw");
            if(rawNode != null) {
                Iterable<Edge> processedEdges = rawNode.getEdges(Direction.OUT, lables);
                for(Edge processedEdge: processedEdges) {
                    Vertex range = processedEdge.getVertex(Direction.OUT);
                    Vertex domain = processedEdge.getVertex(Direction.IN);
                    
                    if(domain != null && range != null) {
                        int srcIdx = index.indexOf((domain.getProperty(IDENTIFER)).toString());
                        int destIdx = index.indexOf((range.getProperty(IDENTIFER)).toString());

                        if(srcIdx != -1 && destIdx != -1) {
                            Map<String, Object> edgeProps = new HashMap<String, Object>();

                            edgeProps.put("source", srcIdx);
                            edgeProps.put("target", destIdx);
                            logger.info("Adding edge from {} -> {}", srcIdx, destIdx);
                            edgeList.add(edgeProps);
                        }
                    }

                }
            }
        }
        
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
           logger.info("Adding prop: {} = {}", propKey,  node.getProperty(propKey));
           vertexProps.put(propKey, node.getProperty(propKey));
       }
       vertexProps.put("id", node.getId()); // add the id
       // add to index
       String mid = (String) node.getProperty(IDENTIFER);
       vertexProps.put(IDENTIFER, mid);
       vertexProps.put("_raw", node);
//       logger.info("Index of mid: {} {}", mid, index.indexOf(mid));
       if(index.indexOf(mid) == -1 ) {
           logger.info("Adding index for {}", mid);
            index.add(mid);
       }
       return vertexProps;
   }
}
