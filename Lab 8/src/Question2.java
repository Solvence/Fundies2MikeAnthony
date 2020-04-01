import java.util.ArrayList;
import java.util.Iterator;

import tester.Tester;

// A generic pair of values. You may access its fields directly
// (similar to Posn), but you may not add any methods to this class.
class Pair<X, Y> {
  X first;
  Y second;
  Pair(X first, Y second) { this.first = first; this.second = second; }
}

// A basic interface to a multi-set of values of type T. No guarantees are
// made about the order of values retrieved during iteration over this multi-set.
interface IMultiSet<T> extends Iterable<Pair<T, Integer>> {
  // How often does the given item appear in this multi set?
  // (If it is not present, then return 0.)
  int itemCount(T item);
  // The given multiset is a subset of this multiset if every item
  // in the given multiset is present in this one, with a count
  // that’s less than or equal to the count in this multiset.
  boolean hasSubset(IMultiSet<T> other);
  // EFFECT: adds the given item into this multiset.
  void add(T item);
}

// Allows creating sets of values where the elements of the set are *comparable*.
class OrderableMultiSet<T> implements IMultiSet<T> {
  IComparator<T> comp;
  ArrayList<Pair<T, Integer>> set;  // represents the items in this set and the amount of then, no 
  // two pair elements may have the same T item
  
  // the constructor
  OrderableMultiSet(IComparator<T> comp) { 
    this.comp = comp;
    this.set = new ArrayList<Pair<T, Integer>>();
  }
  
  // How often does the given item appear in this multi set?
  // (If it is not present, then return 0.)
  public int itemCount(T item) {
    for (Pair<T, Integer> element: this.set) {
      if (this.comp.compare(element.first, item) == 0) {
        return element.second;
      }
    }
    return 0;
  }
  
  // The given multiset is a subset of this multiset if every item
  // in the given multiset is present in this one, with a count
  // that’s less than or equal to the count in this multiset.
  public boolean hasSubset(IMultiSet<T> other) {
    for (Pair<T, Integer> element: other) {
      if (this.itemCount(element.first) == 0) {
        return false;
      }
    }
    return true;
  }
  
  // EFFECT: adds the given item into this multiset.
  public void add(T item) {
    if (this.itemCount(item) == 0) {
      this.set.add(new Pair<T, Integer>(item, 1));
    } else {
      for (Pair<T, Integer> element: this.set) {
        if (this.comp.compare(element.first, item) == 0) {
          element.second += 1;
        }
      }
    }
  }
  
  // is the given Object equal to this OrderableMultiSet?
  // Two OrderableMultiSets are equal if they have the same elements and quantity
  public boolean equals(Object other) {
    if (!(other instanceof OrderableMultiSet)) {
      return false;
    }
    
    OrderableMultiSet<T> that = (OrderableMultiSet<T>) other;
    
    return this.hasSubset(that) && that.hasSubset(this);
  }

  // returns a iterable sequence representing this set in no particular order
  public Iterator<Pair<T, Integer>> iterator() {
    return set.iterator();
  }
  
  // computes the hash code for this set object
  public int hashCode() {
    int hashCode = 0;
    for (Pair<T, Integer> item: this.set) {
      hashCode += item.first.hashCode() * item.second;
    }
    return hashCode;
  }
}

// Compares two OrderableMultiSets: returns 0 if the two sets are equal, a positive number
// if the second set is a subset of the first one, and a negative number by default
class MultiSetOfIntCompare implements IComparator<OrderableMultiSet<Integer>> {
  public int compare(OrderableMultiSet<Integer> left, OrderableMultiSet<Integer> right) {
    if (left.equals(right)) {
      return 0;
    } else if (left.hasSubset(right)) {
      return 1;
    } else {
      return -1;
    }
  }
}

// Examples and tests for methods and data in question 2
class ExamplesQ2 {
  OrderableMultiSet<Integer> intSet1;
  OrderableMultiSet<Integer> intSet2;
  OrderableMultiSet<Integer> intSet3;
  OrderableMultiSet<Integer> intSet4;
  OrderableMultiSet<OrderableMultiSet<Integer>> setSet1;
  OrderableMultiSet<OrderableMultiSet<Integer>> setSet2;
  OrderableMultiSet<OrderableMultiSet<Integer>> setSet3;
  
  void initAddTestData() {
    intSet1 = new OrderableMultiSet<Integer>(new IntCompare());
  }
  
  // test the add method in OrderableMultiSet
  void testAdd(Tester t) {
    this.initAddTestData();
    
    // tests that the conditions remain as expected before any add methods are called
    t.checkExpect(intSet1.set, new ArrayList<Pair<Integer, Integer>>());
    
    intSet1.add(15);
    
    
    ArrayList<Pair<Integer, Integer>> expected = new ArrayList<Pair<Integer, Integer>>();
    expected.add(new Pair<Integer, Integer>(15, 1));
    
    // tests that the expected changes occurred after adding to an empty set
    t.checkExpect(intSet1.set, expected);
    
    intSet1.add(15);
    
    expected.set(0, new Pair<Integer, Integer>(15, 2));
    
    // tests that the expected changes occurred after adding an element that already existed in the
    // set
    t.checkExpect(intSet1.set, expected);
    
    intSet1.add(20);
    
    expected.add(1, new Pair<Integer, Integer>(20, 1));
    
    // tests that the expected changes occurred after adding an element that didn't already exist in
    // the set
    t.checkExpect(intSet1.set, expected);
  }
  
  // initializes all test data, assumes that the add method works as intended
  void initTestData() {
    intSet1 = new OrderableMultiSet<Integer>(new IntCompare());
    intSet2 = new OrderableMultiSet<Integer>(new IntCompare());
    intSet3 = new OrderableMultiSet<Integer>(new IntCompare());
    intSet4 = new OrderableMultiSet<Integer>(new IntCompare());
    
    setSet1 = new OrderableMultiSet<OrderableMultiSet<Integer>>(new MultiSetOfIntCompare());
    setSet2 = new OrderableMultiSet<OrderableMultiSet<Integer>>(new MultiSetOfIntCompare());
    setSet3 = new OrderableMultiSet<OrderableMultiSet<Integer>>(new MultiSetOfIntCompare());
    
    intSet2.add(15);
    intSet2.add(15);
    intSet2.add(14);
    intSet2.add(15);
    
    intSet3.add(15);
    intSet3.add(15);
    intSet3.add(15);
    intSet3.add(14);
    intSet3.add(17);
    
    intSet4.add(14);
    intSet4.add(15);
    intSet4.add(15);
    intSet4.add(15);
    
    setSet1.add(intSet2);
    
    setSet2.add(intSet2);
    setSet2.add(intSet3);
    
    setSet3.add(intSet2);
    setSet3.add(intSet3);
  }
  
  // test the itemCount method in OrderableMultiSet
  void testItemCount(Tester t) {
    this.initTestData();
    
    // tests that the method called on an empty set returns 0
    t.checkExpect(intSet1.itemCount(15), 0);
    // tests that counting an element that occurs more than once in a set works as intended
    t.checkExpect(intSet2.itemCount(15), 3);
    // tests that counting an element that occurs once in a set works as intended
    t.checkExpect(intSet3.itemCount(14), 1);
    // tests that counting an element that occurs 0 times in a non-empty set works as intended
    t.checkExpect(intSet3.itemCount(18), 0);
  }
  
  void testHasSubset(Tester t) {
    this.initTestData();
    
    // tests that the method called on an empty set given the empty set returns true
    t.checkExpect(intSet1.hasSubset(intSet1), true);
    // tests that the method called on a non-empty set given the empty set returns true
    t.checkExpect(intSet2.hasSubset(intSet1), true);
    // tests that the method called on an empty set given a non-empty set returns false
    t.checkExpect(intSet1.hasSubset(intSet2), false);
    // tests that the method called on a non-empty set given a non-empty subset returns true
    t.checkExpect(intSet3.hasSubset(intSet2), true);
    // tests that the method called on a non-empty set given a non-empty non-subset returns false
    t.checkExpect(intSet2.hasSubset(intSet3), false);
    // tests that the method called on a non-empty set given itself returns true
    t.checkExpect(intSet2.hasSubset(intSet2), true);
  }
  
  void testEquals(Tester t) {
    this.initTestData();
    
    // tests that the method called on itself returns true for an empty set
    t.checkExpect(intSet1.equals(intSet1), true);
    // tests that the method called on itself returns true for a non-empty set
    t.checkExpect(intSet2.equals(intSet2), true);
    // demonstrates equality for int sets
    t.checkExpect(intSet2.equals(intSet4), true);
    // demonstrates inequality for int sets
    t.checkExpect(intSet2.equals(intSet3), false);
    // demonstrates equality for sets of sets
    t.checkExpect(setSet2.equals(setSet3), true);
    // demonstrates inequality for sets of sets
    t.checkExpect(setSet2.equals(setSet1), false);
    // demonstrates inequality between sets and data of other types
    t.checkExpect(setSet2.equals(5), false);
  }
  
  void testHashCode(Tester t) {
    this.initTestData();
    
    // tests that the hashcode compared with itself returns true for an empty set
    t.checkExpect(intSet1.hashCode() == intSet1.hashCode(), true);
    // tests that the hashcode compared with itself returns true for a non-empty set
    t.checkExpect(intSet2.hashCode() == intSet2.hashCode(), true);
    // demonstrates hashcode equality for int sets
    t.checkExpect(intSet2.hashCode() == intSet4.hashCode(), true);
    // demonstrates hashcode inequality for int sets
    t.checkExpect(intSet2.hashCode() == intSet3.hashCode(), false);
    // demonstrates hashcode equality for sets of sets
    t.checkExpect(setSet2.hashCode() == setSet3.hashCode(), true);
    // demonstrates hashcode inequality for sets of sets
    t.checkExpect(setSet2.hashCode() == setSet1.hashCode(), false);
    // demonstrates hashcode inequality between sets and data of other types
    t.checkExpect(setSet2.hashCode() == "hi".hashCode(), false);
  }
}