package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.*;

public class Prim {

    private final List<Edge> mst = new ArrayList<>();
    private double totalCost = 0.0;
    private final Metrics metrics;

    public Prim(Graph G, Metrics metrics) {
        this.metrics = metrics;
        run(G);
    }

    private void run(Graph G) {
        int V = G.V();
        boolean[] marked = new boolean[V];
        PriorityQueue<Edge> pq = new PriorityQueue<>(); // min by weight

        for (int s = 0; s < V; s++) {
            if (marked[s]) continue;
            visit(G, s, marked, pq);
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                metrics.incrPQOp();
                int v = e.either(), w = e.other(v);
                if (marked[v] && marked[w]) continue;
                mst.add(e);
                totalCost += e.weight();
                if (!marked[v]) visit(G, v, marked, pq);
                if (!marked[w]) visit(G, w, marked, pq);
                if (mst.size() == V - 1) return;
            }
        }
    }

    private void visit(Graph G, int v, boolean[] marked, PriorityQueue<Edge> pq) {
        marked[v] = true;
        for (Edge e : G.adj(v)) {
            int w = e.other(v);
            if (!marked[w]) {
                pq.add(e);
                metrics.incrEdgePushes();
            }
        }
    }

    public List<Edge> getMST() { return mst; }

    public double totalCost() { return totalCost; }
}
