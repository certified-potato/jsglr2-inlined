/*
 * Created on 17.apr.2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr;

public class AmbiguityMap {

    private boolean[] positions;
    
    public AmbiguityMap(int size) {
        positions = new boolean[size];
    }

    public boolean isMarked(int pos) {
        return positions[pos];
    }

    public void mark(int pos) {
        positions[pos] = true;
    }

    public void unmark(int pos) {
        positions[pos] = false;
    }
}