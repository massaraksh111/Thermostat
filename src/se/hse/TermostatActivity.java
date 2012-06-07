package se.hse;

import android.app.Activity;
import android.os.Bundle;
<<<<<<< HEAD
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
=======
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
>>>>>>> e0dfc8aabace6f846e163fa7c1ff73bc48ec9136
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TermostatActivity extends Activity {

	OnClickListener currTempListener;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        
        TabSpec spec1 = tabHost.newTabSpec("Termostat");
        spec1.setContent(R.id.thermostat);
        spec1.setIndicator("Termostat");
        
        TabSpec spec2 = tabHost.newTabSpec("Set mode");
        spec2.setContent(R.id.day_night_mode);
        spec2.setIndicator("Set mode");
        
        TabSpec spec3 = tabHost.newTabSpec("7 days");
        spec3.setContent(R.id.week);
        spec3.setIndicator("7 days");
        
        TabSpec spec4 = tabHost.newTabSpec("24h");
        spec4.setContent(R.id.day);
        spec4.setIndicator("24h");
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);
        
<<<<<<< HEAD
        /*NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
        np1.setMaxValue(40);
        np1.setMinValue(5);
        
        NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
        np2.setMaxValue(9);
        np2.setMinValue(0);
        
        ImageView im = (ImageView) findViewById(R.id.set_temperature_image);
        im.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setBackgroundResource(R.drawable.little_sun_2_5);
				Log.println(0, "dfddf", "hererrerererererer");
			}
        	
        }
			
        );*/
=======
        final ImageButton changeCurrTempB = (ImageButton) findViewById(R.id.changeCurrTempClick);
        currTempListener = new OnClickListener(){

            public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
            }
        };
        changeCurrTempB.setOnClickListener(currTempListener);
>>>>>>> e0dfc8aabace6f846e163fa7c1ff73bc48ec9136
    }

	protected void setTemp(boolean b) {
		setContentView(R.layout.set_temperature);
	}
}