/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lgraphs;

import lgraphs.ontology.Sort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author santi
 * 
 */
public class LGraph {
    public static int DEBUG = 0;
    
    List<LGraphNode> nodes = new ArrayList<LGraphNode>();

    public static LGraph fromString(String string) throws Exception {
        return fromString(string, new LinkedHashMap<String, LGraphNode>());
    }

    /*
    Syntax:
        Graph:
            NAME:LABEL(LABEL[*]:NAME, ..., LABEL[*]:NAME), ...
            NAME:LABEL(LABEL[[min]-[max]][*] ... ), ...
        NAME:
            String
        LABEL:
            [~]String
            {[~]String, ..., [~]String}
            
    Todo:
    OK - multiple labels nodes
    OK - remove "not" in edges
    OK - add negated graphs
    OK - multiple labels on edges
    - transitive edges in patterns
    - cardinality restrictions
    */
    
    public static LGraph fromString(String string, Map<String, LGraphNode> nodes) throws Exception {        
        LGraph graph = new LGraph();
        int idx = 0;
        int l = string.length();
        
        if (DEBUG>=1) System.out.println("LGraph::fromString `" + string + "'");
        
        do{
            // get node:
            if (idx>=l) break;
            while(string.charAt(idx)==',' || string.charAt(idx)==' ') idx++;
            LGraphNode node = null;
            {
                String nodeName = "";
                String nodeLabel = "";
                while(string.charAt(idx)!=':') nodeName+=string.charAt(idx++);
                idx++;

                while(string.charAt(idx)!='(') nodeLabel+=string.charAt(idx++);
                idx++;

                node = nodes.get(nodeName);
                LabelSet labelSet = LabelSet.fromString(nodeLabel);
                
                if (DEBUG>=1) {
                    System.out.println("LGraph::fromString: nodeLabel: `" + nodeLabel + "'");
                    System.out.println("LGraph::fromString: labelSet: " + labelSet);
                }
                
                if (node==null) {
                    node = graph.addNode(labelSet);
                    nodes.put(nodeName, node);
                    if (DEBUG>=1) System.out.println("LGraph::fromString: adding node: `" + nodeName + "'");
                } else {
                    if (DEBUG>=1) {
                        System.out.println("LGraph::fromString: node `"+nodeName+"' already defined, just adding the labelset...");
                        System.out.println("LGraph::fromString: labelSet before: " + node.getLabelSet());
                    }
                    node.addLabelSet(labelSet);
                    if (DEBUG>=1) System.out.println("LGraph::fromString: labelSet after: " + node.getLabelSet());
                }
            }

            do{
                if (idx>=l) break;
                if (string.charAt(idx)==')') {
                    idx++;
                    break;
                }
                String edgeLabel = "";
                String targetNodeName = "";
                while(string.charAt(idx)!=':') edgeLabel+=string.charAt(idx++);
                idx++;
                
                while(string.charAt(idx)!=',' && string.charAt(idx)!=')') targetNodeName+=string.charAt(idx++);
                if (string.charAt(idx)==',') idx++;
                
//                System.out.println("Target node name: " + targetNodeName);
                LGraphNode targetNode = nodes.get(targetNodeName);
                LabelSet labelSet = LabelSet.fromString(edgeLabel);
                
                if (DEBUG>=1) {
                    System.out.println("LGraph::fromString: edgeLabel: `" + edgeLabel + "'");
                    System.out.println("LGraph::fromString: labelSet: " + labelSet);
                }
                
                if (targetNode==null) {
                    targetNode = graph.addNode(Sort.getSort("any"));
                    nodes.put(targetNodeName, targetNode);
                }
                node.addEdge(labelSet, targetNode);
            }while(true);
        }while(true);

//        System.out.println(graph);
        return graph;
    }


    public LGraph() {

    }

    public List<LGraphNode> getNodes() {
        return nodes;
    }

    public LGraphNode getNode(int n) {
        return nodes.get(n);
    }

    public LGraphNode addNode(Sort l) {
        LGraphNode n = new LGraphNode(l);
        nodes.add(n);
        return n;
    }

    public LGraphNode addNotNode(Sort l) {
        LGraphNode n = new LGraphNode();
        n.addNotLabel(l);
        nodes.add(n);
        return n;
    }


    public LGraphNode addNode(LabelSet l) {
        LGraphNode n = new LGraphNode(l);
        nodes.add(n);
        return n;
    }

    public LGraphNode addNode(Sort s1, Sort s2) {
        LGraphNode n = new LGraphNode(s1, s2);
        nodes.add(n);
        return n;
    }
    
    public LGraphNode addNode(List<Sort> l) {
        LGraphNode n = new LGraphNode(l);
        nodes.add(n);
        return n;
    }

    public void addEdge(LGraphNode n1, Sort l, LGraphNode n2) {
        n1.addEdge(l,n2);
    }

    public void addEdge(LGraphNode n1, LabelSet l, LGraphNode n2) {
        n1.addEdge(l,n2);
    }
    
    
    public boolean subsumesWithMapping(LGraph reference, Map<LGraphNode, LGraphNode> map) {
        for(LGraphNode n:nodes) {
            LGraphNode rn = map.get(n);
            if (!n.subsumes(rn)) return false;
            for(LGraphEdge e:n.edges) {
                boolean found = false;
                for(LGraphEdge re:rn.edges) {
                    if (e.labelSet.subsumes(re.labelSet) &&
                        re.end == map.get(e.end)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }
        return true;
    }
    
    
    public boolean equivalent(LGraph g)
    {
        LGraphMatcher m1 = new LGraphMatcher(this, g, true);
        if (m1.nextMatch()!=null) {        
            LGraphMatcher m2 = new LGraphMatcher(g, this, true);
            if (m2.nextMatch()!=null) return true;
        }
        
        return false;
    }
    
    
    public void removeNodeAndAllConnections(LGraphNode n)
    {
        nodes.remove(n);
        for(LGraphNode n2:nodes) {
            ArrayList<LGraphEdge> toRemove = new ArrayList<LGraphEdge>();
            for(LGraphEdge e:n2.edges) {
                if (e.end == n) toRemove.add(e);
            }
            n2.edges.removeAll(toRemove);
        }
    }
    

    public LGraph clone() {
        LGraph clone = new LGraph();

        for(LGraphNode node:nodes)
            clone.addNode(node.getLabelSet());

        int idx = 0;
        for(LGraphNode node:nodes) {
            for(LGraphEdge edge:node.edges) {
                clone.addEdge(clone.nodes.get(idx), edge.labelSet, clone.nodes.get(nodes.indexOf(edge.end)));
            }
            idx++;
        }

        return clone;
    }

    public LGraph clone(Map<LGraphNode, LGraphNode> map) {
        LGraph clone = new LGraph();

        for(LGraphNode node:nodes) {
            map.put(node, clone.addNode(node.getLabelSet()));
        }

        for(LGraphNode node:nodes) {
            for(LGraphEdge edge:node.edges) {
                clone.addEdge(map.get(node), edge.labelSet, map.get(edge.end));
            }
        }

        return clone;
    }


    public LGraph cloneSubGraph(List<Sort> nodesOfInterest, Map<LGraphNode, LGraphNode> map) {
        return cloneSubGraph(nodesOfInterest, null, map);
    }
    public LGraph cloneSubGraph(List<Sort> nodesOfInterest, List<Sort> edgesOfInterest, Map<LGraphNode, LGraphNode> map) {
        LGraph clone = new LGraph();

        for(LGraphNode node:nodes) {
            boolean consider = false;
            for(Sort l:nodesOfInterest) {
                if (node.subsumedBy(l)) {
                    consider = true;
                    break;
                }
            }
            if (consider) map.put(node, clone.addNode(node.getLabelSet()));
        }

        for(LGraphNode node:nodes) {
            if (map.containsKey(node)) {
                for(LGraphEdge edge:node.edges) {
                    if (map.containsKey(edge.end)){
                        if(edgesOfInterest==null) {
                            clone.addEdge(map.get(node), edge.labelSet, map.get(edge.end));
                        } else {
                            for(Sort e:edgesOfInterest){
                                if(edge.labelSet.subsumedBy(e)){
                                    clone.addEdge(map.get(node), edge.labelSet, map.get(edge.end));
                                }
                            }
                        }
                    }
                }
            }
        }

        return clone;
    }


    public int[][] adjacencyMatrix() {
        int n = nodes.size();
        int matrix[][] = new int[n][n];

        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                matrix[i][j] = 0;
            }
        }
        for(LGraphNode node:nodes) {
            for(LGraphEdge edge:node.edges) {
                matrix[nodes.indexOf(node)][nodes.indexOf(edge.end)] = 1;
            }
        }

        return matrix;
    }

    public int[][] adjacencyMatrix(List<LGraphNode> nodeSubset) {
        int n = nodeSubset.size();
        int matrix[][] = new int[n][n];

        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                matrix[i][j] = 0;
            }
        }
        for(LGraphNode node:nodeSubset) {
            for(LGraphEdge edge:node.edges) {
                int idx = nodeSubset.indexOf(edge.end);
                if (idx!=-1) {
                    matrix[nodeSubset.indexOf(node)][idx] = 1;
                }
            }
        }

        return matrix;
    }


    public int[][] undirectedAdjacencyMatrix() {
        int n = nodes.size();
        int matrix[][] = new int[n][n];

        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                matrix[i][j] = 0;
            }
        }
        for(LGraphNode node:nodes) {
            for(LGraphEdge edge:node.edges) {
                matrix[nodes.indexOf(node)][nodes.indexOf(edge.end)] = 1;
                matrix[nodes.indexOf(edge.end)][nodes.indexOf(node)] = 1;
            }
        }

        return matrix;
    }


    public int[][] undirectedAdjacencyMatrix(List<LGraphNode> nodeSubset) {
        int n = nodeSubset.size();
        int matrix[][] = new int[n][n];

        for(int i = 0;i<n;i++) {
            for(int j = 0;j<n;j++) {
                matrix[i][j] = 0;
            }
        }
        for(LGraphNode node:nodeSubset) {
            for(LGraphEdge edge:node.edges) {
                int idx = nodeSubset.indexOf(edge.end);
                if (idx!=-1) {
                    matrix[nodeSubset.indexOf(node)][idx] = 1;
                    matrix[idx][nodeSubset.indexOf(node)] = 1;
                }
            }
        }

        return matrix;
    }


    public String toString() {
        return toString("N");
    }


    public String toString(String nodePrefix) {
        String tmp = "";
        boolean first = true;
        for(LGraphNode node:nodes) {
            if (!first) tmp+=",";
            first = false;
            tmp += nodePrefix + nodes.indexOf(node) + ":";
            tmp += node.toStringLabel();
            tmp += "(";
            boolean first3 = true;
            for(LGraphEdge edge:node.edges) {
                if (!first3) tmp+=",";
                first3 = false;
                tmp+= edge.labelSet + ":" + nodePrefix + nodes.indexOf(edge.end);
            }
            tmp += ")";
        }
        return tmp;
    }

}
