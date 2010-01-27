package edwardslab.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoadWebChooser extends Activity{
	int numberEntered;
	int sampleEntered;
	String titleEntered;
	EditText phoneNumber;
	EditText sampleNumber;
	EditText title;
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadfile);
        phoneNumber = (EditText) findViewById(R.id.LoadFilename);
        final Button myNumberButton = (Button) findViewById(R.id.UseMyNumber);
        final Button confirmButton = (Button) findViewById(R.id.LoadWeb);
        numberEntered = 5555555;
        sampleEntered = -1;
        titleEntered = "no title";
        
        /*
        phoneNumber.setOnKeyListener(new OnKeyListener() {
    	    public boolean onKey(View v, int keyCode, KeyEvent event) {
    	        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    	          // Perform action on key press
    	        	Bundle bundle = new Bundle();            
    				bundle.putInt(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER, Integer.parseInt(phoneNumber.getText().toString()));
    	            Intent mIntent = new Intent();
    	            mIntent.putExtras(bundle);
    	            setResult(RESULT_OK, mIntent);
    	            finish();
    	          return true;
    	        }
    	        return false;
    	    }
    	});*/
        
        
        confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Integer.parseInt(phoneNumber.getText().toString()) != 0){
					numberEntered = Integer.parseInt(phoneNumber.getText().toString());
				}
				if(Integer.parseInt(sampleNumber.getText().toString()) != 0){
					sampleEntered = Integer.parseInt(sampleNumber.getText().toString());
				}
				if(title.getText().toString() != null){
					titleEntered = title.getText().toString();
				}
				
				Bundle bundle = new Bundle();            
				bundle.putInt(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER, numberEntered);
				bundle.putInt(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER, sampleEntered);
				if(!titleEntered.equals("no title")){
					bundle.putString(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER, titleEntered);
				}
	            Intent mIntent = new Intent();
	            mIntent.putExtras(bundle);
	            setResult(RESULT_OK, mIntent);
	            finish();
			}
    	});
	}
}
