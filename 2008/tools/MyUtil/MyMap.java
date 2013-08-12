
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

public class MyMap<K, V> extends HashMap<K, V> {
    Class defaultValue;
    
    public MyMap(Class defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public V get(Object o) {
        K k = (K)o;
        try {
            V v = super.get(k);
            if (v == null) {            
                v = (V)defaultValue.newInstance();
                put(k, v);
            }
            return v;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
