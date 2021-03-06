import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ArrayDeque;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

/*
 * DOCUMENTATION:
 * While a search has not been initiated, a player can move:
 *   - up, down, left, right (arrow keys) move the character in the respective directions
 *   - Pressing B initiates a Breadth First Search 
 *   - Pressing D initiates a Depth First Search 
 *   - Pressing N initiates a New normal random Maze. 
 *   - Pressing V generates a new maze that favors vertical corridors. 
 *   - Pressing H generates a new maze that favors horizontal corridors. 
 *   - Pressing S toggles the visibility of visited cells
 */

/*
 * EXTRA CREDIT:
 *   - Provide an option to toggle the viewing of the visited paths.
 *   - Allow the user the ability to start a new maze without restarting the program.
 *   - Keep the score of wrong moves � for both the automatic solutions and manual ones
 *   - Construct mazes with a bias in a particular direction �a preference for horizontal or 
 *     vertical corridors. 
 */

// Holds the Score and Displays the Image containing the score 
class ScoreBox {
  private final int score;
  private final OverlayImage image;

  ScoreBox(int score) {
    this.score = score;
    TextImage text = new TextImage(
        "Maze Complete! Wrong Move Count: " + Integer.toString(this.score), 20, Color.black);
    RectangleImage scoreRect = new RectangleImage(500, 60, OutlineMode.SOLID, Color.white);
    this.image = new OverlayImage(text, scoreRect);
  }

  // Displays the image containing the score on the given scene
  public void displayImage(WorldScene scene) {
    scene.placeImageXY(this.image, 300, 100);
  }
}

// Represents a Maze
class Maze extends World {
  private final int width;
  private final int height;
  private ArrayList<Edge> edgesInMaze; // allowed to be reinstantiated when creating a new maze
  private WorldScene mazeScene; // Can be reinstantiated when creating a new maze
  private final int cellSize; // in pixels
  private final ArrayList<Posn> squaresToColor;
  private final ArrayList<Posn> finalPath;
  private final Posn player;
  private final HashMap<Posn, ArrayList<Posn>> mazeMap;
  private boolean isPlayerMode; // This can be toggled
  private final ArrayList<Posn> squaresColored;
  private final HashMap<Posn, Posn> playerPath;
  private SpanningTree mazeTree; // Can be reinstantiated when creating new maze
  private final Random r;
  private boolean toggleVisited; // Holds state of whether or not visited squares should be toggled
  private boolean showingVisited; // Toggled when toggleVisited is true.
  private ScoreBox scoreBox; // Can be reinstantiated when new maze created
  private int initialFinalPathLength;

  // Standard Constructor for maze, with ability to specify random.
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
    this.toggleVisited = false;
    this.showingVisited = true;
    this.scoreBox = new ScoreBox(0);
    this.initialFinalPathLength = 0;

    this.cellSize = Math.min(1250 / this.width, 750 / this.height);

    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < height; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < width; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    this.mazeTree = new SpanningTree(mazeLocations, this.r, new NormalMode());
    this.edgesInMaze = this.mazeTree.getSpanningTree();

    this.mazeScene = this.mazeTree.getMazeScene(this.cellSize);

    this.mazeMap = new HashMap<Posn, ArrayList<Posn>>();

    for (Edge edge : this.edgesInMaze) {
      if (this.mazeMap.get(edge.getSrc()) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.getDest());
        this.mazeMap.put(edge.getSrc(), pathsFromPosn);
      }
      else {
        this.mazeMap.get(edge.getSrc()).add(edge.getDest());
      }

      if (this.mazeMap.get(edge.getDest()) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.getSrc());
        this.mazeMap.put(edge.getDest(), pathsFromPosn);
      }
      else {
        this.mazeMap.get(edge.getDest()).add(edge.getSrc());
      }
    }
  }

  // Convenience Constructor for Maze
  Maze(int width, int height) {
    this(width, height, new Random());
  }

  // Makes a new Scene and displays it. Public due to override
  public WorldScene makeScene() {

    if (this.isPlayerMode && this.showingVisited) {
      WorldScene mazePlayerScene = this.mazeTree.getMazeScene(this.cellSize);
      this.displayVisited(mazePlayerScene);
      this.displayFinalPath(mazePlayerScene);
      this.displayPlayer(mazePlayerScene);
      return mazePlayerScene;
    }
    else if (this.isPlayerMode && !this.showingVisited) {
      WorldScene mazePlayerScene = this.mazeTree.getMazeScene(this.cellSize);
      this.displayFinalPath(mazePlayerScene);
      this.displayPlayer(mazePlayerScene);
      return mazePlayerScene;
    }
    else if (!this.showingVisited) {
      return this.mazeTree.getMazeScene(this.cellSize);
    }
    else if (this.toggleVisited && this.showingVisited) {
      this.addBacklog();
      return this.mazeScene;
    }
    else {
      return this.mazeScene;
    }
  }

  // Display the visited Squares
  // EFFECT: modifies the given scene to include the visited squares
  private void displayVisited(WorldScene scene) {
    for (Posn path : squaresColored) {
      scene.placeImageXY(
          new RectangleImage(this.cellSize - 2, this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          path.x * this.cellSize + this.cellSize / 2 + 1,
          path.y * cellSize + this.cellSize / 2 + 1);
    }
  }

  // Displays the final path
  // EFFECT: modifies the given scene to include the final solution path
  private void displayFinalPath(WorldScene scene) {
    for (Posn path : finalPath) {
      scene.placeImageXY(
          new RectangleImage(this.cellSize - 2, this.cellSize - 2, OutlineMode.SOLID, Color.BLUE),
          path.x * this.cellSize + this.cellSize / 2 + 1,
          path.y * cellSize + this.cellSize / 2 + 1);
      this.displayScore(scene);
    }
  }

  // Displays the player on the board
  // EFFECT: modifies the given scene to include the player image
  private void displayPlayer(WorldScene scene) {
    scene.placeImageXY(new CircleImage(this.cellSize / 3, OutlineMode.SOLID, Color.RED),
        player.x * this.cellSize + this.cellSize / 2 + 1,
        player.y * cellSize + this.cellSize / 2 + 1);
  }

  // Updates the world on tick. Public due to required override.
  // EFFECT: if the user is toggling cell visibility, remove/add the visibility as
  // necessary
  // modifies the mazeScene, showingVisited, and toggleVisited fields to do this
  // modifies the mazeScene field to draw subsequent searched cells from searching
  // algorithms
  // modifies the squaresColored field to add any cells visited by a searching
  // algorithm
  // modifies squaresToColor as cells are colored in, to remove these cells from
  // the queue
  public void onTick() {
    if (this.squaresToColor.size() > 0 && !this.showingVisited && this.toggleVisited) {
      this.addBacklog();
    }
    else if (this.squaresToColor.size() > 0 && this.showingVisited && this.toggleVisited) {
      this.mazeScene = this.mazeTree.getMazeScene(this.cellSize);
    }
    else if (this.squaresToColor.size() > 0 && this.showingVisited && !this.toggleVisited) {
      Posn nextCell = this.squaresToColor.remove(0);
      this.squaresColored.add(nextCell);
      this.mazeScene.placeImageXY(
          new RectangleImage(this.cellSize - 2, this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          nextCell.x * this.cellSize + this.cellSize / 2 + 1,
          nextCell.y * cellSize + this.cellSize / 2 + 1);
    }
    else if (this.squaresToColor.size() > 0 && !this.showingVisited) {
      Posn nextCell = this.squaresToColor.remove(0);
      this.squaresColored.add(nextCell);
    }
    else if (!this.finalPath.isEmpty() && !this.isPlayerMode) {
      Posn nextCell = this.finalPath.remove(0);
      this.mazeScene.placeImageXY(
          new RectangleImage(this.cellSize - 2, this.cellSize - 2, OutlineMode.SOLID, Color.BLUE),
          nextCell.x * this.cellSize + this.cellSize / 2 + 1,
          nextCell.y * cellSize + this.cellSize / 2 + 1);
      this.displayScore(this.mazeScene);
    }

    if (this.toggleVisited) {
      this.showingVisited = !this.showingVisited;
      this.toggleVisited = false;
    }
  }

  // Calculates and Displays the Number of Wrong moves.
  // EFFECT: modifies the given scene to display the score
  private void displayScore(WorldScene scene) {
    this.scoreBox = new ScoreBox(this.getScore());
    this.scoreBox.displayImage(scene);
  }

  // calculates the number of wrong moves that were made when finding the path to
  // the end
  // DESIGN: the user of this class should be able to access the score that it
  // obtains upon reaching
  // the end
  public int getScore() {
    return this.getUniqueColoredSquaresCount() - this.initialFinalPathLength
        + this.squaresToColor.size();
  }

  // Re-adds the visited squares to the scene if showing visited squares is
  // toggled back on.
  // EFFECT: modifies the mazeScene to include the squares that were visited
  private void addBacklog() {
    for (Posn nextCell : this.squaresColored) {
      this.mazeScene.placeImageXY(
          new RectangleImage(this.cellSize - 2, this.cellSize - 2, OutlineMode.SOLID, Color.CYAN),
          nextCell.x * this.cellSize + this.cellSize / 2 + 1,
          nextCell.y * cellSize + this.cellSize / 2 + 1);
    }

  }

  // Calculates the unique amount of squares visited by the player.
  private int getUniqueColoredSquaresCount() {
    HashMap<Posn, Boolean> uniqueSquares = new HashMap<Posn, Boolean>();

    for (Posn square : this.squaresColored) {
      uniqueSquares.put(square, true);
    }

    return uniqueSquares.size();
  }

  // Updates the world when key is pressed. Public due to required override.
  // EFFECT: See documentation, modifies the world accordingly. If a new maze is
  // created, resets all
  // fields
  public void onKeyEvent(String key) {
    if (key.equals("b")) {
      this.isPlayerMode = false;
      this.finalPath.clear();
      this.searchMaze(true);
    }
    else if (key.equals("d")) {
      this.isPlayerMode = false;
      this.finalPath.clear();
      this.searchMaze(false);
    }
    else if (key.equals("n")) {
      this.buildNewMaze(new NormalMode());
    }
    else if (key.equals("v")) {
      this.buildNewMaze(new VerticalMode());
    }
    else if (key.equals("s")) {
      this.toggleVisited = true;
    }
    else if (key.equals("h")) {
      this.buildNewMaze(new HorizontalMode());
    }
    else if (key.equals("left")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.x < this.player.x && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.x -= 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
        }
        else if (neighbor.x < this.player.x) {
          this.player.x -= 1;
        }
      }
    }
    else if (key.equals("right")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.x > this.player.x && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.x += 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
          if (this.player.x == this.width - 1 && this.player.y == this.height - 1) {
            this.reconstruct(playerPath, player);
          }
        }
        else if (neighbor.x > this.player.x) {
          this.player.x += 1;
        }
      }
    }
    else if (key.equals("up")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.y < this.player.y && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.y -= 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
        }
        else if (neighbor.y < this.player.y) {
          this.player.y -= 1;
        }
      }
    }
    else if (key.equals("down")) {
      for (Posn neighbor : this.mazeMap.get(this.player)) {
        if (neighbor.y > this.player.y && playerPath.get(neighbor) == null) {
          this.playerPath.put(neighbor, new Posn(player.x, player.y));
          this.player.y += 1;
          this.squaresColored.add(neighbor);
          this.squaresToColor.add(neighbor);
          if (this.player.x == this.width - 1 && this.player.y == this.height - 1) {
            this.reconstruct(playerPath, player);
          }
        }
        else if (neighbor.y > this.player.y) {
          this.player.y += 1;
        }
      }
    }

  }

  // Builds a new maze of a given type, without restarting the game.
  // EFFECT: resets essential fields for creating a new maze
  private void buildNewMaze(IMode mode) {
    this.finalPath.clear();
    this.player.x = 0;
    this.player.y = 0;
    this.squaresToColor.clear();
    this.squaresToColor.add(new Posn(player.x, player.y));
    this.isPlayerMode = true;
    this.squaresColored.clear();
    this.squaresColored.add(new Posn(player.x, player.y));
    this.playerPath.clear();
    this.initialFinalPathLength = 0;
    this.scoreBox = new ScoreBox(0);

    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < height; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < width; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }

    this.mazeTree = new SpanningTree(mazeLocations, this.r, mode);
    this.edgesInMaze = this.mazeTree.getSpanningTree();

    this.mazeScene = this.mazeTree.getMazeScene(this.cellSize);

    this.mazeMap.clear();

    for (Edge edge : this.edgesInMaze) {
      if (this.mazeMap.get(edge.getSrc()) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.getDest());
        this.mazeMap.put(edge.getSrc(), pathsFromPosn);
      }
      else {
        this.mazeMap.get(edge.getSrc()).add(edge.getDest());
      }

      if (this.mazeMap.get(edge.getDest()) == null) {
        ArrayList<Posn> pathsFromPosn = new ArrayList<Posn>();
        pathsFromPosn.add(edge.getSrc());
        this.mazeMap.put(edge.getDest(), pathsFromPosn);
      }
      else {
        this.mazeMap.get(edge.getDest()).add(edge.getSrc());
      }
    }

  }

  // computes the path from the top left corner to the bottom right corner of the
  // maze
  // EFFECT: modifies squaresToColor to contain all the positions that the
  // searching algorithm went
  // through before finding the solution. Modifies the finalPath field to contain
  // all the positions
  // in the solution path
  ArrayList<Posn> searchMaze(boolean bfs) {

    ArrayDeque<Posn> worklist = new ArrayDeque<Posn>(); // A Queue or a Stack, depending on the
    // algorithm
    worklist.add(new Posn(0, 0));

    HashMap<Posn, Boolean> visitedCells = new HashMap<Posn, Boolean>();
    HashMap<Posn, Posn> cameFromPosn = new HashMap<Posn, Posn>();

    // BFS
    while (worklist.size() > 0) {

      Posn nextItem = worklist.getFirst();
      worklist.removeFirst();

      if (nextItem.equals(new Posn(this.width - 1, this.height - 1))) {
        this.squaresToColor.add(nextItem);
        return this.reconstruct(cameFromPosn, nextItem);
      }
      else if (visitedCells.get(nextItem) == null) {
        this.squaresToColor.add(nextItem);
        visitedCells.put(nextItem, true);
        ArrayList<Posn> neighbors = this.mazeMap.get(nextItem);
        for (Posn neighbor : neighbors) {
          if (visitedCells.get(neighbor) == null) {
            cameFromPosn.put(neighbor, nextItem);
            if (bfs) {
              worklist.addLast(neighbor);
            }
            else {
              worklist.addFirst(neighbor);
            }
          }
        }
      }
    }
    // since the maze is represented by a spanning tree, this should never occur
    throw new RuntimeException("No Path Found");
  }

  // Reconstruct the path from the end of the maze back to the beginning.
  // Yields the path that would be taken if solution was perfect.
  // EFFECT: modifies finalPath to contain the solution path
  // modifies the initialFinalPathLength to contain the length of the solution
  // path
  ArrayList<Posn> reconstruct(HashMap<Posn, Posn> cameFromPosn, Posn nextItem) {
    ArrayList<Posn> steps = new ArrayList<Posn>();
    Posn currPosition = new Posn(nextItem.x, nextItem.y);
    while (!currPosition.equals(new Posn(0, 0))) {
      steps.add(new Posn(currPosition.x, currPosition.y));
      this.finalPath.add(new Posn(currPosition.x, currPosition.y));
      currPosition = cameFromPosn.get(currPosition);
    }
    steps.add(new Posn(0, 0));
    this.finalPath.add(new Posn(0, 0));
    this.initialFinalPathLength = this.finalPath.size();
    return steps;
  }

  // returns the current position where the player is
  // DESIGN: the user should be able to get their position since the user is
  // controlling it
  Posn getPlayerPosition() {
    return player;
  }

  // returns the solution path from the start to the end of this maze, or an empty
  // list if the path
  // has not yet been found. (If the path is in the process of being drawn,
  // returns the path that
  // has not yet been drawn)
  // DESIGN: The user should be able to access this field since the solution path
  // is only revealed
  // after the path has already been found, so this is information that the user
  // already knows
  ArrayList<Posn> getFinalPath() {
    return this.finalPath;
  }

  // computes the ratio of horizontal corridors to vertical corridors
  // DESIGN: this may be a statistic of interest the the user
  double getHorizToVertRatio() {
    double horizEdges = 0;
    double vertEdges = 0;
    for (Edge e : this.edgesInMaze) {
      if (e.getDest().x != e.getSrc().x) {
        horizEdges += 1;
      }
      else {
        vertEdges += 1;
      }
    }

    return horizEdges / vertEdges;
  }

  // returns a list of all the cells visited, either by the player or by a
  // searching algorithm
  // DESIGN: the user should be able to access this as a record of all the cells
  // that were visited
  ArrayList<Posn> getSquaresColored() {
    return this.squaresColored;
  }

}

// Mode that stores the potential biases of 
interface IMode {

  int getHorizWeight(int defaultWeight);

  int getVertWeight(int defaultWeight);

}

// Represents the mode that will create unbiased edges
class NormalMode implements IMode {

  @Override
  public int getHorizWeight(int defaultWeight) {
    return defaultWeight;
  }

  @Override
  public int getVertWeight(int defaultWeight) {
    return defaultWeight;
  }

}

// Represents the mode that will favor vertical edges. 
class VerticalMode implements IMode {

  @Override
  public int getHorizWeight(int defaultWeight) {
    return defaultWeight;
  }

  @Override
  public int getVertWeight(int defaultWeight) {
    return 5 * defaultWeight;
  }

}

//Represents the mode that will favor horizontal edges. 
class HorizontalMode implements IMode {

  @Override
  public int getHorizWeight(int defaultWeight) {
    return 5 * defaultWeight;
  }

  @Override
  public int getVertWeight(int defaultWeight) {
    return 1 * defaultWeight;
  }

}

// Represents a Spanning Tree
class SpanningTree {

  private final HashMap<Posn, Posn> representatives;
  private final ArrayList<Edge> edgesInTree;
  private final ArrayList<Edge> worklist; // all edges in graph, sorted by edge weights
  private final ArrayList<ArrayList<Posn>> graphLocations;
  private final int nodeCount;
  private final IMode mode;

  // Normal Constructor for spanningTree
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, Random r, IMode mode) {
    this.representatives = new HashMap<Posn, Posn>();
    this.graphLocations = graphLocations;
    int nodes = 0;
    for (ArrayList<Posn> arr : graphLocations) {
      nodes += arr.size();
    }
    this.nodeCount = nodes;
    this.mode = mode;
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
              graphLocations.get(row + 1).get(col), r.nextInt(this.mode.getHorizWeight(1000))));
        }
        if (col != graphLocations.get(row).size() - 1) {
          worklist.add(new Edge(graphLocations.get(row).get(col),
              graphLocations.get(row).get(col + 1), r.nextInt(this.mode.getVertWeight(1000))));
        }
      }
    }
    this.worklist.sort(new EdgeComparator());
  }

  // Convenience Constructor for SpanningTree
  SpanningTree(ArrayList<ArrayList<Posn>> graphLocations, int nodeCount, IMode mode) {
    this(graphLocations, new Random(), mode);
  }

  // Computes the minimum spanning tree using Kruskal's algorithm on the worklist
  // of weighted edges
  // EFFECT: removes enough nodes from the worklist to achieve a spanning tree,
  // modifies the
  // edgesInTree to contain all the edges in the resulting spanning tree, and
  // modifies the
  // representatives hashmap to represent a spanning tree, where every node's root
  // representative is
  // the same
  // Gets the spanning Tree as required by Maze Class to build the edges.
  public ArrayList<Edge> getSpanningTree() {
    int counter = 0;
    while (counter < this.nodeCount - 1) {
      Edge nextEdge = this.worklist.get(0);
      if (this.find(nextEdge.getSrc()).equals(this.find(nextEdge.getDest()))) {
        this.worklist.remove(0);
      }
      else {
        counter += 1;
        this.edgesInTree.add(nextEdge);
        this.union(nextEdge.getSrc(), nextEdge.getDest());
        this.worklist.remove(0);
      }
    }
    return this.edgesInTree;
  }

  // Computes the root of the tree with the given Posn in it
  // this method is private as users of the SpanningTree class should not be able
  // to call this
  // method directly as it only deals with private fields in this class
  private Posn find(Posn key) {
    if (representatives.get(key).equals(key)) {
      return key;
    }
    else {
      return this.find(representatives.get(key));
    }
  }

  // Combines the trees with the two given Posns into one tree
  // this method is private as users of the SpanningTree class should not be able
  // to call this
  // method directly as it only deals with private fields in this class
  // EFFECT: modifies the representatives hashmap to account for the new
  // connection: the first given
  // Posn's root's representative will be the second given Posn's root
  private void union(Posn src, Posn dest) {
    representatives.replace(this.find(src), this.find(dest));
  }

  // Creates a mazeScene given a cellSize, which is a grid with the edges in the
  // minSpanningTree cut out.
  WorldScene getMazeScene(int cellSize) {
    int width = graphLocations.get(0).size();
    int height = graphLocations.size();
    WorldScene mazeScene = new WorldScene(width * cellSize, height * cellSize);
    mazeScene.placeImageXY(
        new RectangleImage(width * cellSize, height * cellSize, OutlineMode.OUTLINE, Color.BLACK),
        width * cellSize / 2, height * cellSize / 2);

    WorldImage wall = new LineImage(new Posn(0, cellSize), Color.BLACK);
    WorldImage wallH = new RotateImage(wall, 90);
    for (int row = 0; row < height; row += 1) {
      for (int col = 0; col < width; col += 1) {

        boolean hasRightWall = true;
        boolean hasDownWall = true;
        for (Edge e : this.edgesInTree) {
          if (e.getSrc().equals(new Posn(col, row)) && e.getDest().equals(new Posn(col + 1, row))
              || e.getDest().equals(new Posn(col, row)) && e.getSrc().equals(new Posn(col + 1, row))
              || col == width - 1) {
            hasRightWall = false;
          }
          if (e.getSrc().equals(new Posn(col, row)) && e.getDest().equals(new Posn(col, row + 1))
              || e.getDest().equals(new Posn(col, row)) && e.getSrc().equals(new Posn(col, row + 1))
              || row == height - 1) {
            hasDownWall = false;
          }
        }

        if (hasRightWall) {
          mazeScene.placeImageXY(wall, (col + 1) * cellSize, row * cellSize + cellSize / 2);
        }

        if (hasDownWall) {
          mazeScene.placeImageXY(wallH, col * cellSize + cellSize / 2, (row + 1) * cellSize);
        }
      }
    }

    return mazeScene;
  }
}

// Represents an Edge in a Tree / Maze
class Edge implements Comparable<Edge> {

  private final Posn src;
  private final Posn dest;

  private final int weight;

  Edge(Posn src, Posn dest, int weight) {
    this.src = src;
    this.dest = dest;
    this.weight = weight;
  }

  // Returns the src as Required by getSpanningTree. Public as it is a getter
  public Posn getSrc() {
    return this.src;
  }

  // Returns the dest as Required by getSpanningTree. Public as it is a getter
  public Posn getDest() {
    return this.dest;
  }

  // Compares two edges based on their weight. Public as it must implement
  // comparable and method visibility cannot be reduced.
  public int compareTo(Edge o) {
    return this.weight - o.weight;
  }
}

// Compares on edge with enough and returns an integer based on the compareTo Method
class EdgeComparator implements Comparator<Edge> {

  // Compares on edge with enough and returns an integer based on the compareTo
  // Method
  public int compare(Edge o1, Edge o2) {
    return o1.compareTo(o2);
  }
}

// examples and tests for Mazes and its helper classes
class ExamplesMaze {

  void testWorld(Tester t) {
    new Maze(6, 6).bigBang(1250, 750, 1.0 / 100);
  }

  /*
   * Testing WishList
   * 
   * UnionFind: - There should be length * width - 1 edges in the final list of
   * edges - At the end, all the nodes should connect to the same root node;
   * running find on these edges should return the same result. -
   * 
   * Maze: - A new maze can be created without restarting the program. - A new
   * random maze can be generated
   * 
   * Player: - A Player cannot walk through walls - A Player leaves their previous
   * path behind them - A Player has the same path reconstruction functionality as
   * the built in algorithms
   * 
   * Searching: - Both Depth First Search and Breadth First Search find the goal -
   * The path Backwards does not contain any cycles and it does contain the start
   * and end points. The score of wrong tiles is properly constructed and correct.
   * The solution path is consistent regardless of the search method
   * 
   * OnKeyEvent: - While in Player mode (A search has not been initiated), they
   * can move in all four directions if a wall is not present. Pressing the left
   * key moves the player left, the right key right, the down key down and the up
   * key up. Pressing B initiates a Breadth First Search Pressing D initiates a
   * Depth First Search Pressing N initiates a New normal Random Maze. Pressing V
   * generates a new maze that favors vertical corridors. Pressing H generates a
   * new maze that favors horizontal corridors. Visibility toggling works as
   * intended by pressing S also demonstrates that new mazes can be created
   * without closing the world
   * 
   * OnTick - World is properly updated during searching animation, World gets
   * properly updated once the end is reached. The world properly animates the
   * drawing of the final path at the conclusion of solving the maze. Visibility
   * toggling works as intended
   * 
   * 
   * The User is Notified of the completion of the Game The Solution Path is
   * Displayed.
   */

  Maze maze1;
  Maze maze2;
  Maze maze1b;

  SpanningTree tree1;
  SpanningTree tree2;

  // initializes the testing conditions for mazes and spanning trees
  void initTestConditions() {
    maze1 = new Maze(4, 4, new Random(360));
    maze1b = new Maze(4, 4, new Random(360));
    maze2 = new Maze(3, 3, new Random(350));

    ArrayList<ArrayList<Posn>> mazeLocations = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < 4; row += 1) {
      mazeLocations.add(new ArrayList<Posn>());
      for (int col = 0; col < 4; col += 1) {
        mazeLocations.get(row).add(new Posn(col, row));
      }
    }
    tree1 = new SpanningTree(mazeLocations, new Random(360), new NormalMode());

    ArrayList<ArrayList<Posn>> mazeLocations2 = new ArrayList<ArrayList<Posn>>();
    for (int row = 0; row < 3; row += 1) {
      mazeLocations2.add(new ArrayList<Posn>());
      for (int col = 0; col < 3; col += 1) {
        mazeLocations2.get(row).add(new Posn(col, row));
      }
    }
    tree2 = new SpanningTree(mazeLocations2, new Random(350), new NormalMode());
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
    // mazeScene would practically only be called after getSpanningTree(), otherwise
    // it would just
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
  void testCompareTo(Tester t) {
    Edge e1 = new Edge(new Posn(1, 1), new Posn(1, 2), 10);
    Edge e2 = new Edge(new Posn(2, 3), new Posn(3, 3), 10);
    Edge e3 = new Edge(new Posn(3, 3), new Posn(3, 2), 11);
    Edge e4 = new Edge(new Posn(0, 0), new Posn(1, 0), 9);

    t.checkExpect(e1.compareTo(e2), 0);
    t.checkExpect(e1.compareTo(e3), -1);
    t.checkExpect(e1.compareTo(e4), 1);
    t.checkExpect(e3.compareTo(e4), 2);
    t.checkExpect(e4.compareTo(e4), 0);
  }

  // test the compare method in the EdgeComparator class
  void testCompare(Tester t) {
    Edge e1 = new Edge(new Posn(1, 1), new Posn(1, 2), 10);
    Edge e2 = new Edge(new Posn(2, 3), new Posn(3, 3), 10);
    Edge e3 = new Edge(new Posn(3, 3), new Posn(3, 2), 11);
    Edge e4 = new Edge(new Posn(0, 0), new Posn(1, 0), 9);

    EdgeComparator ec = new EdgeComparator();

    t.checkExpect(ec.compare(e1, e2), 0);
    t.checkExpect(ec.compare(e1, e3), -1);
    t.checkExpect(ec.compare(e1, e4), 1);
    t.checkExpect(ec.compare(e3, e4), 2);
    t.checkExpect(ec.compare(e4, e4), 0);
  }

  // tests that all the key triggers function properly in the onKeyEvent method
  void testOnKeyEvent(Tester t) {
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

    ArrayList<Posn> maze2Path = new ArrayList<Posn>();
    maze2Path.add(new Posn(2, 2));
    maze2Path.add(new Posn(1, 2));
    maze2Path.add(new Posn(0, 2));
    maze2Path.add(new Posn(0, 1));
    maze2Path.add(new Posn(0, 0));

    t.checkExpect(maze1.getFinalPath(), new ArrayList<Posn>());

    maze1.onKeyEvent("b");

    t.checkExpect(maze1.getFinalPath(), maze1Path);
    t.checkExpect(maze1.getScore(), 6);

    this.initTestConditions();

    t.checkExpect(maze1.getFinalPath(), new ArrayList<Posn>());

    maze1.onKeyEvent("d");

    t.checkExpect(maze1.getFinalPath(), maze1Path);
    t.checkExpect(maze1.getScore(), 4);

    this.initTestConditions();

    t.checkExpect(maze1.getHorizToVertRatio() > 0.8, true);
    t.checkExpect(maze1.getHorizToVertRatio() < 1.5, true);

    maze1.onKeyEvent("n");

    t.checkExpect(maze1.getHorizToVertRatio() > 0.8, true);
    t.checkExpect(maze1.getHorizToVertRatio() < 1.5, true);

    maze1.onKeyEvent("v");

    t.checkExpect(maze1.getHorizToVertRatio() < 0.6, true);

    maze1.onKeyEvent("h");

    t.checkExpect(maze1.getHorizToVertRatio() > 1.5, true);

    this.initTestConditions();

    t.checkExpect(maze1.getPlayerPosition(), new Posn(0, 0));

    maze1.onKeyEvent("left");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(0, 0));

    maze1.onKeyEvent("up");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(0, 0));

    maze1.onKeyEvent("right");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(0, 0));

    maze1.onKeyEvent("down");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(0, 1));

    maze1.onKeyEvent("right");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(1, 1));

    maze1.onKeyEvent("right");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(1, 1));

    maze1.onKeyEvent("up");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(1, 0));

    maze1.onKeyEvent("right");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(2, 0));

    maze1.onKeyEvent("down");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(2, 1));

    maze1.onKeyEvent("down");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(2, 2));

    maze1.onKeyEvent("right");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(3, 2));
    t.checkExpect(maze1.getFinalPath(), new ArrayList<Posn>());

    maze1.onKeyEvent("down");

    t.checkExpect(maze1.getPlayerPosition(), new Posn(3, 3));
    t.checkExpect(maze1.getFinalPath(), maze1Path);

    t.checkExpect(maze2.getPlayerPosition(), new Posn(0, 0));

    maze2.onKeyEvent("right");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(1, 0));

    maze2.onKeyEvent("down");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(1, 0));

    maze2.onKeyEvent("left");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(0, 0));

    maze2.onKeyEvent("down");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(0, 1));

    maze2.onKeyEvent("down");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(0, 2));

    maze2.onKeyEvent("right");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(1, 2));
    t.checkExpect(maze2.getFinalPath(), new ArrayList<Posn>());

    maze2.onKeyEvent("right");

    t.checkExpect(maze2.getPlayerPosition(), new Posn(2, 2));
    t.checkExpect(maze2.getFinalPath(), maze2Path);

    this.initTestConditions();
    WorldScene initialScene = maze1.makeScene();
    t.checkExpect(maze1.makeScene(), initialScene);

    maze1.onKeyEvent("s");
    maze1.onTick();

    WorldScene secondScene = maze1.makeScene();
    t.checkExpect(maze1.makeScene(), secondScene);
    t.checkFail(maze1.makeScene(), initialScene);

    maze1.onKeyEvent("s");
    maze1.onTick();

    t.checkExpect(maze1.makeScene(), initialScene);
    t.checkFail(maze1.makeScene(), secondScene);

    maze1.onKeyEvent("s");
    maze1.onTick();

    t.checkExpect(maze1.makeScene(), secondScene);
    t.checkFail(maze1.makeScene(), initialScene);
  }

  // test that the onTick method in Maze functions properly
  void testOnTick(Tester t) {
    this.initTestConditions();
    WorldScene initialScene = maze1.makeScene();
    this.initTestConditions();
    maze1.onKeyEvent("b");
    maze1.onKeyEvent("s");
    maze1.onTick();
    WorldScene blankMaze = maze1.makeScene();
    this.initTestConditions();
    ArrayList<Posn> maze1SquaresColored = new ArrayList<Posn>();
    maze1SquaresColored.add(new Posn(0, 0));

    t.checkExpect(maze1.makeScene(), initialScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    maze1SquaresColored.add(new Posn(0, 0));
    t.checkExpect(maze1.makeScene(), initialScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onKeyEvent("b");

    WorldScene searchScene = maze1.makeScene();
    this.initTestConditions();
    maze1.onTick();
    maze1.onKeyEvent("b");

    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 94);

    maze1SquaresColored.add(new Posn(0, 0));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 281);

    maze1SquaresColored.add(new Posn(0, 1));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 468);

    maze1SquaresColored.add(new Posn(0, 2));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 281, 281);

    maze1SquaresColored.add(new Posn(1, 1));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    this.initTestConditions();
    maze1SquaresColored = new ArrayList<Posn>();
    maze1SquaresColored.add(new Posn(0, 0));
    t.checkExpect(maze1.makeScene(), initialScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    maze1SquaresColored.add(new Posn(0, 0));
    t.checkExpect(maze1.makeScene(), initialScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onKeyEvent("d");

    searchScene = maze1.makeScene();
    this.initTestConditions();
    maze1.onTick();
    maze1.onKeyEvent("d");

    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 94);

    maze1SquaresColored.add(new Posn(0, 0));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 281);

    maze1SquaresColored.add(new Posn(0, 1));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 281, 281);

    maze1SquaresColored.add(new Posn(1, 1));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 281, 94);

    maze1SquaresColored.add(new Posn(1, 0));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 468, 94);

    maze1SquaresColored.add(new Posn(2, 0));
    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onKeyEvent("s");

    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    t.checkExpect(maze1.makeScene(), blankMaze);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onKeyEvent("s");

    t.checkExpect(maze1.makeScene(), blankMaze);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();

    searchScene.placeImageXY(new RectangleImage(185, 185, OutlineMode.SOLID, Color.CYAN), 94, 281);

    t.checkExpect(maze1.makeScene(), searchScene);
    t.checkExpect(maze1.getSquaresColored(), maze1SquaresColored);

    maze1.onTick();
    maze1.onTick();
    maze1.onTick();
    maze1.onTick();
    maze1.onTick();
    maze1.onTick();

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

    t.checkExpect(maze1.getFinalPath(), maze1Path);

    maze1.onTick();

    maze1Path.remove(0);

    t.checkExpect(maze1.getFinalPath(), maze1Path);

    maze1.onTick();

    maze1Path.remove(0);

    t.checkExpect(maze1.getFinalPath(), maze1Path);

    maze1.onTick();

    maze1Path.remove(0);

    t.checkExpect(maze1.getFinalPath(), maze1Path);

    maze1.onTick();

    maze1Path.remove(0);

    t.checkExpect(maze1.getFinalPath(), maze1Path);
  }
}
