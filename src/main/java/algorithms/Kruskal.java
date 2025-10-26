package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Kruskal {

    private final List<Edge> mst = new ArrayList<>();
    private double totalCost = 0.0;
    private final Metrics metrics;

    public Kruskal(Graph G, Metrics metrics) {
        this.metrics = metrics;
        run(G);
    }

    private void run(Graph G) {
        Edge[] edges = G.edgesArray();
        Arrays.sort(edges); // uses Edge.compareTo
        UnionFind uf = new UnionFind(G.V(), metrics);
        for (Edge e : edges) {
            metrics.incrEdgesConsidered();
            int v = e.either(), w = e.other(v);
            if (uf.find(v) != uf.find(w)) {
                uf.union(v, w);
                mst.add(e);
                totalCost += e.weight();
            }
            if (mst.size() == G.V() - 1) break;
        }
    }

    public List<Edge> getMST() { return mst; }

    public double totalCost() { return totalCost; }
}
