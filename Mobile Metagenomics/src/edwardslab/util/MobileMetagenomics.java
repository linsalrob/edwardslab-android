package edwardslab.util;

import org.openintents.intents.FileManagerIntents;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.github.droidfu.activities.BetterDefaultActivity;

public class MobileMetagenomics extends BetterDefaultActivity{
	public static final String PREFS_NAME = "MmPrefs";
	protected static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 1;
	static final int ACTIVITY_CHOOSE_FILE = 0;
	static final int ACTIVITY_LOAD_WEB = 2;
	static final int ACTIVITY_CHOOSE_FILENAME = 3;
	static final int ACTIVITY_GET_JSON_OR_TITLE = 4;
	static final int ACTIVITY_GET_ALL_TITLES = 5;
	private static final int LOAD_LOCAL_ID = Menu.FIRST;
	private static final int LOAD_WEB_ID = Menu.FIRST + 1;
	static final String LOAD_FILE_PHONE_NUMBER = "load file phone number";
	static final String LOAD_FILE_SAMPLE_NUMBER = "load file sample number";
	static final String LOAD_FILE_SAMPLE_TITLE = "load file title";
	static final String LOAD_FILE_NAME = "load file name";
	static final String CHOOSE_FILE_NAME = "choose file name";
	static final String CHOOSE_FILE_NAME_MODE = "choose file name mode";
	static final String RESULTVIEW_MODE = "resultView mode";
	static final String NORMAL_MODE = "normal mode";
	static final String ALL_TITLES_MODE = "all titles mode";
	static final String SHARE_MODE = "share mode";
	static final String SAVE_MODE = "save mode";
	static final String LOAD_LOCAL_FILE = "load local mode";
	static final String LOAD_WEB_JSON_1 = "load web json mode 1";
	static final String LOAD_WEB_JSON_2 = "load web json mode 2";
	static final String FILE_NAME = "filename";
	static final String LEVEL = "level";
	static final String STRINGENCY = "stringency";
	static final String KMER = "kmer";
	static final String MAX_GAP = "maxGap";
	static final String PHONE_NUMBER = "phone number";
	static final String VALID_PHONE_STRING = "Please enter a valid phone number";
	static final String VALID_SAMPLE_STRING = "Please enter a valid sample number";
	EditText fileName;
	Spinner stringencySpinner;
	Spinner levelSpinner;
	Spinner kmerSpinner;
	Spinner maxGapSpinner;
	Object[] keyArr;
	Object[] valArr;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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
        		i.putExtra(RESULTVIEW_MODE, NORMAL_MODE);
        		i.putExtra(FILE_NAME, fileName.getText().toString());
        		i.putExtra(LEVEL, levelSpinner.getSelectedItemPosition() /*+ 1*/);
        		i.putExtra(STRINGENCY, (stringencySpinner.getSelectedItemPosition() + 1));
        		i.putExtra(KMER, (kmerSpinner.getSelectedItemPosition() + 7));
        		i.putExtra(MAX_GAP, (maxGapSpinner.getSelectedItemPosition() + 1)*300);
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
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.server_get_query)
			.setCancelable(true)
			.setPositiveButton(R.string.get_all_titles, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
		        	Intent j = new Intent(MobileMetagenomics.this, GetAllTitles.class);
		        	startActivityForResult(j, ACTIVITY_GET_ALL_TITLES);
				}
			})
			.setNegativeButton(R.string.get_json_or_title, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
		        	Intent j = new Intent(MobileMetagenomics.this, GetJsonOrTitle.class);
		        	startActivityForResult(j, ACTIVITY_GET_JSON_OR_TITLE);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
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
				System.out.println("filename: "+extras.getString(LOAD_FILE_NAME));
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
	        		String phoneNumber = extras.getString(LOAD_FILE_PHONE_NUMBER);
	        		System.out.println("Mobile Metagenomics receiving: " + extras.getInt(LOAD_FILE_SAMPLE_NUMBER));
	        		Integer sampleNumber = extras.getInt(LOAD_FILE_SAMPLE_NUMBER);
	        		//String sampleTitle = extras.getString(LOAD_FILE_SAMPLE_TITLE);
	        		
	        		if(!phoneNumber.equals("") && sampleNumber != 0){
	        			Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
	        			i.putExtra(RESULTVIEW_MODE, LOAD_WEB_JSON_1);
	            		i.putExtra(LOAD_FILE_PHONE_NUMBER, phoneNumber);
	            		i.putExtra(LOAD_FILE_SAMPLE_NUMBER, sampleNumber);
	            		startActivity(i);
	        		}/*
	        		else if(!phoneNumber.equals("") && (sampleTitle != null)){
	        			Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
	        			i.putExtra(RESULTVIEW_MODE, LOAD_WEB_JSON_2);
	        			i.putExtra(LOAD_FILE_PHONE_NUMBER, phoneNumber);
	        			i.putExtra(LOAD_FILE_SAMPLE_TITLE, sampleTitle);
	            		startActivity(i);
	        		}*/
	        	}
				break;
	        case ACTIVITY_GET_ALL_TITLES:
	        	if(extras != null){
		        	//TODO: get information out, display titles
		        	Intent j = new Intent(MobileMetagenomics.this, ResultView.class);
	        		j.putExtra(RESULTVIEW_MODE, ALL_TITLES_MODE);
	        		j.putExtra(PHONE_NUMBER, (extras.getString(PHONE_NUMBER)));
	        		j.putExtra(STRINGENCY, (extras.getInt(STRINGENCY)));
	        		j.putExtra(LEVEL, (extras.getInt(LEVEL)));
	        		j.putExtra(KMER, (extras.getInt(KMER)));
	        		j.putExtra(MAX_GAP, (extras.getInt(MAX_GAP)));
	        		startActivityForResult(j, 0);
	        	}
	        	break;
	        case ACTIVITY_GET_JSON_OR_TITLE:
	        	if(extras != null){
	        		String phoneNumber = extras.getString(LOAD_FILE_PHONE_NUMBER);
	        		System.out.println("Mobile Metagenomics receiving: " + extras.getInt(LOAD_FILE_SAMPLE_NUMBER));
	        		String sampleNumber = extras.getString(LOAD_FILE_SAMPLE_NUMBER);
	        		//String sampleTitle = extras.getString(LOAD_FILE_SAMPLE_TITLE);
	        		
	        		if(!phoneNumber.equals("") && !sampleNumber.equals("0")){
	        			Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
	        			i.putExtra(RESULTVIEW_MODE, LOAD_WEB_JSON_1);
	            		i.putExtra(LOAD_FILE_PHONE_NUMBER, phoneNumber);
	            		i.putExtra(LOAD_FILE_SAMPLE_NUMBER, sampleNumber);
	            		startActivity(i);
	        		}
	        	}
	        	break;
	        }
        }
    }
    
    /**
     * @author jhoffman
     * @param resFile	The results file to load.
     * @return void
     * Loads a results file and passes it to ResultView for display.
     */
	public void loadResults(String resFile){
		            Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
		            i.putExtra(RESULTVIEW_MODE, LOAD_LOCAL_FILE);
	        		i.putExtra(LOAD_FILE_NAME, resFile);
	        		startActivity(i);
	}

	/**
	 * @author jhoffman
	 * @return void
	 * Opens a file on the phone's file system.
	 */
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