package pl.cntrpl.netkey;

import android.os.Build;
import android.os.Environment;

public class RendererThread extends Thread{

    public InputActivity caller;
    public static boolean canDoMultiThreadedRender = true;
    public RendererThread(InputActivity c){
        caller = c;
        canDoMultiThreadedRender = android.os.Build.VERSION.SDK_INT >= 21;
    }

    @Override
    public void run()
    {
        try {
            caller.img.invalidate();
        } catch (Exception e){
            canDoMultiThreadedRender = false;
        }

        while (true) {
            if (caller.isDestroyed()){
                break;
            }
            if (!caller.paused) {
                //float startTime = System.currentTimeMillis();

                //rendercode here

                caller.Redraw();
                //caller.img.invalidate();

                //float endTime = System.currentTimeMillis();
                //long deltaTime = (long) (frameTime - (endTime - startTime));
                try {
                    Thread.sleep(4);
                } catch (InterruptedException e) {
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }

    }
}
