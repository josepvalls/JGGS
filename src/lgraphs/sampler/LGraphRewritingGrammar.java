/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package lgraphs.sampler;

import lgraphs.LGraph;
import lgraphs.LGraphNode;
import lgraphs.ontology.Sort;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author santi
 */
public class LGraphRewritingGrammar {
    List<LGraphRewritingRule> rules;
    
    public LGraphRewritingGrammar() {
        rules = new LinkedList<LGraphRewritingRule>();
    }
    
    
    public void addRule(LGraphRewritingRule rule) {
        rules.add(rule);
    }
    
    
    public static LGraphRewritingGrammar load(String fileName) throws Exception {
        LGraphRewritingGrammar grammar = new LGraphRewritingGrammar();
        
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        do{
            String line = br.readLine();
            if (line==null) break;
            StringTokenizer st = new StringTokenizer(line," ");
            if (!st.hasMoreTokens()) continue;
            String token = st.nextToken();
            if (token.equals("RULE")) {
                // Load the rule:
                String ruleName = st.nextToken();
                double weight = Double.parseDouble(st.nextToken());
                double decay = Double.parseDouble(st.nextToken());
                
                String patternString = null;
                List<String> negatedPatternStrings = new ArrayList<String>();
                String replacementString = null;
                
                while(true) {
                    String line2 = br.readLine().trim();
                    if (line2==null) throw new Exception("End of file reached before a \"REPLACEMENT\" token was found");
                    if (line2.startsWith("PATTERN")) {
                        if (patternString != null) throw new Exception("More than one \"PATTERN\" tag found in rule!");
                        patternString = line2.substring(7).trim();
                    }
                    if (line2.startsWith("NEGATEDPATTERN")) negatedPatternStrings.add(line2.substring(14).trim());
                    if (line2.startsWith("REPLACEMENT")) {
                        if (replacementString != null) throw new Exception("More than one \"REPLACEMENT\" tag found in rule!");
                        replacementString = line2.substring(11).trim();
                        break;
                    }
                }                
                if (patternString==null) throw new Exception("End of file reached before a \"PATTERN\" token was found");
                
                // load the pattern:
                Map<String, LGraphNode> patternMap = new HashMap<String, LGraphNode>();                
                LGraph pattern = LGraph.fromString(patternString, patternMap);
                
                // load the negated patterns:
                List<LGraph> negatedPatterns = new ArrayList<LGraph>();                
                List<Map<LGraphNode, LGraphNode>> negatedPatternMaps = new ArrayList<Map<LGraphNode, LGraphNode>>();
                for(String negatedPatternString:negatedPatternStrings) {
                    Map<String, LGraphNode> negatedPatternMap = new HashMap<String, LGraphNode>();
                    LGraph negatedPattern = LGraph.fromString(negatedPatternString, negatedPatternMap);
                    negatedPatterns.add(negatedPattern);
                    Map<LGraphNode, LGraphNode> np2pmap = new HashMap<LGraphNode, LGraphNode>();
                    for(String nodeName:negatedPatternMap.keySet()) {
                        if (patternMap.containsKey(nodeName)) {
                            np2pmap.put(negatedPatternMap.get(nodeName), patternMap.get(nodeName));
                        }
                    }
                    negatedPatternMaps.add(np2pmap);
                }
                
                // load the replacement:
                Map<String, LGraphNode> replacementMap = new HashMap<String, LGraphNode>();
                LGraph replacement = LGraph.fromString(replacementString, replacementMap);
                Map<LGraphNode, LGraphNode> r2pmap = new HashMap<LGraphNode, LGraphNode>();
                
//                System.out.println("patternMap: " + patternMap.keySet());
//                System.out.println("replacementMap: " + replacementMap.keySet());
                
                for(String nodeName:replacementMap.keySet()) {
                    if (patternMap.containsKey(nodeName)) {
                        r2pmap.put(replacementMap.get(nodeName), patternMap.get(nodeName));
                    }
                }
                grammar.addRule(new LGraphRewritingRule(ruleName, weight, decay, pattern, negatedPatterns, negatedPatternMaps, replacement, r2pmap));
            } else {
                throw new Exception("Expected token \"RULE\", but found \"" + token + "\"");
            }
            
        }while(true);
        
        return grammar;
    }
    
    
    public String toString() {
        String tmp = "";
        for(LGraphRewritingRule r:rules) {
            tmp += r.toString();
        }
        return tmp;
    }
    
}
