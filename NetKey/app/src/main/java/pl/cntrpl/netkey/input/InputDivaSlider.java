package pl.cntrpl.netkey.input;

import android.graphics.Canvas;
import android.graphics.Paint;

public class InputDivaSlider extends CustomInput {

    private int tapsLastTick = 0;
    private short lastStateT1 = 0;
    private short lastStateT2 = 0;

    private int tapsThisTick = 0;
    private short[] states = new short[2];
    private float[] lastPos = new float[2];

    public InputDivaSlider(int index, int splitX, int splitY){
        super(0,0,0,0);
        float mvtX = 1.0f/splitX;
        float mvtY = 1.0f/splitY;

        float pX = mvtX * (index%splitX);
        float pY = mvtY * ((index/splitX));

        posX = pX;
        posY = pY;
        width = mvtX;
        height = mvtY;
    }

    public InputDivaSlider(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public char GetSaveID() {
        return 'S';
    }

    @Override
    public void Render(Canvas a, Paint b) {
        int w = a.getWidth();
        int h = a.getHeight();

        for (int x = 0; x != 2; x++){
            if (tapsLastTick > x){
                b.setColor(0xFF0000A0);
                SDLRectLikeFill(a,b, lastPos[x] - 0.025f, posY, 0.05f, height);
            }
        }

        b.setColor(0xFFFFFFFF);
        SDLRectLikeDraw(a, b, posX, posY, width, height);
    }

    @Override
    public boolean TouchInBound(float x, float y) {
        return x >= posX && x <= posX+width && y >= posY && y <= posY+height;
    }

    @Override
    public void TouchDownAt(float touchX, float touchY, int tID) {

    }

    @Override
    public void AllTouchesUp() {
        tapsThisTick = 0;
    }

    @Override
    public void UpdateState(float touchX, float touchY, int tID) {
        if (tapsThisTick < 2) {

            if (tapsLastTick <= tapsThisTick){
                lastPos[tapsThisTick] = touchX;
                states[tapsThisTick] = 0;
            } else {
                states[tapsThisTick] = (short)(255 * ((touchX - lastPos[tapsThisTick])/0.2f));
                //Log.d("divaslider", "Setting state to " + states[tapsThisTick] + ", diff: " + (touchX - lastPos[tapsThisTick]));
                lastPos[tapsThisTick] = touchX;
            }
            tapsThisTick++;
        }
    }

    @Override
    public int GetState() {
        short part1 = lastStateT1;
        short part2 = lastStateT2;
        int ret = ((int)part1 << 16) | part2;
        //Log.d("divaslider", "Sending int: " + ret);
        return ret;
    }

    @Override
    public void NextTick() {
        for (int x = 0; x != 2; x++){
            if (tapsThisTick <= x){
                states[x] = 0;
            }
        }
        lastStateT1 = states[0];
        lastStateT2 = states[1];
        tapsLastTick = tapsThisTick;
        tapsThisTick = 0;
    }
}
