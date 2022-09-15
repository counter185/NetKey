package pl.cntrpl.netkey;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public abstract class CustomInput {

    public int id;
    public float posX, posY, width, height;

    public CustomInput(float x, float y, float w, float h){
        posX = x;
        posY = y;
        width = w;
        height = h;
    }

    public abstract void Render(Canvas a, Paint b);
    public abstract boolean TouchInBound(float x, float y);
    public abstract void TouchDownAt(float touchX, float touchY);
    public abstract void AllTouchesUp();
    public abstract void UpdateState(float touchX, float touchY);
    public abstract int GetState();
    public abstract void NextTick();

    public static void SDLRectLikeDraw(Canvas a, Paint b, float x, float y, float w, float h){
        int cw = a.getWidth();
        int ch = a.getHeight();
        RectF nRect = new RectF(cw*x,ch*y, cw - (cw*(1.0f-x-w)), ch - (ch*(1.0f-y-h)));
        b.setStyle(Paint.Style.STROKE);
        a.drawRect(nRect,b);
    }
    public static void SDLRectLikeFill(Canvas a, Paint b, float x, float y, float w, float h){
        int cw = a.getWidth();
        int ch = a.getHeight();
        RectF nRect = new RectF(cw*x,ch*y, cw - (cw*(1.0f-x-w)), ch - (ch*(1.0f-y-h)));
        b.setStyle(Paint.Style.FILL);
        a.drawRect(nRect,b);
    }

}
