
package MyUtil;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.regex.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.nio.channels.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.jar.*;

public class MyNode<T> {
    public T data;
    public MyNode<T> parent;
    public Vector<MyNode<T>> children = new Vector<MyNode<T>>();
        
    public MyNode(T data) {
        this.data = data;
    }
    
    public void addChild(MyNode<T> child) {
        children.add(child);
        child.parent = this;
    }
    
    public void addChildAt(MyNode<T> child, int i) {
        children.insertElementAt(child, i);
        child.parent = this;
    }
    
    public MyNode<T> getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }
    
    public boolean equals(Object o) {
        return equals((MyNode<T>)o);
    }
    public boolean equals(MyNode<T> that) {
        if ((this.data == null && that.data == null) || this.data.equals(that.data)) {
            for (int i = 0; i < children.size(); i++) {
                if (!this.children.get(i).equals(that.children.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public int getSize() {
        int size = 1;
        for (MyNode<T> child : children) {
            size += child.getSize();
        }
        return size;
    }
    
    public int getHeight() {
        int maxChildHeight = 0;
        for (MyNode<T> child : children) {
            int childHeight = child.getHeight() + 1;
            if (childHeight > maxChildHeight) {
                maxChildHeight = childHeight;
            }
        }
        return maxChildHeight;
    }
    
    public int getWidth() {
        int maxChildWidth = 1;
        for (MyNode<T> child : children) {
            int childWidth = child.getWidth();
            if (childWidth > maxChildWidth) {
                maxChildWidth = childWidth;
            }
        }
        return Math.max(maxChildWidth, children.size());
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toStringHelper(0, buf);
        return buf.toString();
    }
    
    public void toStringHelper(int indent, StringBuffer buf) {
        for (int i = 0; i < indent; i++) {
            buf.append("    ");
        }
        buf.append("" + data);
        buf.append("\n");
        
        for (MyNode<T> child : children) {
            child.toStringHelper(indent + 1, buf);
        }
    }
}
