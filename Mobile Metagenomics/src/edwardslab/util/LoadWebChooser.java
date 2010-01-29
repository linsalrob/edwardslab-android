package edwardslab.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoadWebChooser extends Activity{
	String numberEntered;
	Integer sampleEntered;
	//String titleEntered;
	EditText phoneNumber;
	EditText sampleNumber;
	//EditText title;
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadweb);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        sampleNumber = (EditText) findViewById(R.id.SampleNumber);
       // title = (EditText) findViewById(R.id.Title);
        final Button myNumberButton = (Button) findViewById(R.id.UseMyNumber);
        final Button confirmButton = (Button) findViewById(R.id.LoadWeb);
        numberEntered = "";
        sampleEntered = -1;
      //  titleEntered = "";
        
        
        confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tmpPhoneNumber = phoneNumber.getText().toString();
				Integer tmpSampleNumber = Integer.parseInt((String)sampleNumber.getText().toString());
				System.out.println("tmp Phone Number is: " + tmpPhoneNumber.toString());
				System.out.println("sample number is: " + tmpSampleNumber.toString());
				if(!tmpPhoneNumber.equals("")){
					numberEntered = tmpPhoneNumber;
				}
				if(tmpSampleNumber != 0){
					sampleEntered = tmpSampleNumber;
				}
				
				/*if(!title.getText().toString().equals("")){
					titleEntered = title.getText().toString();
				}*/
				
				Bundle bundle = new Bundle();            
				bundle.putString(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER, numberEntered);
				bundle.putInt(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER, sampleEntered);
				/*if(!titleEntered.equals("no title")){
					bundle.putString(MobileMetagenomics.LOAD_FILE_SAMPLE_TITLE, titleEntered);
				}*/
	            Intent mIntent = new Intent();
	            mIntent.putExtras(bundle);
	            setResult(RESULT_OK, mIntent);
	            finish();
			}
    	});
        
        myNumberButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TelephonyManager mTelephonyMgr;  
				mTelephonyMgr = (TelephonyManager)  
				getSystemService(Context.TELEPHONY_SERVICE);
				String tmpLineNumber = mTelephonyMgr.getLine1Number();
				phoneNumber.setText(tmpLineNumber);
			}
    	});
	}
}
