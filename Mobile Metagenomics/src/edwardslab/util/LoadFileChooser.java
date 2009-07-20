package edwardslab.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class LoadFileChooser extends Activity {
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadfile);
        final EditText loadFileName = (EditText) findViewById(R.id.LoadFilename);
        
        loadFileName.setOnKeyListener(new OnKeyListener() {
    	    public boolean onKey(View v, int keyCode, KeyEvent event) {
    	        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    	          // Perform action on key press
    	        	Bundle bundle = new Bundle();            
    				bundle.putString(MobileMetagenomics.LOAD_FILE_NAME, loadFileName.getText().toString());
    	            Intent mIntent = new Intent();
    	            mIntent.putExtras(bundle);
    	            setResult(RESULT_OK, mIntent);
    	            finish();
    	          return true;
    	        }
    	        return false;
    	    }
    	});
	}
}
