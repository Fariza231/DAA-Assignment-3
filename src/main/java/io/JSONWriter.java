package io;

import graph.Graph;
import graph.Edge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class JSONWriter {

    public static void writeResults(String outputFile, List<Map<String,Object>> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"results\": [\n");
        for (int i = 0; i < results.size(); i++) {
            Map<String,Object> r = results.get(i);
            sb.append(mapToJson(r, 2));
            if (i < results.size() - 1) sb.append(",\n");
            else sb.append("\n");
        }
        sb.append("  ]\n}\n");
        try {
            Files.write(Paths.get(outputFile), sb.toString().getBytes());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write output JSON: " + ex.getMessage(), ex);
        }
    }

    // Simple conversion for the specific structure we produce (values can be maps, lists, numbers, strings)
    @SuppressWarnings("unchecked")
    private static String mapToJson(Map<String,Object> m, int indent) {
        StringBuilder sb = new StringBuilder();
        String pad = " ".repeat(indent);
        sb.append(pad).append("{\n");
        int count = 0;
        for (Map.Entry<String,Object> e : m.entrySet()) {
            sb.append(pad).append("  \"").append(e.getKey()).append("\": ");
            Object v = e.getValue();
            sb.append(valueToJson(v, indent + 2));
            count++;
            if (count < m.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append(pad).append("}");
        return sb.toString();
    }

    private static String valueToJson(Object v, int indent) {
        if (v == null) return "null";
        if (v instanceof String) return "\"" + v + "\"";
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        if (v instanceof Map) return mapToJson((Map<String,Object>) v, indent);
        if (v instanceof List) return listToJson((List<Object>) v, indent);
        return "\"" + v.toString() + "\"";
    }

    private static String listToJson(List<Object> list, int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        String pad = " ".repeat(indent);
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            sb.append(pad).append("  ").append(valueToJson(item, indent + 2));
            if (i < list.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(pad).append("]");
        return sb.toString();
    }
}
