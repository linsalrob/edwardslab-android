package edwardslab.util;

import org.openintents.intents.FileManagerIntents;

import com.github.droidfu.activities.BetterDefaultActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

public class MobileMetagenomics extends BetterDefaultActivity{
	public static final String PREFS_NAME = "MmPrefs";
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	static final int ACTIVITY_CHOOSE_FILE = 0;
	static final int ACTIVITY_LOAD_WEB = 2;
	private static final int LOAD_LOCAL_ID = Menu.FIRST;
	private static final int LOAD_WEB_ID = Menu.FIRST + 1;
	static final String LOAD_FILE_PHONE_NUMBER = "load file phone number";
	static final String LOAD_FILE_SAMPLE_NUMBER = "load file sample number";
	static final String LOAD_FILE_SAMPLE_TITLE = "load file title";
	static final String LOAD_FILE_NAME = "load file name";
	static final String FILE_NAME = "filename";
	static final String LEVEL = "level";
	static final String STRINGENCY = "stringency";
	static final String KMER = "kmer";
	static final String MAX_GAP = "maxGap";
	public static boolean launchResultView;
	EditText fileName;
	Spinner stringencySpinner;
	Spinner levelSpinner;
	Spinner kmerSpinner;
	Spinner maxGapSpinner;
	Object[] keyArr;
	Object[] valArr;
	ListView resultListView;
    //private ProgressDialog pd;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        Log.e("MobileMetagenomics","onCreate'd, launchResultView is " + MobileMetagenomics.launchResultView);
        launchResultView = settings.getBoolean("launchResultView", false);
//        launchResultView = false;
        setContentView(R.layout.main);
        
        //Initialize UI from xml
        fileName = (EditText) findViewById(R.id.Filename);
        stringencySpinner = (Spinner) findViewById(R.id.StringencySpinner);
        levelSpinner = (Spinner) findViewById(R.id.LevelSpinner);
        kmerSpinner = (Spinner) findViewById(R.id.KmerSpinner);
        maxGapSpinner = (Spinner) findViewById(R.id.MaxGapSpinner);
        kmerSpinner.setSelection(1);
        maxGapSpinner.setSelection(1);
    	final InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        final Button uploadButton = (Button)findViewById(R.id.Upload);
        final Button resetButton = (Button)findViewById(R.id.Reset);
        final Button browseButton = (Button)findViewById(R.id.Browse);
      //Connect Listeners to UI
        uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) { 
        		Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
        		i.putExtra(FILE_NAME, fileName.getText().toString());
        		i.putExtra(LEVEL, levelSpinner.getSelectedItemPosition() /*+ 1*/);
        		i.putExtra(STRINGENCY, (stringencySpinner.getSelectedItemPosition() + 1));
        		i.putExtra(KMER, (kmerSpinner.getSelectedItemPosition() + 7));
        		i.putExtra(MAX_GAP, (maxGapSpinner.getSelectedItemPosition() + 1)*300);
        		launchResultView = true;
        		startActivityForResult(i, 0);
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

       // if((getIntent().getType() != null) && (getIntent().getData() != null)){
        /*if(getIntent().getData() != null){

        	Uri tmpName = getIntent().getData();
        	fileName.setText(FileUtils.getFile(tmpName).getName());
        }
        else{
        	 Toast.makeText(this, "Data was Null!", Toast.LENGTH_LONG).show();
        }*/
    }
    
    @Override
    protected void onResume() {
    	 super.onResume();
    	 
    	/* if(launchResultView){
         	Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
     		startActivityForResult(i, 0);
         }*/
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	 Log.e("MobileMetagenomics","onNewIntent'd");
    	Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
    	startActivity(i);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, LOAD_LOCAL_ID, 0, R.string.load_local).setIcon(android.R.drawable.ic_menu_set_as);
        menu.add(0, LOAD_WEB_ID, 0, R.string.load_web).setIcon(android.R.drawable.ic_menu_set_as);
        return true;
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("launchResultView", launchResultView);
	    editor.commit();
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case LOAD_LOCAL_ID:
        	Intent i = new Intent(MobileMetagenomics.this, LoadFileChooser.class);
        	startActivityForResult(i, ACTIVITY_CHOOSE_FILE);
        	return true;
        case LOAD_WEB_ID:
        	Intent j = new Intent(MobileMetagenomics.this, LoadWebChooser.class);
        	startActivityForResult(j, ACTIVITY_LOAD_WEB);
        }
        return super.onMenuItemSelected(featureId, item);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
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
	        case ACTIVITY_LOAD_WEB:
	        	if(extras != null){
	        		int phoneNumber = extras.getInt(LOAD_FILE_PHONE_NUMBER);
	        		int sampleNumber = extras.getInt(LOAD_FILE_SAMPLE_NUMBER);
	        		String sampleTitle = extras.getString(LOAD_FILE_SAMPLE_TITLE);
	        		
	        		if(phoneNumber != 0 && sampleNumber != 0){
	        			
	        		}
	        		else if(phoneNumber != 0 && !sampleTitle.equals("")){
	        			
	        		}
	        	}
				break;
	        }
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