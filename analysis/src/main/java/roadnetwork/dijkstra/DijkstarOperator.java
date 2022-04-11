package roadnetwork.dijkstra;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/12 22:09
 */
public class DijkstarOperator {

    /**
     *  shortest path on to on
     * @param startNode
     * @param endNode
     * @return
     */
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
                    frontier.offer(new WeightedNode(newCost, nb));
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

    /**
     *  shortest path on to many
     * @param startNode
     * @param endNodes
     * @return
     */
//    public static List<Edge> findShortestPath(Node startNode, List<Node> endNodes) {
//        List<Integer> endIds = endNodes.stream().map(en -> en.getID()).collect(Collectors.toList());
//        PriorityQueue<WeightedNode> frontier = new PriorityQueue<>(new Comparator<WeightedNode>() {
//            @Override
//            public int compare(WeightedNode o1, WeightedNode o2) {
//                return Double.compare(o1.getWeight(), o2.getWeight());
//            }
//        }); //边界
//        Map<Node, Node> comeFromMap = new HashMap<>(); //路径
//        Map<Node, Double> costMap = new HashMap<>(); //花费
//        frontier.offer(new WeightedNode(0.0, startNode));
//        comeFromMap.put(startNode, null);
//        costMap.put(startNode, 0.0);
//
//        while (!frontier.isEmpty()) {
//            Node current = frontier.poll().getNode();
//
//            if (endIds.contains(current.getID())) {
//                break;
//            }
//
//            //获取相邻节点
//            Iterator neibors = current.getRelated();
//            while (neibors.hasNext()) {
//                Node nb = (Node) neibors.next();
//                //计算起点到当前邻接点的花费
//                double newCost = costMap.get(current) + getCost(current, nb);
//                //如果当前邻接点未访问过，或者最新的路径花费小于之前到达当前邻接点的花费，则插入
//                if (!costMap.containsKey(nb) || newCost < costMap.get(nb)) {
//                    frontier.offer(new WeightedNode(newCost, nb));
//                    comeFromMap.put(nb, current);
//                    costMap.put(nb, newCost);
//                }
//            }
//        }
//
//        List<Edge> edges = new ArrayList<>();
//        Node curNode = endNode;
//        while (curNode.getID() != startNode.getID()) {
//            Node preNode = comeFromMap.get(curNode);
//            edges.add(curNode.getEdge(preNode));
//            curNode = preNode;
//        }
//        return edges;
//    }

    private static double getCost(Node n1, Node n2) {
        double cost = ((Geometry) ((SimpleFeature) n1.getEdge(n2).getObject()).getDefaultGeometry()).getLength();
        return cost;
    }

    @Data
    @AllArgsConstructor
    static class WeightedNode {
        private double weight;
        private Node node;
    }
}
