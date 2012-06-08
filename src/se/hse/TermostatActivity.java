package se.hse;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TimePicker;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class TermostatActivity extends Activity {

	final int NUMBER_OF_DAYS = 7;
	final int NUMBER_OF_MODS = 2;
	final int NUMBER_OF_TIMES = 5;
	
	int currentView;
	
	SharedPreferences settings;
	SharedPreferences.Editor settingsEditor;
	boolean night;
	boolean vacation;
	boolean tmpVac;
	Date[][][] timetable = new Date[NUMBER_OF_DAYS][NUMBER_OF_MODS][NUMBER_OF_TIMES];
	boolean[][][] timeAble = new boolean[NUMBER_OF_DAYS][NUMBER_OF_MODS][NUMBER_OF_TIMES];
	float currTemperature;
	float dayTemperature;
	float nightTemperature;
	float tmpTemp;
	boolean[] nextNight = new boolean[3];
	boolean[] nextDay = new boolean[3];
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
    	tmpTemp = currTemperature;
        tmpVac = vacation;
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
	
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { 
        	if (currentView != 0){
        		setContentView(R.layout.main);
        		currentView = 0;
        		tmpVac = vacation;
        		tmpTemp = currTemperature;
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
	    	settingsEditor.apply();
	    } else {
	    	night = true;
	    	settingsEditor.putBoolean("night", true);
	    	settingsEditor.apply();
	    }
	}
	
	private void changeTimeTablePic(LinearLayout ll) {
		Date d1 = findNextTime();
		
		if (d1 == null){
			ll.setVisibility(LinearLayout.GONE);
		} else {
			ImageButton ib1m = (ImageButton) findViewById(R.id.main_view_first_image_moon);
			ImageButton ib1s = (ImageButton) findViewById(R.id.main_view_first_image_sun);
			TextView tw1 = (TextView) findViewById(R.id.main_view_first_time);
			if (nextDay[0]) {
				ib1s.setVisibility(ImageButton.VISIBLE);
				ib1m.setVisibility(ImageButton.GONE);
			} else {
				ib1s.setVisibility(ImageButton.GONE);
				ib1m.setVisibility(ImageButton.VISIBLE);
			}
			tw1.setText("" + d1.getHours()+ ":" + d1.getMinutes());
		}
		
		Date d2 = findSecondTime(d1);
		
		ImageButton ib2m = (ImageButton) findViewById(R.id.main_view_second_image_moon);
		ImageButton ib2s = (ImageButton) findViewById(R.id.main_view_second_image_sun);
		TextView tw2 = (TextView) findViewById(R.id.main_view_second_time);
		LinearLayout ll2 = (LinearLayout) findViewById(R.id.secondTT);
		if (d2 == null){
			ll2.setVisibility(LinearLayout.GONE);
		} else {
			if (nextDay[0]) {
				ib2s.setVisibility(ImageButton.VISIBLE);
				ib2m.setVisibility(ImageButton.GONE);
			} else {
				ib2s.setVisibility(ImageButton.GONE);
				ib2m.setVisibility(ImageButton.VISIBLE);
			}
			tw2.setText("" + d2.getHours()+ ":" + d2.getMinutes());
		}
		
		Date d3 = findThirdTime(d2);
		
		ImageButton ib3m = (ImageButton) findViewById(R.id.main_view_third_image_moon);
		ImageButton ib3s = (ImageButton) findViewById(R.id.main_view_third_image_sun);
		TextView tw3 = (TextView) findViewById(R.id.main_view_third_time);
		LinearLayout ll3 = (LinearLayout) findViewById(R.id.thirdTT);
		if (d2 == null){
			ll3.setVisibility(LinearLayout.GONE);
		} else {
			if (nextDay[0]) {
				ib3s.setVisibility(ImageButton.VISIBLE);
				ib3m.setVisibility(ImageButton.GONE);
			} else {
				ib3s.setVisibility(ImageButton.GONE);
				ib3m.setVisibility(ImageButton.VISIBLE);
			}
			tw3.setText("" + d3.getHours()+ ":" + d3.getMinutes());
		}
	}
	
	private Date findNextTime() {
		Date res = new Date();
		
		Calendar c = Calendar.getInstance(); 
	    int day = c.get(Calendar.DAY_OF_WEEK) - 1;
	    int hour = c.get(Calendar.HOUR_OF_DAY);
	    int min = c.get(Calendar.MINUTE);
	    int sec = c.get(Calendar.SECOND);
	    int[] LastHour = new int[2];
	    int[] LastMin = new int[2];
	    int[] LastSec = new int[2];
	    boolean flagDay = false;
	    boolean flagNight = false;
	    for (int m = 0; m < NUMBER_OF_MODS; m++) {
	    	for (int t = 0; t < NUMBER_OF_TIMES; t++) {
	    		if (timeAble[day][m][t]) {
	    			if (timetable[day][m][t].after( new Date(0, 0, 0, hour, min, sec) ) ) {
	    				LastHour[m] = timetable[day][m][t].getHours();
	    				LastMin[m] = timetable[day][m][t].getMinutes();
	    				LastSec[m] = timetable[day][m][t].getSeconds();
	    				if (m == 0) {
	    					flagDay = true;
	    				} else {
	    					flagNight = true;
	    				}
	    				break;
	    			}
	    		} else {
	    			continue;
	    		}
	    	}
	    }
	    if (flagDay) {
	    	if (flagNight) {
	    		//0 - day, 1 - night
	    	    if ( (new Date(0, 0, 0, LastHour[0], LastMin[0], LastSec[0])).before(new 
	    	    		Date(0, 0, 0, LastHour[1], LastMin[1], LastSec[1]))) {
	    	    	nextNight[0] = false;
	    	    	nextDay[0] = true;
	    	    	res.setHours(LastHour[0]);
		    		res.setMinutes(LastMin[0]);
		    		res.setSeconds(LastSec[0]);
	    	    } else {
	    	    	nextNight[0] = true;
	    	    	nextDay[0] = false;
	    	    	res.setHours(LastHour[1]);
		    		res.setMinutes(LastMin[1]);
		    		res.setSeconds(LastSec[1]);
	    	    }
	    	} else {
	    		nextNight[0] = false;
    	    	nextDay[0] = true;
	    		res.setHours(LastHour[0]);
	    		res.setMinutes(LastMin[0]);
	    		res.setSeconds(LastSec[0]);
	    	}
	    } else {
	    	if (flagNight) {
	    		nextNight[0] = true;
    	    	nextDay[0] = false;
	    		res.setHours(LastHour[1]);
	    		res.setMinutes(LastMin[1]);
	    		res.setSeconds(LastSec[1]);
	    	} else {
	    		nextNight[0] = false;
    	    	nextDay[0] = false;
	    		return null;
	    	}
	    }
	    
		return res;
	}

	private Date findSecondTime(Date d) {
		Date res = new Date();
		
		if (!nextDay[0]) {
	    	if (!nextNight[0]){
	    		nextDay[1] = false;
	    		nextNight[1] = false;
	    		return null;
	    	}
	    }
		
		Calendar c = Calendar.getInstance(); 
	    int day = c.get(Calendar.DAY_OF_WEEK) - 1;
	    int hour = d.getHours();
	    int min = d.getMinutes();
	    int sec = d.getSeconds();
	    int[] LastHour = new int[2];
	    int[] LastMin = new int[2];
	    int[] LastSec = new int[2];
	    boolean flagDay = false;
	    boolean flagNight = false;
	    
	    for (int m = 0; m < NUMBER_OF_MODS; m++) {
	    	for (int t = 0; t < NUMBER_OF_TIMES; t++) {
	    		if (timeAble[day][m][t]) {
	    			if (timetable[day][m][t].after( new Date(0, 0, 0, hour, min, sec) ) ) {
	    				LastHour[m] = timetable[day][m][t].getHours();
	    				LastMin[m] = timetable[day][m][t].getMinutes();
	    				LastSec[m] = timetable[day][m][t].getSeconds();
	    				if (m == 0) {
		    				flagDay = true;
		    			} else {
		    				flagNight = true;
		    			}
	    				break;
	    			}
	    		} else {
	    			continue;
	    		}
	    	}
	    }
	    if (flagDay) {
	    	if (flagNight) {
	    		//0 - day, 1 - night
	    	    if ( (new Date(0, 0, 0, LastHour[0], LastMin[0], LastSec[0])).before(new 
	    	    		Date(0, 0, 0, LastHour[1], LastMin[1], LastSec[1]))) {
	    	    	nextNight[1] = false;
	    	    	nextDay[1] = true;
	    	    	res.setHours(LastHour[0]);
		    		res.setMinutes(LastMin[0]);
		    		res.setSeconds(LastSec[0]);
	    	    } else {
	    	    	nextNight[1] = true;
	    	    	nextDay[1] = false;
	    	    	res.setHours(LastHour[1]);
		    		res.setMinutes(LastMin[1]);
		    		res.setSeconds(LastSec[1]);
	    	    }
	    	} else {
	    		nextNight[1] = false;
    	    	nextDay[1] = true;
	    		res.setHours(LastHour[0]);
	    		res.setMinutes(LastMin[0]);
	    		res.setSeconds(LastSec[0]);
	    	}
	    } else {
	    	if (flagNight) {
	    		nextNight[1] = true;
    	    	nextDay[1] = false;
	    		res.setHours(LastHour[1]);
	    		res.setMinutes(LastMin[1]);
	    		res.setSeconds(LastSec[1]);
	    	} else {
	    		nextNight[1] = false;
    	    	nextDay[1] = false;
	    		return null;
	    	}
	    }
	    
		return res;
	}
	
	private Date findThirdTime(Date d) {
		Date res = new Date();
		
		if (!nextDay[1]) {
	    	if (!nextNight[1]){
	    		nextDay[2] = false;
	    		nextNight[2] = false;
	    		return null;
	    	}
	    }
		
		Calendar c = Calendar.getInstance(); 
	    int day = c.get(Calendar.DAY_OF_WEEK) - 1;
	    int hour = d.getHours();
	    int min = d.getMinutes();
	    int sec = d.getSeconds();
	    int[] LastHour = new int[2];
	    int[] LastMin = new int[2];
	    int[] LastSec = new int[2];
	    boolean flagDay = false;
	    boolean flagNight = false;
	    
	    for (int m = 0; m < NUMBER_OF_MODS; m++) {
	    	for (int t = 0; t < NUMBER_OF_TIMES; t++) {
	    		if (timeAble[day][m][t]) {
	    			if (timetable[day][m][t].after( new Date(0, 0, 0, hour, min, sec) ) ) {
	    				LastHour[m] = timetable[day][m][t].getHours();
	    				LastMin[m] = timetable[day][m][t].getMinutes();
	    				LastSec[m] = timetable[day][m][t].getSeconds();
	    				if (m == 0) {
		    				flagDay = true;
		    			} else {
		    				flagNight = true;
		    			}
	    				break;
	    			}
	    		} else {
	    			continue;
	    		}
	    	}
	    }
	    if (flagDay) {
	    	if (flagNight) {
	    		//0 - day, 1 - night
	    	    if ( (new Date(0, 0, 0, LastHour[0], LastMin[0], LastSec[0])).before(new 
	    	    		Date(0, 0, 0, LastHour[1], LastMin[1], LastSec[1]))) {
	    	    	nextNight[2] = false;
	    	    	nextDay[2] = true;
	    	    	res.setHours(LastHour[0]);
		    		res.setMinutes(LastMin[0]);
		    		res.setSeconds(LastSec[0]);
	    	    } else {
	    	    	nextNight[2] = true;
	    	    	nextDay[2] = false;
	    	    	res.setHours(LastHour[1]);
		    		res.setMinutes(LastMin[1]);
		    		res.setSeconds(LastSec[1]);
	    	    }
	    	} else {
	    		nextNight[2] = false;
    	    	nextDay[2] = true;
	    		res.setHours(LastHour[0]);
	    		res.setMinutes(LastMin[0]);
	    		res.setSeconds(LastSec[0]);
	    	}
	    } else {
	    	if (flagNight) {
	    		nextNight[2] = true;
    	    	nextDay[2] = false;
	    		res.setHours(LastHour[1]);
	    		res.setMinutes(LastMin[1]);
	    		res.setSeconds(LastSec[1]);
	    	} else {
	    		nextNight[2] = false;
    	    	nextDay[2] = false;
	    		return null;
	    	}
	    }
	    
		return res;
	}
	
	private void initMain() {
		//Задаем табы
		setTabs();
        
        //Большая главная картинка
        setBigPic();
		
        //Кнопки дней
        setDaysAction();
        
        //Вывод температуры на глагне
    	TextView mainTemp = (TextView) findViewById(R.id.main_view_temperature);
    	mainTemp.setText(""+currTemperature+"°C");
        
    	//Кнопка изменения ночной температуры
    	nightTemperatureChange();
    	
    	//Кнопка изменения дневной температуры
    	dayTemperatureChange();
    	
    	//24 часа кнопка 
    	showTimeTableChange(-1);
	}
	
	private void dayTemperatureChange() {
		TextView modes_day_edit = (TextView) findViewById(R.id.day_night_mode_day_edit);
    	modes_day_edit.setOnClickListener(new OnClickListener() {
    		   
    		public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
             	
            	TextView tv = (TextView) findViewById(R.id.setTempText);
            	tv.setText("Temporary Temperature");
            	
            	//Кнопка аплая температуры
                Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
                setTemperatureAppl.setOnClickListener(new Button.OnClickListener() {
                	
                	public void onClick(View v) {
                		currTemperature = tmpTemp;
                		vacation = tmpVac;
                		settingsEditor.putFloat("currTemperature", currTemperature);
                		settingsEditor.putBoolean("vacation", vacation);
                		settingsEditor.apply();
                		setContentView(R.layout.main);
                		currentView = 0;
                		initMain();
                	}
                });
            	
            	NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
             	np1.setMaxValue(40);
             	np1.setMinValue(5);
             	np1.setValue((int)currTemperature);
             	np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						tmpTemp = newVal + tmpTemp*10 % 10*0.1f;
					}
				});
             	
             	NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
             	np2.setMaxValue(9);
             	np2.setMinValue(0);
             	np2.setValue((int)(currTemperature * 10 % 10));
             	np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int)tmpTemp + tmp;
					}
				});
             	
             	
             	CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
             	cb.setVisibility(View.INVISIBLE);
             	
             	currentView = 1;
            }
        });
	}

	private void nightTemperatureChange() {
		TextView modes_night_edit = (TextView) findViewById(R.id.day_night_mode_night_edit);
    	modes_night_edit.setOnClickListener(new OnClickListener() {
    		   
    		public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
             	
            	TextView tv = (TextView) findViewById(R.id.setTempText);
            	tv.setText("Temporary Temperature");
            	
            	//Кнопка аплая температуры
                Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
                setTemperatureAppl.setOnClickListener(new Button.OnClickListener() {
                	
                	public void onClick(View v) {
                		currTemperature = tmpTemp;
                		vacation = tmpVac;
                		settingsEditor.putFloat("currTemperature", currTemperature);
                		settingsEditor.putBoolean("vacation", vacation);
                		settingsEditor.apply();
                		setContentView(R.layout.main);
                		currentView = 0;
                		initMain();
                	}
                });
            	
            	NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
             	np1.setMaxValue(40);
             	np1.setMinValue(5);
             	np1.setValue((int)currTemperature);
             	np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						tmpTemp = newVal + tmpTemp*10 % 10*0.1f;
					}
				});
             	
             	NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
             	np2.setMaxValue(9);
             	np2.setMinValue(0);
             	np2.setValue((int)(currTemperature * 10 % 10));
             	np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int)tmpTemp + tmp;
					}
				});
             	
             	
             	CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
             	cb.setVisibility(View.INVISIBLE);
             	
             	currentView = 1;
            }
        });
	}

	private void setDaysAction() {
		TextView mnd = (TextView) findViewById(R.id.monday_button);
        mnd.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Monday");
        		showTimeTableChange(0);
        		currentView = 1;
        	}
        });
        
        TextView tue = (TextView) findViewById(R.id.tuesday_button);
        tue.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Tuesday");
        		showTimeTableChange(1);
        		currentView = 1;
        	}
        });
        
        TextView wen = (TextView) findViewById(R.id.wednesday_button);
        wen.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Wednesday");
        		showTimeTableChange(2);
        		currentView = 1;
        	}
        });
        
        TextView thu = (TextView) findViewById(R.id.thursday_button);
        thu.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Thursday");
        		showTimeTableChange(3);
        		currentView = 1;
        	}
        });
        
        TextView fri = (TextView) findViewById(R.id.friday_button);
        fri.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Friday");
        		showTimeTableChange(4);
        		currentView = 1;
        	}
        });
        
        TextView sat = (TextView) findViewById(R.id.saturday_button);
        sat.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Saturday");
        		showTimeTableChange(5);
        		currentView = 1;
        	}
        });
        
        TextView sun = (TextView) findViewById(R.id.sunday_button);
        sun.setOnClickListener(new OnClickListener() {
   
        	public void onClick(View v) {
        		
        		setContentView(R.layout.day_view);
        		TextView tv = (TextView) findViewById(R.id.dayName);
        		tv.setText("Sunday");
        		showTimeTableChange(6);
        		currentView = 1;
        	}
        });

	}

	private void setBigPic() {
		ImageButton changeCurrTempB;
        if (vacation) {
        	changeCurrTempB = (ImageButton) findViewById(R.id.glagneVacation);
        	
        	changeCurrTempB.setVisibility(ImageButton.VISIBLE);
        	((ImageButton) findViewById(R.id.glagneMoon)).setVisibility(ImageButton.GONE);
        	((ImageButton) findViewById(R.id.glagneSun)).setVisibility(ImageButton.GONE);
        	
        	LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
        	ll.setVisibility(LinearLayout.INVISIBLE);
        } else {
        	if (night) {
        		changeCurrTempB = (ImageButton) findViewById(R.id.glagneMoon);
        		
        		changeCurrTempB.setVisibility(ImageButton.VISIBLE);
        		((ImageButton) findViewById(R.id.glagneVacation)).setVisibility(ImageButton.GONE);
            	((ImageButton) findViewById(R.id.glagneSun)).setVisibility(ImageButton.GONE);
            	
            	LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
            	ll.setVisibility(LinearLayout.VISIBLE);
            	changeTimeTablePic(ll);
        	} else {
        		changeCurrTempB = (ImageButton) findViewById(R.id.glagneSun);
        		
        		changeCurrTempB.setVisibility(ImageButton.VISIBLE);
        		((ImageButton) findViewById(R.id.glagneMoon)).setVisibility(ImageButton.GONE);
            	((ImageButton) findViewById(R.id.glagneVacation)).setVisibility(ImageButton.GONE);
            	
            	LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
            	ll.setVisibility(LinearLayout.VISIBLE);
            	changeTimeTablePic(ll);
        	}
        }

        changeCurrTempB.setOnClickListener(new OnClickListener(){

            public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
             	
            	TextView tv = (TextView) findViewById(R.id.setTempText);
            	tv.setText("Temporary Temperature");
            	
            	//Кнопка аплая температуры
                Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
                setTemperatureAppl.setOnClickListener(new Button.OnClickListener() {
                	
                	public void onClick(View v) {
                		currTemperature = tmpTemp;
                		vacation = tmpVac;
                		settingsEditor.putFloat("currTemperature", currTemperature);
                		settingsEditor.putBoolean("vacation", vacation);
                		settingsEditor.apply();
                		setContentView(R.layout.main);
                		currentView = 0;
                		initMain();
                	}
                });
            	
            	NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
             	np1.setMaxValue(40);
             	np1.setMinValue(5);
             	np1.setValue((int)currTemperature);
             	np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						tmpTemp = newVal + tmpTemp*10 % 10*0.1f;
					}
				});
             	
             	NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
             	np2.setMaxValue(9);
             	np2.setMinValue(0);
             	np2.setValue((int)(currTemperature * 10 % 10));
             	np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
					
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int)tmpTemp + tmp;
					}
				});
             	
             	
             	CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
             	cb.setChecked(vacation);
             	cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						tmpVac = isChecked;
					}
				});
             	
             	currentView = 1;
            }
        });
        
	}

	private void setTabs() {
		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        
        spec1 = tabHost.newTabSpec("Termostat");
        spec1.setContent(R.id.thermostat);
        spec1.setIndicator("Termostat");
        
        spec2 = tabHost.newTabSpec("Day night");
        spec2.setContent(R.id.day_night_mode);
        spec2.setIndicator("Day night");
        
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
	}

	private void showTimeTableChange(final int dNumber) {
		//Экран выбора времен
		final int day = 0;
    	TextView day_view_first = (TextView) findViewById(R.id.day_view_first_edit);
    	day_view_first.setOnClickListener(new OnClickListener() {
 		   
    		public void onClick(View v) {
     		
    			setContentView(R.layout.set_time);
    			TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
    			setTime.setIs24HourView(true);
    			final Date tmpDate = new Date(0, 0, 0, setTime.getCurrentHour(), setTime.getCurrentMinute(), 0);
    			setTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
				
    				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
    					tmpDate.setHours(hourOfDay);
    					tmpDate.setMinutes(minute);
    				}
    			});
    			Button applyTime = (Button) findViewById(R.id.set_time_ok_button);
    			applyTime.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						timetable[dNumber][day][0].setHours(tmpDate.getHours());
						timetable[dNumber][day][0].setHours(tmpDate.getMinutes());
						settingsEditor.putInt("hour"+dNumber+day+"0", tmpDate.getHours());
						settingsEditor.putInt("minute"+dNumber+day+"0", tmpDate.getMinutes());
						settingsEditor.apply();
					}
				});
     		
    			currentView = 1;
    		}});
    	
    	TextView day_view_second = (TextView) findViewById(R.id.day_view_second_edit);
    	day_view_second.setOnClickListener(new OnClickListener() {
 		   
     	public void onClick(View v) {
     		
     		setContentView(R.layout.set_time);
     		TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
     		setTime.setIs24HourView(true);
     		currentView = 1;
     	}});
    	
    	TextView day_view_third = (TextView) findViewById(R.id.day_view_third_edit);
    	day_view_third.setOnClickListener(new OnClickListener() {
 		   
     	public void onClick(View v) {
     		
     		setContentView(R.layout.set_time);
     		TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
     		setTime.setIs24HourView(true);
     		currentView = 1;
     	}});
    	
    	TextView day_view_fourth = (TextView) findViewById(R.id.day_view_fourth_edit);
    	day_view_fourth.setOnClickListener(new OnClickListener() {
 		   
     	public void onClick(View v) {
     		
     		setContentView(R.layout.set_time);
     		TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
     		setTime.setIs24HourView(true);
     		currentView = 1;
     	}});
    	
    	TextView day_view_fifth = (TextView) findViewById(R.id.day_view_fifth_edit);
    	day_view_fifth.setOnClickListener(new OnClickListener() {
 		   
     	public void onClick(View v) {
     		
     		setContentView(R.layout.set_time);
     		TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
     		setTime.setIs24HourView(true);
     		currentView = 1;
     	}});
	}
	
}