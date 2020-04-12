import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ArrayDeque;

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
  private final ArrayList<Posn> squaresToColor;
  private final ArrayList<Posn> finalPath;
  
  Maze(int width, int height, Random r) {
    this.width = width;
    this.height = height;
    this.squaresToColor = new ArrayList<Posn>();
    finalPath = new ArrayList<Posn>();
    
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
  
  public void onTick() {
    if (this.squaresToColor.size() > 0) {
      Posn nextCell = this.squaresToColor.remove(0);
      this.mazeScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          nextCell.x * this.cellSize + this.cellSize / 2, 
          nextCell.y * cellSize + this.cellSize / 2);
    } else if (!this.finalPath.isEmpty()) {
      Posn nextCell = this.finalPath.remove(0);
      this.mazeScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.BLUE),
          nextCell.x * this.cellSize + this.cellSize / 2, 
          nextCell.y * cellSize + this.cellSize / 2);
    }
  }
  
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.searchMaze(true);
    } else if (key.equals("d")) {
      this.searchMaze(false);
    }
  }
  
  ArrayList<Posn> searchMaze(boolean BFS) {
    
    HashMap<Posn, ArrayList<Posn>> mazeMap = new HashMap<Posn, ArrayList<Posn>>();
    
    for (Edge edge : this.edgesInMaze) {
      if (mazeMap.get(edge.src) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.dest);
        mazeMap.put(edge.src, pathsFromPosn);
      } else {
        mazeMap.get(edge.src).add(edge.dest);
      }
      
      if (mazeMap.get(edge.dest) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.src);
        mazeMap.put(edge.dest, pathsFromPosn);
      } else {
        mazeMap.get(edge.dest).add(edge.src);
      }
    }
    
    ArrayDeque<Posn> worklist = new ArrayDeque<Posn>(); // A Queue or a Stack, depending on the algorithm
    worklist.add(new Posn(0,0));
    
    HashMap<Posn, Boolean> visitedCells = new HashMap<Posn, Boolean>();
    HashMap<Posn, Posn> cameFromPosn = new HashMap<Posn, Posn>();
    
    //BFS
    while (worklist.size() > 0) {
      
      Posn nextItem = worklist.getFirst();
      worklist.removeFirst();
      
      if (nextItem.equals(new Posn(this.width - 1, this.height - 1))){
        squaresToColor.add(nextItem);
        return this.reconstruct(cameFromPosn, nextItem);
      } else if (visitedCells.get(nextItem) == null) {
        squaresToColor.add(nextItem);
        visitedCells.put(nextItem, true);
        ArrayList<Posn> neighbors = mazeMap.get(nextItem);
        for (Posn neighbor : neighbors) {
          if (visitedCells.get(neighbor) == null) {
            cameFromPosn.put(neighbor, nextItem);
            if (BFS) {
              worklist.addLast(neighbor);
            } else {
              worklist.addFirst(neighbor);
            }
          }
        }
      } 
    }
    
    
    throw new RuntimeException("No Path Found");
     
  }
  
  ArrayList<Posn> reconstruct(HashMap<Posn, Posn> cameFromPosn, Posn nextItem){
    ArrayList<Posn> steps = new ArrayList<Posn>();
    Posn currPosition = new Posn(nextItem.x, nextItem.y);
    while(!currPosition.equals(new Posn(0,0))) {
      steps.add(new Posn(currPosition.x, currPosition.y));
      this.finalPath.add(new Posn(currPosition.x, currPosition.y));
      currPosition = cameFromPosn.get(currPosition);
    }
    steps.add(new Posn(0, 0));
    this.finalPath.add(new Posn(0, 0));
    return steps;
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
    
    new Maze(100, 60).bigBang(1250, 750, 1.0 / 28);
  }
}

