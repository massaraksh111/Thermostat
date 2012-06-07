package se.hse;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
import android.text.format.Time;
=======
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
<<<<<<< HEAD
import android.widget.Button;
=======
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TermostatActivity extends Activity {

	OnClickListener currTempListener;
<<<<<<< HEAD
	SharedPreferences settings;
	SharedPreferences.Editor settingsEditor;
	boolean night;
	boolean vacation;
	Date[][][] timetable = new Date[7][2][5];
	boolean[][][] timeAble = new boolean[7][2][5];
	double currTemperature;
	double dayTemperature;
	double nightTemperature;
=======
	TabHost tabHost; // tabwidget
	TabSpec spec1; // main tab
	TabSpec spec2; // day/night mode
	TabSpec spec3; // week view
	TabSpec spec4; // 24h
	
	NumberPicker bigNumberPicker; // полные градусы от 5 до 40
	NumberPicker smallNumberPicker; // десятые части градусов
	
	
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
<<<<<<< HEAD
        settings = getPreferences(0);
        settingsEditor = settings.edit();
        night = settings.getBoolean("dayNightMode", false);
        vacation = settings.getBoolean("vacation", false);
        for (int i = 0; i < 7; i++){
        	for (int l = 0; l < 5; l++){
        		int hour, min, sec;
        		hour = settings.getInt("hour" + i + l, 12);
        		min= settings.getInt("min" + i + l, 0);
        		sec = settings.getInt("sec" + i + l, 0);
        		timeAble[i][l] = settings.getBoolean("timeAble" + i + l, false);
        		timetable[i][l] = new Date(0,0,0,hour, min, sec);
        	}
        }
        checkCurrenMode();
        
        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
=======
        tabHost = (TabHost)findViewById(R.id.tabhost);
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
        tabHost.setup();
        
        spec1 = tabHost.newTabSpec("Termostat");
        spec1.setContent(R.id.thermostat);
        spec1.setIndicator("Termostat");
        
        spec2 = tabHost.newTabSpec("Set mode");
        spec2.setContent(R.id.day_night_mode);
        spec2.setIndicator("Set mode");
        
<<<<<<< HEAD
        TabSpec spec3 = tabHost.newTabSpec("7 days");
=======
        spec3 = tabHost.newTabSpec("7 days");
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
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
             	NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
             	np1.setMaxValue(40);
             	np1.setMinValue(5);
             
             	NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
             	np2.setMaxValue(9);
             	np2.setMinValue(0);
            }
        };
<<<<<<< HEAD
        changeCurrTempB.setOnClickListener(currTempListener);
        
        Button mnd = (Button) findViewById(R.id.monday_button);
        mnd.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		
        	}
        });
=======
        changeCurrTempB.setOnClickListener(currTempListener);*/
>>>>>>> 444ac8f351df4b487df4cbdc3ab9d7a6a9bf374f
    }

	protected void setTemp(boolean b) {
		setContentView(R.layout.set_temperature);
	}
	
	protected checkCurrenMode() {
		
	}
	
	protected void changeMode(){
		Calendar c = Calendar.getInstance(); 
	    int day = c.get(Calendar.DAY_OF_WEEK) - 1;
	    
	}
	
}