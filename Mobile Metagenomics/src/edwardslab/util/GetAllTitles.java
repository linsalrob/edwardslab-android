package edwardslab.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
        setContentView(R.layout.filename);
        final Button getAllTitles = (Button) findViewById(R.id.GetAllTitles);
        final Button toggleAdvanced = (Button) findViewById(R.id.ShowAdvanced);
        phoneNumber = (EditText) findViewById(R.id.PhoneNumber);
        advancedOptions = (LinearLayout) findViewById(R.id.ShowAdvancedDiv);
        stringencySpinner = (Spinner) findViewById(R.id.StringencySpinner2);
    	kmerSpinner = (Spinner) findViewById(R.id.KmerSpinner2);
    	maxGapSpinner = (Spinner) findViewById(R.id.MaxGapSpinner2);
        
        getAllTitles.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString(PHONE_NUMBER_FILTER_VAL, phoneNumber.getText().toString());
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
	}
}