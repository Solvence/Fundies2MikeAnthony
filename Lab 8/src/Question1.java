import java.util.Iterator;

import tester.Tester;

// represents a function object to compare two objects
interface IComparator<T> { 
  // compares two given objects: returns a negative number if left comes before right, a positive
  // number if right comes before left, and 0 if the two are equivalent
  int compare(T left, T right); 
}

// represents a function object with signature [A -> R]
interface IFunc<A, R> { 
  // applies this function object to the given argument
  R apply(A arg); 
}

// to represent a binary search tree (BST)
class BinarySearchTree<T> implements Iterable<T> {
  IComparator<T> order;  // the comparison function by which this BST is ordered by
  IBinTree<T> tree;  // the root node of the binary search tree, or a leaf
  
  // the constructor
  BinarySearchTree(IComparator<T> order, IBinTree<T> tree) { 
    this.order = order; 
    this.tree = tree;
  }
  
  // to insert an item into this BST in left-to-right sorted order
  // DESIGN CHOICE: I decided against using mutation since I believe that the lack of mutation is
  // more flexible. I don't know how the user of this class wishes to use Binary Search Trees, but
  // if they wanted mutation, they can simply do bst = bst.insert(item); With an insert
  // implementation that uses mutation, however, a user is forced into the mutable implementation.
  BinarySearchTree<T> insert(T item) {
    return new BinarySearchTree<T>(this.order, this.tree.insert(item, this.order));
  }
  
  // returns the T value in this BST for which the search returns zero, or null if no such value 
  // can be found
  T find(IFunc<T, Integer> search) {
    return this.tree.findInTree(search);
  }
  
  // computes the number of nodes in this BST
  int size() {
    return this.tree.size();
  }
  
  // returns the item with the given index (starting from zero) in this BST when counting from the 
  // leftmost item
  T get(int index) {
    return this.tree.getItem(index);
  }

  // to make this BST representable as a sequence that can be iterated over
  public Iterator<T> iterator() {
    return new InOrderBinTreeIterator<T>(this.tree);
  }
}

// to represent a binary tree
interface IBinTree<T> { 
  // to insert an item into this binary tree sorted from left-to-right given a comparator function 
  // object
  IBinTree<T> insert(T item, IComparator<T> order);
  
  // returns the T value in this tree for which the search returns zero, or null if no such value 
  // can be found
  T findInTree(IFunc<T, Integer> search);
  
  // computes the number of nodes in this binary tree
  int size();
  
  // returns the item with the given index (starting from zero) in this binary tree when counting 
  // from the leftmost item
  T getItem(int index);
}

// to represent a node in a binary tree
class Node<T> implements IBinTree<T> {
  T value;
  IBinTree<T> left, right;
  
  // the constructor
  Node(T val, IBinTree<T> left, IBinTree<T> right) {
    this.value = val;
    this.left = left;
    this.right = right;
  }
  
  // to insert an item into this node sorted from left-to-right given a comparator function object
  public IBinTree<T> insert(T item, IComparator<T> order) {
    if (order.compare(item, this.value) == 0) {
      return this;
    } else if (order.compare(item, this.value) < 0) {
      return new Node<T>(this.value, this.left.insert(item, order), this.right);
    } else {
      return new Node<T>(this.value, this.left, this.right.insert(item, order));
    }
  }
  
  // returns the T value in this node for which the search returns zero, or null if no such value 
  // can be found
  public T findInTree(IFunc<T, Integer> search) {
    if (search.apply(this.value) == 0) {
      return this.value;
    } else if (search.apply(this.value) > 0) {
      return this.left.findInTree(search);
    } else {
      return this.right.findInTree(search);
    }
  }
  
  // computes the number of nodes in the binary tree with this node as the root
  public int size() {
    return 1 + this.left.size() + this.right.size();
  }
  
  // returns the item with the given index (starting from zero) in this node when counting 
  // from the leftmost item
  public T getItem(int index) {
    if (index == this.left.size()) {
      return this.value;
    } else if (index < this.left.size()) {
      return this.left.getItem(index);
    } else {
      return this.right.getItem(index - this.left.size() - 1);
    }
  }
}

// to represent a leaf of a binary tree
class Leaf<T> implements IBinTree<T> { 
  // to insert an item into this leaf sorted from left-to-right given a comparator function object
  public IBinTree<T> insert(T item, IComparator<T> order) {
    return new Node<T>(item, new Leaf<T>(), new Leaf<T>());
  }
  
  // returns the T value in this leaf for which the search returns zero, or null if no such value 
  // can be found
  public T findInTree(IFunc<T, Integer> search) {
    return null;
  }
  
  // computes the number of nodes in this leaf
  public int size() {
    return 0;
  }
  
  // returns the item with the given index (starting from zero) in this leaf when counting 
  // from the leftmost item
  public T getItem(int index) {
    throw new IndexOutOfBoundsException();
  }
}

// to sequentially iterate over a binary tree from left to right, one item at a time
class InOrderBinTreeIterator<T> implements Iterator<T> {
  IBinTree<T> tree;  // the tree that this iterator iterates over
  int nextIdx;  // the index of the next item to be returned in the tree
  
  // the constructor
  InOrderBinTreeIterator(IBinTree<T> tree) {
    this.tree = tree;
    this.nextIdx = 0;
  }

  // does this sequence of items in the binary tree have at least one more item?
  public boolean hasNext() {
    return this.nextIdx < this.tree.size();
  }

  // get the next value in this sequence
  // EFFECT: advance the iterator to the subsequent value
  public T next() {
    T answer = this.tree.getItem(this.nextIdx);
    this.nextIdx += 1;
    return answer;
  }
}

/*
 * PART F:
 * the big-O running time of this loop is O(n^2) where n is the number of nodes in myBinSearchTree
 * Two things happen in every iteration of the loop: a call to hasNext and a call to next. The call
 * to hasNext is itself O(n) since it calls the size() method, which is a method that makes
 * a recursive call to every node in both subtrees. The call to next is O(n) at most since getItem()
 * considers each node at most once, but can potentially get called on all nodes in the tree. 
 * 
 * The number of iterations will be the number of nodes since hasNext will return false and the loop
 * will terminate once the index reaches the last node. The running time is thus O(n * n) = O(n^2)
 * 
 * To make this potentially faster, we first must eliminate the call to size(). This may be done by
 * removing the node in consideration after every call to next(), and replacing it with its right
 * subtree. As a result, hasNext() only has to check if the tree is a node. The next() method, then,
 * would return and remove the leftmost element in the tree. However, this is still O(n) for finding
 * the leftmost element. As a result, we may revise our design for BinarySearchTree so that each
 * node has its left and right subtrees differing at most by 1 node in count. Then, finding the
 * leftmost element would take (O(log n)) time, and the big-O running time of the loop would be 
 * O(n log n)
 */

// an example of a IComparator function object that compares String objects in alphabetical order
// (Strings that come later in the alphabet will return positive when compared to earlier Strings)
class StringAlphaCompare implements IComparator<String> {
  // to apply this comparator object to the two Strings and compare them
  public int compare(String left, String right) {
    return left.compareTo(right);
  }
}

// an example of a IComparator function object that compares Integer objects in value order
class IntCompare implements IComparator<Integer> {
  // to apply this comparator object to the two Integers and compare them
  public int compare(Integer left, Integer right) {
    return left - right;
  }
}

// an example of an IFunc that simulates a guessing game for a particular Integer value
// returns 0 if the guess is equal to the value, a negative number if the guess is less than the
// value, and a positive number if the guess is greater than the value
class IntSearch implements IFunc<Integer, Integer> {
  Integer value;
  
  // the constructor
  IntSearch(Integer value) {
    this.value = value;
  }
  
  // returns 0 if the guess is equal to the value, a negative number if the guess is less than the
  // value, and a positive number if the guess is greater than the value
  public Integer apply(Integer guess) {
    return guess - value;
  }
}

// an example of an IFunc that simulates a guessing game for a particular String value
// returns 0 if the guess is equal to the value, a negative number if the guess is less than the
// value, and a positive number if the guess is greater than the value, according to alphabetical
// comparison
class StringSearch implements IFunc<String, Integer> {
  String value;
  
  // the constructor
  StringSearch(String value) {
   this.value = value;
  }
  
  // returns 0 if the guess is equal to the value, a negative number if the guess is less than the
  // value, and a positive number if the guess is greater than the value
  public Integer apply(String guess) {
   return guess.compareTo(value);
  }
}

// Examples and tests for methods and data in question 1
class ExamplesQ1 {
  // examples of IBinTree<Integer>
  IBinTree<Integer> intLF = new Leaf<Integer>();
  IBinTree<Integer> intTree1 = new Node<Integer>(10, intLF, intLF);
  IBinTree<Integer> intTree2 = new Node<Integer>(20, intLF, intLF);
  IBinTree<Integer> intTree3 = new Node<Integer>(15, intTree1, intTree2);
  IBinTree<Integer> intTree4 = new Node<Integer>(15, 
      new Node<Integer>(10, 
          new Node<Integer>(-5, intLF, intLF), 
          intLF), 
      intTree2);
  IBinTree<Integer> intTree5 = new Node<Integer>(15, intTree1, 
      new Node<Integer>(20, intLF, 
          new Node<Integer>(21, intLF, intLF)));
  IBinTree<Integer> intTree6 = new Node<Integer>(15, 
      new Node<Integer>(10, intLF, new Node<Integer>(13, intLF, intLF)), 
      intTree2);
  
  // examples of IBinTree<String>
  IBinTree<String> strLF = new Leaf<String>();
  IBinTree<String> strTree1 = new Node<String>("banana", strLF, strLF);
  IBinTree<String> strTree2 = new Node<String>("yeet", strLF, strLF);
  IBinTree<String> strTree3 = new Node<String>("nana", strTree1, strTree2);
  
  IBinTree<String> strTree6 = new Node<String>("nana", 
      new Node<String>("banana", strLF, new Node<String>("drag", strLF, strLF)), 
      strTree2);
  
  InOrderBinTreeIterator<Integer> intLFIterator;
  InOrderBinTreeIterator<Integer> intTree6Iterator;
  InOrderBinTreeIterator<String> strTree3Iterator;
  
  // initializes all iterators
  void initIteratorTestData() {
    intLFIterator = new InOrderBinTreeIterator<Integer>(intLF);
    intTree6Iterator = new InOrderBinTreeIterator<Integer>(intTree6);
    strTree3Iterator = new InOrderBinTreeIterator<String>(strTree3);
  }
  
  // tests the insert method in the BinarySearchTree class
  void testInsertBinarySearchTree(Tester t) {
    // tests that the find method in BinarySearchTree delegates correctly for an empty int tree
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intLF).insert(10), 
        new BinarySearchTree<Integer>(new IntCompare(), intLF.insert(10, new IntCompare())));
    // tests that the find method in BinarySearchTree delegates correctly for a non-empty int tree
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intTree3).insert(13), 
        new BinarySearchTree<Integer>(new IntCompare(), intTree3.insert(13, new IntCompare())));
    // tests that the find method in BinarySearchTree delegates correctly for a String tree
    t.checkExpect(new BinarySearchTree<String>(new StringAlphaCompare(), strTree3).insert("drag"), 
        new BinarySearchTree<String>(new StringAlphaCompare(), 
            strTree3.insert("drag", new StringAlphaCompare())));
  }
  
  // tests the insert method in the IBinTree interface
  // also tests the function objects IntCompare and StringAlphaCompare
  void testInsertBinTree(Tester t) {
    // tests inserting into an empty tree (a leaf)
    t.checkExpect(intLF.insert(10, new IntCompare()), intTree1);
    // tests ignoring a value if attempting to insert a value that exists in the tree already
    t.checkExpect(intTree3.insert(20, new IntCompare()), intTree3);
    // tests inserting a value that is less than all the values in a tree
    t.checkExpect(intTree3.insert(-5, new IntCompare()), intTree4);
    // tests inserting a value that is greater than all the values in a tree
    t.checkExpect(intTree3.insert(21, new IntCompare()), intTree5);
    // tests inserting a value that is less than some values and greater than some in a tree
    t.checkExpect(intTree3.insert(13, new IntCompare()), intTree6);
    // tests the insert method on a tree with Strings and a different comparator class
    t.checkExpect(strTree3.insert("drag", new StringAlphaCompare()), strTree6);
  }
  
  // tests the find method in the BinarySearchTree class
  void testFind(Tester t) {
    // tests that the insert method in BinarySearchTree delegates correctly for an empty int tree
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intLF).find(new IntSearch(10)), 
        intLF.findInTree(new IntSearch(10)));
    // tests that the insert method in BinarySearchTree delegates correctly for a non-empty int tree
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intTree6).find(new IntSearch(13)), 
        intTree6.findInTree(new IntSearch(13)));
    // tests that the insert method in BinarySearchTree delegates correctly for a String tree
    t.checkExpect(new BinarySearchTree<String>(new StringAlphaCompare(), strTree6)
        .find(new StringSearch("drag")), strTree6.findInTree(new StringSearch("drag")));
  }
  
  // tests the findInTree method in the IBinTree interface
  // also tests the function objects IntSearch and StringSearch
  void testFindInTree(Tester t) {
    // tests finding a value in an empty tree, which is inevitably missing
    t.checkExpect(intLF.findInTree(new IntSearch(15)), null);
    // tests finding a value that is not present in a node
    t.checkExpect(intTree3.findInTree(new IntSearch(13)), null);
    // tests finding a value that is present deep into a tree
    t.checkExpect(intTree6.findInTree(new IntSearch(13)), 13);
    // tests finding a value that is the value of the root node
    t.checkExpect(intTree6.findInTree(new IntSearch(15)), 15);
    // tests finding a String value in a tree of Strings
    t.checkExpect(strTree6.findInTree(new StringSearch("drag")), "drag");
  }
  
  // test the size method in the BinarySearchTree class
  void testSizeBinarySearchTree(Tester t) {
    // tests finding the size of a leaf (delegating works for leaf)
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intLF).size(), 0);
    // tests finding the size of a tree with nodes (delegating works for nodes)
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intTree6).size(), 4);
    // tests finding the size of a tree of Strings (delegating works for non-Integer trees)
    t.checkExpect(new BinarySearchTree<String>(new StringAlphaCompare(), strTree3).size(), 3);
  }
  
  // test the size method in the IBinTree interface
  void testSizeBinTree(Tester t) {
    // tests finding the size of a leaf
    t.checkExpect(intLF.size(), 0);
    // tests finding the size of a tree with nodes
    t.checkExpect(intTree6.size(), 4);
    // tests finding the size of a tree of Strings
    t.checkExpect(strTree3.size(), 3);
  }
  
  // tests the get method in the BinarySearchTree class
  void testGet(Tester t) {
    // tests that the method delegates properly for empty trees (properly returns an exception)
    t.checkException(new IndexOutOfBoundsException(), 
        new BinarySearchTree<Integer>(new IntCompare(), intLF), "get", 0);
    // tests that delegating works properly for non-empty trees
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intTree6).get(1), 
        intTree6.getItem(1));
    // tests that delegating works properly for non-Integer trees
    t.checkExpect(new BinarySearchTree<String>(new StringAlphaCompare(), strTree6).get(3), 
        strTree6.getItem(3));
  }
  
  // tests the getItem method in  the IBinTree interface
  void testGetItem(Tester t) {
    // tests that the method properly throws an exception if the tree is empty
    t.checkException(new IndexOutOfBoundsException(), intLF, "getItem", 0);
    // tests that the method throws an exception when given an index >= the number of nodes
    t.checkException(new IndexOutOfBoundsException(), intTree3, "getItem", 3);
    // tests that the method properly returns an intermediate item
    t.checkExpect(intTree6.getItem(1), 13);
    // tests that the method properly returns the root of a tree
    t.checkExpect(intTree6.getItem(2), 15);
    // tests that the method functions properly for data other than Integers
    t.checkExpect(strTree6.getItem(3), "yeet");
  }
  
  // tests the iterator method in the BinarySearchTree class
  void testIterator(Tester t) {
    // tests that the iterator is properly constructed for an empty BST
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intLF).iterator(), 
        new InOrderBinTreeIterator<Integer>(intLF));
    // tests that the iterator is properly constructed for a non-empty BST
    t.checkExpect(new BinarySearchTree<Integer>(new IntCompare(), intTree6).iterator(), 
        new InOrderBinTreeIterator<Integer>(intTree6));
    // tests that the iterator is properly constructed for a non-Integer BST
    t.checkExpect(new BinarySearchTree<String>(new StringAlphaCompare(), strTree3).iterator(), 
        new InOrderBinTreeIterator<String>(strTree3));
  }
  
  // tests the hasNext method in the InOrderBinTreeIterator class
  void testHasNext(Tester t) {
    this.initIteratorTestData();
    // tests that the sequence doesn't have a next item when it's empty
    t.checkExpect(intLFIterator.hasNext(), false);
    // tests that the sequence does have a next item when initially it's not empty
    t.checkExpect(intTree6Iterator.hasNext(), true);
    
    intTree6Iterator.next();  // index 0 -> 1
    intTree6Iterator.next();  // index 1 -> 2
    intTree6Iterator.next();  // index 2 -> 3
    intTree6Iterator.next();  // index 3 -> 4
    
    // tests that the sequence does not have a next item when the index >= the size of the sequence
    t.checkExpect(intTree6Iterator.hasNext(), false);
    // tests that the sequence does have a next item when initially it's not empty (for Strings)
    t.checkExpect(strTree3Iterator.hasNext(), true);
  }
  
  // test the next method in the InOrderBinTreeIterator class
  void testNext(Tester t) {
    this.initIteratorTestData();
    // tests that next() properly throws an exception when iterating over an empty tree
    t.checkException(new IndexOutOfBoundsException(), intLFIterator, "next");
    // tests that the conditions in intTree6Iterator exist as expected before any next() calls have
    // been made
    t.checkExpect(intTree6Iterator.nextIdx, 0);
    // tests that the first call to next properly returns the first element in a non-empty tree, 
    // from left to right
    t.checkExpect(intTree6Iterator.next(), 10);
    // tests that the expected changes have occurred within the intTree6Iterator object
    t.checkExpect(intTree6Iterator.nextIdx, 1);
    // tests that the second call to next properly returns the second element in a tree, 
    // from left to right
    t.checkExpect(intTree6Iterator.next(), 13);
    // tests that the expected changes have occurred within the intTree6Iterator object
    t.checkExpect(intTree6Iterator.nextIdx, 2);
    
    intTree6Iterator.next();  // index 2 -> 3
    intTree6Iterator.next();  // index 3 -> 4
    
    // checks that the next method properly throws an exception when attempting to compute the next
    // element when the sequence has already reached the end
    t.checkException(new IndexOutOfBoundsException(), intTree6Iterator, "next");
    // tests that the conditions in strTree3Iterator exist as expected before any next() calls have
    // been made
    t.checkExpect(strTree3Iterator.nextIdx, 0);
    // tests that the first call to next properly returns the first element in a non-empty tree, 
    // from left to right (functions properly on non-Integer trees)
    t.checkExpect(strTree3Iterator.next(), "banana");
    // tests that the expected changes have occurred within the strTree3Iterator object
    t.checkExpect(strTree3Iterator.nextIdx, 1);
  }
}