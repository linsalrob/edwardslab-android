package edwardslab.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class MobileMetagenomics extends Activity{
	static final int GONE = 0x00000008;
	static final int  VISIBLE = 0x00000000;
	static final String FILE_NAME = "filename";
	static final String LEVEL = "level";
	static final String STRINGENCY = "stringency";
	private static final int INSERT_ID = Menu.FIRST;
	EditText fileName;
	Spinner stringencySpinner;
	Spinner levelSpinner;
	Object[] keyArr;
	Object[] valArr;
	ListView resultListView;
    private ProgressDialog pd;
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
        resultListView = (ListView)findViewById(R.id.ResultListView);
        final Button uploadButton = (Button)findViewById(R.id.Upload);
        final Button resetButton = (Button)findViewById(R.id.Reset);
        final Button browseButton = (Button)findViewById(R.id.Browse);
      //Connect Listeners to UI
        uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) { 
        		/*
        		fileName.setVisibility(GONE);
        		stringencySpinner.setVisibility(GONE);
        		levelSpinner.setVisibility(GONE);
        		uploadButton.setVisibility(GONE);
        		browseButton.setVisibility(GONE);
    			inputManager.hideSoftInputFromWindow(fileName.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    			*/
        		Intent i = new Intent(MobileMetagenomics.this, ResultView.class);
        		i.putExtra(FILE_NAME, fileName.getText().toString());
        		i.putExtra(LEVEL, levelSpinner.getSelectedItemPosition());
        		i.putExtra(STRINGENCY, stringencySpinner.getSelectedItemPosition());
        		startActivity(i);
        		/*
    			pd = ProgressDialog.show(MobileMetagenomics.this, "Performing Annotation...", "Please wait (this may take a few moments)", true, false);
    			Thread thread = new Thread(MobileMetagenomics.this);
    			thread.start();*/
        	}
        });       
        resetButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {  
        		fileName.setVisibility(VISIBLE);
        		stringencySpinner.setVisibility(VISIBLE);
        		levelSpinner.setVisibility(VISIBLE);
        		uploadButton.setVisibility(VISIBLE);
        		browseButton.setVisibility(VISIBLE);
        		fileName.setText("");
        		stringencySpinner.setSelection(0);
        		levelSpinner.setSelection(0);
        	}
        });
    }
}