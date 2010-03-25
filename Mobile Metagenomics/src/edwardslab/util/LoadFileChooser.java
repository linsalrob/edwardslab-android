package edwardslab.util;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoadFileChooser extends Activity {
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	String fileName;
	EditText loadFileName;
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadfile);
        loadFileName = (EditText) findViewById(R.id.LoadFilename);
        final Button browseButton = (Button) findViewById(R.id.Browse);
        final Button confirmButton = (Button) findViewById(R.id.Load);
        fileName = "";
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
        
        browseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fileName = loadFileName.getText().toString();
				openFile();
			}
    	});
        
        confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();            
				bundle.putString(MobileMetagenomics.LOAD_FILE_NAME, loadFileName.getText().toString());
	            Intent mIntent = new Intent();
	            mIntent.putExtras(bundle);
	            setResult(RESULT_OK, mIntent);
	            finish();
			}
    	});
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
	        Bundle extras = intent.getExtras();
	        switch(requestCode) {
	        case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
				if (resultCode == RESULT_OK && intent != null) {
					// obtain the filename
					fileName = intent.getDataString();
					if (fileName != null) {
						// Get rid of URI prefix:
						if (fileName.startsWith("file://")) {
							fileName = fileName.substring(7);
						}
						
						loadFileName.setText(fileName);
					}				
					
				}
				break;
	        }
        }
    }
	
	/**
	 * @author jhoffman
	 * Opens a file from the phone's file system.
	 */
	public void openFile() {
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
	//	intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.open_title));
	//	intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.open_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
	}
}
