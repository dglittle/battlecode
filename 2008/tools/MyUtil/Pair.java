
package MyUtil;

public class Pair<T_left, T_right> implements Comparable {
	public T_left left;
	public T_right right;
	
	public Pair(T_left left, T_right right) {
		this.left = left;
		this.right = right;
	}
	
	public String toString() {
		return "(" + left + ", " + right + ")";
	}
	
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair<T_left, T_right> that = (Pair<T_left, T_right>)o;
			return
                ((left == null && that.left == null) || (left != null && left.equals(that.left))) &&
                ((right == null && that.right == null) || (right != null && right.equals(that.right)));
		}
		return false;
	}
	
	public int hashCode() {
		return left.hashCode() + right.hashCode();
	}
    
    public int compareTo(Object o) {
        Pair<T_left, T_right> that = (Pair)o;
        if (this.left instanceof Comparable) {
            int c = ((Comparable)this.left).compareTo(that.left);
            if (c == 0) {
                if (this.right instanceof Comparable) {
                    return ((Comparable)this.right).compareTo(that.right);
                } else {
                    return 0;
                }
            } else {
                return c;
            }
        } else {
            if (this.right instanceof Comparable) {
                return ((Comparable)this.right).compareTo(that.right);
            } else {
                return 0;
            }
        }
    }
}
