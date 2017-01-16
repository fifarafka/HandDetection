package com.example.monikawojtasik.setupopencbv;

import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class Hand {

    private List<Point> coordinates = new ArrayList<>();

    private List<MatOfPoint> contours;

    private Integer handContour;

    public Integer getHandContour() {
        return handContour;
    }

    public void setHandContour(Integer handContour) {
        this.handContour = handContour;
    }

    private Point center;

    private Rect boundingRect;

    private MatOfInt convex;

    public List<Point> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Point> coordinates) {
        this.coordinates = coordinates;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    public void setContours(List<MatOfPoint> contours) {
        this.contours = contours;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Rect getBoundingRect() {
        return boundingRect;
    }

    public void setBoundingRect(Rect boundingRect) {
        this.boundingRect = boundingRect;
    }

    public MatOfInt getConvex() {
        return convex;
    }

    public void setConvex(MatOfInt convex) {
        this.convex = convex;
    }
}
