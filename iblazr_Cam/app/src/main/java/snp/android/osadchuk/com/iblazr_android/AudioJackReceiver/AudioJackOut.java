package snp.android.osadchuk.com.iblazr_android.AudioJackReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import snp.android.osadchuk.com.iblazr_android.BeforeInputActivity;
import snp.android.osadchuk.com.iblazr_android.Iblazr_Library.shot.GeneratorPulseThread;
import snp.android.osadchuk.com.iblazr_android.MainActivity;

/**
 * Created by ScarS on 09.04.2015.
 */
public class AudioJackOut extends BroadcastReceiver {
    private static final String TAG = "MainActivity";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Log.d(TAG, "Headset is unplugged");

                    Intent i = new Intent(context, BeforeInputActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(TAG, true);
                    context.startActivity(i);

                    break;
                case 1:
                    Log.d(TAG, "Headset is plugged");
                default:
                    Log.d(TAG, "I have no idea what the headset state is");
            }
        }
    }
}
