package edwardslab.util;

import com.github.droidfu.concurrent.BetterAsyncTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class GetAllTitles extends Activity{
	Spinner stringencySpinner;
	Spinner kmerSpinner;
	Spinner maxGapSpinner;
	EditText phoneNumber;
	Boolean showAdvanced = false;
	LinearLayout advancedOptions;
	final String KMER_FILTER_VAL = "kmer filter value";
	final String MAX_GAP_FILTER_VAL = "maxGap filter value";
	final String STRINGENCY_FILTER_VAL = "stringency filter value";
	final String PHONE_NUMBER_FILTER_VAL = "phone number filter value";
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_all_titles);
        final Button getAllTitles = (Button) findViewById(R.id.GetAllTitles);
        final Button toggleAdvanced = (Button) findViewById(R.id.ShowAdvanced);
        final Button useMyNumber = (Button) findViewById(R.id.UseMyNumber);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        advancedOptions = (LinearLayout) findViewById(R.id.ShowAdvancedDiv);
        stringencySpinner = (Spinner) findViewById(R.id.StringencySpinner2);
    	kmerSpinner = (Spinner) findViewById(R.id.KmerSpinner2);
    	maxGapSpinner = (Spinner) findViewById(R.id.MaxGapSpinner2);
    	advancedOptions.setVisibility(View.GONE);
    	
        getAllTitles.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				String tmpPhoneNumber = phoneNumber.getText().toString();
				if(!tmpPhoneNumber.equals("")){
					bundle.putString(PHONE_NUMBER_FILTER_VAL, tmpPhoneNumber);
					if(showAdvanced){
						bundle.putInt(STRINGENCY_FILTER_VAL, (stringencySpinner.getSelectedItemPosition() + 1));
						bundle.putInt(MAX_GAP_FILTER_VAL, ((maxGapSpinner.getSelectedItemPosition() + 1)*300));
						bundle.putInt(KMER_FILTER_VAL, (kmerSpinner.getSelectedItemPosition() + 7));
					}
					else{
						bundle.putInt(STRINGENCY_FILTER_VAL, -1);
						bundle.putInt(MAX_GAP_FILTER_VAL, -1);
						bundle.putInt(KMER_FILTER_VAL, -1);
					}
		            Intent mIntent = new Intent();
		            mIntent.putExtras(bundle);
		            setResult(RESULT_OK, mIntent);
		            finish();
				}
				else{
					MgUtilFunc.showToast(GetAllTitles.this, MobileMetagenomics.VALID_PHONE_STRING);
				}
				
			}
    	});
        
        toggleAdvanced.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(showAdvanced){
					showAdvanced = false;
					advancedOptions.setVisibility(View.GONE);
					toggleAdvanced.setText(R.string.show_advanced);
				}
				else{
					showAdvanced = true;
					advancedOptions.setVisibility(View.VISIBLE);
					toggleAdvanced.setText(R.string.hide_advanced);
				}
			}
    	});
        
        useMyNumber.setOnClickListener(new OnClickListener() {
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
	
	/**
	 * 
	 * @author jhoffman
	 * Downloads a sample title from the results server.
	 */
	private class LoadAllTitlesAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadAllTitlesAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status = 0;
			HashTable MgUtilFunc.JSONToHash(MgUtilFunc.doJsonAllTitlesQuery(phoneNumber.getText().toString())).get("allTitles");
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
		} 

	}
}