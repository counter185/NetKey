package pl.cntrpl.netkey;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.method.Touch;

public class InputButton extends CustomInput {

    private boolean state = false;

    private boolean lastState = false;

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
    public void Render(Canvas a, Paint b) {
        int w = a.getWidth();
        int h = a.getHeight();

        if (lastState){
            b.setColor(0xA00000A0);
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
    public void TouchDownAt(float touchX, float touchY) {

    }

    @Override
    public void AllTouchesUp() {
        state = false;
        lastState = false;
    }

    @Override
    public void UpdateState(float touchX, float touchY) {
        state = TouchInBound(touchX, touchY);
    }

    @Override
    public int GetState() {
        return lastState ? 255 : 0;
    }

    @Override
    public void NextTick() {
        lastState = state;
        state = false;
    }
}
