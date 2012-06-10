package se.hse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.Comparator;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import android.widget.ToggleButton;

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
	boolean temporaryMode;
	int currDayNightLastTab = 0;
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
	List<Task> nextThreeSwichers;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		weekString[Calendar.MONDAY - 1] = "Monday";
		weekString[Calendar.TUESDAY - 1] = "Tuesday";
		weekString[Calendar.WEDNESDAY - 1] = "Wednesday";
		weekString[Calendar.THURSDAY - 1] = "Thursday";
		weekString[Calendar.FRIDAY - 1] = "Friday";
		weekString[Calendar.SATURDAY - 1] = "Saturday";
		weekString[Calendar.SUNDAY - 1] = "Sunday";

		settings = getPreferences(0);
		settingsEditor = settings.edit();
		night = settings.getBoolean("dayNightMode", false);
		vacation = settings.getBoolean("vacation", false);
		currTemperature = settings.getFloat("currTemperature", 20.0f);
		dayTemperature = settings.getFloat("dayTemperature", 18.0f);
		nightTemperature = settings.getFloat("nightTemperature", 23.0f);
		tmpTemp = currTemperature;
		tmpVac = vacation;
		for (int d = 0; d < NUMBER_OF_DAYS; d++) {
			for (int m = 0; m < NUMBER_OF_MODS; m++) {
				for (int t = 0; t < NUMBER_OF_TIMES; t++) {
					int hour, min, sec;
					hour = settings.getInt("hour" + d + m + t,
							(m == 0 ? 9 : 10));
					min = settings.getInt("min" + +d + m + t, t);
					sec = settings.getInt("sec" + +d + m + t, 0);
					timeAble[d][m][t] = settings.getBoolean("timeAble" + +d + m
							+ t, false);
					timetable[d][m][t] = new Date(0, 0, 0, hour, min, sec);
				}
			}
		}
		nextThreeSwichers = getListOfNextSwichers();
		checkCurrenMode();

		initMain(0);
		currentView = 0;
		mHandler = new Handler();
		mHandler.removeCallbacks(timerTask);
		mHandler.postDelayed(timerTask, 100);
	}

	int yy;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (currentView != 0) {
				setContentView(R.layout.main);
				// if (currentView == 3) {
				// initMain(currDayNightLastTab);
				// currentView = 0;
				// return true;
				// }
				if (currentView == 4) {
					initMain(currDayNightLastTab);
					currentView = 3;
					return true;
				}
				if (currentView != 2) {
					currentView = 0;
				}

				tmpVac = vacation;
				tmpTemp = currTemperature;
				initMain(0);
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}
		}
		return false;
	}

	private Handler mHandler;

	private Runnable timerTask = new Runnable() {
		public void run() {
			updateUI();
			mHandler.postDelayed(this, 1 * 1000);
		}
	};

	private int getCurrentDay() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DAY_OF_WEEK) - 1;
	}

	protected List<Task> getListOfNextSwichers() {
		Calendar c = Calendar.getInstance();
		final int Day = getCurrentDay(); // получаем день

		Comparator<Task> comparator = new TaskComparator(); // сортировщик дат
		PriorityQueue<Task> queue = new PriorityQueue<Task>(10, comparator);
		for (int m = 0; m < NUMBER_OF_MODS; m++) {
			for (int t = 0; t < NUMBER_OF_TIMES; t++) {
				if (timeAble[Day][m][t])
					queue.add(new Task((m == 0), timetable[Day][m][t]));
			}
		}

		List<Task> list = new ArrayList<Task>();
		Date now = new Date(0, 0, 0, c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE));
		while (queue.size() != 0 && list.size() != 3) {
			Task t = queue.remove();
			if (now.before(t.d))
				list.add((Task) t.copy());
		}

		for (int i = list.size(); i < 3; i++) {
			list.add(null);
		}

		return list;
	}

	protected void checkCurrenMode() {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		Date now = new Date(0, 0, 0, hour, min);
		Task first = nextThreeSwichers.get(0);
		if (first != null && !now.before(first.d) && !vacation) {
			night = !first.day; // ибо аки мудак думу над моделью программы
								// думал
			currTemperature = first.day ? dayTemperature : nightTemperature;
			nextThreeSwichers = getListOfNextSwichers();
		}
		if (hour == 0 && min == 0 && !vacation) {
			night = true;
			settingsEditor.putBoolean("night", true);
			currTemperature = nightTemperature;
			nextThreeSwichers = getListOfNextSwichers();
		}
		settingsEditor.putFloat("currTemperature", currTemperature);
		settingsEditor.putBoolean("night", night);
		settingsEditor.apply();
		// updateUI();
		/*
		 * Calendar c = Calendar.getInstance(); int day = getCurrentDay(); int
		 * hour = c.get(Calendar.HOUR_OF_DAY); int min = c.get(Calendar.MINUTE);
		 * int sec = c.get(Calendar.SECOND); int[] LastHour = new int[2]; int[]
		 * LastMin = new int[2]; int[] LastSec = new int[2]; for (int m = 0; m <
		 * NUMBER_OF_MODS; m++) { for (int t = 0; t < NUMBER_OF_TIMES; t++) { if
		 * (timeAble[day][m][t]) { if (timetable[day][m][t].before(new Date(0,
		 * 0, 0, hour, min, sec))) { LastHour[m] =
		 * timetable[day][m][t].getHours(); LastMin[m] =
		 * timetable[day][m][t].getMinutes(); LastSec[m] =
		 * timetable[day][m][t].getSeconds(); } } else { continue; } } } // 0 -
		 * day, 1 - night if ((new Date(0, 0, 0, LastHour[0], LastMin[0],
		 * LastSec[0])) .after(new Date(0, 0, 0, LastHour[1], LastMin[1],
		 * LastSec[1]))) { night = false; currTemperature = dayTemperature;
		 * settingsEditor.putFloat("currTemperature", currTemperature);
		 * settingsEditor.putBoolean("night", false); settingsEditor.apply(); //
		 * updateUI(); } else { night = true; settingsEditor.putBoolean("night",
		 * true); currTemperature = nightTemperature;
		 * settingsEditor.putFloat("currTemperature", currTemperature);
		 * settingsEditor.apply(); // updateUI(); } if (hour == 0 && min == 0) {
		 * night = true; settingsEditor.putBoolean("night", true);
		 * currTemperature = nightTemperature;
		 * settingsEditor.putFloat("currTemperature", currTemperature);
		 * settingsEditor.apply(); } <<<<<<< HEAD //currTemperature = yy; yy++;
		 */
		// currTemperature = yy;
	}

	private void showOnePicOnTimeTable(LinearLayout layout, int imB1, int imB2,
			int textView, Task t) {
		if (t == null) {
			layout.setVisibility(LinearLayout.GONE);
		} else {
			ImageButton ib1m = (ImageButton) findViewById(imB1);
			ImageButton ib1s = (ImageButton) findViewById(imB2);
			TextView tw1 = (TextView) findViewById(textView);
			if (t.day) {
				ib1s.setVisibility(ImageButton.VISIBLE);
				ib1m.setVisibility(ImageButton.GONE);
			} else {
				ib1s.setVisibility(ImageButton.GONE);
				ib1m.setVisibility(ImageButton.VISIBLE);
			}
			tw1.setText(showFormatter(t.d.getHours(), t.d.getMinutes()));
		}
	}

	private void changeTimeTablePic(LinearLayout ll) {
		nextThreeSwichers = getListOfNextSwichers();

		Log.d("size of nextThreeSwichers", nextThreeSwichers.size() + "");
		LinearLayout ll2 = (LinearLayout) findViewById(R.id.secondTT);
		LinearLayout ll3 = (LinearLayout) findViewById(R.id.thirdTT);

		showOnePicOnTimeTable(ll, R.id.main_view_first_image_moon,
				R.id.main_view_first_image_sun, R.id.main_view_first_time,
				nextThreeSwichers.get(0));
		showOnePicOnTimeTable(ll2, R.id.main_view_second_image_moon,
				R.id.main_view_second_image_sun, R.id.main_view_second_time,
				nextThreeSwichers.get(1));
		showOnePicOnTimeTable(ll3, R.id.main_view_third_image_moon,
				R.id.main_view_third_image_sun, R.id.main_view_third_time,
				nextThreeSwichers.get(2));
	}

	private void initMain(final int mode) {
		// Задаем табы
		setTabs(mode);

		// Большая главная картинка
		setBigPic();

		// Кнопки дней
		setDaysAction();

		// Вывод температуры на глагне
		setGlangTemperature();

		// Кнопка изменения ночной температуры
		nightTemperatureChange();

		// Кнопка изменения дневной температуры
		dayTemperatureChange();

		// 24 часа кнопка
		int day = getCurrentDay();
		showTimeTableChange(day, mode, false);
		initLabels(day, mode);
	}

	private void dayTemperatureChange() {
		TextView modes_day_edit = (TextView) findViewById(R.id.day_night_mode_day_edit);

		modes_day_edit.setText(showTemp(dayTemperature) + "°C");

		modes_day_edit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setContentView(R.layout.set_temperature);

				TextView tv = (TextView) findViewById(R.id.setTempText);
				tv.setText("Day Temperature");

				// Кнопка аплая температуры
				Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
				setTemperatureAppl
						.setOnClickListener(new Button.OnClickListener() {

							public void onClick(View v) {
								dayTemperature = tmpTemp;
								vacation = tmpVac;
								settingsEditor.putFloat("dayTemperature",
										dayTemperature);
								settingsEditor.putBoolean("vacation", vacation);
								settingsEditor.apply();
								setContentView(R.layout.main);
								currentView = 1;
								initMain(0);
							}
						});

				final NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
				final NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
				np1.setMaxValue(30);
				np1.setMinValue(5);
				float dt = dayTemperature;
				np1.setValue((int) dt);

				np2.setMaxValue(9);
				np2.setMinValue(0);
				int dt2 = (int) (dt * 10) % 10;
				np2.setValue(dt2);

				np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						tmpTemp = newVal + tmpTemp * 10 % 10 * 0.1f;
						if (newVal == 30) {
							np2.setEnabled(false);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						} else {
							np2.setEnabled(true);
							np2.setMaxValue(9);
							np2.setMinValue(0);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						}
					}

				});

				if (np1.getValue() == 30) {
					np2.setEnabled(false);
					float tmp = 0.0f;
					tmpTemp = (int) tmpTemp + tmp;
				}

				np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int) tmpTemp + tmp;
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

		modes_night_edit.setText(showTemp(nightTemperature) + "°C");

		modes_night_edit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setContentView(R.layout.set_temperature);

				TextView tv = (TextView) findViewById(R.id.setTempText);
				tv.setText("Night Temperature");

				// Кнопка аплая температуры
				Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
				setTemperatureAppl
						.setOnClickListener(new Button.OnClickListener() {

							public void onClick(View v) {
								nightTemperature = tmpTemp;
								vacation = tmpVac;
								settingsEditor.putFloat("nightTemperature",
										nightTemperature);
								settingsEditor.putBoolean("vacation", vacation);
								settingsEditor.apply();
								setContentView(R.layout.main);
								currentView = 1;
								initMain(0);
							}

						});

				final NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
				final NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
				np1.setMaxValue(30);
				np1.setMinValue(5);

				float nt = nightTemperature;
				int nt2 = (int) (nt * 10) % 10;

				np1.setValue((int) nt);

				np2.setMaxValue(9);
				np2.setMinValue(0);
				np2.setValue(nt2);

				np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						tmpTemp = newVal + tmpTemp * 10 % 10 * 0.1f;
						if (newVal == 30) {
							np2.setEnabled(false);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						} else {
							np2.setEnabled(true);
							np2.setMaxValue(9);
							np2.setMinValue(0);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						}
					}

				});

				if (np1.getValue() == 30) {
					np2.setEnabled(false);
					float tmp = 0.0f;
					tmpTemp = (int) tmpTemp + tmp;
				}

				np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int) tmpTemp + tmp;
					}

				});

				CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
				cb.setVisibility(View.INVISIBLE);

				currentView = 1;
			}
		});
	}

	private void setDaysAction() {

		List<TextView> days = new ArrayList<TextView>();
		days.add((TextView) findViewById(R.id.weekViewButton1));
		days.add((TextView) findViewById(R.id.weekViewButton2));
		days.add((TextView) findViewById(R.id.weekViewButton3));
		days.add((TextView) findViewById(R.id.weekViewButton4));
		days.add((TextView) findViewById(R.id.weekViewButton5));
		days.add((TextView) findViewById(R.id.weekViewButton6));
		days.add((TextView) findViewById(R.id.weekViewButton7));

		for (int i = 0; i < days.size(); i++) {
			days.get(i).setText(weekString[i]);
			days.get(i).setOnClickListener(
					new DayButtonListener(i, weekString[i]));
		}

	}

	private void setGlangTemperature() {
		TextView mainTemp = (TextView) findViewById(R.id.main_view_temperature);
		mainTemp.setText("" + showTemp(currTemperature) + "°C");
	}

	private void updateUI() {
		Log.d("updateUI", "in updateUI()");

		checkCurrenMode();

		if (currentView == 0) {

			setContentView(R.layout.main);
			currentView = 0;
			initMain(currDayNightLastTab);
		}
	}

	private void setBigPic() {
		ImageButton changeCurrTempB;

		if (vacation) {
			changeCurrTempB = (ImageButton) findViewById(R.id.glagneVacation);

			changeCurrTempB.setVisibility(ImageButton.VISIBLE);
			((ImageButton) findViewById(R.id.glagneMoon))
					.setVisibility(ImageButton.GONE);
			((ImageButton) findViewById(R.id.glagneSun))
					.setVisibility(ImageButton.GONE);

			LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
			ll.setVisibility(LinearLayout.INVISIBLE);
		} else {
			if (night) {
				changeCurrTempB = (ImageButton) findViewById(R.id.glagneMoon);

				changeCurrTempB.setVisibility(ImageButton.VISIBLE);
				((ImageButton) findViewById(R.id.glagneVacation))
						.setVisibility(ImageButton.GONE);
				((ImageButton) findViewById(R.id.glagneSun))
						.setVisibility(ImageButton.GONE);

				LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
				ll.setVisibility(LinearLayout.VISIBLE);
				changeTimeTablePic(ll);
			} else {
				changeCurrTempB = (ImageButton) findViewById(R.id.glagneSun);

				changeCurrTempB.setVisibility(ImageButton.VISIBLE);
				((ImageButton) findViewById(R.id.glagneMoon))
						.setVisibility(ImageButton.GONE);
				((ImageButton) findViewById(R.id.glagneVacation))
						.setVisibility(ImageButton.GONE);

				LinearLayout ll = (LinearLayout) findViewById(R.id.timeTableLayOut);
				ll.setVisibility(LinearLayout.VISIBLE);
				changeTimeTablePic(ll);
			}
		}

		changeCurrTempB.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setContentView(R.layout.set_temperature);
				TextView tv = (TextView) findViewById(R.id.setTempText);
				tv.setText("Temporary Temperature");

				// Кнопка аплая температуры
				Button setTemperatureAppl = (Button) findViewById(R.id.set_temperature_apply_button);
				setTemperatureAppl
						.setOnClickListener(new Button.OnClickListener() {

							public void onClick(View v) {
								if (tmpTemp != currTemperature) {
									temporaryMode = (true != tmpVac);
								} else {
									temporaryMode = false;
								}
								currTemperature = tmpTemp;
								vacation = tmpVac;
								settingsEditor.putFloat("currTemperature",
										currTemperature);
								settingsEditor.putBoolean("vacation", vacation);
								settingsEditor.apply();

								setContentView(R.layout.main);
								currentView = 0;
								initMain(0);
							}
						});

				final NumberPicker np1 = (NumberPicker) findViewById(R.id.temperature_big_setter);
				final NumberPicker np2 = (NumberPicker) findViewById(R.id.temperature_small_setter);
				np1.setMaxValue(30);
				np1.setMinValue(5);
				np2.setMaxValue(9);
				np2.setMinValue(0);

				float tt = currTemperature;
				int tt1 = (int) (tt * 10) % 10;

				np1.setValue((int) tt);
				np2.setValue(tt1);

				np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						tmpTemp = newVal + tmpTemp * 10 % 10 * 0.1f;
						if (newVal == 30) {
							np2.setEnabled(false);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						} else {
							np2.setEnabled(true);
							np2.setMaxValue(9);
							np2.setMinValue(0);
							float tmp = 0.0f;
							tmpTemp = (int) tmpTemp + tmp;
						}
					}

				});

				
				
				if (np1.getValue() == 30) {
					np2.setEnabled(false);
					float tmp = 0.0f;
					tmpTemp = (int) tmpTemp + tmp;
				}

				np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal) {
						float tmp = newVal * 0.1f;
						tmpTemp = (int) tmpTemp + tmp;
					}

				});

				CheckBox cb = (CheckBox) findViewById(R.id.set_temperature_vacation_button);
				cb.setChecked(vacation);
				cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						tmpVac = isChecked;
					}
				});

				currentView = 1;
			}
		});

	}

	private String showFormatter(int h, int m) {
		String hour = (h < 10 ? "0" : "") + h;
		String minute = (m < 10 ? "0" : "") + m;

		return (hour + ":" + minute);
	}

	private String showTemp(float t) {
		return Float.toString((float) ((int) (t * 10) / 10f));
	}

	private void initLabels(int day_number, int mode) {
		ToggleButton[] buttons = new ToggleButton[NUMBER_OF_TIMES];
		TextView list[] = new TextView[NUMBER_OF_TIMES];
		list[0] = (TextView) findViewById(R.id.day_view_first_edit);
		list[1] = (TextView) findViewById(R.id.day_view_second_edit);
		list[2] = (TextView) findViewById(R.id.day_view_third_edit);
		list[3] = (TextView) findViewById(R.id.day_view_fourth_edit);
		list[4] = (TextView) findViewById(R.id.day_view_fifth_edit);

		buttons[0] = (ToggleButton) findViewById(R.id.day_view_first_button);
		buttons[1] = (ToggleButton) findViewById(R.id.day_view_second_button);
		buttons[2] = (ToggleButton) findViewById(R.id.day_view_third_button);
		buttons[3] = (ToggleButton) findViewById(R.id.day_view_fourth_button);
		buttons[4] = (ToggleButton) findViewById(R.id.day_view_fifth_button);

		for (int i = 0; i < list.length; i++) {
			Date date = timetable[day_number][mode][i];
			boolean b = timeAble[day_number][mode][i];

			int h = date.getHours();
			int m = date.getMinutes();

			list[i].setText(showFormatter(h, m));
			buttons[i].setChecked(b);
		}
	}

	private void setTabs(final int mode) {
		/* TabHost */tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();

		spec1 = tabHost.newTabSpec("Main");
		spec1.setContent(R.id.thermostat);
		spec1.setIndicator("Main");

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

		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			public void onTabChanged(String tabId) {
				if (tabId == "24h") {
					currentView = 3;
					int day = getCurrentDay();
					showTimeTableChange(day, mode, false);
					initLabels(day, mode);
				}
				if (tabId == "Main") {
					currentView = 0;
					updateUI();
				}
				if (tabId == "Day night") {
					currentView = 3;
				}
				if (tabId == "7 days") {
					currentView = 3;
				}
			}
		});

		if (currentView == 2) {
			tabHost.setCurrentTab(2);
			currentView = 0;
		} else {
			if (currentView == 3) {
				tabHost.setCurrentTab(3);
				currentView = 3;
			} else {
				if (currentView == 1) {
					tabHost.setCurrentTab(1);
					currentView = 1;
				} else {
					if (currentView == 4) {
						tabHost.setCurrentTab(3);
						currentView = 4;
					}
				}
			}
		}
		setContentView(tabHost);
	}

	private void showTimeTableChange(final int dNumber, final int day,
			final boolean flag) {

		boolSwitchersActivated(dNumber, day, flag);

		currDayNightLastTab = day;

		TextView tv = (TextView) findViewById(R.id.dayName);
		tv.setText(weekString[dNumber]);

		ImageButton dayToNightSwitch = (ImageButton) findViewById(R.id.day_view_img_sun);
		ImageButton nightToDaySwitch = (ImageButton) findViewById(R.id.day_view_img_moon);

		int dayVisibility = day == 0 ? ImageButton.VISIBLE : ImageButton.GONE;
		int nightVisibility = day == 0 ? ImageButton.GONE : ImageButton.VISIBLE;

		dayToNightSwitch.setVisibility(dayVisibility);
		nightToDaySwitch.setVisibility(nightVisibility);

		dayToNightSwitch.setOnClickListener(new DaySwicher(flag,
				R.layout.day_view, R.layout.main, R.id.dayName, dNumber, 1));
		nightToDaySwitch.setOnClickListener(new DaySwicher(flag,
				R.layout.day_view, R.layout.main, R.id.dayName, dNumber, 0));

		TextView day_view_first = (TextView) findViewById(R.id.day_view_first_edit);
		day_view_first.setOnClickListener(new DayViewListener(dNumber, 0, flag,
				day));

		TextView day_view_second = (TextView) findViewById(R.id.day_view_second_edit);
		day_view_second.setOnClickListener(new DayViewListener(dNumber, 1,
				flag, day));

		TextView day_view_third = (TextView) findViewById(R.id.day_view_third_edit);
		day_view_third.setOnClickListener(new DayViewListener(dNumber, 2, flag,
				day));

		TextView day_view_fourth = (TextView) findViewById(R.id.day_view_fourth_edit);
		day_view_fourth.setOnClickListener(new DayViewListener(dNumber, 3,
				flag, day));

		TextView day_view_fifth = (TextView) findViewById(R.id.day_view_fifth_edit);
		day_view_fifth.setOnClickListener(new DayViewListener(dNumber, 4, flag,
				day));
	}

	// просто выводит диалоговое окно
	void showAlert() {

		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setTitle("Date errors");
		dlgAlert.setMessage("Some dates are equal");
		dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		dlgAlert.create().show();
	}

	// проверяет весь день(2 режима) на совпадение
	boolean testDateForComparing(int day, int mode, int editNumber) {

		if (true)
			return false;

		Date d = timetable[day][mode][editNumber]; // получаем время

		for (int m = 0; m < 2; m++) {
			for (int i = 0; i < NUMBER_OF_TIMES; i++) { // сравниваем всё время,
														// за исключением его
														// самого
				if (!(editNumber == i && mode == m)
						&& (d.compareTo(timetable[day][mode][i]) == 0)) {
					return true; // нашли совпадение
				}

			}
		}

		return false;
	}

	private void boolSwitchersActivated(final int dNumber, final int day,
			boolean flag) {

		ToggleButton tb1 = (ToggleButton) findViewById(R.id.day_view_first_button);
		tb1.setChecked(timeAble[dNumber][day][0]);

		tb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				timeAble[dNumber][day][0] = isChecked;
				settingsEditor.putBoolean("timeAble" + dNumber + day + "0",
						isChecked);
				settingsEditor.apply();
			}
		});

		ToggleButton tb2 = (ToggleButton) findViewById(R.id.day_view_second_button);
		tb2.setChecked(timeAble[dNumber][day][1]);

		tb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				timeAble[dNumber][day][1] = isChecked;
				settingsEditor.putBoolean("timeAble" + dNumber + day + "1",
						isChecked);
				settingsEditor.apply();
			}
		});

		ToggleButton tb3 = (ToggleButton) findViewById(R.id.day_view_third_button);
		tb3.setChecked(timeAble[dNumber][day][2]);

		tb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				timeAble[dNumber][day][2] = isChecked;
				settingsEditor.putBoolean("timeAble" + dNumber + day + "2",
						isChecked);
				settingsEditor.apply();
			}
		});

		ToggleButton tb4 = (ToggleButton) findViewById(R.id.day_view_fourth_button);
		tb4.setChecked(timeAble[dNumber][day][3]);

		tb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				timeAble[dNumber][day][3] = isChecked;
				settingsEditor.putBoolean("timeAble" + dNumber + day + "3",
						isChecked);
				settingsEditor.apply();
			}
		});

		ToggleButton tb5 = (ToggleButton) findViewById(R.id.day_view_fifth_button);
		tb5.setChecked(timeAble[dNumber][day][4]);

		tb5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				timeAble[dNumber][day][4] = isChecked;
				settingsEditor.putBoolean("timeAble" + dNumber + day + "4",
						isChecked);
				settingsEditor.apply();
			}
		});

	}

	class DaySwicher implements View.OnClickListener {
		private boolean flag;
		private int layoutId;
		private int mainId;
		private int textId;
		private int dNumber;
		private int initMainWith;

		DaySwicher(boolean f, int lId, int mId, int tId, int dNum, int init) {
			flag = f;
			layoutId = lId;
			mainId = mId;
			textId = tId;
			dNumber = dNum;
			initMainWith = init;
		}

		public void onClick(View v) {

			currentView = 3;
			if (flag) {
				setContentView(layoutId);
				initLabels(dNumber, initMainWith);
				TextView tv = (TextView) findViewById(textId);
				tv.setText(weekString[dNumber]);
				showTimeTableChange(dNumber, initMainWith, flag);
			} else {
				setContentView(mainId);
				initMain(initMainWith);
			}
		}
	}

	class DayViewListener implements View.OnClickListener {
		private int dNumber;
		private int numberOfTheDay;
		private boolean flag;
		private int day;

		public DayViewListener(int dNumber, int numberOfTheDay, boolean flag,
				int day) {
			this.dNumber = dNumber;
			this.numberOfTheDay = numberOfTheDay;
			this.flag = flag;
			this.day = day;
		}

		public void onClick(View v) {
			currentView = 4;
			setContentView(R.layout.set_time);
			TimePicker setTime = (TimePicker) findViewById(R.id.set_time_time_setter);
			setTime.setIs24HourView(true);
			setTime.setCurrentHour(timetable[dNumber][day][numberOfTheDay]
					.getHours());
			setTime.setCurrentMinute(timetable[dNumber][day][numberOfTheDay]
					.getMinutes());
			final Date tmpDate = new Date(0, 0, 0, setTime.getCurrentHour(),
					setTime.getCurrentMinute(), 0);
			setTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

				public void onTimeChanged(TimePicker view, int hourOfDay,
						int minute) {
					tmpDate.setHours(hourOfDay);
					tmpDate.setMinutes(minute);
				}
			});

			Button applyTime = (Button) findViewById(R.id.set_time_ok_button);
			applyTime.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Date d = (Date) timetable[dNumber][day][numberOfTheDay]
							.clone();
					timetable[dNumber][day][numberOfTheDay].setHours(tmpDate
							.getHours());
					timetable[dNumber][day][numberOfTheDay].setMinutes(tmpDate
							.getMinutes());

					if (testDateForComparing(dNumber, day, numberOfTheDay)) {
						showAlert();
						timetable[dNumber][day][numberOfTheDay] = d;
						setContentView(R.layout.main);
						initMain(day);
						return;
					}

					currentView = 3;
					settingsEditor.putInt("hour" + dNumber + day
							+ numberOfTheDay, tmpDate.getHours());
					settingsEditor.putInt("minute" + dNumber + day
							+ numberOfTheDay, tmpDate.getMinutes());
					settingsEditor.apply();
					if (flag) {
						setContentView(R.layout.day_view);
						initLabels(dNumber, day);
						TextView tv = (TextView) findViewById(R.id.dayName);
						tv.setText(weekString[dNumber]);
						showTimeTableChange(dNumber, day, flag);

					} else {
						setContentView(R.layout.main);
						initMain(day);
					}

				}
			});
		}
	}

	class DayButtonListener implements View.OnClickListener {
		private int day;
		private String dayName;

		public DayButtonListener(int day, String dayName) {
			this.day = day;
			this.dayName = dayName;
		}

		public void onClick(View v) {
			setContentView(R.layout.day_view);
			initLabels(day, 0);
			TextView tv = (TextView) findViewById(R.id.dayName);
			tv.setText(dayName);
			showTimeTableChange(day, 0, true);
			currentView = 2;
		}
	}
}

class Task {
	public Task(boolean day, Date d) {
		this.day = day;
		this.d = (Date) d.clone();
	}

	Task copy() {
		return new Task(day, d);
	}

	public boolean day;
	public Date d;
}

class TaskComparator implements Comparator<Task> {
	public int compare(Task x, Task y) {
		return x.d.compareTo(y.d);
	}
}
