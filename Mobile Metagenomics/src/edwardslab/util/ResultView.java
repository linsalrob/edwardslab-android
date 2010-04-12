package edwardslab.util;

/*References: http://www.glenmccl.com/tip_030.htm for serializable code.
	http://brainflush.wordpress.com/2009/04/08/android-in-sync-handling-concurrent-tasks-in-google-android/
	for BetterAsyncTask Code
	http://www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
	Used for the web access portion of code.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.github.droidfu.activities.BetterDefaultActivity;
import com.github.droidfu.concurrent.BetterAsyncTask;

public class ResultView extends BetterDefaultActivity{

	private static final int SHARE_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	private static final int LOAD_ID = Menu.FIRST + 2;
	private static final int APP_ID = 0; 
	private NotificationManager mManager;
	static final int  ID_DIALOG_ANNOTATE = 0;
	static final int  ID_DIALOG_LOAD=1;
	static final int  ID_DIALOG_SAVE=2;
	static final int  ID_DIALOG_SHARE=3;
	static final int  ID_DIALOG_TITLES=4;
	static final String APP_NAME = "Mobile Metagenomics";
	static final String ANNOTATION = "Annotation Complete!";
	static final String LOADING = "Loading Complete!";
	static final String SAVING = "Saving Complete!";
	static final String SHARING = "Sharing Complete!";
	static final String LEVEL = "level";
	static final String MAX = "max";
	String fileName;
	String shareMode;
	int stringency = -1;
	int level = -1;
	int kmer = -1;
	int maxGap = 1;
	String phoneNumberForQuery = "";
	String sampleNumber = "-1";
	String sampleTitle = "";
	Object[] resultsArr;
	ListView resultListView;
	int max;
	int downloadIterationValue;
	int errFlag = 0;
	String url, onResumeAction;
	Thread setupInitialResult;
	Thread downloadRemainingResults;
	TextView mDisplay;
	ProgressBar mBar = null;
	int PROGRESS_MODIFIER;
	boolean isInFocus;
//making a change to test commit for daniel
	/*			HashTable MgUtilFunc.JSONToHash(MgUtilFunc.doJsonAllTitlesQuery(phoneNumber.getText().toString())).get("allTitles");
*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.resultview);
		Log.e("ResultView","onCreate'd");
		resultListView = (ListView)findViewById(R.id.ResultsListView);
		setProgressBarVisibility(false);
		mBar = (ProgressBar) findViewById(R.id.placeholder);
		mBar.setVisibility(ProgressBar.GONE);
		mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		isInFocus = true;
	}

	@Override  
	protected void onResume() {  
		super.onResume();  
		Log.e("ResultView","onResume'd");
		Bundle extras = getIntent().getExtras();
		isInFocus = true;
		if(isResuming()){
			Log.e("ResultView","Supposedly isResuming() == true");
		}
		else if(isLaunching()){
			Log.e("ResultView","Supposedly isLaunching() == true");
			if(extras != null){
				Log.e("ResultView","extras != null");
				if(extras.containsKey(MobileMetagenomics.RESULTVIEW_MODE)){
					String mode = extras.getString(MobileMetagenomics.RESULTVIEW_MODE);
					if(mode.equals(MobileMetagenomics.NORMAL_MODE)){
						Log.e("ResultView","extras doesn't contain LOAD FILE NAME");
						level = (extras.getInt(MobileMetagenomics.LEVEL));
						stringency = (extras.getInt(MobileMetagenomics.STRINGENCY));
						fileName = (extras.getString(MobileMetagenomics.FILE_NAME));
						kmer = (extras.getInt(MobileMetagenomics.KMER));
						maxGap = (extras.getInt(MobileMetagenomics.MAX_GAP));
						AnnotationAsyncTask1 task = new AnnotationAsyncTask1(this);
						setProgressDialogTitleId(ID_DIALOG_ANNOTATE);
						setProgressDialogMsgId(ID_DIALOG_ANNOTATE);
						// task.disableDialog();
						task.execute();
					}
					else if(mode.equals(MobileMetagenomics.LOAD_LOCAL_FILE)){
						fileName = extras.getString(MobileMetagenomics.LOAD_FILE_NAME);
					new LoadResultsAsyncTask(this).execute(fileName);
					}
					else if(mode.equals(MobileMetagenomics.LOAD_WEB_JSON_1)){
						phoneNumberForQuery = extras.getString(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER);
						sampleNumber = extras.getString(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER);
						LoadJsonMode1AsyncTask task = new LoadJsonMode1AsyncTask(this);
						setProgressDialogTitleId(ID_DIALOG_LOAD);
						setProgressDialogMsgId(ID_DIALOG_LOAD);
						task.execute();
					}
					else if(mode.equals(MobileMetagenomics.ALL_TITLES_MODE)){
						phoneNumberForQuery = extras.getString(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER);
						level = (extras.getInt(MobileMetagenomics.LEVEL));
						stringency = (extras.getInt(MobileMetagenomics.STRINGENCY));
						kmer = (extras.getInt(MobileMetagenomics.KMER));
						maxGap = (extras.getInt(MobileMetagenomics.MAX_GAP));
						GetAllTitlesAsyncTask task = new GetAllTitlesAsyncTask(this);
						setProgressDialogTitleId(ID_DIALOG_TITLES);
						setProgressDialogMsgId(ID_DIALOG_TITLES);
						task.execute();
					}
						/*
					else if(mode.equals(MobileMetagenomics.LOAD_WEB_JSON_2)){
						phoneNumberForQuery = extras.getString(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER);
	            		sampleTitle = extras.getString(MobileMetagenomics.LOAD_FILE_SAMPLE_TITLE);
	            		LoadJsonMode2AsyncTask task = new LoadJsonMode2AsyncTask(this);
						setProgressDialogTitleId(ID_DIALOG_ANNOTATE);
						setProgressDialogMsgId(ID_DIALOG_ANNOTATE);
						task.execute();
					}*/
					else{
						//TODO: error: invalid mode
					}
				}
				else{
					//TODO: error: no mode specified
				}
			}
			else{
				Log.e("ResultView","extras == null");
			}
		}		
	}

	@Override  
	protected void onPause() {  
		super.onPause();  
		isInFocus = false;
		Log.e("ResultView","onPause'd");
	}

	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences settings = getSharedPreferences(MobileMetagenomics.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(LEVEL, level);
		editor.putInt(MAX, max);
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SHARE_ID, 0, R.string.share).setIcon(android.R.drawable.ic_menu_share);
		menu.add(0, SAVE_ID, 0, R.string.save).setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, LOAD_ID, 0, R.string.load_local).setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case SHARE_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(ResultView.this);
			builder.setMessage(R.string.share_query)
			.setCancelable(true)
			.setPositiveButton(R.string.email, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shareMode = "txt";
					new ShareResultsAsyncTask(ResultView.this).execute("String");
				}
			})
			.setNegativeButton(R.string.web, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shareMode = "json";
					Intent i = new Intent(ResultView.this, FileNameChooser.class);
					i.putExtra(MobileMetagenomics.CHOOSE_FILE_NAME_MODE, MobileMetagenomics.SHARE_MODE);
		        	startActivityForResult(i, MobileMetagenomics.ACTIVITY_CHOOSE_FILENAME);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		case SAVE_ID:
			Intent i = new Intent(ResultView.this, FileNameChooser.class);
			i.putExtra(MobileMetagenomics.CHOOSE_FILE_NAME_MODE, MobileMetagenomics.SAVE_MODE);
        	startActivityForResult(i, MobileMetagenomics.ACTIVITY_CHOOSE_FILENAME);
			//new SaveResultsAsyncTask(this).execute("String");
			return true;
		case LOAD_ID:
			Intent j = new Intent(ResultView.this, LoadFileChooser.class);
			startActivityForResult(j, MobileMetagenomics.ACTIVITY_CHOOSE_FILE);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO: make sure this is correct! Test loading a different file after performing annotation.
		super.onActivityResult(requestCode, resultCode, intent);
		Bundle extras = intent.getExtras();
		switch(requestCode) {
		case MobileMetagenomics.ACTIVITY_CHOOSE_FILE:
			new LoadResultsAsyncTask(ResultView.this).execute(extras.getString(MobileMetagenomics.LOAD_FILE_NAME));
			break;
		case MobileMetagenomics.ACTIVITY_CHOOSE_FILENAME:
			String mode = (String) extras.get(MobileMetagenomics.CHOOSE_FILE_NAME_MODE);
			if(mode.equals(MobileMetagenomics.SHARE_MODE)){
				new ShareResultsAsyncTask(ResultView.this).execute(extras.getString(MobileMetagenomics.CHOOSE_FILE_NAME));
			}
			else if(mode.equals(MobileMetagenomics.SAVE_MODE)){
				new SaveResultsAsyncTask(this).execute(extras.getString(MobileMetagenomics.CHOOSE_FILE_NAME));
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == ID_DIALOG_ANNOTATE){
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Performing Annotation...");
			loadingDialog.setMessage("Please wait (this may take a few moments)");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(true);
			return loadingDialog;
		}
		else if(id == ID_DIALOG_LOAD){
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Loading Results...");
			loadingDialog.setMessage("Please wait (this may take a few moments)");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(true);
			return loadingDialog;
		}
		else if(id == ID_DIALOG_SAVE){
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Saving Results...");
			loadingDialog.setMessage("Please wait (this may take a few moments)");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(true);
			return loadingDialog;
		}
		else if(id == ID_DIALOG_SHARE){
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Saving Results...");
			loadingDialog.setMessage("Please wait, your file will be sent shortly");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(true);
			return loadingDialog;
		}
		else if(id == ID_DIALOG_TITLES){
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setTitle("Loading Titles...");
			loadingDialog.setMessage("Please wait, your file will be sent shortly");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(true);
			return loadingDialog;
		}
		return super.onCreateDialog(id);
	}

	/**
	 * 
	 * @author jhoffman
	 * Starts metagenome annotation by uploading data, downloading and processing initial results, and starting AnnotationAsyncTask2
	 */
	private class AnnotationAsyncTask1 extends BetterAsyncTask<String, Integer, Integer> {
		public AnnotationAsyncTask1(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status;
			status = 0;
			Log.e("ResultView","Performing file upload with level " + level + " and stringency " + stringency);
			doInitialAsynchWork(MgUtilFunc.doFileUpload(fileName.toString(),
					level,
					stringency, kmer, maxGap));
			status++;
			publishProgress(status);
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			//Log.e("ResultView","After, reporting in...");
			AnnotationAsyncTask2 task2 = new  AnnotationAsyncTask2(ResultView.this);
			task2.disableDialog();
			task2.execute();
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
			if(resultsArr != null){
				switch (level){
				//Handle the "Function" operation mode
				case 0: resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr)); break;
				case 1: displaySubsystemsGraph(); break;
				case 2: displaySubsystemsGraph(); break;
				case 3: displaySubsystemsGraph(); break;
				case 4: displaySubsystemsGraph(); break;
				//TODO: replace this with some proper means of handling this error.
				default: System.out.println("Invalid mode - terminating."); break;
				}
				mBar.setProgress(mBar.getProgress() + 1);
				setProgress(PROGRESS_MODIFIER * mBar.getProgress());
				setTitle("Downloading segments: " + mBar.getProgress() + "/" + max);
			}
		}
	}

	/**
	 * 
	 * @author jhoffman
	 * Picks up where AnnotationAsyncTask1 leaves off. Performs annotation results downloads from the second to the last.
	 */
	private class AnnotationAsyncTask2 extends BetterAsyncTask<String, Integer, Integer> {
		public AnnotationAsyncTask2(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status = 1;
			Log.e("ResultView","Started second task");
			//Do remaining blocks.
			for(int i=2; i<=max; i++){
				status++;
				addToResults(MgUtilFunc.JSONToHash((MgUtilFunc.makeWebRequest((String) url + i))));    
				Log.e("ResultView","Supposedly finished addToResults");
				publishProgress(status);
				//if(resultsArr != null){resultListView.setAdapter(new ArrayAdapter(fuTest.this, android.R.layout.simple_list_item_1, resultsArr));}
				Log.e("ResultView","Supposedly posted second task results");
			}
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			showNotification(APP_NAME, ANNOTATION);
			setProgress(10000);
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
			if(resultsArr != null){
				switch (level){
				//Handle the "Function" operation mode
				case 0: resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr)); break;
				case 1: displaySubsystemsGraph(); break;
				case 2: displaySubsystemsGraph(); break;
				case 3: displaySubsystemsGraph(); break;
				case 4: displaySubsystemsGraph(); break;
				//TODO: replace this with some proper means of handling this error.
				default: System.out.println("Invalid mode - terminating."); break;
				}
				mBar.setProgress(mBar.getProgress() + 1);
				setProgress(PROGRESS_MODIFIER * mBar.getProgress());
				setTitle("Downloading segments: " + mBar.getProgress() + "/" + max);
			}
		} 

	}

	/**
	 * 
	 * @author jhoffman
	 * Downloads a json formatted annotation result from the server.
	 */
	private class LoadJsonMode1AsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadJsonMode1AsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status = 0;
			Log.e("ResultView","Performing load json mode 1 with phone# " + phoneNumberForQuery + " and sample number " + sampleNumber);
			//TODO: Here is where we would need to save metadata like stringency, maxGap, etc. Do this by unchaining the last 
			//MgUtilFunc.JSONToHash call and saving the hash returned from the inner JSONToHash call.
			loadInitialResults(MgUtilFunc.JSONToHash((MgUtilFunc.JSONToHash(MgUtilFunc.doJsonQuery1(phoneNumberForQuery, sampleNumber))).get("data")));
			status = 1;
			publishProgress(status);
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			//Log.e("ResultView","After, reporting in...");
			AnnotationAsyncTask2 task2 = new  AnnotationAsyncTask2(ResultView.this);
			task2.disableDialog();
			task2.execute();
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
			//Handle the "Function" operation mode
			if(resultsArr != null){resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));}
			setProgress(10000);
		} 

	}

	/**
	 * 
	 * @author jhoffman
	 * Downloads a json formatted list of results files (organized by title) stored on the server.
	 */
	private class GetAllTitlesAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public GetAllTitlesAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status = 0;
			Log.e("ResultView","Performing getAllTitles with phone# " + phoneNumberForQuery);
			//TODO: Here is where we would need to save metadata like stringency, maxGap, etc. Do this by unchaining the last 
			//MgUtilFunc.JSONToHash call and saving the hash returned from the inner JSONToHash call.
			loadInitialResults(MgUtilFunc.JSONToHash((MgUtilFunc.doJsonAllTitlesQuery(phoneNumberForQuery, stringency, level, maxGap, kmer))));
			status = 1;
			publishProgress(status);
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			//Log.e("ResultView","After, reporting in...");
			AnnotationAsyncTask2 task2 = new  AnnotationAsyncTask2(ResultView.this);
			task2.disableDialog();
			task2.execute();
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
			//Handle the "Function" operation mode
			if(resultsArr != null){resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));}
			setProgress(10000);
		} 

	}
	
	/**
	 * 
	 * @author jhoffman
	 * Saves annotation results locally to the phone's SDCard.
	 */
	private class SaveResultsAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public SaveResultsAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void before(Context context){
			showDialog(ID_DIALOG_SAVE);
		}   	
		@Override
		protected Integer doCheckedInBackground(Context context, String... params) {
			try {
				System.out.println("saveResults should be making a file here.");
				FileOutputStream fos = new FileOutputStream(new File("/sdcard/" + params[0] + ".json"));
				JSONObject tmpJo = new JSONObject();
				for(int i=0; i<resultsArr.length; i++){
					tmpJo.put("" + i, resultsArr[i]);
				}
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				//osw.write(tmpJo.toString());
				String dumbString = tmpJo.toString();
				char[] dumbArr = dumbString.toCharArray();
				for(int j=0; j<dumbArr.length; j++){
					osw.write(dumbArr[j]);
				}
				osw.close();
				fos.close();
				System.out.println("Writing the following to file: " + tmpJo.toString());
				return 1;
			}
			catch (Throwable e) {
				Log.e("SaveRes","exception thrown: " + e.toString());
				System.err.println("exception thrown: " + e.toString());
				return -1;
			}
		}   	
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				dismissDialog(ID_DIALOG_SAVE);
			}
			if(values[0] == -1){
				dismissDialog(ID_DIALOG_SAVE);
				// TODO: popup toast that says save failed.
			}
		}
		@Override
		protected void after(Context context, Integer value) {
			if(value == 1){
				showNotification(APP_NAME, SAVING);
				dismissDialog(ID_DIALOG_SAVE);
			}
			if(value == -1){
				dismissDialog(ID_DIALOG_SAVE);
				// TODO: popup toast that says save failed.
			}
		}
		@Override
		protected void handleError(Context arg0, Exception arg1) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 
	 * @author jhoffman
	 * Loads locally saved annotation results.
	 */
	private class LoadResultsAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadResultsAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void before(Context context){
			showDialog(ID_DIALOG_LOAD);
		}	
		@Override
		protected Integer doCheckedInBackground(Context context, String... params) {
			//String[] tmpStringArr = params[0].split("/sdcard/");
			//TODO: this may be unneeded, look at what happens when a normal (non-loaded) result is done.
			/*if(tmpStringArr.length > 1)
				tmpStringArr = tmpStringArr[1].split(".json");
			else
				tmpStringArr = tmpStringArr[0].split(".json");*/
			//fileName = tmpStringArr[0];
			fileName = params[0];
			String concatJson = "";
			try {
				FileInputStream fis = new FileInputStream(new File(params[0]));
				 DataInputStream dis = new DataInputStream(fis);
			        BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			    String strLine;
			    strLine = br.readLine();
			    //Read File Line By Line
			    if(strLine != null)   {
			      // Print the content on the console
			      concatJson = strLine;
			    }
			    System.out.println("concat json = " + concatJson);
			    loadSavedResults(MgUtilFunc.JSONToHash(concatJson));
			    //Close the input stream
			    dis.close();
			    return 1;
			}
			catch (Exception e) {
				System.err.println("exception thrown from LoadResults doInBackground");
				return -1;
				// TODO: Pop up a toast or something
			}
		}    	
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				showNotification(APP_NAME, LOADING);
				dismissDialog(ID_DIALOG_LOAD);
			}
			if(values[0] == -1){
				dismissDialog(ID_DIALOG_LOAD);
				// TODO: popup toast that says save failed.
			}
		}
		@Override
		protected void after(Context context, Integer value) {
			if(value == 1){
				if(resultsArr != null){resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));}
				dismissDialog(ID_DIALOG_LOAD);
			}
			if(value == -1){
				dismissDialog(ID_DIALOG_LOAD);
				// TODO: popup toast that says save failed.
			}
		}
		@Override
		protected void handleError(Context arg0, Exception arg1) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 
	 * @author jhoffman
	 * Shares annotation results via email.
	 */
	private class ShareResultsAsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public ShareResultsAsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		@Override
		protected void before(Context context){
			showDialog(ID_DIALOG_SHARE);
		}
		@Override
		protected Integer doCheckedInBackground(Context context, String... params) {
			File saveFile;
			if(shareMode == "mmr"){
				saveFile = new File("/sdcard/" + fileName + ".mmr");
				// MMR is broken if it is ever re-enabled. See the else block upload.
				// it used to be outside of the else block.
				try {
					FileOutputStream fos = new FileOutputStream(saveFile);
					ObjectOutputStream oos =
						new ObjectOutputStream(fos);
					oos.writeObject(resultsArr);
					oos.flush();
					fos.close();
					publishProgress(1);
				}
				catch (Throwable e) {
					System.err.println("exception thrown");
					return -1;
				}
			}
			else if(shareMode == "json"){
				Log.e("shareResults","shareMode set to json. Setting up JSON Object");
				TelephonyManager mTelephonyMgr;  
				mTelephonyMgr = (TelephonyManager)  
				getSystemService(Context.TELEPHONY_SERVICE); 
				try {
					JSONObject tmpJo = new JSONObject();
					for(int i=0; i<resultsArr.length; i++){
						//This is the offending line of code!
						String[] tmpStringArr = resultsArr[i].toString().split(" value: ");
						tmpJo.put(tmpStringArr[0], Integer.parseInt(tmpStringArr[1]));
					}
					String tmpString = tmpJo.toString();
					String tmpLineNumber = mTelephonyMgr.getLine1Number();
					System.out.println("Setting filename to " + params[0]);
					String tmpFileName = params[0];					
					// TODO: can check success/failure here, just need to examine what the server does on failure!
					MgUtilFunc.doJsonUpload(tmpLineNumber, tmpFileName, tmpString);
					return 1;
				}
				catch (Throwable e) {
					Log.e("shareResults","exception thrown: " + e.toString());
					System.err.println("exception thrown: " + e.toString());
					return -1;
				}
			}
			else{
				writeFileOut();
				saveFile = new File("/sdcard/" + fileName + ".txt");
				// Use this later to fix SHARE
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.putExtra(Intent.EXTRA_TEXT, "Attached are the results of the Mobile Metagenomics app on " + fileName);
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Annotation Results: " + fileName);
				emailIntent.setType("message/rfc822");
				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saveFile));
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
			return 1;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				dismissDialog(ID_DIALOG_SHARE);
			}
		}
		@Override
		protected void after(Context context, Integer value) {
			// TODO: Conclude progress dialogues etc...
			if(value == 1){
				//showNotification(APP_NAME, SHARING);
				dismissDialog(ID_DIALOG_SHARE);
			}
		}
		@Override
		protected void handleError(Context arg0, Exception arg1) {
			// TODO Auto-generated method stub

		}
	}	
	/*
	private class LoadJsonMode2AsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadJsonMode2AsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status;
			status = 0;
			Log.e("ResultView","Performing load json mode 1");
			loadInitialResults(JSONToHash(doJsonQuery2(phoneNumberForQuery, sampleTitle)));
			status++;
			publishProgress(status);
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			//Log.e("ResultView","After, reporting in...");
			AnnotationAsyncTask2 task2 = new  AnnotationAsyncTask2(ResultView.this);
			task2.disableDialog();
			task2.execute();
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
	        //Handle the "Function" operation mode
	        if(resultsArr != null){resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));}
	        setProgress(10000);
		} 

	}*/

	/**
	 * @author jhoffman
	 * @param name
	 * @param msg
	 * Displays a notification with a given name and message in the notification bar.
	 */
	public void showNotification(String name, String msg){
		if(!isInFocus){
			Intent intent = new Intent(ResultView.this, ResultView.class);
			Notification notification = new Notification(R.drawable.icon,
					name, System.currentTimeMillis());
			notification.setLatestEventInfo(ResultView.this,
					name,msg,
					PendingIntent.getActivity(this.getBaseContext(), 0, intent,
							PendingIntent.FLAG_CANCEL_CURRENT));
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			mManager.notify(APP_ID, notification);
		}
	}

	/**
	 * @author jhoffman
	 * Draws a graph based on annotation results saved to resultsArr.
	 */
	public void displaySubsystemsGraph(){
		WindowManager wm =
			(WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		int width = disp.getWidth();   	
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.LinearLayout01);
		myLayout.removeAllViews();
		LinearLayout.LayoutParams params2 = new
		LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);   	
		Hashtable<String, String> helperHash = new Hashtable<String, String>();
		//TODO: this is an issue if the resultsArr isn't populated properly. Null ex
		for(int i=0; i<resultsArr.length; i++){
			System.out.println("dSG adding " + resultsArr[i] + " to graph");
			String delims = "value: ";
			String[] tokens = ((String) resultsArr[i]).split(delims);
			if(!helperHash.containsKey(tokens[0])){
				helperHash.put(tokens[0], tokens[1]);
			}
			else{
				helperHash.put(tokens[0] + "value ", (helperHash.get(tokens[0]) + tokens[1]));
			}
		}
		String[] newArr = new String[helperHash.size()];
		Enumeration<String> col = helperHash.keys();
		int index = 0;
		while(col.hasMoreElements()){
			String tmp = col.nextElement();
			newArr[index++] = tmp + " value: " + helperHash.get(tmp);
		}
		Arrays.sort(newArr);		
		int largest = 0;
		for(int i=0; i<newArr.length; i++){
			String delims = "value: ";
			String[] tokens = ((String) newArr[i]).split(delims);
			if(Integer.parseInt(tokens[1]) > largest){
				largest = Integer.parseInt(tokens[1]);
			}
		}
		ShapeDrawable myShape;
		TextView myTv;		
		for(int i=0; i<newArr.length; i++){
			String delims = "value: ";
			String[] tokens = ((String) newArr[i]).split(delims);	

			myTv = new TextView(this);
			myTv.setGravity(Gravity.LEFT);
			myTv.setText(tokens[0]);
			myLayout.addView(myTv, params2);

			myTv = new TextView(this);
			myTv.setGravity(Gravity.LEFT);		
			Float tmp = Float.parseFloat(tokens[1]) / largest;			
			Integer w = (int) (width * tmp);
			myTv.setWidth(w+1);			
			myShape = new ShapeDrawable();
			myShape.setIntrinsicHeight(25);
			/*if(i%2 == 1){
				myShape.getPaint().setColor(Color.GREEN);
			}
			else{*/
			myShape.getPaint().setColor(Color.BLUE);
			//}
			myTv.setBackgroundDrawable(myShape);
			myLayout.addView(myTv, params2);

		}
	}

	/**
	 * @author jhoffman
	 * @param resString	The result of a query to the server for results
	 * 
	 */
	private void doInitialAsynchWork(String resString){
		Hashtable tmpHash = MgUtilFunc.JSONToHash(resString);
		if(tmpHash != null){
			url = (String) tmpHash.get("url");
			String tmpMax = (String) tmpHash.get("max");
			if(url != null && tmpMax != null){
				max = Integer.parseInt(tmpMax);
				PROGRESS_MODIFIER = 10000 / max;
				loadInitialResults(MgUtilFunc.JSONToHash((MgUtilFunc.makeWebRequest((String) url + 1))));
			}
		}
	}

	/**
	 * @author jhoffman
	 * @param newData	A hash of results from the annotation server.
	 * Loads the first set of results to the resultsArr and sorts them for display.
	 */
	public void loadInitialResults(Hashtable<String,String> newData){
		Object thisElem;
		ArrayList<String> helperList = new ArrayList<String>();
		for (Enumeration<String> e = newData.keys(); e.hasMoreElements();) {
			thisElem = e.nextElement();
			helperList.add(((String) thisElem) + " value: " + newData.get(thisElem));
		}
		resultsArr = helperList.toArray();
		Arrays.sort(resultsArr);
	}

	/**
	 * @author jhoffman
	 * @param newData	A hash of results from the annotation server.
	 * Loads results from a one-line file containing a json string to the resultsArr and sorts them for display.
	 */
	public void loadSavedResults(Hashtable<String,String> newData){
		Object thisElem;
		ArrayList<String> helperList = new ArrayList<String>();
		for (Enumeration<String> e = newData.keys(); e.hasMoreElements();) {
			thisElem = e.nextElement();
			helperList.add(newData.get(thisElem));
		}
		resultsArr = helperList.toArray();
		Arrays.sort(resultsArr);
	}
	
	/**
	 * @author jhoffman
	 * @param newData	A hash of results from the annotation server.
	 * Adds all subsequent sets of results to the resultsArr and sorts them for display.
	 */
	public void addToResults(Hashtable<String,String> newData){
		Object thisElem;
		int i=resultsArr.length;
		//Massaging data so that duplicate entries end up with the same values.
		for(int h=0; h<resultsArr.length; h++){
			String[] arrParts = ((String) resultsArr[h]).split(" value: ");
			if(newData.containsKey(arrParts[0])){
				resultsArr[h] = (arrParts[0] 
				                          + " value: " 
				                          + (Integer.parseInt(arrParts[1]) + Integer.parseInt(newData.get(arrParts[0])))
				);	
				newData.remove(arrParts[0]);
			}
		}

		Object[] tmp = new Object[resultsArr.length + newData.size()];
		for(int j=0; j<resultsArr.length; j++){
			tmp[j]=resultsArr[j];
		}
		for (Enumeration<String> e = newData.keys(); e.hasMoreElements();) {
			thisElem = e.nextElement();
			tmp[i++] = ((String) thisElem) + " value: " + newData.get(thisElem);
		}
		resultsArr = tmp;
		Arrays.sort(resultsArr);
	}

	/**
	 * @author jhoffman
	 * Writes a file containing the data from resultsArr.
	 */
	public void writeFileOut(){
		try{
			OutputStreamWriter osw = new OutputStreamWriter(	new FileOutputStream(
					new File("/sdcard/" + fileName + ".txt")));
			for(int i=0; i<resultsArr.length; i++){
				osw.write((String) resultsArr[i] + "\n\r");
			}
			osw.flush();
			osw.close();
		}
		catch (Throwable e) {
			System.err.println("exception thrown");
		}
	}
}
