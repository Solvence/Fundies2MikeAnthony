import java.util.ArrayList;

class Stack<T> {
  Deque<T> contents;
  
  // adds an item to the head of the list
  void push(T item) {
    this.contents.addAtHead(item);
  }
  boolean isEmpty() {
    return this.contents.size() == 0;
  }
  T pop() {
    return this.contents.removeFromHead();
  }
}

class Queue<T> {
  Deque<T> contents;
  void enqueue(T item) {
    this.contents.addAtTail(item);
  }
  boolean isEmpty() {
    return this.contents.size() == 0;
  }
  T dequeue() {
    return this.contents.removeFromHead();
  }
}

class Utils {
  <T> ArrayList<T> reverse(ArrayList<T> source) {
    Stack<T> stk = new Stack<T>();
    for(T item: source) {
      stk.push(item);
    }
    ArrayList<T> reversed = new ArrayList<T>();
    for(T item: source) {
      reversed.add(stk.pop());
    }
    return reversed;
  }
}

