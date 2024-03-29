package pl.cntrpl.netkey.activity;

import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import pl.cntrpl.netkey.InputSurfaceView;
import pl.cntrpl.netkey.thread.RendererThread;
import pl.cntrpl.netkey.TouchState;
import pl.cntrpl.netkey.configuration.InputConfiguration;
import pl.cntrpl.netkey.input.CustomInput;
import pl.cntrpl.netkey.input.InputButton;
import pl.cntrpl.netkey.input.InputDivaSlider;
import pl.cntrpl.netkey.thread.InputConnectionThread;

public class InputActivity extends Activity {

    private Paint mPaint = null;
    public int fbW, fbH;

    public boolean paused = false;
    public Thread renderThread = null;
    public Thread netPostThread = null;

    public String ip ="";
    public int port = 5555;
    public int pollRate;

    //todo: pass this to the buttons in some other way
    public static boolean repeatButtons;
    public int connectionID;

    public boolean isPreview = false;

    public SurfaceView nSrfc = null;

    public List<CustomInput> customInputs = new ArrayList<CustomInput>();

    private int lastTs = 0;
    public List<TouchState> tStates = new ArrayList<TouchState>();
    long lastUpd = 0;
    int evts = 0;

    private void generateInputsList(InputConfiguration inputs){
        customInputs = new ArrayList<>();
        for (int x = 0; x != inputs.NRows(); x++){
            for (int y = 0; y != inputs.NCols(x); y++) {
                int inputID = inputs.GetInputAt(x,y);
                CustomInput addInput =
                        inputID == 1 ? new InputButton(inputs.NCols(x)*x+ y, inputs.NCols(x), inputs.NRows())
                                : inputID == 2 ? new InputDivaSlider(inputs.NCols(x)*x+ y, inputs.NCols(x), inputs.NRows())
                                : null;
                if (addInput != null) {
                    customInputs.add(addInput);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bndl = getIntent().getBundleExtra("pl.cntrpl.netkey");
        isPreview = bndl.getBoolean("preview", false);
        repeatButtons = bndl.getBoolean("repeatbtn", true);
        if (!isPreview) {
            ip = bndl.getString("ipaddr");
            try {
                port = Integer.parseInt(bndl.getString("port"));
            } catch (Exception e) {
                port = 5555;
            }
            pollRate = bndl.getInt("pollrate");
            connectionID = bndl.getInt("connid");
            //customInputs = ConfigActivity.customInputs;
        }
        generateInputsList(bndl.getParcelable("inputconf"));
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //getWindow().addFlags();
        //getWindow().addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE);
        //this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        insetsController.hide(WindowInsetsCompat.Type.statusBars());
        insetsController.hide(WindowInsetsCompat.Type.navigationBars());
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        nSrfc = new InputSurfaceView(this, this, android.os.Build.VERSION.SDK_INT < 21);
        setContentView(nSrfc);

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

        if (!isPreview) {
            netPostThread = new InputConnectionThread(this);
            netPostThread.start();
        }
    }


}