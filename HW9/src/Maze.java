import java.util.HashMap;
import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class UnionFind {
  
  
  private final HashMap<Posn, Posn> representatives;
  private final ArrayList<Edge> edgesInTree;
  private final ArrayList<Edge> worklist;
  
  UnionFind(ArrayList<Posn> graphLocations){
    this.representatives = new HashMap<Posn, Posn>();
    for (Posn p : graphLocations) {
      this.representatives.put(p, p);
    }
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = new ArrayList<Edge>();
  }
  
  void sortEdges() {
    //this.edgesInTree.sort();
  }
  
}

class Edge {
  
  Posn src;
  Posn dest;
  
  int weight;
}

class compareEdges extends Comparator<Edge>{
  
}

