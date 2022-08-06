package roadnetwork.astar;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.graph.GraphWalk;
import org.locationtech.jts.geom.Point;
import roadnetwork.model.jgrapht.JgRoadSegment;
import roadnetwork.model.jgrapht.JgRoadVertex;

import java.util.*;

/**
 * @author Sui Yuan
 * @description
 * @date 2022/5/6 1:59
 */
public class AStarOperatorJgrapht implements ShortestPathAlgorithm<JgRoadVertex, JgRoadSegment> {

    private final Graph<JgRoadVertex, JgRoadSegment> graph;

    public AStarOperatorJgrapht(Graph<JgRoadVertex, JgRoadSegment> graph) {
        this.graph = graph;
    }

    @Override
    public GraphPath<JgRoadVertex, JgRoadSegment> getPath(JgRoadVertex source, JgRoadVertex sink) {
        PriorityQueue<WeightedVertex> frontier = new PriorityQueue<>(Comparator.comparingDouble(WeightedVertex::getWeight));
        Map<JgRoadVertex, JgRoadVertex> comeFromMap = new HashMap<>();
        Map<JgRoadVertex, Double> costMap = new HashMap<>();
        frontier.offer(new WeightedVertex(0.0, source));
        comeFromMap.put(source, null);
        costMap.put(source, 0.0);

        while (!frontier.isEmpty()) {
            JgRoadVertex current = frontier.poll().getVertex();

            if (current.getRsid() == sink.getRsid()) {
                break;
            }

            //获取相邻节点
            Set<JgRoadSegment> outEdgeSet = graph.outgoingEdgesOf(current);
            for (JgRoadSegment jgRoadSegment : outEdgeSet) {
                JgRoadVertex nbVertex = jgRoadSegment.getAnotherVertex(current);
                //计算起点到当前邻接点的花费
                double newCost = costMap.get(current) + jgRoadSegment.getLength();
                //如果当前邻接点未访问过，或者最新的路径花费小于之前到达当前邻接点的花费，则插入
                if (!costMap.containsKey(nbVertex) || newCost < costMap.get(nbVertex)) {
                    frontier.offer(new WeightedVertex(newCost + getEstimate(nbVertex, sink), nbVertex));
                    comeFromMap.put(nbVertex, current);
                    costMap.put(nbVertex, newCost);
                }
            }
        }
        double weight = 0.0;
        List<JgRoadSegment> segments = new ArrayList<>();
        JgRoadVertex curVertex = sink;
        while (curVertex.getRsid() != source.getRsid()) {
            JgRoadVertex preNode = comeFromMap.get(curVertex);
            JgRoadSegment curSegment = this.graph.getEdge(preNode, curVertex);
            segments.add(curSegment);
            weight += curSegment.getLength();
            curVertex = preNode;
        }
        Collections.reverse(segments);
        return new GraphWalk<>(this.graph, source, sink, segments, weight);
    }

    @Override
    public double getPathWeight(JgRoadVertex source, JgRoadVertex sink) {
        GraphPath path = getPath(source, sink);
        return path.getWeight();
    }

    @Override
    public SingleSourcePaths<JgRoadVertex, JgRoadSegment> getPaths(JgRoadVertex source) {
        throw new UnsupportedOperationException("暂不支持此方法");
    }

    /**
     * 获取预估花费
     *
     * @param n1
     * @param n2
     * @return
     */
    private double getEstimate(JgRoadVertex n1, JgRoadVertex n2) {
        Point pt1 = n1.getRaw();
        Point pt2 = n2.getRaw();

        return pt1.distance(pt2);
    }


    @Data
    @AllArgsConstructor
    static class WeightedVertex {
        private double weight;
        private JgRoadVertex vertex;
    }
}
