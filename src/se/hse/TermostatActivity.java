package se.hse;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TermostatActivity extends Activity {
   
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
        
        final Button changeTempAccept = (Button) findViewById(R.id.setTempAccept);
        changeTempAccept.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                changeTemp(false);
            }
        });
        
        final ImageButton changeTmpTempButton = (ImageButton) findViewById(R.id.changeTmpTemp);
        changeTmpTempButton.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                changeTemp(true);
            }
        });

    }

	protected void changeTemp(boolean vacation) {
		//setContentView(R.layout.set_temperature);
	}
    
    
}