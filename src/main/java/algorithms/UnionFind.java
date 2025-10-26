package algorithms;

import metrics.Metrics;

public class UnionFind {
    private int[] parent;
    private int[] rank;
    private Metrics metrics;

    public UnionFind(int n, Metrics metrics) {
        parent = new int[n];
        rank = new int[n];
        this.metrics = metrics;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        metrics.incrUFFind();
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }

    public void union(int x, int y) {
        metrics.incrUFUnion();
        int rx = find(x);
        int ry = find(y);
        if (rx == ry) return;
        if (rank[rx] < rank[ry]) parent[rx] = ry;
        else if (rank[ry] < rank[rx]) parent[ry] = rx;
        else { parent[ry] = rx; rank[rx]++; }
    }
}
