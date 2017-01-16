package com.example.monikawojtasik.setupopencbv;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;


public class DetectionService {

    private static final int MIN_FINGER_DEPTH = 20;

    private static final int MAX_FINGER_ANGLE = 40;

    public static final Mat RECT_STRUCTURING_ELEMENT = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));

    public static final Scalar LOWER = new Scalar(0,0,0,0);

    public static final Scalar UPPER = new Scalar(180,255,30,0);

    private Mat matToProcces = new Mat();

    private Mat matToShow = new Mat();

    private Mat binMat;

    private Point center;

    private Integer biggestContour;

    private Rect boundingRect;

    private List<Point> tipPts = new ArrayList<>();

    private List<Point> fingerTips = new ArrayList<>();

    private List<Point> foldPts = new ArrayList<>();

    private List<Integer> depths = new ArrayList<>();

    private List<MatOfPoint> contours = new ArrayList<>();

    public Rect makeBoundingRect(MatOfPoint cont) {
        Rect rect = Imgproc.boundingRect(cont);
        boundingRect = rect;
        return rect;
    }

    private Point findCenter(MatOfPoint cont)
    {
        Moments moments = Imgproc.contourMoments(cont);
        Point centroid = new Point();
        if (moments.get_m00()!=0) {
            centroid.x = moments.get_m10() / moments.get_m00();
            centroid.y = moments.get_m01() / moments.get_m00();
        }
        center = centroid;
        return centroid;
    }

    private void produceBinImg()
    {
        if (binMat == null) {
            binMat = new Mat();
        }
        Core.inRange(matToProcces, LOWER, UPPER, binMat);
    }

    private void prepareBinImg() {
        Imgproc.medianBlur(binMat, binMat, 3);
        Imgproc.erode(binMat, binMat, RECT_STRUCTURING_ELEMENT);
        Imgproc.dilate(binMat, binMat, RECT_STRUCTURING_ELEMENT);
        Imgproc.dilate(binMat, binMat, RECT_STRUCTURING_ELEMENT);
        Imgproc.erode(binMat, binMat, RECT_STRUCTURING_ELEMENT);
        Imgproc.dilate(binMat, binMat, Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2, 2)));
        Imgproc.erode(binMat, binMat, Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2,2)));
    }

    private int findBiggestContour()
    {
        int idx = -1;
        int cNum = 0;
        for (int i = 0; i < contours.size(); i++)
        {
            int curNum = contours.get(i).toList().size();
            if (curNum > cNum) {
                idx = i;
                cNum = curNum;
            }
        }
        biggestContour = Integer.valueOf(idx);
        return idx;
    }

    private void findContours() {
        contours.clear();
        Imgproc.findContours(binMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        int biggestContour = findBiggestContour();
        if (biggestContour > -1) {
            Imgproc.drawContours(matToShow, contours, biggestContour, new Scalar(255, 0, 0, 255), 5);
        }
    }

    private MatOfInt convex(MatOfPoint contour) {
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        return hull;
    }

    private void prepareImage() {
        Imgproc.GaussianBlur(matToProcces, matToProcces, new Size(5,5), 5, 5);
        Imgproc.cvtColor(matToProcces, matToProcces, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(matToProcces, matToProcces, Imgproc.COLOR_RGB2HSV);
    }

    private void findFingerTips(MatOfPoint contour, MatOfInt hull) {
        tipPts.clear();
        foldPts.clear();
        depths.clear();
        MatOfInt4 convDef = new MatOfInt4();
        Imgproc.convexityDefects(contour, hull, convDef);
        List<Integer> cdList = convDef.toList();
        Point data[] = contour.toArray();
        int total = 0;
        Point previewTip = null;
        Point previewFold = null;
        for (int j = 0; j < cdList.size(); j = j + 4) {
            Point start = data[cdList.get(j)];
            Point end = data[cdList.get(j + 1)];
            Point defect = data[cdList.get(j + 2)];
            int depth = cdList.get(j + 3);
            decideTipPoint(previewTip, start);
            decideFoldPoint(previewFold,defect);
            previewTip = start;
            previewFold = defect;
            depths.add(Integer.valueOf(depth));
            total++;
        }
    }

    private void decideFoldPoint(Point previewFold, Point defect) {
        int distance2 = checkDistance(defect, center);
        if (previewFold!=null) {
            int distance = checkDistance(previewFold, defect);
            if (distance>20) {
                if (distance2<(boundingRect.height/3)) {
                    foldPts.add(defect);
                }
            }
        } else {
            if (distance2<(boundingRect.height/3)) {
                foldPts.add(defect);
            }
        }
    }

    private void decideTipPoint(Point previewTip, Point start) {
        int distance2 = checkDistance(new Point(start.x, boundingRect.y+ boundingRect.height), start);
        if (previewTip!=null) {
            int distance = checkDistance(previewTip, start);
            if (distance>20) {
                if (distance2>(boundingRect.height/4))
                    tipPts.add(start);
            }

        } else {
            if (distance2>(boundingRect.height/4))
                tipPts.add(start);
        }
    }

    private int checkDistance(Point x, Point y) {
         return (int) Math.sqrt(Math.pow(Math.abs(x.x-y.x),2)+Math.pow(Math.abs(x.y-y.y),2));
    }

    private void reduceTips(int pointNumber) {
        fingerTips.clear();
        for (int i=0; i < pointNumber; i++) {
            if (depths.get(i) < MIN_FINGER_DEPTH)
                continue;
            int pdx = (i == 0) ? (pointNumber-1) : (i - 1);
            int sdx = (i == pointNumber-1) ? 0 : (i + 1);
            int angle = angleBetween(tipPts.get(i), foldPts.get(pdx), foldPts.get(sdx));
            System.out.println(angle);
            if (angle >= MAX_FINGER_ANGLE)
                continue;
            fingerTips.add(tipPts.get(i));
        }
    }

    private int angleBetween(Point tip, Point next, Point prev)
    {
        return Math.abs( (int)Math.round(
                Math.toDegrees(
                        Math.atan2(next.x - tip.x, next.y - tip.y) -
                        Math.atan2(prev.x - tip.x, prev.y - tip.y)) ));
    }


    public Hand detectHand(Mat rgbaMat) {
        Hand hand = new Hand();
        rgbaMat.copyTo(matToProcces);
        rgbaMat.copyTo(matToShow);
        prepareImage();
        produceBinImg();
        prepareBinImg();
        findContours();
        if (biggestContour!=null && biggestContour > -1) {
            MatOfPoint handContour = contours.get(biggestContour.intValue());
            hand.setContours(contours);
            hand.setHandContour(biggestContour);
            MatOfInt hull = convex(handContour);
            hand.setConvex(hull);
            Point center = findCenter(handContour);
            hand.setCenter(center);
            Rect r = makeBoundingRect(handContour);
            hand.setBoundingRect(r);
            findFingerTips(handContour, hull);
            hand.setCoordinates(tipPts);
        }
        return hand;
    }
}
