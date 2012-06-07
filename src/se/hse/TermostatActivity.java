package se.hse;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TermostatActivity extends Activity {

	OnClickListener currTempListener;
	TabHost tabHost; // tabwidget
	TabSpec spec1; // main tab
	TabSpec spec2; // day/night mode
	TabSpec spec3; // week view
	TabSpec spec4; // 24h
	
	NumberPicker bigNumberPicker; // полные градусы от 5 до 40
	NumberPicker smallNumberPicker; // десятые части градусов
	
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        
        spec1 = tabHost.newTabSpec("Termostat");
        spec1.setContent(R.id.thermostat);
        spec1.setIndicator("Termostat");
        
        spec2 = tabHost.newTabSpec("Set mode");
        spec2.setContent(R.id.day_night_mode);
        spec2.setIndicator("Set mode");
        
        spec3 = tabHost.newTabSpec("7 days");
        spec3.setContent(R.id.week_view);
        spec3.setIndicator("7 days");
        
        spec4 = tabHost.newTabSpec("24h");
        spec4.setContent(R.id.day);
        spec4.setIndicator("24h");
        
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);
        /*bigNumberPicker = (NumberPicker) findViewById(R.id.temperature_big_setter);
        bigNumberPicker.setMaxValue(40);
        bigNumberPicker.setMinValue(5);
        
        smallNumberPicker= (NumberPicker) findViewById(R.id.temperature_small_setter);
        smallNumberPicker.setMaxValue(9);
        smallNumberPicker.setMinValue(0);
        
        ImageView im = (ImageView) findViewById(R.id.set_temperature_image);
        im.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				v.setBackgroundResource(R.drawable.little_sun_2_5);
				Log.println(0, "dfddf", "hererrerererererer");
			}
        	
        }
			
        );
        final ImageButton changeCurrTempB = (ImageButton) findViewById(R.id.changeCurrTempClick);
        View.OnClickListener currTempListener = new OnClickListener(){

            public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
            }
        };
        changeCurrTempB.setOnClickListener(currTempListener);*/
    }

	protected void setTemp(boolean b) {
		setContentView(R.layout.set_temperature);
	}
}