package trajectory.mapmatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.geotools.graph.structure.Node;
import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.sylab.geolego.index.rtree.RTreeIndexOper;
import org.sylab.geolego.model.utils.GeoFunction;
import org.sylab.geolego.model.utils.SpatialProjectUtils;
import roadnetwork.model.jtsgraph.JTSRoadNetwork;
import roadnetwork.model.jtsgraph.JTSRoadSegment;
import roadnetwork.model.jtsgraph.JTSRoutePath;
import trajectory.Trajectory;
import trajectory.GPSPoint;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Sui Yuan
 * @Description: trajectory map match helper
 * @Date: 2022/4/7 18:21
 * @since:
 **/
public class TrajectoryMapMatcher {

    private JTSRoadNetwork roadNetwork;

    private Trajectory trajectory;

    private double searchDistance;

    private List<CandidatePoint> prevCandidates;

    private Map<CandidatePoint, Double> allCandidateScore;

    private HmmProbabilities hmmProbabilities = new HmmProbabilities();


    public TrajectoryMapMatcher(JTSRoadNetwork roadNetwork, Trajectory trajectory, double searchDistance) {
        Assert.assertNotNull(roadNetwork);
        Assert.assertNotNull(trajectory);
        Assert.assertTrue(searchDistance > 0);
        this.roadNetwork = roadNetwork;
        this.trajectory = trajectory;
        this.searchDistance = searchDistance;
        this.allCandidateScore = new HashMap<>();
    }

    public List<CandidatePoint> hmmMapMatch() {
        //1. loop trajectory points find candidates
        RTreeIndexOper rtree = roadNetwork.getRTree();
        List<GPSPoint> allObservations = trajectory.getGPSPoints();
        allObservations.remove(0);
        for (GPSPoint observation : allObservations) {
            //find all observation's candidates
            List<CandidatePoint> candidatePoints = findCandidateByObservation(rtree, observation);
            //no candidate will break the chain(optimize the logic later)
            Assert.assertNotNull(candidatePoints);
            if (prevCandidates == null) {
                prevCandidates = new ArrayList<>(allObservations.size());
                //init emission probability
//                initObservationProb(candidatePoints);
                initObservationProbNew(candidatePoints);
                continue;
            }
            //step next observation
            step(candidatePoints);
//            stepNew(candidatePoints);
        }
        //2. find most probability candidate points
        return getMostProbabilityPoints();
    }

    /**
     * get map matched roads
     *
     * @return
     */
    public List<JTSRoadSegment> hmmMapMatchRoad() {
        List<CandidatePoint> candidatePoints = hmmMapMatch();
        List<JTSRoadSegment> roadSegments = new LinkedList<>();
        JTSRoadSegment preRs = null;
        for (CandidatePoint candidatePoint : candidatePoints) {
            JTSRoadSegment roadSegment = candidatePoint.getRoadSegment();
            if (preRs != null && preRs.getId() == roadSegment.getId()) {
                continue;
            }
            preRs = roadSegment;
            roadSegments.add(roadSegment);
        }
        return roadSegments;
    }

    private List<CandidatePoint> findCandidateByObservation(RTreeIndexOper rtree, GPSPoint observation) {
        Point raw = observation.getRaw();
        Geometry bufferPlg = raw.buffer(GeoFunction.getDegreeFromM(searchDistance));
        List<Geometry> results = rtree.searchIntersectEstmate(bufferPlg);//rtree.searchIntersect(bufferPlg, false);
        if (results == null || results.size() == 0) {
            return null;
        }
        List<CandidatePoint> candidatePoints = new ArrayList<>();
        List<JTSRoadSegment> candidateRS = results.stream().map(geometry -> (JTSRoadSegment) geometry.getUserData()).collect(Collectors.toList());
        for (JTSRoadSegment candidateRoad : candidateRS) {
            //find project point
            SpatialProjectUtils.ProjectPoint projectPoint = SpatialProjectUtils.project(candidateRoad.getLineString(), observation.getRaw());
            candidatePoints.add(new CandidatePoint()
                    .setRaw(projectPoint.getRaw())
                    .setObservationGPSPoint(observation)
                    .setOffsetLengthInM(projectPoint.getOffsetLength())
                    .setProjectDistanceInM(projectPoint.getDistance())
                    .setRoadSegment(candidateRoad));
        }
        return candidatePoints;
    }

    private void initObservationProb(List<CandidatePoint> candidates) {
        //compute emission probability
        for (CandidatePoint candidate : candidates) {
            double emProb = hmmProbabilities.emissionLogProbability(candidate.getProjectDistanceInM());
            if (emProb > Double.NEGATIVE_INFINITY) {
                this.prevCandidates.add(candidate);
                this.allCandidateScore.put(candidate, emProb);
            }
        }
    }

    private void initObservationProbNew(List<CandidatePoint> candidates) {
        //compute emission probability
        for (CandidatePoint candidate : candidates) {
//            double emProb = hmmProbabilities.emissionLogProbability(candidate.getProjectDistanceInM());
//            if (emProb > Double.NEGATIVE_INFINITY) {
            this.prevCandidates.add(candidate);
            this.allCandidateScore.put(candidate, 0.0);
//            }
        }
    }

    private void step(List<CandidatePoint> candidates) {
        for (CandidatePoint curCandidate : candidates) {
            double maxAccumulateProb = Double.NEGATIVE_INFINITY;
            CandidatePoint maxAccumulateProbPrevCandidate = null;

            for (CandidatePoint prevCandidate : prevCandidates) {
                double prevProb = allCandidateScore.get(prevCandidate);
                double candRouteLength = computeRouteLengthDirectDup(prevCandidate, curCandidate);
                //判断可达后，再计算
                if (candRouteLength < Double.POSITIVE_INFINITY) {
                    double observDistance = GeoFunction.getDistanceInM(prevCandidate.getObservationGPSPoint().getRaw(), curCandidate.getObservationGPSPoint().getRaw());
                    double timeDiff = curCandidate.getObservationGPSPoint().getTimestamp().getTime() - prevCandidate.getObservationGPSPoint().getTimestamp().getTime();
                    double accumulateProb = prevProb + hmmProbabilities.transitionLogProbability(candRouteLength, observDistance, timeDiff);
                    if (accumulateProb > maxAccumulateProb) {
                        maxAccumulateProb = accumulateProb;
                        maxAccumulateProbPrevCandidate = prevCandidate;
                    }
                }
            }
            Assert.assertNotNull(maxAccumulateProbPrevCandidate);
            //compute current candidate probability
            double curProb = maxAccumulateProb + hmmProbabilities.emissionLogProbability(curCandidate.getProjectDistanceInM());
            curCandidate.setPrevMaxProbCandidate(maxAccumulateProbPrevCandidate);
            allCandidateScore.put(curCandidate, curProb);
        }
        this.prevCandidates = candidates;
    }

    private void stepNew(List<CandidatePoint> candidates) {
        for (CandidatePoint curCandidate : candidates) {
            double maxAccumulateProb = Double.NEGATIVE_INFINITY;
            CandidatePoint maxAccumulateProbPrevCandidate = null;

            for (CandidatePoint prevCandidate : prevCandidates) {
                double prevProb = allCandidateScore.get(prevCandidate);
//                double candRouteLength = computeRouteLength(prevCandidate, curCandidate);
                double candRouteLength = computeRouteLengthDirectDup(prevCandidate, curCandidate);
                //判断可达后，再计算
                if (candRouteLength < Double.POSITIVE_INFINITY) {
                    double candidateDistance = GeoFunction.getDistanceInM(prevCandidate.getRaw(), curCandidate.getRaw());
                    double timeDiff = curCandidate.getObservationGPSPoint().getTimestamp().getTime() - prevCandidate.getObservationGPSPoint().getTimestamp().getTime();
//                    Assert.assertNotNull(maxAccumulateProbPrevCandidate);
                    //compute current candidate probability
                    double curProb = prevProb
                            + hmmProbabilities.transitionLogProbability(candRouteLength, candidateDistance, timeDiff)
                            + hmmProbabilities.emissionLogProbability(prevCandidate.getProjectDistanceInM());

                    //debug
//                    System.out.println(curCandidate.getObservationGPSPoint().getTimestamp().getTime() + "|"
//                            + curCandidate.getRoadSegment().getOid() + "|" + WKTUtils.write(curCandidate.getRaw()) + "|"
//                            + prevCandidate.getRoadSegment().getOid() + "|" + WKTUtils.write(prevCandidate.getRaw()) + "|" + candRouteLength + "|" + curProb);
                    if (curProb > maxAccumulateProb) {
                        maxAccumulateProb = curProb;
                        maxAccumulateProbPrevCandidate = prevCandidate;
                    }
                }
            }
            curCandidate.setPrevMaxProbCandidate(maxAccumulateProbPrevCandidate);
            allCandidateScore.put(curCandidate, maxAccumulateProb);

            //debug
//            System.out.println(new StatStates(curCandidate.getObservationGPSPoint().getTimestamp().getTime()
//                    , curCandidate.getRoadSegment().getOid(), WKTUtils.write(curCandidate.getRaw())
//                    , maxAccumulateProb
//                    , maxAccumulateProbPrevCandidate.getRoadSegment().getOid()
//                    , WKTUtils.write(maxAccumulateProbPrevCandidate.getRaw())));
        }
        this.prevCandidates = candidates;
    }

    private double computeRouteLength(CandidatePoint candidatePointA, CandidatePoint candidatePointB) {
        JTSRoadSegment roadSegmentA = candidatePointA.getRoadSegment();
        JTSRoadSegment roadSegmentB = candidatePointB.getRoadSegment();
        double routeDistance = Double.POSITIVE_INFINITY;
        if (roadSegmentA.getId() == roadSegmentB.getId()) {
            //project on same road
            routeDistance = Math.abs(candidatePointB.getOffsetLengthInM() - candidatePointA.getOffsetLengthInM());
        } else if (roadSegmentA.getToVertex().equals(roadSegmentB.getFromVertex())) {
            // project on adjacent road, same direction with road
            double roadALength = roadSegmentA.getLength();
            routeDistance = candidatePointB.getOffsetLengthInM() + (roadALength - candidatePointA.getOffsetLengthInM());
        } else if (roadSegmentB.getToVertex().equals(roadSegmentA.getFromVertex())) {
            //project on adjacent road, diff direction with road
            double roadBLength = roadSegmentB.getLength();
            routeDistance = candidatePointA.getOffsetLengthInM() + (roadBLength - candidatePointB.getOffsetLengthInM());
        } else {
            Node roadAToNode = roadSegmentA.getToVertex().getRaw();
            Node roadBFromNode = roadSegmentB.getFromVertex().getRaw();
            JTSRoutePath routePath = this.roadNetwork.shortestPath(roadAToNode, roadBFromNode);
            double roadSegLength = routePath.getLength();
            //reture if disconnected
            if (roadSegLength == Double.POSITIVE_INFINITY) {
                return routeDistance;
            }
            if (routePath.getStartRoad().getId() == roadSegmentA.getId()) {
                //如果最短路径起始路段和roadSegmentA相同，说明roadAToNode出发方向与路的方向相反，需要roadSegLength-roadALength，再加上candidatePointA.getOffsetLengthInM()
                routeDistance = roadSegLength - roadSegmentA.getLength() + candidatePointA.getOffsetLengthInM();
            } else {
                //如果最短路径起始路段和roadSegmentA不同，说明roadAToNode出发方向与路的方向相同，需要roadALength - candidatePointA.getOffsetLengthInM()，再加上roadSegLength
                routeDistance = roadSegLength + (roadSegmentA.getLength() - candidatePointA.getOffsetLengthInM());
            }
            if (routePath.getEndRoad().getId() == roadSegmentB.getId()) {
                //如果最短路径终止路段和roadSegmentB相同，说明回到roadBFromNode的方向和路的方向相同，需要roadBLength - candidatePointB.getOffsetLengthInM()，再加上routeDistance
                routeDistance = routeDistance + (roadSegmentB.getLength() - candidatePointB.getOffsetLengthInM());
            } else {
                //如果最短路径起始路段和roadSegmentB不同，说明roadBFromNode出发方向与路的方向相反，需要routeDistance加上candidatePointB.getOffsetLengthInM()
                routeDistance = routeDistance + candidatePointB.getOffsetLengthInM();
            }
        }
        return routeDistance;
    }

    private double computeRouteLengthDirectDup(CandidatePoint candidatePointA, CandidatePoint candidatePointB) {
        JTSRoadSegment roadSegmentA = candidatePointA.getRoadSegment();
        JTSRoadSegment roadSegmentB = candidatePointB.getRoadSegment();
        double routeDistance = Double.POSITIVE_INFINITY;
        if (roadSegmentA.getId() == roadSegmentB.getId() && candidatePointB.getOffsetLengthInM() >= candidatePointA.getOffsetLengthInM()) {
            //project on same road
            routeDistance = candidatePointB.getOffsetLengthInM() - candidatePointA.getOffsetLengthInM();
        } else if (roadSegmentA.getToVertex().equals(roadSegmentB.getFromVertex())) {
            // project on adjacent road, same direction with road
            double roadALength = roadSegmentA.getLength();
            routeDistance = candidatePointB.getOffsetLengthInM() + (roadALength - candidatePointA.getOffsetLengthInM());
        } else {
            Node roadAToNode = roadSegmentA.getToVertex().getRaw();
            Node roadBFromNode = roadSegmentB.getFromVertex().getRaw();
            JTSRoutePath routePath = this.roadNetwork.shortestPath(roadAToNode, roadBFromNode);
            double roadSegLength = routePath.getLength();
            routeDistance = roadSegLength + (roadSegmentA.getLength() - candidatePointA.getOffsetLengthInM()) + candidatePointB.getOffsetLengthInM();
        }
        return routeDistance;
    }

    private List<CandidatePoint> getMostProbabilityPoints() {
        List<CandidatePoint> finalPath = new LinkedList<>();
        double mostProb = Double.NEGATIVE_INFINITY;
        CandidatePoint mostProbCandidate = null;
        for (CandidatePoint candidate : this.prevCandidates) {
            double prob = this.allCandidateScore.get(candidate);
            if (prob > mostProb) {
                mostProb = prob;
                mostProbCandidate = candidate;
            }
        }
        Assert.assertNotNull(mostProbCandidate);
        CandidatePoint maxProbPrevCandidate = mostProbCandidate.getPrevMaxProbCandidate();
        finalPath.add(mostProbCandidate);
        while (maxProbPrevCandidate != null) {
            finalPath.add(maxProbPrevCandidate);
            maxProbPrevCandidate = maxProbPrevCandidate.getPrevMaxProbCandidate();
        }
        return finalPath;
    }

    @Data
    @AllArgsConstructor
    static class StatStates {
        private long gpsTime;
        private int roadId;
        private String wkt;
        private double score;
        private int prevRoadId;
        private String prevWkt;

        @Override
        public String toString() {
            return gpsTime + "," + roadId + "," + wkt + "," + score + "," + prevRoadId + "," + prevWkt;
        }
    }

}
