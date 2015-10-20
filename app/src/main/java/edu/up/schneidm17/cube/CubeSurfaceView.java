package edu.up.schneidm17.cube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by Matthew Schneider on 10/19/15
 */
public class CubeSurfaceView extends SurfaceView {

    /*
     * The camera is stored in spherical coordinates as degrees {d, phi, theta}, which
     * make it easier for the user to rotate the board and prevents rounding errors, but
     * the function that maps the xyz coordinates of the board to the xy of the screen requires
     * cartesian coordinates {a, b, c} that are only calculated once after moving the camera
     */
    private double phi; //angle of the camera from the z axis (in spherical coordinates)
    private double theta; //angle of the camera from the x axis (in spherical coordinates)
    private double a; //the x coordinate of the view plane (in cartesian coordinates)
    private double b; //the y coordinate of the view plane (in cartesian coordinates)
    private double c; //the z coordinate of the view plane (in cartesian coordinates)
    private double k1; //the coefficient for the i unit vector of getX()
    private double k2; //the coefficient for the j unit vector of getX()
    private double k3; //the coefficient for the i unit vector of getY()
    private double k4; //the coefficient for the j unit vector of getY()
    private double k5; //the coefficient for the k unit vector of getY()
    private float cx; //the horizontal center of this SurfaceView
    private float cy; //the vertical center of this SurfaceView

    public boolean canRotatePhi;
    public boolean canRotateTheta;

    public final double s = 1000;
    public final double d = 5; //distance of the camera from the origin (constant)
    public final double p = 4; //distance of the view plane from the origin (constant)
    public final double deg = 0.017453292519943295; //conversion factor for deg to rad

    Paint line;
    Paint text;

    public CubeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        phi = 60.0;
        theta = 0.0;
        canRotatePhi = true;
        canRotateTheta = true;

        line = new Paint();
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);

        text = new Paint();
        text.setColor(Color.GRAY);
        text.setStyle(Paint.Style.FILL);
        text.setTextSize(30);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateABC();

        canvas.drawLine(getx(1, 1, 1), gety(1, 1, 1), getx(1, 1, -1), gety(1, 1, -1), line);
        canvas.drawLine(getx(1, 1, 1), gety(1, 1, 1), getx(1, -1, 1), gety(1, -1, 1), line);
        canvas.drawLine(getx(1, 1, 1), gety(1, 1, 1), getx(-1, 1, 1), gety(-1, 1, 1), line);
        canvas.drawLine(getx(1, 1, -1), gety(1, 1, -1), getx(1, -1, -1), gety(1, -1, -1), line);
        canvas.drawLine(getx(1, 1, -1), gety(1, 1, -1), getx(-1, 1, -1), gety(-1, 1, -1), line);
        canvas.drawLine(getx(1, -1, 1), gety(1, -1, 1), getx(1, -1, -1), gety(1, -1, -1), line);
        canvas.drawLine(getx(1, -1, 1), gety(1, -1, 1), getx(-1, -1, 1), gety(-1, -1, 1), line);
        canvas.drawLine(getx(1, -1, -1), gety(1, -1, -1), getx(-1, -1, -1), gety(-1, -1, -1), line);
        canvas.drawLine(getx(-1, 1, 1), gety(-1, 1, 1), getx(-1, 1, -1), gety(-1, 1, -1), line);
        canvas.drawLine(getx(-1, 1, 1), gety(-1, 1, 1), getx(-1, -1, 1), gety(-1, -1, 1), line);
        canvas.drawLine(getx(-1, 1, -1), gety(-1, 1, -1), getx(-1, -1, -1), gety(-1, -1, -1), line);
        canvas.drawLine(getx(-1, -1, 1), gety(-1, -1, 1), getx(-1, -1, -1), gety(-1, -1, -1), line);

        canvas.drawText("phi = " + phi, 30, 60, text);
        canvas.drawText("theta = "+theta, 30,100, text);
    }

    /**
     * updateABC updates the x,y,z coordinates of intersection of the vector form the
     * camera to the origin with the view plane. Must be updated after the camera rotates
     */
    private void updateABC() {
        if (theta <= -180)
            theta += 360;
        else if (theta > 180)
            theta -= 360;

        if (theta <= -180)
            theta += 360;
        else if (theta > 180)
            theta -= 360;

        a = p * Math.sin(phi * deg) * Math.cos(theta * deg); //x value of view plane axis
        b = p * Math.sin(phi * deg) * Math.sin(theta * deg); //y value of view plane axis
        c = p * Math.cos(phi * deg); //z value of view plane axis (in R3)
        k1 = s * Math.cos(deg * theta);
        k2 = -s * Math.sin(deg * theta);
        k3 = -s * Math.cos(deg * theta) * Math.cos(deg * phi);
        k4 = -s * Math.sin(deg * theta) * Math.cos(deg * phi);
        k5 = s * Math.sin(deg * phi);
        cy = this.getHeight() / 2.0f;
        cx = this.getWidth() / 2.0f;
    }

    /**
     * getx returns the x coordinate of the point {x,y,z} in 3D space as viewed from
     * the camera at {d, theta, phi} as it would appear on the plane ax+by+cz=p^2
     *
     * @param x - the x coordinate of a point in 3D space
     * @param y - the y coordinate of a point in 3D space
     * @param z - the z coordinate of a point in 3D space
     * @return the x coordinate on the screen of the point {x,y,x} in 3D space
     */
    public float getx(double x, double y, double z) {
        double t = (p * p - x * a - y * b - z * c) / (d * p - x * a - y * b - c * z);
        return cx + (float) ((b - y - t * (d * b / p - y)) * k1 + (a - x - t * (d * a / p - x)) * k2);
    }

    /**
     * gety returns the y coordinate of the point {x,y,z} in 3D space as viewed from
     * the camera at {d, theta, phi} as it would appear on the plane ax+by+cz=p^2
     *
     * @param x - the x coordinate of a point in 3D space
     * @param y - the y coordinate of a point in 3D space
     * @param z - the z coordinate of a point in 3D space
     * @return the y coordinate on the screen of the point {x,y,x} in 3D space
     */
    public float gety(double x, double y, double z) {
        double t = (p * p - x * a - y * b - z * c) / (d * p - x * a - y * b - c * z);
        return cy + (float) ((a - x - t * (d * a / p - x)) * k3 +
                (b - y - t * (d * b / p - y)) * k4 + (c - z - t * (d * c / p - z)) * k5);
    }

    /**
     * if canRotateTheta is true,
     * rotate the game board 15 degrees to the right
     */
    public void rotateRight() {
        if(canRotateTheta) {
            theta = 15 * Math.round(theta / 15) + 15;
            updateABC();
        }
    }

    /**
     * if canRotateTheta is true,
     * rotate the game board 15 degrees to the left
     */
    public void rotateLeft() {
        if(canRotateTheta) {
            theta = 15 * Math.round(theta / 15) - 15;
            updateABC();
        }
    }

    /**
     * if canRotatePhi is true,
     * rotate the camera 15 degrees up
     */
    public void rotateUp() {
        if(canRotatePhi) {
            phi = 15 * Math.round(phi / 15) - 15;
            updateABC();
        }
    }

    /**
     * if canRotatePhi is true,
     * rotate the camera 15 degrees down
     */
    public void rotateDown() {
        if(canRotatePhi) {
            phi = 15 * Math.round(phi / 15) + 15;
            updateABC();
        }
    }

    public double getPhi() {
        return phi;
    }

    public double getTheta() {
        return theta;
    }

    public void setPhi(double angle) {
        if(canRotatePhi) {
            this.phi = angle % 360;
            updateABC();
        }
    }

    public void setTheta(double angle) {
        if(canRotateTheta) {
            this.theta = angle % 360;
            updateABC();
        }
    }
}
