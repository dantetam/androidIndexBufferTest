package io.github.dantetam.indexbufferobjecttest;

import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 8/6/2016.
 */
public class TerrainGenerator {

    public Point[][] allPoints;
    public List<Polygon> hexes;

    public TerrainGenerator(int len, float scale) {
        hexagonProcess(len, scale);
    }

    public void hexagonProcess(int len, float scale) {
        allPoints = new Point[len][len];
        hexes = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                float x = scale*i*0.5f, y = scale*j*0.75f;
                if ((j % 2 == 0 && i % 2 == 1) || (j % 2 == 1 && i % 2 == 0)) y -= scale*0.125f;
                else y += scale*0.125f;
                //if (j % 2 == 1) x -= scale*1f;
                allPoints[i][j] = new Point(x, y);
            }
        }
        for (int r = 0; r < len - 2; r += 2) {
            for (int c = 0; c < len - 2; c += 2) {
                List<Point> hex = new ArrayList<>();
                hex.add(allPoints[r][c]);
                hex.add(allPoints[r][c + 1]);
                hex.add(allPoints[r][c + 2]);
                hex.add(allPoints[r + 1][c + 2]);
                hex.add(allPoints[r + 1][c + 1]);
                hex.add(allPoints[r + 1][c]);
                hexes.add(new Polygon(hex));
            }
        }
        for (int r = 1; r < len - 2; r += 2) {
            for (int c = 1; c < len - 2; c += 2) {
                List<Point> hex = new ArrayList<>();
                hex.add(allPoints[r][c]);
                hex.add(allPoints[r][c + 1]);
                hex.add(allPoints[r][c + 2]);
                hex.add(allPoints[r + 1][c + 2]);
                hex.add(allPoints[r + 1][c + 1]);
                hex.add(allPoints[r + 1][c]);
                hexes.add(new Polygon(hex));
            }
        }
    }

    public static int SEGMENTS = 16, SAMPLE_SEGMENT = 4;
    public List<Edge> roughEdge(Edge edge) {
        float[] newEdgeData = new PerlinNoiseLine(870).generatePerlinLine(SEGMENTS, 0.5f, 0.25f);
        //float slope = edge.slope();
        float normalSlope = - 1f / edge.slope();
        float angle = (float) Math.atan(normalSlope);
        //float len = edge.length();
        List<Point> points = new ArrayList<>();
        for (int i = SAMPLE_SEGMENT / 2; i < SEGMENTS; i += SAMPLE_SEGMENT) {
            float mid = (float) i / (float) SEGMENTS;
            Point between = edge.inBetween(mid);
            Point newPoint = between.add(new Point(newEdgeData[i]*(float)Math.cos(angle), newEdgeData[i]*(float)Math.sin(angle)));
            points.add(newPoint);
        }
        points.add(0, edge.edge0());
        points.add(edge.edge1());
        List<Edge> results = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            results.add(new Edge(points.get(i), points.get(i + 1)));
        }
        return results;
    }

    public class Edge {
        private Point edge0, edge1;
        public float slope;
        public Edge(Point p0, Point p1) {
            if (p0.x < p1.x) {
                edge0 = p0;
                edge1 = p1;
            }
            else {
                edge0 = p1;
                edge1 = p0;
            }
            if (edge0.x < edge1.x) {
                slope = (edge1.y - edge0.y) / (edge1.x - edge0.x);
            }
            else {
                slope = (edge1.y - edge0.y) / (edge0.x - edge1.x);
            }
        }
        public Point edge0() {return edge0;}
        public Point edge1() {return edge1;}
        public float slope() {
            return slope;
        }
        public float length() {
            return edge0.dist(edge1);
        }
        public Point inBetween(float percent) {
            float offX = (edge1.x - edge0.x) * percent;
            float offY = (edge1.y - edge0.y) * percent;
            return new Point(edge0.x + offX, edge1.y + offY);
        }
    }

    public class Point {
        public float x,y;
        public Point(float a, float b) {
            x = a; y = b;
        }
        public float dist(Point p) {
            return (float) Math.sqrt((x - p.x)*(x - p.x) + (y - p.y)*(y - p.y));
        }
        public Point add(Point p) {
            return new Point(x + p.x, y + p.y);
        }
    }

    public class Polygon {
        public List<Polygon> neighbors;
        public List<Point> points;
        public List<Edge> edges;
        public Polygon(List<Point> createFromPoints) {
            neighbors = new ArrayList<>();
            points = createFromPoints;
            edges = new ArrayList<>();
            for (int i = 0; i < points.size() - 1; i++) {
                edges.add(new Edge(points.get(i), points.get(i + 1)));
            }
            edges.add(new Edge(points.get(points.size() - 1), points.get(0)));
        }
    }

}
