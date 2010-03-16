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

import com.github.droidfu.concurrent.BetterAsyncTask;

public class GetJsonOrTitle extends Activity{
	EditText phoneNumber;
	EditText sampleNumber;
	String title;
	
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_json_or_title);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        sampleNumber = (EditText) findViewById(R.id.SampleNumber);
        final Button myNumberButton = (Button) findViewById(R.id.UseMyNumber);
        final Button loadWeb = (Button) findViewById(R.id.LoadJson);
        final Button getTitle = (Button) findViewById(R.id.GetTitle);
        
        
        loadWeb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					String phoneNumberEntered = phoneNumber.getText().toString();
					String sampleNumberEntered = sampleNumber.getText().toString();
					boolean dataVerified = true;
					if(phoneNumberEntered.equals("")){
						dataVerified = false;
						MgUtilFunc.showToast(GetJsonOrTitle.this, MobileMetagenomics.VALID_PHONE_STRING);
					}
					if(sampleNumberEntered.equals("")){
						dataVerified = false;
						MgUtilFunc.showToast(GetJsonOrTitle.this, MobileMetagenomics.VALID_SAMPLE_STRING);
					}
					if(dataVerified){
						Bundle bundle = new Bundle();            
						bundle.putString(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER, phoneNumberEntered);
						bundle.putString(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER, sampleNumberEntered);
			            Intent mIntent = new Intent();
			            mIntent.putExtras(bundle);
			            setResult(RESULT_OK, mIntent);
			            finish();
					}
					
				}
				catch(Exception E){
					MgUtilFunc.showToast(GetJsonOrTitle.this, "hit the exception block! exception is: " + E.toString());
				}
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
        
        getTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try{
					String phoneNumberEntered = phoneNumber.getText().toString();
					String sampleNumberEntered = sampleNumber.getText().toString();
					boolean dataVerified = true;
					if(phoneNumberEntered.equals("")){
						dataVerified = false;
						MgUtilFunc.showToast(GetJsonOrTitle.this, MobileMetagenomics.VALID_PHONE_STRING);
					}
					if(sampleNumberEntered.equals("")){
						dataVerified = false;
						MgUtilFunc.showToast(GetJsonOrTitle.this, MobileMetagenomics.VALID_SAMPLE_STRING);
					}
					if(dataVerified){
						LoadTitleAsyncTask task = new LoadTitleAsyncTask(GetJsonOrTitle.this);
						task.execute();
					}
					
				}
				catch(Exception E){
					MgUtilFunc.showToast(GetJsonOrTitle.this, "hit the exception block! exception is: " + E.toString());

				}
			}
    	});
	}
	
	/**
	 * 
	 * @author jhoffman
	 * Downloads a sample title from the results server.
	 */
	private class LoadTitleAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadTitleAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status = 0;
			title = MgUtilFunc.JSONToHash(MgUtilFunc.doJsonTitleQuery(phoneNumber.getText().toString(), sampleNumber.getText().toString())).get("title");
			status = 1;
			publishProgress(status);
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
			//Handle the "Function" operation mode
			MgUtilFunc.showToast(GetJsonOrTitle.this, "Sample #" + sampleNumber.getText().toString() + "'s title is: " + title);
		} 

	}
}