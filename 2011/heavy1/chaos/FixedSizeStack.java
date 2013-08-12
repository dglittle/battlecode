package heavy1.chaos;

import java.util.Stack;

public class FixedSizeStack<T> extends Stack<T> {
  
  private static final long serialVersionUID = 5722617418504764584L;

  @Override
  public T push(T item) {
    if (this.size() >= 2) {
      T temp = pop();
      pop();
      push(temp);
    }
    return super.push(item);
  }
  
  @Override
  public T peek() {
    if (!isEmpty()) return super.peek();
    else return null;
  }
  
  @Override
  public T pop() {
    if (isEmpty()) return null;
    else return super.pop();
  }
  
}
