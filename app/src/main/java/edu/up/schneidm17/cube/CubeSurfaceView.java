package edu.up.schneidm17.cube;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

    public final double d = 5; //distance of the camera from the origin (constant)
    public final double p = 4; //distance of the view plane from the origin (constant)
    public final double s = 1000; //scale factor on the view plane
    public final double deg = 0.017453292519943295; //conversion factor for deg to rad

    public boolean isWireframe;

    Paint line;
    Paint text;

    Paint face1;
    Paint face2;
    Paint face3;
    Paint face4;
    Paint face5;
    Paint face6;

    public CubeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        phi = 60.0;
        theta = 0.0;
        isWireframe = false;

        line = new Paint();
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        text = new Paint();
        text.setColor(Color.GRAY);
        text.setStyle(Paint.Style.FILL);
        text.setTextSize(30);

        face1 = new Paint();
        face2 = new Paint();
        face3 = new Paint();
        face4 = new Paint();
        face5 = new Paint();
        face6 = new Paint();
        face1.setColor(Color.RED);
        face2.setColor(Color.YELLOW);
        face3.setColor(Color.BLUE);
        face4.setColor(Color.YELLOW);
        face5.setColor(Color.BLUE);
        face6.setColor(Color.RED);
        face1.setStyle(Paint.Style.FILL);
        face2.setStyle(Paint.Style.FILL);
        face3.setStyle(Paint.Style.FILL);
        face4.setStyle(Paint.Style.FILL);
        face5.setStyle(Paint.Style.FILL);
        face6.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateABC();

        if(isWireframe) {
            canvas.drawLine(mapX(1, 1, 1), mapY(1, 1, 1), mapX(1, 1, -1), mapY(1, 1, -1), line);
            canvas.drawLine(mapX(1, 1, 1), mapY(1, 1, 1), mapX(1, -1, 1), mapY(1, -1, 1), line);
            canvas.drawLine(mapX(1, 1, 1), mapY(1, 1, 1), mapX(-1, 1, 1), mapY(-1, 1, 1), line);
            canvas.drawLine(mapX(1, 1, -1), mapY(1, 1, -1), mapX(1, -1, -1), mapY(1, -1, -1), line);
            canvas.drawLine(mapX(1, 1, -1), mapY(1, 1, -1), mapX(-1, 1, -1), mapY(-1, 1, -1), line);
            canvas.drawLine(mapX(1, -1, 1), mapY(1, -1, 1), mapX(1, -1, -1), mapY(1, -1, -1), line);
            canvas.drawLine(mapX(1, -1, 1), mapY(1, -1, 1), mapX(-1, -1, 1), mapY(-1, -1, 1), line);
            canvas.drawLine(mapX(1, -1, -1), mapY(1, -1, -1), mapX(-1, -1, -1), mapY(-1, -1, -1), line);
            canvas.drawLine(mapX(-1, 1, 1), mapY(-1, 1, 1), mapX(-1, 1, -1), mapY(-1, 1, -1), line);
            canvas.drawLine(mapX(-1, 1, 1), mapY(-1, 1, 1), mapX(-1, -1, 1), mapY(-1, -1, 1), line);
            canvas.drawLine(mapX(-1, 1, -1), mapY(-1, 1, -1), mapX(-1, -1, -1), mapY(-1, -1, -1), line);
            canvas.drawLine(mapX(-1, -1, 1), mapY(-1, -1, 1), mapX(-1, -1, -1), mapY(-1, -1, -1), line);
        } else {
            double pts[][] = {{1, 1, 1}, {1, -1, 1}, {-1, 1, 1}, {-1, -1, 1},
                    {1, 1, -1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, -1}};
            double cents[] = {distance(0, 0, 1), distance(1, 0, 0), distance(0, -1, 0),
                    distance(-1, 0, 0), distance(0, 1, 0), distance(0, 0, -1)};
            int faces[][] = {{0, 1, 3, 2}, {0, 1, 5, 4}, {1, 3, 7, 5},
                    {2, 3, 7, 6}, {0, 2, 6, 4}, {4, 5, 7, 6}};
            Paint colors[] = {face1, face2, face3, face4, face5, face6};

            for (int i = 0; i < 6; i++) {
                int temp = i;
                for (int j = i; j < 6; j++) {
                    if (cents[j] > cents[temp])
                    {temp = j;}
                }
                double tempCent = cents[i];
                cents[i] = cents[temp];
                cents[temp] = tempCent;
                int tempFace[] = faces[i];
                faces[i] = faces[temp];
                faces[temp] = tempFace;
                Paint tempColor = colors[i];
                colors[i] = colors[temp];
                colors[temp] = tempColor;
            }
            for (int i = 0; i < 6; i++) {
                Path cubeFace = new Path();
                cubeFace.moveTo(
                        mapX(pts[faces[i][0]][0], pts[faces[i][0]][1], pts[faces[i][0]][2]),
                        mapY(pts[faces[i][0]][0], pts[faces[i][0]][1], pts[faces[i][0]][2]));
                cubeFace.lineTo(
                        mapX(pts[faces[i][1]][0], pts[faces[i][1]][1], pts[faces[i][1]][2]),
                        mapY(pts[faces[i][1]][0], pts[faces[i][1]][1], pts[faces[i][1]][2]));
                cubeFace.lineTo(
                        mapX(pts[faces[i][2]][0], pts[faces[i][2]][1], pts[faces[i][2]][2]),
                        mapY(pts[faces[i][2]][0], pts[faces[i][2]][1], pts[faces[i][2]][2]));
                cubeFace.lineTo(
                        mapX(pts[faces[i][3]][0], pts[faces[i][3]][1], pts[faces[i][3]][2]),
                        mapY(pts[faces[i][3]][0], pts[faces[i][3]][1], pts[faces[i][3]][2]));
                cubeFace.close();
                canvas.drawPath(cubeFace, colors[i]);
            }
        }
    }

    /**
     * updateABC updates the x,y,z coordinates of intersection of the vector form the
     * camera to the origin with the view plane. Must be updated after the camera rotates
     */
    private void updateABC() {
        if (phi <= -180)
            phi += 360;
        else if (phi > 180)
            phi -= 360;
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
     * mapX returns the x coordinate of the point {x,y,z} in 3D space as viewed from
     * the camera at {d, theta, phi} as it would appear on the plane ax+by+cz=p^2
     *
     * @param x the x coordinate of a point in 3D space
     * @param y the y coordinate of a point in 3D space
     * @param z the z coordinate of a point in 3D space
     * @return the x coordinate on the screen of the point {x,y,x} in 3D space
     */
    public float mapX(double x, double y, double z) {
        double t = (p * p - x * a - y * b - z * c) / (d * p - x * a - y * b - c * z);
        return cx + (float) ((b - y - t * (d * b / p - y)) * k1 + (a - x - t * (d * a / p - x)) * k2);
    }

    /**
     * mapY returns the y coordinate of the point {x,y,z} in 3D space as viewed from
     * the camera at {d, theta, phi} as it would appear on the plane ax+by+cz=p^2
     *
     * @param x the x coordinate of a point in 3D space
     * @param y the y coordinate of a point in 3D space
     * @param z the z coordinate of a point in 3D space
     * @return the y coordinate on the screen of the point {x,y,x} in 3D space
     */
    public float mapY(double x, double y, double z) {
        double t = (p * p - x * a - y * b - z * c) / (d * p - x * a - y * b - c * z);
        return cy + (float) ((a - x - t * (d * a / p - x)) * k3 +
                (b - y - t * (d * b / p - y)) * k4 + (c - z - t * (d * c / p - z)) * k5);
    }

    /**
     * distance returns the distance from the camera at {d, theta, phi} to the point {x,y,z}
     *
     * @param x the x coordinate of a point in 3D space
     * @param y the y coordinate of a point in 3D space
     * @param z the z coordinate of a point in 3D space
     * @return the distance from the camera to the point {x,y,z} in 3D space
     */
    public double distance(double x, double y, double z) {
        return Math.sqrt((a - x) * (a - x) + (b - y) * (b - y) + (c - z) * (c - z));
    }

    /**
     * rotate the game board 15 degrees to the right
     */
    public void rotateRight() {
        theta = 15 * Math.round(theta / 15) + 15;
        updateABC();
    }

    /**
     * rotate the game board 15 degrees to the left
     */
    public void rotateLeft() {
        theta = 15 * Math.round(theta / 15) - 15;
        updateABC();
    }

    /**
     * rotate the camera 15 degrees up
     */
    public void rotateUp() {
        phi = 15 * Math.round(phi / 15) - 15;
        updateABC();
    }

    /**
     * rotate the camera 15 degrees down
     */
    public void rotateDown() {
        phi = 15 * Math.round(phi / 15) + 15;
        updateABC();
    }

    /**
     * get the value of phi
     *
     * @return this.phi
     */
    public double getPhi() {
        return phi;
    }

    /**
     * get the value of theta
     *
     * @return this.theta
     */
    public double getTheta() {
        return theta;
    }

    /**
     * change the value of phi based on user touch
     *
     * @param angle the new value of phi
     */
    public void setPhi(double angle) {
        this.phi = angle % 360;
        updateABC();
    }

    /**
     * change the value of theta based on user touch
     *
     * @param angle the new value of theta
     */
    public void setTheta(double angle) {
        this.theta = angle % 360;
        updateABC();
    }
}
