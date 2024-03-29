package roadnetwork;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/12 11:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadGraphProperty {
    /**
     * 方向字段
     */
    private String directionField;

    /**
     * 路径花费字段
     */
    private String costField;

    /**
     *  是否为有向图
     */
    private boolean directed;

}
