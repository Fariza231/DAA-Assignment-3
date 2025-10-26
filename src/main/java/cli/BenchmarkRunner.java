package cli;

import algorithms.Kruskal;
import algorithms.Prim;
import io.JSONReader;
import io.JSONWriter;
import metrics.Metrics;
import graph.Graph;
import io.JSONReader.GraphWithId;
import io.JSONReader.EdgeTriple;

import java.util.*;

public class BenchmarkRunner {
    private static final String DEFAULT_OUTPUT = "output.json";

    public static void run(String inputJson, String outputJson) {
        List<GraphWithId> graphList = JSONReader.readGraphs(inputJson);
        List<Map<String,Object>> results = new ArrayList<>();

        for (GraphWithId gw : graphList) {

            Graph G = new Graph(gw.nodes);
            for (EdgeTriple et : gw.edges) {
                G.addEdge(et.from, et.to, et.weight);
            }

            Map<String,Object> report = new LinkedHashMap<>();
            report.put("graph_id", gw.id);
            Map<String,Object> inputStats = new LinkedHashMap<>();
            inputStats.put("vertices", G.V());
            inputStats.put("edges", G.E());
            report.put("input_stats", inputStats);

            Metrics mPrim = new Metrics();
            long t0 = System.nanoTime();
            Prim prim = new Prim(G, mPrim);
            long t1 = System.nanoTime();
            mPrim.setTimeMs((t1 - t0) / 1_000_000);
            Map<String,Object> primMap = new LinkedHashMap<>();
            List<Map<String,Object>> primEdges = new ArrayList<>();
            for (var e : prim.getMST()) {
                Map<String,Object> edgeObj = new LinkedHashMap<>();
                edgeObj.put("from", G.labelOf(e.either()));
                edgeObj.put("to", G.labelOf(e.other(e.either())));
                edgeObj.put("weight", e.weight());
                primEdges.add(edgeObj);
            }
            primMap.put("mst_edges", primEdges);
            primMap.put("total_cost", prim.totalCost());
            primMap.put("operations_count", mPrim.toMap());
            primMap.put("execution_time_ms", mPrim.getTimeMs());
            report.put("prim", primMap);

            Metrics mK = new Metrics();
            t0 = System.nanoTime();
            Kruskal kr = new Kruskal(G, mK);
            t1 = System.nanoTime();
            mK.setTimeMs((t1 - t0)/1_000_000);
            Map<String,Object> krMap = new LinkedHashMap<>();
            List<Map<String,Object>> krEdges = new ArrayList<>();
            for (var e : kr.getMST()) {
                Map<String,Object> edgeObj = new LinkedHashMap<>();
                edgeObj.put("from", G.labelOf(e.either()));
                edgeObj.put("to", G.labelOf(e.other(e.either())));
                edgeObj.put("weight", e.weight());
                krEdges.add(edgeObj);
            }
            krMap.put("mst_edges", krEdges);
            krMap.put("total_cost", kr.totalCost());
            krMap.put("operations_count", mK.toMap());
            krMap.put("execution_time_ms", mK.getTimeMs());
            report.put("kruskal", krMap);

            results.add(report);
        }

        JSONWriter.writeResults(outputJson, results);
        System.out.println("Benchmark completed. Output written to " + outputJson);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java cli.BenchmarkRunner input.json output.json");
            return;
        }
        run(args[0], args[1]);
    }
}
