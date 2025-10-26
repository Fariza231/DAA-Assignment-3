package graph;

import java.util.*;

public class Graph {
    private final int V;
    private final List<Edge>[] adj;
    private final List<Edge> edges;
    // mapping from label string to index and back
    private final String[] labels; // index -> label
    private final Map<String, Integer> labelToIndex;

    @SuppressWarnings("unchecked")
    public Graph(List<String> nodeLabels) {
        this.V = nodeLabels.size();
        adj = (List<Edge>[]) new List[V];
        for (int i = 0; i < V; i++) adj[i] = new ArrayList<>();
        edges = new ArrayList<>();
        labels = new String[V];
        labelToIndex = new HashMap<>();
        for (int i = 0; i < V; i++) {
            labels[i] = nodeLabels.get(i);
            labelToIndex.put(labels[i], i);
        }
    }

    public int V() { return V; }

    public int E() { return edges.size(); }

    public boolean hasLabel(String label) { return labelToIndex.containsKey(label); }

    public int indexOf(String label) {
        Integer i = labelToIndex.get(label);
        if (i == null) throw new IllegalArgumentException("Unknown label: " + label);
        return i;
    }

    public String labelOf(int idx) {
        return labels[idx];
    }

    public void addEdge(String fromLabel, String toLabel, double weight) {
        if (!labelToIndex.containsKey(fromLabel) || !labelToIndex.containsKey(toLabel))
            throw new IllegalArgumentException("Unknown node label");
        int v = indexOf(fromLabel);
        int w = indexOf(toLabel);
        Edge e = new Edge(v, w, weight);
        adj[v].add(e);
        adj[w].add(e);
        edges.add(e);
    }

    public void addEdge(Edge e) {
        int v = e.either();
        int w = e.other(v);
        adj[v].add(e);
        adj[w].add(e);
        edges.add(e);
    }

    public Iterable<Edge> adj(int v) { return adj[v]; }

    public Iterable<Edge> edges() { return edges; }

    public Edge[] edgesArray() { return edges.toArray(new Edge[0]); }

}
