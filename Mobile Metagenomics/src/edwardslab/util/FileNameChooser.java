package edwardslab.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class FileNameChooser extends Activity{
	String fileName;
	EditText chooseFileName;
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filename);
        chooseFileName = (EditText) findViewById(R.id.ChooseFilename);
        final Button confirmButton = (Button) findViewById(R.id.Confirm);
        fileName = "";
        chooseFileName.setOnKeyListener(new OnKeyListener() {
    	    public boolean onKey(View v, int keyCode, KeyEvent event) {
    	        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    	          // Perform action on key press
    	        	Bundle bundle = new Bundle();            
    				bundle.putString(MobileMetagenomics.CHOOSE_FILE_NAME, chooseFileName.getText().toString());
    	            Intent mIntent = new Intent();
    	            mIntent.putExtras(bundle);
    	            setResult(RESULT_OK, mIntent);
    	            finish();
    	          return true;
    	        }
    	        return false;
    	    }
    	});
        
        confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();            
				bundle.putString(MobileMetagenomics.CHOOSE_FILE_NAME, chooseFileName.getText().toString());
	            Intent mIntent = new Intent();
	            mIntent.putExtras(bundle);
	            setResult(RESULT_OK, mIntent);
	            finish();
			}
    	});
	}
}