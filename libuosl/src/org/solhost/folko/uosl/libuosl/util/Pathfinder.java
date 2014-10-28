/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.uosl.libuosl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.solhost.folko.uosl.libuosl.data.SLData;
import org.solhost.folko.uosl.libuosl.types.Direction;
import org.solhost.folko.uosl.libuosl.types.Point3D;

public class Pathfinder {
    private final Point3D start, dest;
    private final ObjectLister lister;
    private PathEntry last;
    private final Map<Point3D, Integer> knownCosts;
    private int iterations;

    private class PathEntry implements Comparable<PathEntry> {
        Point3D point;
        PathEntry prev;
        int cost;

        public PathEntry(Point3D point, PathEntry prev, int cost) {
            this.point = point;
            this.prev = prev;
            this.cost = cost;
        }

        @Override
        public int compareTo(PathEntry o) {
            return cost - o.cost;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((point == null) ? 0 : point.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PathEntry other = (PathEntry) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (point == null) {
                if (other.point != null)
                    return false;
            } else if (!point.equals(other.point))
                return false;
            return true;
        }

        private Pathfinder getOuterType() {
            return Pathfinder.this;
        }
    }

    public Pathfinder(Point3D start, Point3D dest, ObjectLister lister) {
        this.start = start;
        this.dest = dest;
        this.lister = lister;
        this.knownCosts = new HashMap<Point3D, Integer>();
    }

    private List<Point3D> getValidNeighbours(Point3D src) {
        List<Point3D> res = new ArrayList<Point3D>(8);
        for(Direction dir : Direction.values()) {
            Point3D nxt = SLData.get().getElevatedPoint(src, dir, lister);
            if(nxt != null) {
                res.add(nxt);
            }
        }
        return res;
    }

    // h(x): approximated cost from x to dest
    private int h(PathEntry e) {
        return e.point.distanceTo(dest);
    }

    // c(from, to) -> cost for this edge
    private int c(Point3D from, Point3D to) {
        boolean isShorter = to.distanceTo(dest) < from.distanceTo(dest);
        int deltaZBefore = Math.abs(to.getZ() - dest.getZ());
        int deltaZNow =  Math.abs(from.getZ() - dest.getZ());

        int cost = 0;
        if(isShorter) {
            cost--;
        } else {
            cost++;
        }
        // when near the destination, take Z difference into account
        if(to.distanceTo(dest) < 30 && deltaZNow > 5) {
            if(deltaZBefore <= deltaZNow) {
                cost++;
            } else {
                cost--;
            }
        }
        return cost;
    }

    // A* implementation
    public boolean findPath(int maxIter) {
        iterations = 0;
        PriorityQueue<PathEntry> openList = new PriorityQueue<PathEntry>();
        Set<Point3D> closedList = new HashSet<Point3D>();

        openList.add(new PathEntry(start, null, 0));
        knownCosts.put(start, 0);
        do {
            PathEntry currentNode = openList.poll();
            if(currentNode.point.equals(dest)) {
                last = currentNode;
                return true;
            }

            // Means we already visited this node -> don't visit again to avoid cycles
            closedList.add(currentNode.point);

            // check all directions
            for(Point3D neigh : getValidNeighbours(currentNode.point)) {
                // already visited
                if(closedList.contains(neigh)) {
                    continue;
                }
                int tentative_g = knownCosts.get(currentNode.point) + c(currentNode.point, neigh);
                PathEntry successor = new PathEntry(neigh, currentNode, tentative_g);

                // we already know this point via another path that's cheaper
                if(knownCosts.containsKey(successor.point) && tentative_g >= knownCosts.get(successor.point)) {
                    continue;
                }
                successor.prev = currentNode;
                successor.cost = tentative_g + h(successor);
                knownCosts.put(successor.point, tentative_g);
                if(openList.contains(successor)) {
                    openList.remove(successor);
                }
                openList.add(successor);
            }
            iterations++;
        } while(!openList.isEmpty() && iterations < maxIter);

        return false;
    }

    public Point3D getStart() {
        return start;
    }

    public List<Direction> getPath() {
        LinkedList<Direction> res = new LinkedList<Direction>();
        PathEntry cur = last, prev;

        do {
            prev = cur.prev;
            if(prev != null) {
                Direction d = prev.point.getDirectionTo(cur.point);
                res.addFirst(d);
            }
            cur = prev;
        } while(cur != null);

        return res;
    }

    public boolean hasPath() {
        return last != null;
    }

    public int getPathLength() {
        if(last == null) {
            return 0;
        }

        PathEntry cur = last;
        int len = 0;
        do {
            cur = cur.prev;
            len++;
        } while(cur != null);
        return len;
    }

    public String getPathInfo() {
        if(hasPath()) {
            return String.format("Length %d after %d iterations", getPathLength(), iterations);
        } else {
            return "No path found after " + iterations + " iterations";
        }
    }
}
