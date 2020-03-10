import java.util.Iterator;

import javalib.worldimages.Posn;
import tester.Tester;

// Represents a Sentinel at the beginning or end of a deque,
// or a node in the middle of a deque
abstract class ANode<T> {
  // Fields
  ANode<T> next;
  ANode<T> prev;

  // to compute the number of nodes in the remainder of this deque list
  abstract int countHelp();

  // Returns this node's data or an error if it's a Sentinel because sentinels can
  // not be removed
  // since they are the keystone of a deque list, and removes it from the deque
  // EFFECT: Removes this node from the deque list (unless it's a Sentinel)
  abstract T remove();

  // Finds the first Node the for which the given predicate returns true
  abstract ANode<T> findHelp(IPred<T> pred);
}

// represents the dummy node before the first actual node in the deque list and after the last
// actual node in the deque list
class Sentinel<T> extends ANode<T> {
  // the constructor
  Sentinel() {
    this.next = this;
    this.prev = this;
  }

  // To count how many nodes are in the deque list with this sentinel as its head
  int count() {
    return this.next.countHelp();
  }

  // Returns 0 if this sentinel's next is itself, as there are no nodes in the
  // deque
  public int countHelp() {
    return 0;
  }

  // Removes and returns the first node in the deque list with this sentinel as
  // its head, or
  // throws an error if said deque list is empty
  // EFFECT: removes the first node in the deque list with this sentinel as its
  // head
  T removeFirst() {
    return this.next.remove();
  }

  // Removes and returns the last node in the deque list with this sentinel as its
  // head,
  // or throws an error if said deque list is empty
  // EFFECT: removes the last node in the deque list with this sentinel as its
  // head
  T removeLast() {
    return this.prev.remove();
  }

  // Removing this Sentinel causes an error since only Nodes can be removed
  T remove() {
    throw new RuntimeException("Error: Can't remove from an empty Deque list");
  }

  // Computes the first Node in the deque list with this sentinel as its head for
  // which the
  // given predicate returns true
  ANode<T> find(IPred<T> pred) {
    return this.next.findHelp(pred);
  }

  // If no Node for which the given predicate returns true, the Sentinel gets
  // returned
  ANode<T> findHelp(IPred<T> pred) {
    return this;
  }
}

// Represents a Node of a Deque
class Node<T> extends ANode<T> {
  // Field
  T data;

  // the constructor that simply takes in the data value for this node
  Node(T data) {
    this.data = data;
    this.next = null;
    this.prev = null;
  }

  // the constructor that takes in the next and previous nodes with respect to
  // this node
  // if either the next or previous nodes are null, the constructor will throw an
  // error
  // EFFECT: sets the previous and next nodes to refer respectively to this node
  // as well
  Node(T data, ANode<T> next, ANode<T> prev) {
    if (next == null) {
      throw new IllegalArgumentException("next node is null");
    }
    if (prev == null) {
      throw new IllegalArgumentException("previous node is null");
    }
    this.data = data;
    this.next = next;
    this.prev = prev;

    this.next.prev = this;
    this.prev.next = this;
  }

  // Computes the number of nodes in the deque including and after this node
  int countHelp() {
    return 1 + this.next.countHelp();
  }

  // Removes and returns this node's data
  // EFFECT: sets the next and previous nodes to refer to each other, effectively
  // skipping this
  // node
  T remove() {
    this.prev.next = this.next;
    this.next.prev = this.prev;
    return this.data;
  }

  // Finds the Node for which the given predicate returns true
  ANode<T> findHelp(IPred<T> pred) {
    if (pred.apply(this.data)) {
      return this;
    }
    else {
      return this.next.findHelp(pred);
    }
  }
}

// Represents a double ended queue
class Deque<T> implements Iterable {
  Sentinel<T> header;

  // the constructor that takes in no arguments and simply constructs an empty
  // deque
  Deque() {
    this.header = new Sentinel<T>();
  }

  // the constructor that takes in a sentinel
  Deque(Sentinel<T> header) {
    this.header = header;
  }

  // Computes the number of Nodes in this deque
  int size() {
    return this.header.count();
  }

  // Adds the given data as a node to the head of the deque
  // EFFECT: Modfies the deque to add the given data as a Node to the start of the
  // deque
  void addAtHead(T data) {
    new Node<T>(data, this.header.next, this.header);
  }

  // Adds the given data as a node to the tail of the deque
  // EFFECT: Modfies the deque to add the given data as a Node to the end of the
  // deque
  void addAtTail(T data) {
    new Node<T>(data, this.header, this.header.prev);
  }

  // Removes the first node and returns the data in the first node in the deque
  // EFFECT: Removes the first element from the deque
  // Throws an error if deque is empty
  T removeFromHead() {
    return this.header.removeFirst();
  }

  // Removes and returns the data in the last node in the deque
  // EFFECT: Removes the last element from the deque
  // Throws an error if deque is empty
  T removeFromTail() {
    return this.header.removeLast();
  }

  // Finds the first Node in this deque for which the given predicate returns true
  // If none returns true, returns the Sentinel of the deque
  ANode<T> find(IPred<T> pred) {
    return this.header.find(pred);
  }

  // Removes the given Node from the deque
  // EFFECT: Modifies this deque to remove the given Node
  void removeNode(ANode<T> node) {
    node.remove();
  }

  @Override
  public Iterator<T> iterator() {
    // TODO Auto-generated method stub
    return new ForwardDequeIterator<T>(this);
  }
}

// Represents a boolean-valued question over values of type T
interface IPred<T> {
  // Applies this function object to the given input
  boolean apply(T t);
}

// The following IPred<T> implementations are made for testing
// to represent a function object to determine if a String has 3 characters
class HasThreeChars implements IPred<String> {
  // does the given string have three characters?
  public boolean apply(String str) {
    return str.length() == 3;
  }
}

// to represent a function object to determine if a position (Posn) has a y-coordinate of 1
class YCoord1 implements IPred<Posn> {
  // does the given position have a y coordinate of 1?
  public boolean apply(Posn p) {
    return p.y == 1;
  }
}

class ForwardDequeIterator<T> implements Iterator<T> {

  ANode<T> currentNode;

  ForwardDequeIterator(Deque<T> deq) {
    this.currentNode = deq.header.next;
  }

  @Override
  public boolean hasNext() {
    // TODO Auto-generated method stub
    return (this.currentNode.countHelp() >= 1);
  }

  @Override
  public T next() {

    if (!this.hasNext()) {
      throw new RuntimeException("No next");
    }
    // TODO Auto-generated method stub
    T answer = ((Node<T>) currentNode).data;
    currentNode = currentNode.next;
    return answer;
  }

}

// examples and tests
class ExamplesDeque {
  Deque<String> deque1;

  Deque<String> deque2;
  Node<String> nodeABC;
  Node<String> nodeBCD;
  Node<String> nodeCDE;
  Node<String> nodeDEF;

  Deque<String> deque3;
  Node<String> nodeREE;
  Node<String> nodeHA;
  Node<String> nodeBLAH;
  Node<String> nodeNAH;

  Deque<Posn> deque4;
  Node<Posn> nodePosn1;
  Node<Posn> nodePosn2;
  Node<Posn> nodePosn3;
  Node<Posn> nodePosn4;

  void initTestConditions() {
    deque1 = new Deque<String>();

    deque2 = new Deque<String>();
    nodeABC = new Node<String>("abc", deque2.header, deque2.header);
    nodeBCD = new Node<String>("bcd", deque2.header, nodeABC);
    nodeCDE = new Node<String>("cde", deque2.header, nodeBCD);
    nodeDEF = new Node<String>("def", deque2.header, nodeCDE);

    deque3 = new Deque<String>();
    nodeREE = new Node<String>("ree", deque3.header, deque3.header);
    nodeHA = new Node<String>("ha", deque3.header, nodeREE);
    nodeBLAH = new Node<String>("blah", deque3.header, nodeHA);
    nodeNAH = new Node<String>("nah", deque3.header, nodeBLAH);

    deque4 = new Deque<Posn>();
    nodePosn1 = new Node<>(new Posn(0, 0), deque4.header, deque4.header);
    nodePosn2 = new Node<>(new Posn(0, 1), deque4.header, nodePosn1);
    nodePosn3 = new Node<>(new Posn(1, 1), deque4.header, nodePosn2);
    nodePosn4 = new Node<>(new Posn(2, 1), deque4.header, nodePosn3);
  }

  void testRemoveFromHead(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeFromHead();

    t.checkExpect(deque2.header.next, nodeBCD);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeFromHead();

    t.checkExpect(deque2.header.next, nodeCDE);
    t.checkExpect(deque2.header.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.prev, nodeDEF);

    t.checkExpect(deque1.header.next, deque1.header);

    t.checkException(new RuntimeException("Error: Can't remove from an empty Deque list"), deque1,
        "removeFromHead");

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    deque4.removeFromHead();

    t.checkExpect(deque4.header.next, nodePosn2);
    t.checkExpect(deque4.header.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn4);
  }

  void testRemoveFromTail(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeFromTail();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeCDE);

    deque2.removeFromTail();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeABC);
    t.checkExpect(deque2.header.prev, nodeBCD);

    t.checkExpect(deque1.header.next, deque1.header);

    t.checkException(new RuntimeException("Error: Can't remove from an empty Deque list"), deque1,
        "removeFromTail");

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    deque4.removeFromTail();

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn3);
  }

  void testFind(Tester t) {
    this.initTestConditions();
    t.checkExpect(deque2.find(new HasThreeChars()), nodeABC);
    t.checkExpect(deque3.find(new HasThreeChars()), nodeREE);
    t.checkExpect(deque1.find(new HasThreeChars()), deque1.header);
    t.checkExpect(deque4.find(new YCoord1()), nodePosn2);
  }

  void testAddAtHead(Tester t) {
    this.initTestConditions();
    t.checkExpect(((Node<Posn>) deque4.header.next).data, new Posn(0, 0));
    t.checkExpect(((Node<String>) deque3.header.next).data, "ree");
    t.checkExpect(deque1.header.next, deque1.header);
    deque4.addAtHead(new Posn(3, 3));
    deque3.addAtHead("SDS");
    deque1.addAtHead("ADA");
    t.checkExpect(((Node<Posn>) deque4.header.next).data, new Posn(3, 3));
    t.checkExpect(((Node<String>) deque3.header.next).data, "SDS");
    t.checkExpect(((Node<String>) deque1.header.next).data, "ADA");
  }

  void testAddAtTail(Tester t) {
    this.initTestConditions();
    t.checkExpect(((Node<Posn>) deque4.header.prev).data, new Posn(2, 1));
    t.checkExpect(((Node<String>) deque3.header.prev).data, "nah");
    t.checkExpect(deque1.header.prev, deque1.header);
    deque4.addAtTail(new Posn(3, 3));
    deque3.addAtTail("SDS");
    deque1.addAtTail("ADA");
    t.checkExpect(((Node<Posn>) deque4.header.prev).data, new Posn(3, 3));
    t.checkExpect(((Node<String>) deque3.header.prev).data, "SDS");
    t.checkExpect(((Node<String>) deque1.header.prev).data, "ADA");
  }

  void testSize(Tester t) {
    this.initTestConditions();
    t.checkExpect(deque1.size(), 0);
    t.checkExpect(deque2.size(), 4);
    t.checkExpect(deque3.size(), 4);
    t.checkExpect(deque4.size(), 4);
  }

  void testCount(Tester t) {
    this.initTestConditions();
    t.checkExpect(deque1.header.count(), 0);
    t.checkExpect(deque2.header.count(), 4);
    t.checkExpect(deque3.header.count(), 4);
    t.checkExpect(deque4.header.count(), 4);
  }

  void testCountHelp(Tester t) {
    this.initTestConditions();
    t.checkExpect(new Sentinel<Integer>().countHelp(), 0);
    t.checkExpect(nodeNAH.countHelp(), 1);
    t.checkExpect(nodeHA.countHelp(), 3);
    t.checkExpect(nodeCDE.countHelp(), 2);
    t.checkExpect(nodeREE.countHelp(), 4);
  }

  void testRemoveFirst(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.header.removeFirst();

    t.checkExpect(deque2.header.next, nodeBCD);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.header.removeFirst();

    t.checkExpect(deque2.header.next, nodeCDE);
    t.checkExpect(deque2.header.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.prev, nodeDEF);

    t.checkExpect(deque1.header.next, deque1.header);

    t.checkException(new RuntimeException("Error: Can't remove from an empty Deque list"),
        deque1.header, "removeFirst");

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    deque4.header.removeFirst();

    t.checkExpect(deque4.header.next, nodePosn2);
    t.checkExpect(deque4.header.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn4);
  }

  void testRemoveLast(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.header.removeLast();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeCDE);

    deque2.header.removeLast();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeABC);
    t.checkExpect(deque2.header.prev, nodeBCD);

    t.checkExpect(deque1.header.next, deque1.header);

    t.checkException(new RuntimeException("Error: Can't remove from an empty Deque list"),
        deque1.header, "removeLast");

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    deque4.header.removeLast();

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn3);
  }

  void testRemoveNode(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeNode(nodeBCD);

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeNode(nodeBCD);

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeNode(nodeCDE);

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeABC);
    t.checkExpect(deque2.header.prev, nodeDEF);

    deque2.removeNode(nodeDEF);

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeABC);

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    deque4.removeNode(nodePosn3);

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn4);
  }

  void testRemove(Tester t) {
    this.initTestConditions();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeBCD);
    t.checkExpect(deque2.header.next.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.prev, nodeDEF);

    nodeBCD.remove();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    nodeBCD.remove();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeCDE);
    t.checkExpect(deque2.header.next.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeDEF);

    nodeCDE.remove();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, nodeDEF);
    t.checkExpect(deque2.header.next.next.next, deque2.header);
    t.checkExpect(deque2.header.next.next.next.next, nodeABC);
    t.checkExpect(deque2.header.prev, nodeDEF);

    nodeDEF.remove();

    t.checkExpect(deque2.header.next, nodeABC);
    t.checkExpect(deque2.header.next.next, deque2.header);
    t.checkExpect(deque2.header.prev, nodeABC);

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn3);
    t.checkExpect(deque4.header.next.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.prev, nodePosn4);

    nodePosn3.remove();

    t.checkExpect(deque4.header.next, nodePosn1);
    t.checkExpect(deque4.header.next.next, nodePosn2);
    t.checkExpect(deque4.header.next.next.next, nodePosn4);
    t.checkExpect(deque4.header.next.next.next.next, deque4.header);
    t.checkExpect(deque4.header.prev, nodePosn4);

    t.checkException(new RuntimeException("Error: Can't remove from an empty Deque list"),
        deque4.header, "remove");
  }

  void testFindInSentinel(Tester t) {
    this.initTestConditions();
    t.checkExpect(deque2.header.find(new HasThreeChars()), nodeABC);
    t.checkExpect(deque3.header.find(new HasThreeChars()), nodeREE);
    t.checkExpect(deque1.header.find(new HasThreeChars()), deque1.header);
    t.checkExpect(deque4.header.find(new YCoord1()), nodePosn2);
  }

  void testFindHelp(Tester t) {
    this.initTestConditions();
    t.checkExpect(deque1.header.findHelp(new HasThreeChars()), deque1.header);
    t.checkExpect(deque4.header.findHelp(new YCoord1()), deque4.header);

    t.checkExpect(nodeBCD.findHelp(new HasThreeChars()), nodeBCD);
    t.checkExpect(nodeABC.findHelp(new HasThreeChars()), nodeABC);
    t.checkExpect(nodeHA.findHelp(new HasThreeChars()), nodeNAH);

    t.checkExpect(nodePosn1.findHelp(new YCoord1()), nodePosn2);
    t.checkExpect(nodePosn4.findHelp(new YCoord1()), nodePosn4);
  }

  void testNodeConstructorException(Tester t) {
    t.checkConstructorException(new IllegalArgumentException("next node is null"), "Node", "blah",
        null, null);
    t.checkConstructorException(new IllegalArgumentException("next node is null"), "Node", "blah",
        null, deque2.header);
    t.checkConstructorException(new IllegalArgumentException("previous node is null"), "Node",
        new Posn(3, 2), deque4.header, null);
  }

}