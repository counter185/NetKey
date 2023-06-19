package pl.cntrpl.netkey.input;

import android.graphics.Canvas;
import android.graphics.Paint;

import pl.cntrpl.netkey.activity.InputActivity;

public class InputButton extends CustomInput {

    private boolean state = false;

    private boolean lastState = false;
    public long blockTimes = 0;
    public int tapsThisTick = 0;
    public int tapsLastTick = 0;

    public InputButton(int index, int splitX, int splitY){
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

    public InputButton(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    @Override
    public char GetSaveID(){
        return 'B';
    }

    @Override
    public void Render(Canvas a, Paint b) {
        int w = a.getWidth();
        int h = a.getHeight();

        if (lastState){
            b.setColor(blockTimes % 8 != 0 ? 0xA0AA0000 : 0xA00000A0);
            SDLRectLikeFill(a, b, posX, posY, width, height);
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
        state = false;
    }

    @Override
    public void UpdateState(float touchX, float touchY, int tID) {
        state = TouchInBound(touchX, touchY);
        if (InputActivity.repeatButtons && ++tapsThisTick > tapsLastTick && tapsLastTick > 0){
            blockTimes+=8;
        }
        lastState = state;
    }

    @Override
    public int GetState() {
        if (blockTimes > 0){
            blockTimes--;
        }
        return ((lastState && blockTimes % 8 == 0) || blockTimes == 1) ? 255 : 0;
    }

    @Override
    public void NextTick() {
        lastState = state;
        state = false;
        tapsLastTick = tapsThisTick;
        tapsThisTick = 0;
    }
}
