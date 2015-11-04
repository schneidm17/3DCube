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
    Paint color;
    Paint temp;
    Path path;

    public CubeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        phi = 60.0;
        theta = 60.0;
        isWireframe = false;

        line = new Paint();
        line.setColor(Color.BLACK);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2);
        color = new Paint();
        color.setColor(Color.RED);
        color.setStyle(Paint.Style.FILL);
        temp = new Paint();
        temp.setStyle(Paint.Style.FILL);
        path = new Path();
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
            int faces[][] = {{0, 1, 3, 2}, {1, 0, 4, 5}, {3, 1, 5, 7},
                    {2, 3, 7, 6}, {0, 2, 6, 4}, {5, 4, 6, 7}};

            for (int i = 0; i < 6; i++) {
                //parameterize the normal vector for the face of the cube
                double[] v1 = {pts[faces[i][0]][0] - pts[faces[i][1]][0],
                        pts[faces[i][0]][1] - pts[faces[i][1]][1],
                        pts[faces[i][0]][2] - pts[faces[i][1]][2]};
                double[] v2 = {pts[faces[i][0]][0] - pts[faces[i][2]][0],
                        pts[faces[i][0]][1] - pts[faces[i][2]][1],
                        pts[faces[i][0]][2] - pts[faces[i][2]][2]};
                double[] v3 = {v1[1] * v2[2] - v1[2] * v2[1], v1[2] * v2[0] - v1[0] * v2[2], v1[0] * v2[1] - v1[1] * v2[0]};
                double normV3 = Math.sqrt(v3[0]*v3[0] + v3[1]*v3[1] + v3[2]*v3[2]);
                v3[0] /= normV3; v3[1] /= normV3; v3[2] /= normV3; //normalize the vector

                //parameterize the vector from the camera to the face
                double[] cam = {pts[faces[i][0]][0] - a*d, pts[faces[i][0]][1] - b*d, pts[faces[i][0]][2] - c*d};
                double normCam = Math.sqrt(cam[0]*cam[0] + cam[1]*cam[1] + cam[2]*cam[2]);
                cam[0] /= normCam; cam[1] /= normCam; cam[2] /= normCam;

                //dot the normal vector with the camera vector
                double dot = cam[0]*v3[0] + cam[1]*v3[1] + cam[2]*v3[2];

                if(dot>0) {
                    path.reset();
                    path.moveTo(
                            mapX(pts[faces[i][0]][0], pts[faces[i][0]][1], pts[faces[i][0]][2]),
                            mapY(pts[faces[i][0]][0], pts[faces[i][0]][1], pts[faces[i][0]][2]));
                    path.lineTo(
                            mapX(pts[faces[i][1]][0], pts[faces[i][1]][1], pts[faces[i][1]][2]),
                            mapY(pts[faces[i][1]][0], pts[faces[i][1]][1], pts[faces[i][1]][2]));
                    path.lineTo(
                            mapX(pts[faces[i][2]][0], pts[faces[i][2]][1], pts[faces[i][2]][2]),
                            mapY(pts[faces[i][2]][0], pts[faces[i][2]][1], pts[faces[i][2]][2]));
                    path.lineTo(
                            mapX(pts[faces[i][3]][0], pts[faces[i][3]][1], pts[faces[i][3]][2]),
                            mapY(pts[faces[i][3]][0], pts[faces[i][3]][1], pts[faces[i][3]][2]));
                    path.close();

                    double factor = Math.min(1, Math.max(0, 0.4 + 0.6 * dot));
                    temp.setColor(Color.rgb(
                            (int) (Color.red(color.getColor()) * factor),
                            (int) (Color.green(color.getColor()) * factor),
                            (int)(Color.blue(color.getColor())*factor)));

                    canvas.drawPath(path, temp);
                }
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

        a = Math.sin(phi*deg)*Math.cos(theta*deg);
        b = Math.sin(phi*deg)*Math.sin(theta*deg);
        c = Math.cos(phi*deg);
        k1 = -s*Math.sin(theta*deg);
        k2 = s*Math.cos(theta*deg);
        k3 = -s*Math.cos(phi*deg)*Math.cos(theta*deg);
        k4 = -s*Math.cos(phi*deg)*Math.sin(theta*deg);
        k5 = s*Math.sin(phi*deg);
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
        return cx+(float)((x*k1 + y*k2)/(a*x + b*y + c*z - d));
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
        return cy+(int)((x*k3 + y*k4 + z*k5)/(a*x + b*y + c*z - d));
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
