package se.hse;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
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
        
        final ImageButton changeCurrTempB = (ImageButton) findViewById(R.id.changeCurrTempClick);
        currTempListener = new OnClickListener(){

            public void onClick(View v) {
            	setContentView(R.layout.set_temperature);
            }
        };
        changeCurrTempB.setOnClickListener(currTempListener);
    }

	protected void setTemp(boolean b) {
		setContentView(R.layout.set_temperature);
	}
}