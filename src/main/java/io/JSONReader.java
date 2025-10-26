package io;

import graph.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONReader {

    public static class GraphWithId {
        public int id;
        public List<String> nodes;
        public List<EdgeTriple> edges;
        public GraphWithId(int id) { this.id = id; nodes = new ArrayList<>(); edges = new ArrayList<>(); }
    }

    public static class EdgeTriple {
        public String from, to;
        public double weight;
        public EdgeTriple(String from, String to, double weight) {
            this.from = from; this.to = to; this.weight = weight;
        }
    }

    public static List<GraphWithId> readGraphs(String filename) {
        try {
            String text = new String(Files.readAllBytes(Paths.get(filename)));

            int graphsIndex = text.indexOf("\"graphs\"");
            if (graphsIndex < 0) return Collections.emptyList();
            int start = text.indexOf('[', graphsIndex);
            int end = findMatchingBracket(text, start);
            String graphsContent = text.substring(start + 1, end);

            List<String> graphBlocks = splitTopLevelObjects(graphsContent);
            List<GraphWithId> result = new ArrayList<>();
            for (String block : graphBlocks) {
                GraphWithId g = parseGraphBlock(block);
                if (g != null) result.add(g);
            }
            return result;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read JSON: " + ex.getMessage(), ex);
        }
    }

    private static GraphWithId parseGraphBlock(String block) {

        Integer id = extractIntField(block, "\"id\"");
        if (id == null) return null;
        GraphWithId g = new GraphWithId(id);

        int nodesIdx = block.indexOf("\"nodes\"");
        if (nodesIdx >= 0) {
            int start = block.indexOf('[', nodesIdx);
            int end = findMatchingBracket(block, start);
            String nodesContent = block.substring(start + 1, end);

            List<String> tokens = simpleSplitStrings(nodesContent);
            for (String t : tokens) {
                g.nodes.add(unquote(t.trim()));
            }
        }

        int edgesIdx = block.indexOf("\"edges\"");
        if (edgesIdx >= 0) {
            int start = block.indexOf('[', edgesIdx);
            int end = findMatchingBracket(block, start);
            String edgesContent = block.substring(start + 1, end);
            List<String> edgeObjs = splitTopLevelObjects(edgesContent);
            for (String eo : edgeObjs) {
                String from = extractStringField(eo, "\"from\"");
                String to = extractStringField(eo, "\"to\"");
                Double weight = extractDoubleField(eo, "\"weight\"");
                if (from != null && to != null && weight != null) {
                    g.edges.add(new EdgeTriple(from, to, weight));
                }
            }
        }

        return g;
    }

    private static Integer extractIntField(String s, String field) {
        int idx = s.indexOf(field);
        if (idx < 0) return null;
        int colon = s.indexOf(':', idx);
        if (colon < 0) return null;
        String rest = s.substring(colon + 1);
        Matcher m = Pattern.compile("\\d+").matcher(rest);
        if (m.find()) return Integer.parseInt(m.group());
        return null;
    }

    private static Double extractDoubleField(String s, String field) {
        int idx = s.indexOf(field);
        if (idx < 0) return null;
        int colon = s.indexOf(':', idx);
        if (colon < 0) return null;
        String rest = s.substring(colon + 1);
        Matcher m = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(rest);
        if (m.find()) return Double.parseDouble(m.group());
        return null;
    }

    private static String extractStringField(String s, String field) {
        int idx = s.indexOf(field);
        if (idx < 0) return null;
        int colon = s.indexOf(':', idx);
        int quote = s.indexOf('"', colon);
        if (quote < 0) return null;
        int endQuote = s.indexOf('"', quote + 1);
        if (endQuote < 0) return null;
        return s.substring(quote + 1, endQuote);
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) return s.substring(1, s.length() - 1);
        return s;
    }

    private static int findMatchingBracket(String s, int pos) {
        char open = s.charAt(pos);
        char close = (open == '[') ? ']' : '}';
        int depth = 0;
        for (int i = pos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static List<String> splitTopLevelObjects(String s) {
        List<String> list = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            while (i < s.length() && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) i++;
            if (i >= s.length()) break;
            if (s.charAt(i) == '{') {
                int j = findMatchingBracket(s, i);
                if (j < 0) break;
                list.add(s.substring(i, j + 1));
                i = j + 1;
            } else {
                int comma = s.indexOf(',', i);
                if (comma < 0) break;
                i = comma + 1;
            }
        }
        return list;
    }

    private static List<String> simpleSplitStrings(String s) {
        List<String> out = new ArrayList<>();
        int i = 0;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
            i++;
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
