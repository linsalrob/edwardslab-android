package edwardslab.util;

import org.openintents.intents.FileManagerIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MobileMetagenomics extends Activity{
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	static final int ACTIVITY_CHOOSE_FILE = 0;
	private static final int LOAD_ID = Menu.FIRST;
	static final String LOAD_FILE_NAME = "load file name";
	static final String FILE_NAME = "filename";
	static final String LEVEL = "level";
	static final String STRINGENCY = "stringency";
	EditText fileName;
	Spinner stringencySpinner;
	Spinner levelSpinner;
	Object[] keyArr;
	Object[] valArr;
	ListView resultListView;
    //private ProgressDialog pd;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Initialize UI from xml
        fileName = (EditText) findViewById(R.id.Filename);
        stringencySpinner = (Spinner) findViewById(R.id.StringencySpinner);
        levelSpinner = (Spinner) findViewById(R.id.LevelSpinner);
    	final InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        final Button uploadButton = (Button)findViewById(R.id.Upload);
        final Button resetButton = (Button)findViewById(R.id.Reset);
        final Button browseButton = (Button)findViewById(R.id.Browse);
      //Connect Listeners to UI
        uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) { 
        		Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
        		i.putExtra(FILE_NAME, fileName.getText().toString());
        		i.putExtra(LEVEL, levelSpinner.getSelectedItemPosition());
        		i.putExtra(STRINGENCY, (stringencySpinner.getSelectedItemPosition() + 1));
        		startActivity(i);
        	}
        });       
        resetButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {  
        		fileName.setText("");
        		stringencySpinner.setSelection(0);
        		levelSpinner.setSelection(0);
        	}
        });
        browseButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) { 
        		openFile();
        	}
        });       
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, LOAD_ID, 0, R.string.load).setIcon(android.R.drawable.ic_menu_set_as);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case LOAD_ID:
        	Intent i = new Intent(MobileMetagenomics.this, LoadFileChooser.class);
        	startActivityForResult(i, ACTIVITY_CHOOSE_FILE);
        	return true;
        }
        return super.onMenuItemSelected(featureId, item);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
        switch(requestCode) {
        case ACTIVITY_CHOOSE_FILE:
    		loadResults(extras.getString(LOAD_FILE_NAME));
        	break;
        case REQUEST_CODE_PICK_FILE_OR_DIRECTORY:
			if (resultCode == RESULT_OK && intent != null) {
				// obtain the filename
				String filename = intent.getDataString();
				if (filename != null) {
					// Get rid of URI prefix:
					if (filename.startsWith("file://")) {
						filename = filename.substring(7);
					}
					
					fileName.setText(filename);
				}				
				
			}
			break;
        }
    }
    
	public void loadResults(String resFile){
		            Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
	        		i.putExtra(LOAD_FILE_NAME, resFile);
	        		startActivity(i);
	}

	
    public void openFile() {
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		
		// Construct URI from file name.
		intent.setData(Uri.parse("file://" + fileName));
		
		// Set fancy title and button (optional)
//		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.open_title));
//		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.open_button));
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
		} catch (ActivityNotFoundException e) {
			// No compatible file manager was found.
			Toast.makeText(this, R.string.no_filemanager_installed, 
					Toast.LENGTH_SHORT).show();
		}
}
}