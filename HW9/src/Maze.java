import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Maze extends World {
  private final int width;
  private final int height;
  private final ArrayList<Edge> edgesInMaze;
  private final WorldScene mazeScene;
  private final int cellSize; // in pixels
  
  Maze(int width, int height, Random r) {
    this.width = width;
    this.height = height;
    
    this.cellSize = Math.min(1250 / this.width, 750 / this.height);
    
    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < height; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < width; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    
    this.edgesInMaze = new SpanningTree(mazeLocations, r).getSpanningTree();
    
    WorldScene mazeSceneInProgress = new WorldScene(this.width * this.cellSize, 
        this.height * this.cellSize);
    mazeSceneInProgress.placeImageXY(new RectangleImage(this.width * this.cellSize, 
        this.height * this.cellSize, 
        OutlineMode.OUTLINE, Color.BLACK), this.width * this.cellSize / 2, 
        this.height * this.cellSize / 2);
        
    WorldImage WALL = new LineImage(new Posn(0, this.cellSize), Color.BLACK);
    WorldImage WALL_H = new RotateImage(WALL, 90);
    for (int row = 0; row < this.height; row += 1) {
      for (int col = 0; col < this.width; col += 1) {
        
        boolean hasRightWall = true;
        boolean hasDownWall = true;
        for (Edge e : this.edgesInMaze) {
          if (e.src.equals(new Posn(col, row)) && e.dest.equals(new Posn(col + 1, row)) || 
              e.dest.equals(new Posn(col, row)) && e.src.equals(new Posn(col + 1, row))) {
            hasRightWall = false;
          }
          if (e.src.equals(new Posn(col, row)) && e.dest.equals(new Posn(col, row + 1)) || 
              e.dest.equals(new Posn(col, row)) && e.src.equals(new Posn(col, row + 1))) {
            hasDownWall = false;
          }
        }
        
        if (hasRightWall) {
          mazeSceneInProgress.placeImageXY(WALL, (col + 1) * this.cellSize, 
              row * this.cellSize + this.cellSize / 2);
        }
        
        if (hasDownWall) {
          mazeSceneInProgress.placeImageXY(WALL_H, col * this.cellSize + this.cellSize / 2, 
              (row + 1) * this.cellSize);
        }
      }
    }
    
    this.mazeScene = mazeSceneInProgress;
  }
  
  Maze(int width, int height) {
    this(width, height, new Random());
  }
  
  public WorldScene makeScene() {
    return this.mazeScene;
  }
}

class SpanningTree {
  
  private final HashMap<Posn, Posn> representatives;
  private final ArrayList<Edge> edgesInTree;
  private final ArrayList<Edge> worklist;  // all edges in graph, sorted by edge weights
  private final ArrayList<ArrayList<Posn>> graphLocations;
  
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, Random r) {
    this.representatives = new HashMap<Posn, Posn>();
    this.graphLocations = graphLocations;
    for (ArrayList<Posn> arr : graphLocations) {
      for (Posn p : arr) {
        this.representatives.put(p, p);
      }
    }
    
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = new ArrayList<Edge>();  
    
    for (int row = 0; row < graphLocations.size(); row += 1) {
      for (int col = 0; col < graphLocations.get(row).size(); col += 1) {
        if (row != graphLocations.size() - 1) {
          worklist.add(new Edge(graphLocations.get(row).get(col), 
              graphLocations.get(row + 1).get(col), r.nextInt(1000)));
        }
        if (col != graphLocations.get(row).size() - 1) {
          worklist.add(new Edge(graphLocations.get(row).get(col), 
              graphLocations.get(row).get(col + 1), r.nextInt(1000)));
        }
      }
    }
    this.worklist.sort(new EdgeComparator());
  }
  
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations) {
    this(graphLocations, new Random());
  }
  
  ArrayList<Edge> getSpanningTree() {
    
    while (this.isNotSpanning()) {
      Edge nextEdge = this.worklist.get(0);
      if (this.find(nextEdge.src).equals(this.find(nextEdge.dest))) {
        this.worklist.remove(0);
      } else {
        this.edgesInTree.add(nextEdge);
        this.union(nextEdge.src, nextEdge.dest);
        this.worklist.remove(0);
      }
    }
    return this.edgesInTree;
  }
  
  boolean isNotSpanning() {
    Posn rootPosn = this.find(graphLocations.get(0).get(0));
    for (ArrayList<Posn> arr : graphLocations) {
      for (Posn p : arr) {
        if (!this.find(p).equals(rootPosn)) {
          return true;
        }
      }
    }
    return false;
  }
  
  Posn find(Posn key) {
    if (representatives.get(key).equals(key)) {
      return key;
    } else {
      return this.find(representatives.get(key));
    }
  }
  
  void union(Posn src, Posn dest) {
    representatives.replace(this.find(src), this.find(dest));
  }
  
  void sortEdges() {
    this.edgesInTree.sort(new EdgeComparator());
  }
}

class Edge implements Comparable<Edge> {
  
  Posn src;
  Posn dest;
  
  int weight;
  
  Edge(Posn src, Posn dest, int weight) {
    this.src = src;
    this.dest = dest;
    this.weight = weight;
  }
  
  // 
  public int compareTo(Edge o) {
    return this.weight - o.weight;
  }
}

class EdgeComparator implements Comparator<Edge> {
  public int compare(Edge o1, Edge o2) {
    return o1.compareTo(o2);
  }
}

class ExamplesMaze {
  void testWorld(Tester t) {
    
    new Maze(100, 60).bigBang(1250, 750, 1.0 / 16);
  }
}

