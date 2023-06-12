package pl.cntrpl.netkey;

import static pl.cntrpl.netkey.ConfigActivity.customInputs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceView;

import pl.cntrpl.netkey.input.CustomInput;

public class InputSurfaceView extends SurfaceView {

    InputActivity caller;
    Paint mPaint = new Paint();

    public InputSurfaceView(Context context, InputActivity cl, boolean selfUpdate) {
        super(context);
        caller = cl;
        if (selfUpdate) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                    handler.postDelayed(this, 16);
                }
            }, 16);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //long drawStart = System.currentTimeMillis();
        canvas.drawColor(0xFF000000);
        mPaint.setColor(0xFFFFFFFF);
        canvas.drawLine(50,50,100,100, mPaint);
        for (CustomInput inp : caller.customInputs){
            inp.Render(canvas, mPaint);
        }

        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);
        for (TouchState ts : caller.tStates){
            if (ts.active){
                canvas.drawCircle(ts.x, ts.y, 10,mPaint);
            }
        }
    }
}
