package com.example.monikawojtasik.setupopencbv;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DrawingService {

    public static final Scalar COORDINATE_POINT_COLOR = new Scalar(0, 255, 0);
    public static final Scalar COORDINATE_LINE_COLOR = new Scalar(255, 255, 255);
    public static final int COORDINATE_POINT_THICKNESS = 10;
    public static final Scalar CONTOURS_COLOR = new Scalar(255, 0, 0, 255);
    public static final int CONTOURS_THICKNESS = 5;
    public static final Scalar CONVEX_COLOR = new Scalar(0, 255, 40);
    public static final int CONVEX_THICKNESS = 5;
    public static final Scalar CENTER_POINT_COLOR = new Scalar(4, 100, 100);
    public static final int CENTER_POINT_THICKNESS = 5;
    public static final Scalar BOUNDING_RECT_COLOR = new Scalar(100, 100, 100);
    public static final int BOUNDING_RECT_THICKNESS = 5;

    public Mat drawHand(Hand hand, Mat rgbaMat) {
        drawConvex(hand, rgbaMat);
        drawContours(hand, rgbaMat);
        drawBoundingRect(hand, rgbaMat);
        drawCenter(hand, rgbaMat);
        drawCoordinates(hand, rgbaMat);
        return rgbaMat;
    }

    private void drawConvex(Hand hand, Mat rgbaMat) {
        MatOfInt convex = hand.getConvex();
        Integer handContour = hand.getHandContour();
        List<MatOfPoint> contours = hand.getContours();
        if (convex!=null && contours!=null && handContour!=null) {
            MatOfPoint contour = contours.get(handContour.intValue());
            MatOfPoint matOut = new MatOfPoint();
            matOut.create((int) convex.size().height, 1, CvType.CV_32SC2);
            for (int i = 0; i < convex.size().height; i++) {
                int index = (int) convex.get(i, 0)[0];
                double[] point = new double[]{
                        contour.get(index, 0)[0], contour.get(index, 0)[1]
                };
                matOut.put(i, 0, point);
            }
            List<MatOfPoint> outPointHull = new ArrayList<>();
            outPointHull.add(matOut);
            Imgproc.drawContours(rgbaMat, outPointHull, -1, CONVEX_COLOR, CONVEX_THICKNESS);
        }
    }

    private void drawCoordinates(Hand hand, Mat rgbaMat) {
        List<Point> coordinates = hand.getCoordinates();
        Point center = hand.getCenter();
        for (int i = 0; i <coordinates.size(); i++) {
            Imgproc.circle(rgbaMat, coordinates.get(i), 5, COORDINATE_POINT_COLOR, COORDINATE_POINT_THICKNESS);
            if (center!=null) {
                Imgproc.line(rgbaMat, coordinates.get(i), center, COORDINATE_LINE_COLOR);
            }
        }

    }

    private void drawBoundingRect(Hand hand, Mat rgbaMat) {
        Rect rect = hand.getBoundingRect();
        if (rect!=null){
            Point leftTop = new Point(rect.x, rect.y);
            Point rightBottom = new Point(rect.x+rect.width, rect.y+rect.height);
            Imgproc.rectangle(rgbaMat,leftTop,rightBottom, BOUNDING_RECT_COLOR, BOUNDING_RECT_THICKNESS);
        }
    }

    private void drawContours(Hand hand, Mat rgbaMat) {
        List<MatOfPoint> contours = hand.getContours();
        Integer biggestContour = hand.getHandContour();
        if (contours!=null && contours!=null) {
            Imgproc.drawContours(rgbaMat, contours, biggestContour, CONTOURS_COLOR, CONTOURS_THICKNESS);
        }
    }

    private void drawCenter(Hand hand, Mat rgbaMat) {
        Point center = hand.getCenter();
        if (center!=null) {
            Imgproc.circle(rgbaMat, center, 5, CENTER_POINT_COLOR, CENTER_POINT_THICKNESS);
        }
    }
}
