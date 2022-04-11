package roadnetwork.bfs;

import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;

import java.util.*;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/12 22:27
 */
public class BreadthFirstSearchOperator {

    public static List<Edge> FindPath(Node startNode, Node endNode) {
        Queue<Node> frontier = new ArrayDeque<>(); //边界
        Map<Node, Node> comeFromMap = new HashMap<>(); //路径
        frontier.offer(startNode);
        comeFromMap.put(startNode, null);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            if (current.getID() == endNode.getID()) {
                break;
            }

            Iterator neibors = current.getRelated();
            while (neibors.hasNext()) {
                Node nb = (Node) neibors.next();
                if (!comeFromMap.containsKey(nb)) {
                    frontier.offer(nb);
                    comeFromMap.put(nb, current);
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
}
