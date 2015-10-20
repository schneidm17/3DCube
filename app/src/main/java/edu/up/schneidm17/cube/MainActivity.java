package edu.up.schneidm17.cube;

import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener{

    CubeSurfaceView mySurfaceView;
    Button rotateUpButton;
    Button rotateRightButton;
    Button rotateDownButton;
    Button rotateLeftButton;

    float x0; //previous x-coordinate of touch event
    float y0; //previous y-coordinate of touch event

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySurfaceView = (CubeSurfaceView) findViewById(R.id.mainSurfaceView);
        rotateUpButton = (Button) findViewById(R.id.buttonRotateUp);
        rotateRightButton = (Button) findViewById(R.id.buttonRotateRight);
        rotateDownButton = (Button) findViewById(R.id.buttonRotateDown);
        rotateLeftButton = (Button) findViewById(R.id.buttonRotateLeft);

        mySurfaceView.setOnTouchListener(this);
        rotateUpButton.setOnClickListener(this);
        rotateRightButton.setOnClickListener(this);
        rotateDownButton.setOnClickListener(this);
        rotateLeftButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(rotateUpButton)) {
            Canvas myCanvas = mySurfaceView.getHolder().lockCanvas();
            mySurfaceView.rotateUp();
            mySurfaceView.getHolder().unlockCanvasAndPost(myCanvas);
            mySurfaceView.postInvalidate();
        } else if (v.equals(rotateRightButton)) {
            Canvas myCanvas = mySurfaceView.getHolder().lockCanvas();
            mySurfaceView.rotateRight();
            mySurfaceView.getHolder().unlockCanvasAndPost(myCanvas);
            mySurfaceView.postInvalidate();
        } else if (v.equals(rotateDownButton)) {
            Canvas myCanvas = mySurfaceView.getHolder().lockCanvas();
            mySurfaceView.rotateDown();
            mySurfaceView.getHolder().unlockCanvasAndPost(myCanvas);
            mySurfaceView.postInvalidate();
        } else if (v.equals(rotateLeftButton)) {
            Canvas myCanvas = mySurfaceView.getHolder().lockCanvas();
            mySurfaceView.rotateLeft();
            mySurfaceView.getHolder().unlockCanvasAndPost(myCanvas);
            mySurfaceView.postInvalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        if(v.equals(mySurfaceView) && e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = x - x0;
            float dy = y - y0;

            double phi0 = mySurfaceView.getPhi();
            double theta0 = mySurfaceView.getTheta();

            Canvas myCanvas = mySurfaceView.getHolder().lockCanvas();
            mySurfaceView.setTheta(theta0 + dx/mySurfaceView.p);
            mySurfaceView.setPhi(phi0 - dy/mySurfaceView.p);
            mySurfaceView.getHolder().unlockCanvasAndPost(myCanvas);
            mySurfaceView.postInvalidate();
        }

        x0 = x;
        y0 = y;

        return true;
    }
}
