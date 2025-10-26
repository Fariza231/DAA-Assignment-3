package tests;

import algorithms.Kruskal;
import algorithms.Prim;
import algorithms.UnionFind;
import graph.Edge;
import graph.Graph;
import metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MSTAlgorithmsTest {

    private Graph sampleGraph4Nodes() {
        Graph G = new Graph(List.of("A", "B", "C", "D"));
        G.addEdge("A", "B", 5.0);
        G.addEdge("A", "C", 2.0);
        G.addEdge("B", "C", 8.0);
        G.addEdge("B", "D", 3.0);
        G.addEdge("C", "D", 4.0);
        return G;
    }

    private Graph triangleGraph() {
        Graph G = new Graph(List.of("A", "B", "C"));
        G.addEdge("A", "B", 1.0);
        G.addEdge("B", "C", 2.0);
        G.addEdge("A", "C", 3.0);
        return G;
    }

    private Graph disconnectedGraph() {
        Graph G = new Graph(List.of("A", "B", "C", "D"));
        G.addEdge("A", "B", 1.0);
        G.addEdge("C", "D", 2.0);
        return G;
    }

    // Utility: check acyclicity of a set of edges on V vertices using a fresh UnionFind
    private boolean isAcyclic(List<Edge> edges, int V) {
        Metrics m = new Metrics();
        UnionFind uf = new UnionFind(V, m);
        for (Edge e : edges) {
            int v = e.either();
            int w = e.other(v);
            int rv = uf.find(v);
            int rw = uf.find(w);
            if (rv == rw) return false; // cycle detected
            uf.union(rv, rw);
        }
        return true;
    }

    // Utility: check connectivity - whether MST edges connect all vertices
    private boolean connectsAllVertices(List<Edge> edges, int V) {
        Metrics m = new Metrics();
        UnionFind uf = new UnionFind(V, m);
        for (Edge e : edges) {
            int v = e.either();
            int w = e.other(v);
            uf.union(v, w);
        }
        // check all have same root
        int root = uf.find(0);
        for (int i = 1; i < V; i++) {
            if (uf.find(i) != root) return false;
        }
        return true;
    }

    @Test
    public void testMSTCorrectnessSampleGraph() {
        Graph G = sampleGraph4Nodes();
        int V = G.V();

        Metrics mPrim = new Metrics();
        Prim prim = new Prim(G, mPrim);

        Metrics mKruskal = new Metrics();
        Kruskal kr = new Kruskal(G, mKruskal);

        List<Edge> primMST = prim.getMST();
        List<Edge> krMST = kr.getMST();

        // both MSTs should have V - 1 edges
        assertEquals(V - 1, primMST.size(), "Prim MST should have V-1 edges");
        assertEquals(V - 1, krMST.size(), "Kruskal MST should have V-1 edges");

        // total cost must be identical
        double primCost = prim.totalCost();
        double krCost = kr.totalCost();
        assertEquals(primCost, krCost, 1e-9, "Prim and Kruskal total costs must match");

        // expected MST cost for this sample: compute manually:
        // Graph edges sorted: A-C 2, B-D 3, C-D 4, A-B 5, B-C 8
        // One MST (possible): A-C (2) + B-D (3) + C-D (4) = 9.0
        // Another MST could be A-C (2) + A-B (5) + B-D (3) = 10 -> but that is larger, so expected is 9.0
        // Let's assert the found cost equals 9.0
        assertEquals(9.0, primCost, 1e-9, "Expected MST total cost is 9.0 for sample graph");

        // acyclic checks
        assertTrue(isAcyclic(primMST, V), "Prim MST must be acyclic");
        assertTrue(isAcyclic(krMST, V), "Kruskal MST must be acyclic");

        // connectivity
        assertTrue(connectsAllVertices(primMST, V), "Prim MST must connect all vertices");
        assertTrue(connectsAllVertices(krMST, V), "Kruskal MST must connect all vertices");

        // operation counts and execution time non-negative
        assertTrue(mPrim.getTimeMs() >= 0, "Prim execution time must be non-negative");
        assertTrue(mKruskal.getTimeMs() >= 0, "Kruskal execution time must be non-negative");

        assertTrue(mPrim.getEdgesConsidered() >= 0, "Prim edges considered must be non-negative");
        assertTrue(mKruskal.getEdgesConsidered() >= 0, "Kruskal edges considered must be non-negative");
    }

    @Test
    public void testTriangleGraphMSTProperties() {
        Graph G = triangleGraph();
        int V = G.V();

        Metrics mp = new Metrics();
        Prim prim = new Prim(G, mp);
        Metrics mk = new Metrics();
        Kruskal kr = new Kruskal(G, mk);

        // MST should have V-1 = 2 edges and cost = 3.0
        assertEquals(2, prim.getMST().size(), "Prim MST edges count");
        assertEquals(2, kr.getMST().size(), "Kruskal MST edges count");

        assertEquals(3.0, prim.totalCost(), 1e-9, "Prim MST total cost expected 3.0");
        assertEquals(3.0, kr.totalCost(), 1e-9, "Kruskal MST total cost expected 3.0");

        // reproducibility: run again and compare
        Metrics mp2 = new Metrics();
        Prim prim2 = new Prim(G, mp2);
        Metrics mk2 = new Metrics();
        Kruskal kr2 = new Kruskal(G, mk2);

        assertEquals(prim.totalCost(), prim2.totalCost(), 1e-9, "Prim should be reproducible");
        assertEquals(kr.totalCost(), kr2.totalCost(), 1e-9, "Kruskal should be reproducible");

        // edge sets comparison by weights and endpoints may differ order - compare total cost and size already done
    }

    @Test
    public void testDisconnectedGraphHandledGracefully() {
        Graph G = disconnectedGraph();
        int V = G.V();

        Metrics mp = new Metrics();
        Prim prim = new Prim(G, mp);
        Metrics mk = new Metrics();
        Kruskal kr = new Kruskal(G, mk);

        // For disconnected graphs MST cannot have V-1 edges. Both algorithms should return fewer edges.
        assertTrue(prim.getMST().size() < V - 1, "Prim on disconnected graph should have fewer than V-1 edges");
        assertTrue(kr.getMST().size() < V - 1, "Kruskal on disconnected graph should have fewer than V-1 edges");

        // Total cost should be sum of MSTs of components (non-negative)
        assertTrue(prim.totalCost() >= 0, "Prim total cost non-negative");
        assertTrue(kr.totalCost() >= 0, "Kruskal total cost non-negative");

        // Execution time and operation counts non-negative
        assertTrue(mp.getTimeMs() >= 0, "Prim time non-negative");
        assertTrue(mk.getTimeMs() >= 0, "Kruskal time non-negative");

        assertTrue(mp.getEdgesConsidered() >= 0, "Prim op counts non-negative");
        assertTrue(mk.getEdgesConsidered() >= 0, "Kruskal op counts non-negative");
    }

    @Test
    public void testMSTEdgesAreUniqueAndNoDuplicateEndpoints() {
        // create a somewhat larger small graph and verify edges in MST are unique (no duplicate exact edges)
        Graph G = new Graph(List.of("A", "B", "C", "D", "E"));
        G.addEdge("A", "B", 1.0);
        G.addEdge("A", "C", 2.0);
        G.addEdge("B", "C", 3.0);
        G.addEdge("B", "D", 4.0);
        G.addEdge("C", "E", 5.0);
        G.addEdge("D", "E", 6.0);

        Metrics mp = new Metrics();
        Prim prim = new Prim(G, mp);

        List<Edge> edges = prim.getMST();
        Set<String> repr = new HashSet<>();
        for (Edge e : edges) {
            int a = e.either();
            int b = e.other(a);
            String key = Math.min(a,b) + "-" + Math.max(a,b) + "-" + String.format("%.9f", e.weight());
            assertFalse(repr.contains(key), "Duplicate edge should not be present in MST");
            repr.add(key);
        }
    }
}
