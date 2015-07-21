package snp.android.osadchuk.com.iblazr_android;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import snp.android.osadchuk.com.iblazr_android.AudioJackReceiver.AudioJackIntentReceiver;

/**
 * Created by ScarS on 09.04.2015.
 */
public class BeforeInputActivity extends Activity {
    AudioJackIntentReceiver mAudioJackIntentReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beforeinputactivity);
        mAudioJackIntentReceiver=new AudioJackIntentReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mAudioJackIntentReceiver, filter);
    }

    public void onBackPressed() {
        this.finish();
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mAudioJackIntentReceiver);
    }
}
