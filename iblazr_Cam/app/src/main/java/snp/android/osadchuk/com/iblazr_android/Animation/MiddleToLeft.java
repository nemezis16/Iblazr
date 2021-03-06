package snp.android.osadchuk.com.iblazr_android.Animation;


import android.animation.Animator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

/**
 * Created by ScarS on 10.03.2015.
 */
public class MiddleToLeft extends Animation {

    private View view1;

    private float cx1, cy1;           // center x,y position of circular path

    private float prevX1, prevY1;     // previous x,y position of image during animation

    private float r;                // radius of circle

    /**
     * @param view1 - View that will be animated
     * @param r - radius of circular path
     */
    public MiddleToLeft(View view1, float r){
        this.view1 = view1;
        this.r = r;
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t){

        float angleDeg = (interpolatedTime * 90+90)%360 ;
        float angleRad = (float) Math.toRadians(angleDeg);

        // r = radius, cx and cy = center point, a = angle (radians)
        float x = (float) (cx1 +r * -Math.cos(angleRad)); // -cos - protiv chsovoy/+cos - po chsovoy
        float y = (float) (cy1-r + r * Math.sin(angleRad)); //-sin- vniz / - cos - vverh

        float dx = prevX1 - x;
        float dy = prevY1 - y;

            /*prevX = x;
            prevY = y;*/

        t.getMatrix().setTranslate(dx, dy);
    }
}