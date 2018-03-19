package com.evacipated.cardcrawl.modthespire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Heavily modified from
// http://www.java2s.com/Code/Java/Collections-Data-Structure/Topologicalsorting.htm
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
}
