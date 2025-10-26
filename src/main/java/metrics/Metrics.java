package metrics;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
    private long ufFinds = 0;
    private long ufUnions = 0;
    private long pqOps = 0;
    private long edgesConsidered = 0;
    private long edgePushes = 0;
    private long timeMs = 0;

    public void incrUFFind() { ufFinds++; }
    public void incrUFUnion() { ufUnions++; }
    public void incrPQOp() { pqOps++; }
    public void incrEdgesConsidered() { edgesConsidered++; }
    public void incrEdgePushes() { edgePushes++; }

    public void setTimeMs(long ms) { timeMs = ms; }

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("uf_finds", ufFinds);
        m.put("uf_unions", ufUnions);
        m.put("pq_ops", pqOps);
        m.put("edges_considered", edgesConsidered);
        m.put("edge_pushes", edgePushes);
        m.put("execution_time_ms", timeMs);
        return m;
    }

    public long getUfFinds() { return ufFinds; }
    public long getUfUnions() { return ufUnions; }
    public long getPqOps() { return pqOps; }
    public long getEdgesConsidered() { return edgesConsidered; }
    public long getEdgePushes() { return edgePushes; }
    public long getTimeMs() { return timeMs; }
}
