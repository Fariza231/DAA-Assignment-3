package cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GraphGenerator {

    private static final Random rnd = new Random(42); // фиксируем seed для воспроизводимости

    private static class Category {
        final String name;
        final int count;
        final int minV;
        final int maxV;
        final double densityMultiplier;

        Category(String name, int count, int minV, int maxV, double densityMultiplier) {
            this.name = name;
            this.count = count;
            this.minV = minV;
            this.maxV = maxV;
            this.densityMultiplier = densityMultiplier;
        }
    }

    public static void main(String[] args) {
        String outFile = "input.json";
        if (args.length >= 1 && args[0] != null && !args[0].isBlank()) outFile = args[0];

        List<Category> cats = List.of(
                new Category("Small", 5, 10, 30, 2.0),    // edges ~ 2 * V
                new Category("Medium", 10, 50, 300, 1.5), // edges ~ 1.5 * V
                new Category("Large", 10, 400, 1000, 1.2),// edges ~ 1.2 * V
                new Category("Extra", 5, 1300, 2000, 1.0) // edges ~ 1.0 * V
        );

        List<MapSpec> graphs = new ArrayList<>();
        int idCounter = 1;
        for (Category c : cats) {
            for (int i = 0; i < c.count; i++) {
                int V = randInt(c.minV, c.maxV);
                int targetEdges = Math.max(V - 1, (int) Math.round(V * c.densityMultiplier));
                MapSpec spec = generateConnectedGraphSpec(idCounter++, V, targetEdges);
                graphs.add(spec);
                System.out.printf("Generated graph id=%d category=%s V=%d edges=%d%n",
                        spec.id, c.name, V, spec.edges.size());
            }
        }

        String json = buildJson(graphs);
        try {
            Files.write(Paths.get(outFile), json.getBytes());
            System.out.println("Generated " + graphs.size() + " graphs and wrote to " + outFile);
        } catch (IOException e) {
            System.err.println("Failed to write input JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class MapSpec {
        int id;
        List<String> nodes;
        List<EdgeSpec> edges;
        MapSpec(int id) { this.id = id; nodes = new ArrayList<>(); edges = new ArrayList<>(); }
    }

    private static class EdgeSpec {
        String from;
        String to;
        int weight;
        EdgeSpec(String f, String t, int w) { from = f; to = t; weight = w; }
    }

    private static List<String> makeLabels(int n) {
        List<String> labels = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) labels.add("N" + i);
        return labels;
    }

    private static MapSpec generateConnectedGraphSpec(int id, int V, int targetEdges) {
        MapSpec g = new MapSpec(id);
        List<String> labels = makeLabels(V);
        g.nodes.addAll(labels);

        Set<Long> used = new HashSet<>(Math.min(targetEdges * 2, V * (V - 1) / 2));

        for (int i = 1; i < V; i++) {
            int a = i - 1, b = i;
            int w = randWeight();
            g.edges.add(new EdgeSpec(labels.get(a), labels.get(b), w));
            used.add(edgeKey(a, b));
        }

        int maxPossible = V * (V - 1) / 2;
        int desired = Math.min(targetEdges, maxPossible);
        int attempts = 0;
        while (g.edges.size() < desired && attempts < desired * 10) {
            int u = rnd.nextInt(V);
            int v = rnd.nextInt(V);
            if (u == v) { attempts++; continue; }
            int a = Math.min(u, v), b = Math.max(u, v);
            long key = edgeKey(a, b);
            if (used.contains(key)) { attempts++; continue; }
            used.add(key);
            g.edges.add(new EdgeSpec(labels.get(a), labels.get(b), randWeight()));
            attempts = 0;
        }

        return g;
    }

    private static long edgeKey(int a, int b) {
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    private static int randInt(int lo, int hi) {
        if (lo >= hi) return lo;
        return lo + rnd.nextInt(hi - lo + 1);
    }

    private static int randWeight() {
        return 1 + rnd.nextInt(100); // 1..100
    }

    private static String buildJson(List<MapSpec> graphs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"graphs\": [\n");
        for (int i = 0; i < graphs.size(); i++) {
            MapSpec g = graphs.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": ").append(g.id).append(",\n");

            sb.append("      \"nodes\": [");
            for (int j = 0; j < g.nodes.size(); j++) {
                sb.append("\"").append(escape(g.nodes.get(j))).append("\"");
                if (j < g.nodes.size() - 1) sb.append(", ");
            }
            sb.append("],\n");
            sb.append("      \"edges\": [\n");
            for (int j = 0; j < g.edges.size(); j++) {
                EdgeSpec e = g.edges.get(j);
                sb.append("        {\"from\": \"").append(escape(e.from)).append("\", ");
                sb.append("\"to\": \"").append(escape(e.to)).append("\", ");
                sb.append("\"weight\": ").append(e.weight).append("}");
                if (j < g.edges.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ]\n");
            sb.append("    }");
            if (i < graphs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}\n");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
