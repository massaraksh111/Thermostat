package se.hse;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TermostatActivity extends Activity {

	final int NUMBER_OF_DAYS = 7;
	final int NUMBER_OF_MODS = 2;
	final int NUMBER_OF_TIMES = 5;
	
	int currentView;
	
	OnClickListener currTempListener;
	SharedPreferences settings;
	SharedPreferences.Editor settingsEditor;
	boolean night;
	boolean vacation;
	Date[][][] timetable = new Date[NUMBER_OF_DAYS][NUMBER_OF_MODS][NUMBER_OF_TIMES];
	boolean[][][] timeAble = new boolean[NUMBER_OF_DAYS][NUMBER_OF_MODS][NUMBER_OF_TIMES];
	float currTemperature;
	float dayTemperature;
	float nightTemperature;
	TabHost tabHost; // tabwidget
	TabSpec spec1; // main tab
	TabSpec spec2; // day/night mode
	TabSpec spec3; // week view
	TabSpec spec4; // 24h
	Timer timer;
	String[] weekString = new String[7];
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        weekString[0] = "Monday";
        weekString[1] = "Tuesday";
        weekString[2] = "Wednesday";
        weekString[3] = "Thursday";
        weekString[4] = "Friday";
        weekString[5] = "Saturday";
        weekString[6] = "Sunday";
        
        settings = getPreferences(0);
        settingsEditor = settings.edit();
        night = settings.getBoolean("dayNightMode", false);
        vacation = settings.getBoolean("vacation", false);
        currTemperature = settings.getFloat("currTemperature", 20.0f);
    	dayTemperature = settings.getFloat("dayTemperature", 18.0f);
    	nightTemperature = settings.getFloat("nightTemperature", 23.0f);
        for (int d = 0; d < NUMBER_OF_DAYS; d++){
        	for (int m = 0; m < NUMBER_OF_MODS; m++){
        		for (int t = 0; t < NUMBER_OF_TIMES; t++){
        			int hour, min, sec;
        			hour = settings.getInt("hour" + d + m + t, 12);
        			min= settings.getInt("min" + + d + m + t, 0);
        			sec = settings.getInt("sec" + + d + m + t, 0);
        			timeAble[d][m][t] = settings.getBoolean("timeAble" + + d + m + t, false);
        			timetable[d][m][t] = new Date(0,0,0,hour, min, sec);
        		}
        	}
        }
        checkCurrenMode();
        
        initMain();
        currentView = 0;
        timer = new Timer();
        setTimerTask();
        

    }
    
    void setTimerTask() {
     timer.schedule(new TimerTask() {
      @Override
      public void run() { checkCurrenMode(); } }, 45*1000);
     
    }
    
	protected void setTemp(boolean b) {
		setContentView(R.layout.set_temperature);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { 
        	if (currentView != 0){
        		setContentView(R.layout.main);
        		initMain();
        		return true;
        	} else {
        		return super.onKeyDown(keyCode, event);
        	}
        }
        return false;
     }
	
	protected void checkCurrenMode() {
		Calendar c = Calendar.getInstance(); 
	    int day = c.get(Calendar.DAY_OF_WEEK) - 1;
	    int hour = c.get(Calendar.HOUR_OF_DAY);
	    int min = c.get(Calendar.MINUTE);
	    int sec = c.get(Calendar.SECOND);
	    int[] LastHour = new int[2];
	    int[] LastMin = new int[2];
	    int[] LastSec = new int[2];
	    for (int m = 0; m < NUMBER_OF_MODS; m++) {
	    	for (int t = 0; t < NUMBER_OF_TIMES; t++) {
	    		if (timeAble[day][m][t]) {
	    			if (timetable[day][m][t].before( new Date(0, 0, 0, hour, min, sec) ) ) {
	    				LastHour[m] = timetable[day][m][t].getHours();
	    				LastMin[m] = timetable[day][m][t].getMinutes();
	    				LastSec[m] = timetable[day][m][t].getSeconds();
	    			}
	    		} else {
	    			continue;
	    		}
	    	}
	    }
	    //0 - day, 1 - night
	    if ( (new Date(0, 0, 0, LastHour[0], LastMin[0], LastSec[0])).after(new 
	    		Date(0, 0, 0, LastHour[1], LastMin[1], LastSec[1]))) {
	    	night = false;
	    	settingsEditor.putBoolean("night", false);
	    } else {
	    	night = true;
	    	settingsEditor.putBoolean("night", true);
	    }
	}
	
	private void initMain() {
		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        
        spec1 = tabHost.newTabSpec("Termostat");
        spec1.setContent(R.id.thermostat);
        spec1.setIndicator("Termostat");
        
        spec2 = tabHost.newTabSpec("Set mode");
        spec2.setContent(R.id.day_night_mode);
        spec2.setIndicator("Set mode");
        
        TabSpec spec3 = tabHost.newTabSpec("7 days");
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
        final ImageButton changeCurrTempB = (ImageButton) findViewById(R.id.changeCurrTempClick);
        currTempListener = new OnClickListener(){

            public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
             	
            	TextView tv = (TextView) findViewById(R.id.setTempText);
            	tv.setText("Temporary Temperature");
            	
            	/*ImageButton ib = (ImageButton) findViewById(R.id.set_temperature_image);
            	ib.setVisibility(ImageButton.INVISIBLE);*/
            	
            	NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
             	np1.setMaxValue(40);
             	np1.setMinValue(5);
             	np1.setValue((int)currTemperature);
             	np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						currTemperature += (newVal - oldVal);
						settingsEditor.putFloat("currTemperature", currTemperature);
						settingsEditor.apply();
					}
				});
             	
             	NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
             	np2.setMaxValue(9);
             	np2.setMinValue(0);
             	np2.setValue((int)(currTemperature * 10 % 10));
             	np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						float tmp = newVal * 0.1f;
						currTemperature = (int)currTemperature + tmp;
						settingsEditor.putFloat("currTemperature", currTemperature);
						settingsEditor.apply();
					}
				});
             	
             	
             	CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
             	cb.setChecked(vacation);
             	cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						vacation = !vacation;
					}
				});
             	
             	currentView = 1;
            }
        };
        changeCurrTempB.setOnClickListener(currTempListener);
        
        Button mnd = (Button) findViewById(R.id.monday_button);
        mnd.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Monday");
        		currentView = 1;
        	}
        });
        Button tue = (Button) findViewById(R.id.tuesday_button);
        tue.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Tuesday");
        		currentView = 1;
        	}
        });
        Button wen = (Button) findViewById(R.id.wednesday_button);
        wen.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Wednesday");
        		currentView = 1;
        	}
        });
        Button thu = (Button) findViewById(R.id.thursday_button);
        thu.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Thursday");
        		currentView = 1;
        	}
        });
        Button fri = (Button) findViewById(R.id.friday_button);
        fri.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Friday");
        		currentView = 1;
        	}
        });
        Button sat = (Button) findViewById(R.id.saturday_button);
        sat.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Saturday");
        		currentView = 1;
        	}
        });
        Button sun = (Button) findViewById(R.id.sunday_button);
        sun.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Sunday");
        		currentView = 1;
        	}
        });
	}
	
}