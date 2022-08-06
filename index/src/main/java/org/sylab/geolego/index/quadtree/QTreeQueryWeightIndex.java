package org.sylab.geolego.index.quadtree;

import lombok.extern.slf4j.Slf4j;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.sylab.geolego.index.model.QueryParams;
import org.sylab.geolego.index.utils.BaseQueryOperator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * @author : suiyuan
 * @description : 四叉树查询权重索引
 * @date : Created in 2019-10-19 17:08
 * @modified by :
 **/
@Slf4j
public class QTreeQueryWeightIndex {
    /**
     * 根节点
     */
    protected QTreeNode root;

    /**
     * 整体范围 ( 尽量保持长宽一致)
     */
    protected QTreeBBox entireRange;

    /**
     * 起始级别节点列表
     */
    protected List<QTreeNode> startLevelNodeList;

    /**
     * 节点最小级别
     */
    protected int bottomLevel;

    /**
     * 最小长度
     */
    protected double minLength;

    /**
     * 最大长度
     */
    protected double maxLength;

    /**
     * 返回值数量阈值
     */
    protected int resultThreshold;

    /**
     * 查询操作类
     */
    protected BaseQueryOperator queryOperator;

    /**
     * 几何字段
     */
    protected String geomField;

    /**
     * 时间字段
     */
    protected String timeField;

    /**
     * 起始时间
     */
    protected String startTime;

    /**
     * 中止时间
     */
    protected String endTime;

    public QTreeQueryWeightIndex(BaseQueryOperator queryOperator, String geomField, String timeField, String startTime, String endTime
            , QTreeBBox entireRange, double minLength, double maxLength, int resultThreshold) {
        this.queryOperator = queryOperator;
        this.geomField = geomField;
        this.timeField = timeField;
        this.startTime = startTime;
        this.endTime = endTime;
        this.entireRange = entireRange;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.resultThreshold = resultThreshold;
        startLevelNodeList = new LinkedList<>();
    }


    /**
     * 生成一棵到上边界的满四叉树
     */
    public void initTree() {
        root = new QTreeNode(null, entireRange, 0, "0");
        bottomLevel = getBottomLevel(root);
        root = new QTreeNode(null, entireRange, 0, "0");
        buildQTree(root);
    }

    /**
     * 获取最低级别
     *
     * @param node 节点
     * @return 最低级别
     */
    public int getBottomLevel(QTreeNode node) {
        if (node.getWidth() * node.getHeight() < minLength * minLength) {
            return node.getLevel();
        }
        node.split();
        return getBottomLevel(node.getChildren()[0]);
    }

    /**
     * 递归生成树
     *
     * @param node 节点
     */
    protected void buildQTree(QTreeNode node) {
        //如果节点面积抵达上限，则加入起始节点列表并退出
        if (node.getWidth() * node.getHeight() < maxLength * maxLength) {
            log.info(String.format("到达起始级别:%s width:%f height:%f", node.getLevel(), node.getWidth(), node.getHeight()));
            startLevelNodeList.add(node);
            return;
        }
        node.split();
        log.info(String.format("第%s级分裂", node.getLevel()));
        QTreeNode[] children = node.getChildren();
        for (int i = 0; i < 4; i++) {
            buildQTree(children[i]);
        }
    }

    /**
     * 生成权重树
     */
    public void buildWightTree() {
        if (startLevelNodeList == null) {
            log.error("没有初始化树！");
            return;
        }
        log.info("开始分裂...");
        for (QTreeNode node : startLevelNodeList) {
            try {
                dividNode(node);
            } catch (IOException | CQLException e) {
                log.error(e.getMessage());
            }
        }
        log.info("分裂完成");
    }

    /**
     * 并行生成权重树
     *
     * @param partNum 并行度
     */
    public void buildWightTreeMultiple(int partNum) {
        if (startLevelNodeList == null) {
            log.error("没有初始化树！");
            return;
        }
        log.info("开始分裂...");
        ForkJoinPool forkJoinPool = new ForkJoinPool(partNum);
        forkJoinPool.submit(() ->
                startLevelNodeList.parallelStream().forEach(node -> {
                    try {
                        dividNode(node);
                    } catch (IOException | CQLException e) {
                        log.error(e.getMessage());
                    }
                })).join();

        log.info("分裂完成");
    }

    /**
     * 分裂节点
     *
     * @param node 节点
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    protected void dividNode(QTreeNode node) throws IOException, CQLException {
        long count = queryNode(node);
        log.info("node: " + node.getBbox() + "count: " + count);
        //如果返回记录数小于阈值，或者，节点长或宽小于长度下限，则停止分裂
        if (count < resultThreshold || node.getWidth() * node.getHeight() < minLength * minLength) {
            log.info("area: " + node.getWidth() * node.getHeight() + "return！");
            return;
        }
        node.split();
        QTreeNode[] children = node.getChildren();
        for (int i = 0; i < 4; i++) {
            dividNode(children[i]);
        }
    }

    /**
     * 查询节点范围内数据
     *
     * @param node 节点
     * @return 节点内数据个数
     * @throws IOException  IOException
     * @throws CQLException CQLException
     */
    protected long queryNode(QTreeNode node) throws IOException, CQLException {
        QueryParams queryParams = new QueryParams.QueryParamsBuilder(geomField, timeField, node.getMidLng(), node.getMidLat(), startTime, endTime).build();

        //todo: 暂用面积开根号偏移
        List<SimpleFeature> list = queryOperator.querySpatialTemporalDistance(queryParams, Math.sqrt(node.getWidth() * node.getHeight()) * 100000 / 2);
        if (list == null || list.size() == 0) {
            node.setWeight(0);
            return 0;
        }
        log.info("****************count: " + list.size() + "*************************");
        node.setWeight(list.size());
        return list.size();
    }

    /**
     * 获取对应点的查询范围
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 缓冲距离
     */
    public double searchQueryDis(double lng, double lat) {
        if (!getRoot().getBbox().contains(lng, lat)) {
            log.warn(String.format("点 %f %f 不在范围内！", lng, lat));
            return 0;
        }
        QTreeNode node = queryNode(lng, lat, getRoot());
        if (node == null) {
            return 0;
        }
        return Math.sqrt(node.getWidth() * node.getHeight());
    }


    /**
     * 获取最低层code
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 最低层code
     */
    public String getBottomCode(double lng, double lat) {
        QTreeNode node = new QTreeNode(null, entireRange, 0, "0");
        while (node.getLevel() > bottomLevel) {
            int code = node.getCode(lng, lat);
            node = node.getChildren()[code];
        }
        return node.getCode();
    }

    /**
     * 找到坐标对应的节点
     *
     * @param lng  经度
     * @param lat  纬度
     * @param node 节点
     * @return 目标节点
     */
    public QTreeNode queryNode(double lng, double lat, QTreeNode node) {
        if (node == null) {
            return null;
        }
        if (node.hasChild()) {
            QTreeNode[] children = node.getChildren();
            for (QTreeNode child : children) {
                if (child.getBbox().contains(lng, lat)) {
                    if (child.hasChild()) {
                        return queryNode(lng, lat, child);
                    } else {
                        return child;
                    }
                }
            }
        }
        return node;
    }

    /**
     * 根节点
     */
    public QTreeNode getRoot() {
        return root;
    }
}
