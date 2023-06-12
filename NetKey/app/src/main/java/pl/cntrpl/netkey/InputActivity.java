package pl.cntrpl.netkey;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import pl.cntrpl.netkey.input.CustomInput;

public class InputActivity extends Activity {

    private Paint mPaint = null;
    public int fbW, fbH;

    public boolean paused = false;
    public Thread renderThread = null;
    public Thread netPostThread = null;

    public String ip ="";
    public int port = 5555;

    public SurfaceView nSrfc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bndl = getIntent().getBundleExtra("pl.cntrpl.netkey");
        ip = bndl.getString("ipaddr");
        try {
            port = Integer.parseInt(bndl.getString("port"));
        } catch (Exception e){
            port = 5555;
        }
        customInputs = ConfigActivity.customInputs;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nSrfc = new InputSurfaceView(this, this, android.os.Build.VERSION.SDK_INT < 21);
        setContentView(nSrfc);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*customInputs.add(new InputButton(0,7,1));
        customInputs.add(new InputButton(1,7,1));
        customInputs.add(new InputButton(2,7,1));
        customInputs.add(new InputButton(3,7,1));
        customInputs.add(new InputButton(4,7,1));
        customInputs.add(new InputButton(5,7,1));
        customInputs.add(new InputButton(6,7,1));*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*public void Redraw(){

        //long drawStart = System.currentTimeMillis();
        mCanvas.drawColor(0xFF000000);
        mPaint.setColor(0xFFFFFFFF);
        mCanvas.drawLine(50,50,100,100, mPaint);
        for (CustomInput inp : customInputs){
            inp.Render(mCanvas, mPaint);
        }

        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);
        for (TouchState ts : tStates){
            if (ts.active){
                mCanvas.drawCircle(ts.x, ts.y, 10,mPaint);
            }
        }
        fbCanbas.drawBitmap(mBitmap,0,0, fbPaint);
        //long drawEnd = System.currentTimeMillis();
        //System.out.println("render took " + (drawEnd-drawStart) + "ms");
        if (RendererThread.canDoMultiThreadedRender) {
            img.invalidate();
        }
    }*/

    public List<CustomInput> customInputs = new ArrayList<CustomInput>();

    private int lastTs = 0;
    public List<TouchState> tStates = new ArrayList<TouchState>();
    long lastUpd = 0;
    int evts = 0;
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        //Log.d("tt", "events"+(evts++)+", "+motionEvent.getAction());
        //Log.d("TouchTest", "Evts: " + motionEvent.getPointerCount());
        while (tStates.size() < motionEvent.getPointerCount()){
            tStates.add(new TouchState());
        }



        int cLastTs = 0;
        if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            cLastTs = 1;
            cLastTs += motionEvent.getPointerCount()-1;
        } else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
            Log.d("TouchTest", "action_up");
            cLastTs = 0;
            for (CustomInput inp : customInputs){
                inp.AllTouchesUp();
            }
            cLastTs += motionEvent.getPointerCount()-1;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_POINTER_3_UP) {
            cLastTs += motionEvent.getPointerCount() - 1;
        } else if (motionEvent.getAction() == MotionEvent.ACTION_POINTER_2_UP) {
            cLastTs += motionEvent.getPointerCount() - 1;
        } else {
            cLastTs += motionEvent.getPointerCount();
        }
        if (cLastTs != lastTs){
            Log.d("TouchTest", "Number of active touches changed from " + lastTs + " to " + cLastTs);
        }
        lastTs = cLastTs;

        for (int x = 0; x != lastTs; x++){
            float px = motionEvent.getX(x);
            float py = motionEvent.getY(x);
            for (CustomInput inp : customInputs){
                if (inp.TouchInBound(px/fbW, py/fbH)){
                    inp.UpdateState(px/fbW, py/fbH, x);
                }
            }

            tStates.get(x).x = px;
            tStates.get(x).y = py;
            //Log.d("TouchTest", "TouchEvent " + x + " at " + px + "," + py);
        }

        //Log.d("t", "states: " + lastTs);
        for (int x = 0; x < tStates.size(); x++){
            tStates.get(x).active = x < lastTs;
        }
        /*if(motionEvent.getPointerCount() > 1) {
            System.out.println("Multitouch detected! " + pID);
        }*/
        for (CustomInput inp : customInputs){
            inp.NextTick();
        }
        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Log.d("a","created");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        fbW = displayMetrics.widthPixels;
        fbH = displayMetrics.heightPixels;

        /*img = this.findViewById(R.id.mainCanvas);
        mBitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        mainFramebuffer = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);
        //fbCanbas = new Canvas(mainFramebuffer);
        mCanvas.drawColor(0xFF000000);
        img.setImageBitmap(mainFramebuffer);*/

        mPaint = new Paint();

        //Redraw();
        if (android.os.Build.VERSION.SDK_INT >= 21){
            renderThread = new RendererThread(this);
            renderThread.start();
        }

        netPostThread = new InputConnectionThread(this);
        netPostThread.start();
    }


}