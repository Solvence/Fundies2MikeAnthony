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
  private final Random r;
  
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
    this.r = r;
    
    this.cellSize = Math.min(1250 / this.width, 750 / this.height);

    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < height; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < width; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    this.mazeTree =  new SpanningTree(mazeLocations, this.r);
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
      
      this.mazeTree = new SpanningTree(mazeLocations, this.r);
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
  
  // computes the path from the top left corner to the bottom right corner of the maze
  // EFFECT: modifies squaresToColor to contain all the positions that the searching algorithm went
  // through before finding the solution. Modifies the finalPath field to contain all the positions
  // in the solution path
  ArrayList<Posn> searchMaze(boolean BFS) {
    
    ArrayDeque<Posn> worklist = new ArrayDeque<Posn>(); // A Queue or a Stack, depending on the 
    // algorithm
    worklist.add(new Posn(0,0));
    
    HashMap<Posn, Boolean> visitedCells = new HashMap<Posn, Boolean>();
    HashMap<Posn, Posn> cameFromPosn = new HashMap<Posn, Posn>();
    
    //BFS
    while (worklist.size() > 0) {
      
      Posn nextItem = worklist.getFirst();
      worklist.removeFirst();
      
      if (nextItem.equals(new Posn(this.width - 1, this.height - 1))){
        this.squaresToColor.add(nextItem);
        return this.reconstruct(cameFromPosn, nextItem);
      } else if (visitedCells.get(nextItem) == null) {
        this.squaresToColor.add(nextItem);
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
  
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, Random r) {
    this.representatives = new HashMap<Posn, Posn>();
    this.graphLocations = graphLocations;
    int nodes = 0;
    for (ArrayList<Posn> arr : graphLocations) {
      nodes += arr.size();
    }
    this.nodeCount = nodes;
    
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
  
  // Computes the minimum spanning tree using Kruskal's algorithm on the worklist of weighted edges
  // EFFECT: removes enough nodes from the worklist to achieve a spanning tree, modifies the
  // edgesInTree to contain all the edges in the resulting spanning tree, and modifies the
  // representatives hashmap to represent a spanning tree, where every node's root representative is
  // the same
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
  
  // Computes the root of the tree with the given Posn in it
  // this method is private as users of the SpanningTree class should not be able to call this
  // method directly as it only deals with private fields in this class
  private Posn find(Posn key) {
    if (representatives.get(key).equals(key)) {
      return key;
    } else {
      return this.find(representatives.get(key));
    }
  }
  
  // Combines the trees with the two given Posns into one tree
  // this method is private as users of the SpanningTree class should not be able to call this
  // method directly as it only deals with private fields in this class
  // EFFECT: modifies the representatives hashmap to account for the new connection: the first given
  // Posn's root's representative will be the second given Posn's root
  private void union(Posn src, Posn dest) {
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
              e.dest.equals(new Posn(col, row)) && e.src.equals(new Posn(col + 1, row)) ||
              col == width - 1) {
            hasRightWall = false;
          }
          if (e.src.equals(new Posn(col, row)) && e.dest.equals(new Posn(col, row + 1)) || 
              e.dest.equals(new Posn(col, row)) && e.src.equals(new Posn(col, row + 1)) ||
              row == height - 1) {
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
  
  Maze maze1;
  Maze maze2;
  
  SpanningTree tree1;
  SpanningTree tree2;
  
  void initTestConditions() {
    maze1 = new Maze(4, 4, new Random(360));
    maze2 = new Maze(3, 3, new Random(350));
    
    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < 4; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < 4; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    tree1 = new SpanningTree(mazeLocations, new Random(360));
    
    ArrayList<ArrayList<Posn>> mazeLocations2 = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < 3; row += 1) {
      mazeLocations2.add(new ArrayList<Posn>());
      for (int col = 0; col < 3; col += 1) {
        mazeLocations2.get(row).add(new Posn(col, row));
      }
    }
    tree2 = new SpanningTree(mazeLocations2, new Random(350));
  }
  
  // test that the getSpanningTree method in SpanningTree functions properly
  void testGetSpanningTree(Tester t) {
    this.initTestConditions();
    
    ArrayList<Edge> tree1Edges = new ArrayList<Edge>();
    tree1Edges.add(new Edge(new Posn(1, 3), new Posn(2, 3), 10));
    tree1Edges.add(new Edge(new Posn(2, 1), new Posn(2, 2), 27));
    tree1Edges.add(new Edge(new Posn(2, 0), new Posn(2, 1), 48));
    tree1Edges.add(new Edge(new Posn(3, 2), new Posn(3, 3), 61));
    tree1Edges.add(new Edge(new Posn(0, 1), new Posn(0, 2), 76));
    tree1Edges.add(new Edge(new Posn(2, 3), new Posn(3, 3), 81));
    tree1Edges.add(new Edge(new Posn(2, 0), new Posn(3, 0), 107));
    tree1Edges.add(new Edge(new Posn(2, 1), new Posn(3, 1), 260));
    tree1Edges.add(new Edge(new Posn(0, 0), new Posn(0, 1), 276));
    tree1Edges.add(new Edge(new Posn(1, 2), new Posn(1, 3), 299));
    tree1Edges.add(new Edge(new Posn(2, 2), new Posn(3, 2), 357));
    tree1Edges.add(new Edge(new Posn(1, 0), new Posn(2, 0), 403));
    tree1Edges.add(new Edge(new Posn(1, 0), new Posn(1, 1), 447));
    tree1Edges.add(new Edge(new Posn(0, 1), new Posn(1, 1), 517));
    tree1Edges.add(new Edge(new Posn(0, 2), new Posn(0, 3), 629));
    t.checkExpect(tree1.getSpanningTree(), tree1Edges);
    
    ArrayList<Edge> tree2Edges = new ArrayList<Edge>();
    tree2Edges.add(new Edge(new Posn(2, 0), new Posn(2, 1), 88));
    tree2Edges.add(new Edge(new Posn(1, 2), new Posn(2, 2), 190));
    tree2Edges.add(new Edge(new Posn(0, 0), new Posn(1, 0), 272));
    tree2Edges.add(new Edge(new Posn(1, 1), new Posn(1, 2), 398));
    tree2Edges.add(new Edge(new Posn(0, 0), new Posn(0, 1), 501));
    tree2Edges.add(new Edge(new Posn(0, 1), new Posn(0, 2), 529));
    tree2Edges.add(new Edge(new Posn(1, 1), new Posn(2, 1), 582));
    tree2Edges.add(new Edge(new Posn(0, 2), new Posn(1, 2), 618));
    t.checkExpect(tree2.getSpanningTree(), tree2Edges);
  }
  
  // test that the getMazeScene method in SpanningTree functions properly
  void testGetMazeScene(Tester t) {
    this.initTestConditions();
    WorldScene maze0Scene = new WorldScene(80, 80);
    maze0Scene.placeImageXY(new RectangleImage(80, 80, OutlineMode.OUTLINE, Color.BLACK), 40, 40);
       
    WorldImage WALL = new LineImage(new Posn(0, 20), Color.BLACK);
    WorldImage WALL_H = new RotateImage(WALL, 90);
    for (int row = 0; row < 4; row += 1) {
      for (int col = 0; col < 4; col += 1) {
        maze0Scene.placeImageXY(WALL, (col + 1) * 20, row * 20 + 10);
        maze0Scene.placeImageXY(WALL_H, col * 20 + 10, (row + 1) * 20);
      }
    }
   
    t.checkExpect(tree1.getMazeScene(20), maze0Scene);
   
    tree1.getSpanningTree();
    tree2.getSpanningTree();
    // mazeScene would practically only be called after getSpanningTree(), otherwise it would just
    // return a checkerboard, as tested above
   
    WorldScene maze1Scene = new WorldScene(80, 80);
    maze1Scene.placeImageXY(new RectangleImage(80, 80, OutlineMode.OUTLINE, Color.BLACK), 40, 40);
   
    for (int row = 0; row < 4; row += 1) {
      for (int col = 0; col < 4; col += 1) {
        if ((col == 0 && row != 1) || (col == 1 && row == 1) || (col == 1 && row == 2)) {
          maze1Scene.placeImageXY(WALL, (col + 1) * 20, row * 20 + 10);
        }
        if ((col == 1 && row == 1) || (col == 2 && row == 2) || (col == 3 && row < 2)) {
          maze1Scene.placeImageXY(WALL_H, col * 20 + 10, (row + 1) * 20);
        }
      }
    }
   
    t.checkExpect(tree1.getMazeScene(20), maze1Scene);
   
    WorldScene maze2Scene = new WorldScene(60, 60);
    maze2Scene.placeImageXY(new RectangleImage(60, 60, OutlineMode.OUTLINE, Color.BLACK), 30, 30);
   
    for (int row = 0; row < 3; row += 1) {
      for (int col = 0; col < 3; col += 1) {
        if ((col == 0 && row == 1) || (col == 1 && row == 0)) {
          maze2Scene.placeImageXY(WALL, (col + 1) * 20, row * 20 + 10);
        }
        if ((col == 1 && row == 0) || (col == 2 && row == 1)) {
          maze2Scene.placeImageXY(WALL_H, col * 20 + 10, (row + 1) * 20);
        }
      }
    }
   
    t.checkExpect(tree2.getMazeScene(20), maze2Scene);
  }
 
  // tests that the searchMaze method in the Maze class functions properly
  void testSearchMaze(Tester t) {
    this.initTestConditions();
    
    ArrayList<Posn> maze1Path = new ArrayList<Posn>();
    maze1Path.add(new Posn(3, 3));
    maze1Path.add(new Posn(3, 2));
    maze1Path.add(new Posn(2, 2));
    maze1Path.add(new Posn(2, 1));
    maze1Path.add(new Posn(2, 0));
    maze1Path.add(new Posn(1, 0));
    maze1Path.add(new Posn(1, 1));
    maze1Path.add(new Posn(0, 1));
    maze1Path.add(new Posn(0, 0));
    
    t.checkExpect(maze1.searchMaze(true), maze1Path);
    t.checkExpect(maze1.searchMaze(false), maze1Path);
    
    ArrayList<Posn> maze2Path = new ArrayList<Posn>();
    maze2Path.add(new Posn(2, 2));
    maze2Path.add(new Posn(1, 2));
    maze2Path.add(new Posn(0, 2));
    maze2Path.add(new Posn(0, 1));
    maze2Path.add(new Posn(0, 0));
    
    t.checkExpect(maze2.searchMaze(true), maze2Path);
    t.checkExpect(maze2.searchMaze(false), maze2Path);
    
    maze2.onKeyEvent("n");
    
    maze2Path = new ArrayList<Posn>();
    maze2Path.add(new Posn(2, 2));
    maze2Path.add(new Posn(2, 1));
    maze2Path.add(new Posn(2, 0));
    maze2Path.add(new Posn(1, 0));
    maze2Path.add(new Posn(0, 0));
    
    t.checkExpect(maze2.searchMaze(true), maze2Path);
    t.checkExpect(maze2.searchMaze(false), maze2Path);
  }
  
  // tests that the reconstruct method in the Maze class functions properly
  void testReconstruct(Tester t) {
    this.initTestConditions();
    
    HashMap<Posn, Posn> diagonalMap = new HashMap<Posn, Posn>();
    diagonalMap.put(new Posn(1, 0), new Posn(0, 0));
    diagonalMap.put(new Posn(1, 1), new Posn(1, 0));
    diagonalMap.put(new Posn(2, 1), new Posn(1, 1));
    diagonalMap.put(new Posn(2, 2), new Posn(2, 1));
    diagonalMap.put(new Posn(3, 2), new Posn(2, 2));
    diagonalMap.put(new Posn(3, 3), new Posn(3, 2));
    
    HashMap<Posn, Posn> bottomMap3 = new HashMap<Posn, Posn>();
    bottomMap3.put(new Posn(0, 1), new Posn(0, 0));
    bottomMap3.put(new Posn(0, 2), new Posn(0, 1));
    bottomMap3.put(new Posn(1, 2), new Posn(0, 2));
    bottomMap3.put(new Posn(2, 2), new Posn(1, 2));
    
    HashMap<Posn, Posn> bottomMap4 = new HashMap<Posn, Posn>();
    bottomMap4.put(new Posn(0, 1), new Posn(0, 0));
    bottomMap4.put(new Posn(0, 2), new Posn(0, 1));
    bottomMap4.put(new Posn(0, 3), new Posn(0, 2));
    bottomMap4.put(new Posn(1, 3), new Posn(0, 3));
    bottomMap4.put(new Posn(2, 3), new Posn(1, 3));
    bottomMap4.put(new Posn(3, 3), new Posn(2, 3));
    
    ArrayList<Posn> maze1DiagonalPath = new ArrayList<Posn>();
    maze1DiagonalPath.add(new Posn(3, 3));
    maze1DiagonalPath.add(new Posn(3, 2));
    maze1DiagonalPath.add(new Posn(2, 2));
    maze1DiagonalPath.add(new Posn(2, 1));
    maze1DiagonalPath.add(new Posn(1, 1));
    maze1DiagonalPath.add(new Posn(1, 0));
    maze1DiagonalPath.add(new Posn(0, 0));
    
    ArrayList<Posn> maze1BottomPath = new ArrayList<Posn>();
    maze1BottomPath.add(new Posn(3, 3));
    maze1BottomPath.add(new Posn(2, 3));
    maze1BottomPath.add(new Posn(1, 3));
    maze1BottomPath.add(new Posn(0, 3));
    maze1BottomPath.add(new Posn(0, 2));
    maze1BottomPath.add(new Posn(0, 1));
    maze1BottomPath.add(new Posn(0, 0));
    
    ArrayList<Posn> maze2DiagonalPath = new ArrayList<Posn>();
    maze2DiagonalPath.add(new Posn(2, 2));
    maze2DiagonalPath.add(new Posn(2, 1));
    maze2DiagonalPath.add(new Posn(1, 1));
    maze2DiagonalPath.add(new Posn(1, 0));
    maze2DiagonalPath.add(new Posn(0, 0));
    
    ArrayList<Posn> maze2BottomPath = new ArrayList<Posn>();
    maze2BottomPath.add(new Posn(2, 2));
    maze2BottomPath.add(new Posn(1, 2));
    maze2BottomPath.add(new Posn(0, 2));
    maze2BottomPath.add(new Posn(0, 1));
    maze2BottomPath.add(new Posn(0, 0));
    
    t.checkExpect(maze1.reconstruct(diagonalMap, new Posn(3, 3)), maze1DiagonalPath);
    t.checkExpect(maze1.reconstruct(bottomMap4, new Posn(3, 3)), maze1BottomPath);
    t.checkExpect(maze2.reconstruct(diagonalMap, new Posn(2, 2)), maze2DiagonalPath);
    t.checkExpect(maze2.reconstruct(bottomMap3, new Posn(2, 2)), maze2BottomPath);
  }
  
  // test the compareTo method in the Edge class
  /*void testCompareTo(Tester t) {
    Edge e1 = 
  }*/
  
  void testWorld(Tester t) {
    new Maze(30, 30).bigBang(1250, 750, 1.0 / 100);
  }
  
  
  
  
}

