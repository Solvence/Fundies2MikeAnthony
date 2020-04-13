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
  private ArrayList<Edge> edgesInMaze;  // allowed to be reinstantiated when creating a new maze
  private WorldScene mazeScene;
  private final int cellSize; // in pixels
  private final ArrayList<Posn> squaresToColor;
  private final ArrayList<Posn> finalPath;
  private final Posn player;
  private final HashMap<Posn, ArrayList<Posn>> mazeMap;
  private boolean isPlayerMode;
  private final ArrayList<Posn> squaresColored;
  private final HashMap<Posn, Posn> playerPath;
  private SpanningTree mazeTree;
  
  Maze(int width, int height, Random r) {
    this.width = width;
    this.height = height;
    this.squaresToColor = new ArrayList<Posn>();
    this.finalPath = new ArrayList<Posn>();
    this.player = new Posn(0, 0);
    this.isPlayerMode = true;
    this.squaresToColor.add(new Posn(0, 0));
    this.squaresColored = new ArrayList<Posn>();
    this.squaresColored.add(new Posn(0, 0));
    this.playerPath = new HashMap<Posn, Posn>();
    
    this.cellSize = Math.min(1250 / this.width, 750 / this.height);

    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < height; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < width; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    this.mazeTree =  new SpanningTree(mazeLocations, new Random(), this.height * this.width);
    this.edgesInMaze = this.mazeTree.getSpanningTree();
    
    this.mazeScene = this.mazeTree.getMazeScene(this.cellSize);
    
    this.mazeMap = new HashMap<Posn, ArrayList<Posn>>();
    
    for (Edge edge : this.edgesInMaze) {
      if (this.mazeMap.get(edge.src) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.dest);
        this.mazeMap.put(edge.src, pathsFromPosn);
      } else {
        this.mazeMap.get(edge.src).add(edge.dest);
      }
      
      if (this.mazeMap.get(edge.dest) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.src);
        this.mazeMap.put(edge.dest, pathsFromPosn);
      } else {
        this.mazeMap.get(edge.dest).add(edge.src);
      }
    }
  }
  
  Maze(int width, int height) {
    this(width, height, new Random());
  }
  
  public WorldScene makeScene() {
    if (this.isPlayerMode) {
      WorldScene mazePlayerScene = this.mazeTree.getMazeScene(this.cellSize);
      for (Posn path : squaresColored) {
        mazePlayerScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          path.x * this.cellSize + this.cellSize / 2 + 1, 
          path.y * cellSize + this.cellSize / 2 + 1);
      }
      for (Posn path : finalPath) {
        mazePlayerScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.BLUE),
          path.x * this.cellSize + this.cellSize / 2 + 1, 
          path.y * cellSize + this.cellSize / 2 + 1);
      }
      mazePlayerScene.placeImageXY(new CircleImage(this.cellSize / 3, OutlineMode.SOLID, Color.RED), 
          player.x * this.cellSize + this.cellSize / 2 + 1, 
          player.y * cellSize + this.cellSize / 2 + 1);
      return mazePlayerScene;
    } else {
      return this.mazeScene;
    }
  }
  
  public void onTick() {
    if (this.squaresToColor.size() > 0) {
      Posn nextCell = this.squaresToColor.remove(0);
      this.mazeScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          nextCell.x * this.cellSize + this.cellSize / 2 + 1, 
          nextCell.y * cellSize + this.cellSize / 2 + 1);
    } else if (!this.finalPath.isEmpty() && !this.isPlayerMode) {
      Posn nextCell = this.finalPath.remove(0);
      this.mazeScene.placeImageXY(new RectangleImage(this.cellSize - 2,
          this.cellSize - 2, OutlineMode.SOLID, Color.BLUE),
          nextCell.x * this.cellSize + this.cellSize / 2 + 1, 
          nextCell.y * cellSize + this.cellSize / 2 + 1);
    }
  }
  
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.isPlayerMode = false;
      this.finalPath.clear();
      this.searchMaze(true);
    } else if (key.equals("d")) {
      this.isPlayerMode = false;
      this.finalPath.clear();
      this.searchMaze(false);
    } else if (key.equals("n")) {
      this.finalPath.clear();
      this.player.x = 0;
      this.player.y = 0;
      this.squaresToColor.clear();
      this.squaresToColor.add(new Posn(player.x, player.y));
      this.isPlayerMode = true;
      this.squaresColored.clear();
      this.squaresColored.add(new Posn(player.x, player.y));
      this.playerPath.clear();
      
      ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
      for (int row = 0; row < height; row += 1) {
        mazeLocations.add(new ArrayList<Posn>());
        for (int col = 0; col < width; col += 1) {
          mazeLocations.get(row).add(new Posn(col, row));
        }
      }
      
      this.mazeTree = new SpanningTree(mazeLocations, new Random(), this.height * this.width);
      this.edgesInMaze = this.mazeTree.getSpanningTree();
      
      this.mazeScene = this.mazeTree.getMazeScene(this.cellSize);
      
      this.mazeMap.clear();
      
      for (Edge edge : this.edgesInMaze) {
        if (this.mazeMap.get(edge.src) == null) {
          ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
          pathsFromPosn.add(edge.dest);
          this.mazeMap.put(edge.src, pathsFromPosn);
        } else {
          this.mazeMap.get(edge.src).add(edge.dest);
        }
        
        if (this.mazeMap.get(edge.dest) == null) {
          ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
          pathsFromPosn.add(edge.src);
          this.mazeMap.put(edge.dest, pathsFromPosn);
        } else {
          this.mazeMap.get(edge.dest).add(edge.src);
        }
      }
    } else if (key.equals("left")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.x < this.player.x && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.x -= 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
        } else if (neighbor.x < this.player.x) {
          this.player.x -= 1;
        }
      }
    } else if (key.equals("right")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.x > this.player.x && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.x += 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
          if (this.player.x == this.width - 1 && this.player.y == this.height - 1) {
            this.reconstruct(playerPath, player);
          }
        } else if (neighbor.x > this.player.x) {
          this.player.x += 1;
        }
      }
    } else if (key.equals("up")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.y < this.player.y && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.y -= 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
        } else if (neighbor.y < this.player.y) {
          this.player.y -= 1;
        }
      }
    } else if (key.equals("down")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.y > this.player.y && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.y += 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
          if (this.player.x == this.width - 1 && this.player.y == this.height - 1) {
            this.reconstruct(playerPath, player);
          }
        } else if (neighbor.y > this.player.y) {
          this.player.y += 1;
        }
      }
    }
  }
  
  ArrayList<Posn> searchMaze(boolean BFS) {
    
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
        ArrayList<Posn> neighbors = this.mazeMap.get(nextItem);
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
      System.out.println(currPosition.x + ", " + currPosition.y);
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
  private final int nodeCount;
  
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, Random r, int nodeCount) {
    this.representatives = new HashMap<Posn, Posn>();
    this.graphLocations = graphLocations;
    this.nodeCount = nodeCount;
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
    System.out.print("Edges Sorted"); 
  }
  
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, int nodeCount) {
    this(graphLocations, new Random(), nodeCount);
  }
  
  ArrayList<Edge> getSpanningTree() {
    int counter = 0;
    while (counter < this.nodeCount - 1) {
      Edge nextEdge = this.worklist.get(0);
      if (this.find(nextEdge.src).equals(this.find(nextEdge.dest))) {
        this.worklist.remove(0);
      } else {
        counter += 1;
        this.edgesInTree.add(nextEdge);
        this.union(nextEdge.src, nextEdge.dest);
        this.worklist.remove(0);
      }
    }
    return this.edgesInTree;
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
  
  WorldScene getMazeScene(int cellSize) {
    int width = graphLocations.get(0).size();
    int height = graphLocations.size();
    WorldScene mazeScene = new WorldScene(width * cellSize, 
        height * cellSize);
    mazeScene.placeImageXY(new RectangleImage(width * cellSize, 
        height * cellSize, 
        OutlineMode.OUTLINE, Color.BLACK), width * cellSize / 2, 
        height * cellSize / 2);
        
    WorldImage WALL = new LineImage(new Posn(0, cellSize), Color.BLACK);
    WorldImage WALL_H = new RotateImage(WALL, 90);
    for (int row = 0; row < height; row += 1) {
      for (int col = 0; col < width; col += 1) {
        
        boolean hasRightWall = true;
        boolean hasDownWall = true;
        for (Edge e : this.edgesInTree) {
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
          mazeScene.placeImageXY(WALL, (col + 1) * cellSize, 
              row * cellSize + cellSize / 2);
        }
        
        if (hasDownWall) {
          mazeScene.placeImageXY(WALL_H, col * cellSize + cellSize / 2, 
              (row + 1) * cellSize);
        }
      }
    }
    
    return mazeScene;
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
  
  
  /*
   * Testing WishList
   * 
   * UnionFind:
   * - There should be length * width - 1 edges in the final list of edges
   * - At the end, all the nodes should connect to the same root node; running
   * find on these edges should return the same result. 
   * - 
   * 
   * Maze:
   * - A new maze can be created without restarting the program. 
   * - A new random maze can be generated
   * 
   * Player:
   * - A Player cannot walk through walls
   * - A Player leaves their previous path behind them
   * - A Player has the same path reconstruction functionality as the built in algorithms
   * 
   * Searching: 
   * - Both Depth First Search and Breadth First Search find the goal
   * - The path Backwards does not contain any cycles and it does contain the start and end points. 
   * -
   * 
   * OnKeyEvent:
   * - While in Player mode (A search has not been initiated), they can move in all four directions
   *  if a wall is not present.
   *  Pressing the left key moves the player left, the right key right, the down key down and the up key up. 
   *  Pressing B initiates a Breadth First Search
   *  Pressing D initiates a Depth First Search
   *  Pressing N initiates a New Random Maze. 
   *  
   *  The User is Notified of the completion of the Game
   *  The Solution Path is Displayed. 
   */
  
  void testWorld(Tester t) {
    new Maze(30, 30).bigBang(1250, 750, 1.0 / 100);
  }
  
  
  
  
}

