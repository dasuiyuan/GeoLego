package roadnetwork.astar;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import roadnetwork.dijkstra.DijkstarOperator;

import java.util.*;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/13 0:59
 */
public class AStarOperator {
    public static List<Edge> findShortestPath(Node startNode, Node endNode) {
        PriorityQueue<WeightedNode> frontier = new PriorityQueue<>(new Comparator<WeightedNode>() {
            @Override
            public int compare(WeightedNode o1, WeightedNode o2) {
                return Double.compare(o1.getWeight(), o2.getWeight());
            }
        }); //边界
        Map<Node, Node> comeFromMap = new HashMap<>(); //路径
        Map<Node, Double> costMap = new HashMap<>(); //花费
        frontier.offer(new WeightedNode(0.0, startNode));
        comeFromMap.put(startNode, null);
        costMap.put(startNode, 0.0);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll().getNode();

            if (current.getID() == endNode.getID()) {
                break;
            }

            //获取相邻节点
            Iterator neibors = current.getRelated();
            while (neibors.hasNext()) {
                Node nb = (Node) neibors.next();
                //计算起点到当前邻接点的花费
                double newCost = costMap.get(current) + getCost(current, nb);
                //如果当前邻接点未访问过，或者最新的路径花费小于之前到达当前邻接点的花费，则插入
                if (!costMap.containsKey(nb) || newCost < costMap.get(nb)) {
                    //todo：在优先队列内添加启发预估值
                    frontier.offer(new WeightedNode(newCost + getEstimate(nb, endNode), nb));
                    comeFromMap.put(nb, current);
                    costMap.put(nb, newCost);
                }
            }
        }

        List<Edge> edges = new ArrayList<>();
        Node curNode = endNode;
        while (curNode.getID() != startNode.getID()) {
            Node preNode = comeFromMap.get(curNode);
            edges.add(curNode.getEdge(preNode));
            curNode = preNode;
        }
        return edges;
    }

    //获取两个节点间的花费
    private static double getCost(Node n1, Node n2) {
        double cost = ((Geometry) ((SimpleFeature) n1.getEdge(n2).getObject()).getDefaultGeometry()).getLength();
        return cost;
    }

    /**
     * 获取预估花费
     *
     * @param n1
     * @param n2
     * @return
     */
    private static double getEstimate(Node n1, Node n2) {
        Point pt1 = (Point) n1.getObject();
        Point pt2 = (Point) n2.getObject();

        return pt1.distance(pt2);
    }

    @Data
    @AllArgsConstructor
    static class WeightedNode {
        private double weight;
        private Node node;
    }
}
