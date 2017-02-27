package lgraphs.visualizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lgraphs.LGraph;
import lgraphs.LGraphEdge;
import lgraphs.LGraphNode;
import lgraphs.ontology.Ontology;
import lgraphs.sampler.LGraphRewritingGrammar;
import lgraphs.sampler.LGraphRewritingRule;

/**
 *
 * @author josepvalls
 */
public class DotVisualizer {
    int subgraph_count = 0;
    int node_count = 0;
    LGraphRewritingGrammar g;
    public DotVisualizer(LGraphRewritingGrammar g){
        this.g = g;
    }
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("digraph G {\n");
        this.subgraph_count++;
        for(LGraphRewritingRule rule:g.getRules()){
            this.subgraph_count++;
            sb.append("subgraph cluster_"+this.subgraph_count+" {\n");
            sb.append("label=\"");
            sb.append(rule.getName());
            sb.append("\";\n");
            sb.append(ruleToString(rule));
            sb.append("}\n");
        }
        sb.append("}");
        return sb.toString();
    }
    public String ruleToString(LGraphRewritingRule rule){
        StringBuffer sb = new StringBuffer();
        this.subgraph_count++;
        sb.append(graphToString(rule.getPattern(),"subgraph cluster_"+this.subgraph_count, "pattern"));
        for(LGraph n:rule.getNegatedPatterns()){
            this.subgraph_count++;
            sb.append(graphToString(n,"subgraph cluster_"+this.subgraph_count, "negated"));        
        }
        this.subgraph_count++;
        sb.append(graphToString(rule.getReplacement(),"subgraph cluster_"+this.subgraph_count, "replacement"));
        return sb.toString();
    }
    public String graphToString(LGraph graph){
        return graphToString(graph, "digraph G", null);
    }
    public String graphToString(LGraph graph, String name, String label){
        StringBuffer sb = new StringBuffer();
        sb.append(name+" {\n");
        if(label!=null){
            sb.append("label=\"");
            sb.append(label);
            sb.append("\";\n");
        }
        Map<LGraphNode,Integer> node_map = new HashMap();
        for(LGraphNode n:graph.getNodes()){
            this.node_count++;
            node_map.put(n, node_count);
            sb.append("N");
            sb.append(node_count);
            sb.append(" [label=\"");
            sb.append(n.getLabelSet().toString());
            sb.append(" \"];\n");
        }
        for(LGraphNode n:graph.getNodes()){
            for(LGraphEdge e:n.getEdges()){
                sb.append("N");
                sb.append(node_map.get(n));
                sb.append(" -> N");
                sb.append(node_map.get(e.end));
                sb.append(" [label=\"");
                sb.append(e.labelSet.toString());
                sb.append(" \"];\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
    public static void main(String args[]) throws Exception{
        if (args.length >= 2) {
            Ontology ontology = new Ontology(args[0]);
            LGraphRewritingGrammar g = LGraphRewritingGrammar.load(args[1]);
            DotVisualizer v = new DotVisualizer(g);
            System.out.println(v.toString());
        } else {
            System.out.println("Usage: DotVisualizer ontology.xml grammar_file.txt");
        }       
    }
    
}