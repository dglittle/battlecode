
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

public class Bag<V> extends HashMap<V, Integer> {
    
    public Integer add(V v, int amount) {
        Integer i = super.get(v);
        if (i == null) {
            i = 0;
        }
        i = i + amount;
        put(v, i);
        return i;
    }
    
    public Integer add(V v) {
        return add(v, 1);
    }
    
    public Integer remove(V v) {
        return add(v, -1);
    }
    
    public Integer get(V v) {
        Integer i = super.get(v);
        if (i == null) {
            return 0;
        }
        return i;
    }
    
    public Vector<Pair<Integer, V>> getPairs() {
        Vector<Pair<Integer, V>> pairs = new Vector<Pair<Integer, V>>();
        for (V v : keySet()) {
            pairs.add(new Pair<Integer, V>(get(v), v));
        }
        return pairs;
    }
    
    public Vector<Pair<Integer, V>> getSortedPairs() {
        Vector<Pair<Integer, V>> pairs = getPairs();
        Collections.sort(pairs, Collections.reverseOrder());
        return pairs;
    }
}
