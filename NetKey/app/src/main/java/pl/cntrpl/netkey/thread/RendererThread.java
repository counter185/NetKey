package pl.cntrpl.netkey.thread;

import pl.cntrpl.netkey.activity.InputActivity;

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
        while (true) {
            if (caller.isDestroyed()){
                break;
            }
            if (!caller.paused) {
                //float startTime = System.currentTimeMillis();

                //rendercode here
                caller.nSrfc.invalidate();

                //caller.Redraw();
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
