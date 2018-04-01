package com.evacipated.cardcrawl.modthespire;

import java.util.*;

// Heavily modified from
// http://www.java2s.com/Code/Java/Collections-Data-Structure/Topologicalsorting.htm
// and
// http://blog.gapotchenko.com/stable-topological-sort
public class GraphTS<T>
{
    class Vertex
    {
        T value;

        Vertex(T v) {
            this.value = v;
        }
    }

    private List<Vertex> vertexList;
    private List<List<Boolean>> matrix;

    public List<T> sortedArray;

    public GraphTS()
    {
        vertexList = new ArrayList<>();
        matrix = new ArrayList<>();
        sortedArray = new ArrayList<>();
    }

    public void addVertex(T v)
    {
        vertexList.add(new Vertex(v));
        List<Boolean> tmp = new ArrayList<>();
        matrix.add(tmp);

        for (List<Boolean> row : matrix) {
            for (int i=row.size(); i<vertexList.size(); ++i) {
                row.add(false);
            }
        }
    }

    public void addEdge(int start, int end)
    {
        matrix.get(start).set(end, true);
    }

    public void displayVertex(int idx) {
        System.out.print(vertexList.get(idx).value);
    }

    public void tsort() throws CyclicDependencyException
    {
        sortedArray.clear();
        while (vertexList.size() > 0) { // while vertices remain,
            // get a vertex with no successors, or -1
            int currentVertex = noSuccessors();
            if (currentVertex == -1) { // must be a cycle
                throw new CyclicDependencyException();
            }
            // insert vertex label in sorted array
            sortedArray.add(vertexList.get(currentVertex).value);

            deleteVertex(currentVertex); // delete vertex
        }

        Collections.reverse(sortedArray);
    }

    public void tsortStable()
    {
        sortedArray.clear();
        for (Vertex v : vertexList) {
            sortedArray.add(v.value);
        }

        int n = sortedArray.size();

        Dependencies depends = new Dependencies();

        boolean restart = false;
        do {
            restart = false;
            for (int i=0; i<n; ++i) {
                for (int j=0; j<i; ++j) {
                    if (depends.doesXHaveDirectDependencyOnY(sortedArray.get(j), sortedArray.get(i))) {
                        boolean iOnJ = depends.doesXHaveTransientDependencyOnY(sortedArray.get(j), sortedArray.get(i));
                        boolean jOnI = depends.doesXHaveTransientDependencyOnY(sortedArray.get(i), sortedArray.get(j));

                        if (!(jOnI && iOnJ)) { // not circular depend
                            T t = sortedArray.get(i);
                            List<Boolean> children = matrix.get(i);
                            sortedArray.remove(i);
                            matrix.remove(i);
                            sortedArray.add(j, t);
                            matrix.add(j, children);
                            restart = true;
                            break;
                        }
                    }
                }
                if (restart) break;
            }
        } while (restart);
    }

    private int noSuccessors() // returns vert with no successors (or -1 if no such verts)
    {
        boolean isEdge; // edge from row to column in adjMat

        for (int row = 0; row < vertexList.size(); row++) {
            isEdge = false; // check edges
            for (int col = 0; col < vertexList.size(); col++) {
                if (matrix.get(row).get(col)) {// if edge to another,
                    isEdge = true;
                    break; // this vertex has a successor try another
                }
            }
            if (!isEdge) // if no edges, has no successors
                return row;
        }
        return -1; // no
    }

    public void deleteVertex(int delVert)
    {
        vertexList.remove(delVert);

        for (List<Boolean> row : matrix) {
            row.remove(delVert);
        }
        matrix.remove(delVert);
    }

    private class Dependencies
    {
        class Node
        {
            List<T> children = new ArrayList<>();
        }

        private Map<T, Node> Nodes = new HashMap<>();

        private Set<T> visitedNodes = new HashSet<>();

        Dependencies()
        {
            for (int i=0; i<vertexList.size(); ++i) {
                Node node = Nodes.get(vertexList.get(i).value);
                if (node == null) {
                    node = new Node();
                    Nodes.put(vertexList.get(i).value, node);
                }

                for (int j=0; j<vertexList.size(); ++j) {
                    if (i == j) continue;

                    if (matrix.get(j).get(i)) {
                        node.children.add(vertexList.get(j).value);
                    }
                }
            }
        }

        boolean doesXHaveDirectDependencyOnY(T x, T y)
        {
            Node node = Nodes.get(x);
            if (node != null) {
                if (node.children.contains(y)) {
                    return true;
                }
            }
            return false;
        }

        boolean doesXHaveTransientDependencyOnY(T x, T y)
        {
            if (!visitedNodes.add(x)) {
                return false;
            }

            if (doesXHaveDirectDependencyOnY(x, y)) {
                return true;
            }
            Node node = Nodes.get(x);
            if (node != null) {
                for (T t : node.children) {
                    if (doesXHaveTransientDependencyOnY(t, y)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
