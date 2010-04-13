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
import android.widget.LinearLayout;
import android.widget.Spinner;

public class GetAllTitles extends Activity{
	Spinner stringencySpinner;
	Spinner kmerSpinner;
	Spinner maxGapSpinner;
	Spinner levelSpinner;
	EditText phoneNumber;
	Boolean showAdvanced = false;
	LinearLayout advancedOptions;
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_all_titles);
        final Button getAllTitles = (Button) findViewById(R.id.GetAllTitles);
        final Button toggleAdvanced = (Button) findViewById(R.id.ShowAdvanced);
        final Button useMyNumber = (Button) findViewById(R.id.UseMyNumber);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        advancedOptions = (LinearLayout) findViewById(R.id.ShowAdvancedDiv);
        stringencySpinner = (Spinner) findViewById(R.id.StringencySpinner2);
        levelSpinner = (Spinner) findViewById(R.id.LevelSpinner2);
    	kmerSpinner = (Spinner) findViewById(R.id.KmerSpinner2);
    	maxGapSpinner = (Spinner) findViewById(R.id.MaxGapSpinner2);
    	advancedOptions.setVisibility(View.GONE);
    	
        getAllTitles.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				String tmpPhoneNumber = phoneNumber.getText().toString();
				System.out.println("GetAllTitles.java returning phone number: " + tmpPhoneNumber);
				if(!tmpPhoneNumber.equals("")){
					bundle.putString(MobileMetagenomics.PHONE_NUMBER, tmpPhoneNumber);
					if(showAdvanced){
						bundle.putInt(MobileMetagenomics.STRINGENCY, (stringencySpinner.getSelectedItemPosition()));
						bundle.putInt(MobileMetagenomics.LEVEL, (levelSpinner.getSelectedItemPosition()) - 1);
						bundle.putInt(MobileMetagenomics.MAX_GAP, ((maxGapSpinner.getSelectedItemPosition())*300));
						bundle.putInt(MobileMetagenomics.KMER, (kmerSpinner.getSelectedItemPosition() + 6));
					}
					else{
						bundle.putInt(MobileMetagenomics.STRINGENCY, -1);
						bundle.putInt(MobileMetagenomics.LEVEL, -1);
						bundle.putInt(MobileMetagenomics.MAX_GAP, -1);
						bundle.putInt(MobileMetagenomics.KMER, -1);
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
}