package edwardslab.util;

/*References: http://www.glenmccl.com/tip_030.htm for serializable code.
	http://brainflush.wordpress.com/2009/04/08/android-in-sync-handling-concurrent-tasks-in-google-android/
	for Task/Task Interface code
	http://www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
	Used for the web access portion of code.
 */

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.LinearLayout.LayoutParams;

import com.github.droidfu.activities.BetterDefaultActivity;
import com.github.droidfu.concurrent.BetterAsyncTask;

public class ResultView extends BetterDefaultActivity{

	private static final int SHARE_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	private static final int LOAD_ID = Menu.FIRST + 2;
	private static boolean statusOk = true;
	static final int  ID_DIALOG_ANNOTATE = 0;
	static final int  ID_DIALOG_LOAD=1;
	static final int  ID_DIALOG_SAVE=2;
	static final int  ID_DIALOG_SHARE=3;
	String LEVEL = "level";
	String MAX = "max";
	String fileName;
	String shareMode;
	int stringency = -1;
	int level = -1;
	int kmer = -1;
	int maxGap = 1;
	double phoneNumberForQuery = -1;
	Integer sampleNumber = 1;
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
	}

	@Override  
	protected void onResume() {  
		super.onResume();  
		Log.e("ResultView","onResume'd");
		Bundle extras = getIntent().getExtras();
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
					else if(mode.equals(MobileMetagenomics.LOAD_LOCAL_FILE)){;
							new LoadResults().execute(extras.getString(MobileMetagenomics.LOAD_FILE_NAME));
					}
					else if(mode.equals(MobileMetagenomics.LOAD_WEB_JSON_1)){
						phoneNumberForQuery = extras.getDouble(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER);
	            		sampleNumber = extras.getInt(MobileMetagenomics.LOAD_FILE_SAMPLE_NUMBER);
	            		LoadJsonMode1AsyncTask task = new LoadJsonMode1AsyncTask(this);
						setProgressDialogTitleId(ID_DIALOG_ANNOTATE);
						setProgressDialogMsgId(ID_DIALOG_ANNOTATE);
						task.execute();
					}
					else if(mode.equals(MobileMetagenomics.LOAD_WEB_JSON_2)){
						phoneNumberForQuery = extras.getDouble(MobileMetagenomics.LOAD_FILE_PHONE_NUMBER);
	            		sampleTitle = extras.getString(MobileMetagenomics.LOAD_FILE_SAMPLE_TITLE);
	            		LoadJsonMode2AsyncTask task = new LoadJsonMode2AsyncTask(this);
						setProgressDialogTitleId(ID_DIALOG_ANNOTATE);
						setProgressDialogMsgId(ID_DIALOG_ANNOTATE);
						task.execute();
					}
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

			//TODO: stupid things happen when the intent above is empty, fix this!

			/*
		        switch (level){
		        //Handle the "Function" operation mode
		        case 0: doFunctionWork(); break;
		        case 1: doSubsystemsWork(); break;
		        case 2: doSubsystemsWork(); break;
		        case 3: doSubsystemsWork(); break;
		        case 4: doSubsystemsWork(); break;
		        //TODO: replace this with some proper means of handling this error.
		        default: System.out.println("Invalid mode - terminating."); break;
		        }*/
		}		
	}

	@Override  
	protected void onPause() {  
		super.onPause();  
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
/*
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  

		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			// If we are killing the MM/ResultView process, we don't
			//	want the tasks to continue work floating in memory.
			 
			//Task.cancelAll(this);
			MobileMetagenomics.launchResultView = false;
		}  

		return super.onKeyDown(keyCode, event);  
	}
*/
	
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
			builder.setMessage("Share as plain text or Android-viewable format?")
			.setCancelable(true)
			.setPositiveButton("text", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shareMode = "txt";
					new shareResults().execute("String");
				}
			})
			.setNegativeButton("json", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shareMode = "json";
					new shareResults().execute("String");
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		case SAVE_ID:
			new SaveResults().execute("String");
			return true;
		case LOAD_ID:
			Intent i = new Intent(ResultView.this, LoadFileChooser.class);
			startActivityForResult(i, MobileMetagenomics.ACTIVITY_CHOOSE_FILE);
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
			new LoadResults().execute(extras.getString(MobileMetagenomics.LOAD_FILE_NAME));
			break;
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
		return super.onCreateDialog(id);
	}

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
			doInitialAsynchWork(doFileUpload(fileName.toString(),
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
				addToResults(JSONToHash((makeWebRequest((String) url + i))));    
				Log.e("ResultView","Supposedly finished addToResults");
				publishProgress(status);
				//resultListView.setAdapter(new ArrayAdapter(fuTest.this, android.R.layout.simple_list_item_1, resultsArr));
				Log.e("ResultView","Supposedly posted second task results");
			}
			return 1;
		}

		@Override
		protected void after(Context context, Integer result) {
			// TODO Auto-generated method stub
			setProgress(10000);
		}

		@Override
		protected void handleError(Context context, Exception error) {
			// TODO Auto-generated method stub

		}

		@Override  
		public void onProgressUpdate(Integer...values){
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
	
	private class LoadJsonMode1AsyncTask extends BetterAsyncTask<String, Integer, Integer> {
		public LoadJsonMode1AsyncTask(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Integer doCheckedInBackground(Context context, String... params) throws Exception{
			Integer status;
			status = 0;
			Log.e("ResultView","Performing load json mode 1");
			loadInitialResults(JSONToHash(doJsonQuery1(phoneNumberForQuery, sampleNumber)));
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
	        resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));
	        setProgress(10000);
		} 

	}
	
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
	        resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));
	        setProgress(10000);
		} 

	}
	
	@Override
    protected void onNewIntent(Intent intent){
    	 Log.e("ResultView","onNewIntent'd");
    }
	/*
	public void doFunctionWork(){
		startAsynchWorkTask = Task.getOrCreate(this, TASK1);  
		continueAsynchWorkTask = Task.getOrCreate(this, TASK2);  
		//  task3 = Task.getOrCreate(this, TASK3);
		setSecondaryProgress(0);
		switch (startAsynchWorkTask.state()) {  
		case NOT_STARTED:  
			//startAsynchWorkTask.run(this, startAsynchWork);  
			showDialog(ID_DIALOG_ANNOTATE);
			break;  
		case RUNNING:  
			//If task 2 is running, task 1 is actually COMPLETED!
			if(continueAsynchWorkTask.state() == Task.State.RUNNING)
			{
				resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, startAsynchWorkTask.getResult()));
				switch(continueAsynchWorkTask.state()){
				case RUNNING:
					System.out.println("task2 still running"); 
					break;
				case COMPLETED:
					System.out.println("task2 completed"); 
					resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, continueAsynchWorkTask.getResult()));
					MobileMetagenomics.launchResultView = false;
				}
			}
			else{
				System.out.println("task1 still running");
				showDialog(ID_DIALOG_ANNOTATE);
			}
			break;
		case COMPLETED:  
			resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, startAsynchWorkTask.getResult()));
			switch(continueAsynchWorkTask.state()){
			case RUNNING:
				System.out.println("task2 still running"); 
				break;
			case COMPLETED:
				resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, continueAsynchWorkTask.getResult()));
				MobileMetagenomics.launchResultView = false;
			}
			break;
		}
	}

	public void doSubsystemsWork(){
		startAsynchWorkTask = Task.getOrCreate(this, TASK1);  
		continueAsynchWorkTask = Task.getOrCreate(this, TASK2);  
		//  task3 = Task.getOrCreate(this, TASK3);
		setSecondaryProgress(0);
		switch (startAsynchWorkTask.state()) {  
		case NOT_STARTED:  
			//startAsynchWorkTask.run(this, startAsynchWork);  
			showDialog(ID_DIALOG_ANNOTATE);
			break;  
		case RUNNING:  
			//If task 2 is running, task 1 is actually COMPLETED!
			if(continueAsynchWorkTask.state() == Task.State.RUNNING)
			{
				displaySubsystemsGraph();
				switch(continueAsynchWorkTask.state()){
				case RUNNING:
					System.out.println("task2 still running"); 
					break;
				case COMPLETED:
			    	displaySubsystemsGraph();
					MobileMetagenomics.launchResultView = false;
				}
			}
			else{
				System.out.println("task1 still running");
				showDialog(ID_DIALOG_ANNOTATE);
			}
			break;
		case COMPLETED:  
			displaySubsystemsGraph();
			switch(continueAsynchWorkTask.state()){
			case RUNNING:
				System.out.println("task2 still running"); 
				break;
			case COMPLETED:
				displaySubsystemsGraph();
				MobileMetagenomics.launchResultView = false;
			}
			break;
		}
	}*/

	public void displaySubsystemsGraph(){
		System.out.println("displaySubsystemsGraph was called. resultArr length is: " + resultsArr.length);
		WindowManager wm =
			(WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		int width = disp.getWidth();   	
		LinearLayout myLayout = (LinearLayout) findViewById(R.id.LinearLayout01);
		myLayout.removeAllViews();
		LinearLayout.LayoutParams params2 = new
		LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);   	
		Hashtable helperHash = new Hashtable();
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
		Enumeration col = helperHash.keys();
		int index = 0;
		while(col.hasMoreElements()){
			String tmp = (String) col.nextElement();
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

	public String makeWebRequest(String s){
		Log.e("makeWebRequest","Performing " + s);
		if(statusOk){
			/* Will be filled and displayed later. */
			String webResultString = null;
			try {
				/* Define the URL we want to load data from. */
				URL urlToOpen = new URL(s);
				/* Open a connection to that URL. */
				URLConnection ucon = urlToOpen.openConnection();
				/* Define InputStreams to read
				 * from the URLConnection. */
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				/* Read bytes to the Buffer until
				 * there is nothing more to read(-1). */
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current = 0;
				while((current = bis.read()) != -1){
					baf.append((byte)current);
				}
				/* Convert the Bytes read to a String. */
				webResultString = new String(baf.toByteArray());
			} catch (Exception e) {
				/* On any Error we want to display it. */
				statusOk = false;
				webResultString = e.getMessage();
			}
			return webResultString;
		}
		else{
			//pop up a toast or something
			return null;
		}
	}

	public Hashtable<String,String> JSONToHash(String jsonString){
		if(statusOk){
			System.out.println("JSONToHash reached, input is: " + jsonString);
			//This is a more general parse method (and perhaps I should reconsider the names), which we can hopefully re-use.
			Hashtable<String,String> resultHash = new Hashtable<String,String>();
			try{// Take the stringified JSON Hash of Hashes and put it into our Hash
				JSONObject convObj = new JSONObject(jsonString);       	
				//convObj is null when an unknown search item was entered in second text box
				//If true, return empty hash table
				if(convObj != null) {
					Iterator<String> iter= convObj.keys();
					String currKey;
					String currVal;
					while(iter.hasNext()){
						//Parse jsonString and fill our hash from it, then connect it to our spinner
						currKey = (String) iter.next();
						currVal = convObj.get(currKey).toString();  
						resultHash.put(currKey, currVal);
					}
				}
			} catch (Exception E){
				Log.e("MobileMetagenomics", "JSON to Hash failed: " + E);
				statusOk = false;
			}
			return resultHash;
		}
		else{
			// Pop up a toast or something.
			return null;
		}
	}

	private Hashtable doInitialAsynchWork(String resString){
		Hashtable tmpHash = JSONToHash(resString);
		if(tmpHash != null){
			url = (String) tmpHash.get("url");
			String tmpMax = (String) tmpHash.get("max");
			if(url != null && tmpMax != null){
				max = Integer.parseInt(tmpMax);
				PROGRESS_MODIFIER = 10000 / max;
				loadInitialResults(JSONToHash((makeWebRequest((String) url + 1))));
			}
		}
		return tmpHash;
	}

	public void loadInitialResults(Hashtable<String,String> newData){
		if(statusOk){
			Object thisElem;
			ArrayList<String> helperList = new ArrayList<String>();
			for (Enumeration<String> e = newData.keys(); e.hasMoreElements();) {
				thisElem = e.nextElement();
				helperList.add(((String) thisElem) + " value: " + ((String) newData.get(thisElem)));
			}
			resultsArr = helperList.toArray();
			Arrays.sort(resultsArr);
		}
	}

	public void addToResults(Hashtable<String,String> newData){
		if(statusOk){
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
				tmp[i++] = ((String) thisElem) + " value: " + ((String) newData.get(thisElem));
			}
			resultsArr = tmp;
			Arrays.sort(resultsArr);
		}
	}

	private String doFileUpload(String ourFile, int level, int stringency, int kmer, int maxGap){
		if(statusOk){
			final String existingFileName = ourFile;   	  
			final String lineEnd = "\r\n";
			final String twoHyphens = "--";
			final String boundary =  "*****";
			final int maxBufferSize = 1*1024*1024;
			final String urlString = "http://bioseed.mcs.anl.gov/~redwards/FIG/RTMg_cellphone.cgi";
			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			DataInputStream inStream = null;
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			String responseFromServer = "";
			try
			{
				//------------------ CLIENT REQUEST
				FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );
				URL url = new URL(urlString);
				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");  	 
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
				// Set up a data output stream to write to the web
				dos = new DataOutputStream( conn.getOutputStream() );
				dos.writeBytes(twoHyphens + boundary + lineEnd +
						"Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" 
						+ existingFileName +"\"" + lineEnd
						+ "Content-Type: text/plain" + lineEnd + lineEnd);    	   
				Log.e("UploadFile","Headers are written");
				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];
				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);  	   
				while (bytesRead > 0)
				{
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd + lineEnd 
						+ twoHyphens + boundary + lineEnd 
						+ "Content-Disposition: form-data; name=\"stringency\"" + lineEnd + lineEnd
						+ stringency + lineEnd
						+ twoHyphens + boundary + lineEnd
						+ "Content-Disposition: form-data; name=\"level\"" + lineEnd + lineEnd
						+ level + lineEnd
						+ twoHyphens + boundary + lineEnd
						+ "Content-Disposition: form-data; name=\"kmer\"" + lineEnd + lineEnd
						+ kmer + lineEnd
						+ twoHyphens + boundary + lineEnd
						+ "Content-Disposition: form-data; name=\"maxGap\"" + lineEnd + lineEnd
						+ maxGap + lineEnd
						+ twoHyphens + boundary + lineEnd
						+ "Content-Disposition: form-data; name=\"submit\"" + lineEnd + lineEnd 
						+ "Upload" + lineEnd
						+ twoHyphens + boundary + twoHyphens + lineEnd
				);
				// close streams
				Log.e("UploadFile","File is written");
				fileInputStream.close();
				dos.flush();
				dos.close();
			}
			catch (MalformedURLException ex)
			{
				statusOk = false;
				Log.e("UploadFile", "error: " + ex.getMessage(), ex);
			}
			catch (IOException ioe)
			{
				statusOk = false;
				Log.e("UploadFile", "error: " + ioe.getMessage(), ioe);
			}
			//------------------ read the SERVER RESPONSE
			try {
				inStream = new DataInputStream ( conn.getInputStream() );
				String str;   	       
				while (( str = inStream.readLine()) != null)
				{
					//TODO: We can verify success/failure here, just need to know what to expect from server!
					responseFromServer += str;
					Log.e("UploadFile","Server Response"+str);
				}
				inStream.close();
			}
			catch (IOException ioex){
				statusOk = false;
				Log.e("UploadFile", "error: " + ioex.getMessage(), ioex);
			}
			return responseFromServer;
		}
		else{
			//pop up a toast or something
			return null;
		}
	}

	private String doJsonUpload(String phoneNumber, String fileName, String jsonObject){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes_josh.cgi");
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");  	 
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			// Set up a data output stream to write to the web
			dos = new DataOutputStream( conn.getOutputStream() );
			dos.writeBytes(twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumber + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					fileName + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd			
					+ jsonObject +	lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"put\"" + lineEnd + lineEnd +
					"Save this JSON Object" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("UploadFile","JSON is written");
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex)
		{
			statusOk = false;
			Log.e("UploadFile", "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe)
		{
			statusOk = false;
			Log.e("UploadFile", "error: " + ioe.getMessage(), ioe);
		}
		//------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
			String str;   	       
			while (( str = inStream.readLine()) != null)
			{
				//TODO: We can verify success/failure here, just need to know what to expect from server!
				responseFromServer += str;
				Log.e("UploadFile","Server Response"+str);
			}
			inStream.close();
		}
		catch (IOException ioex){
			statusOk = false;
			Log.e("UploadFile", "error: " + ioex.getMessage(), ioex);
		}
		return responseFromServer;
	}

	private String doJsonQuery1(double phoneNumberForQuery2, int sampleNumber){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes_josh.cgi");
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");  	 
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			// Set up a data output stream to write to the web
			dos = new DataOutputStream( conn.getOutputStream() );
			dos.writeBytes(twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumberForQuery2 + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					sampleNumber + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"get\"" + lineEnd + lineEnd +
					"Give me this JSON Object" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("GetJSON","JSON reqest sent");
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex)
		{
			statusOk = false;
			Log.e("GetJSON1", "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe)
		{
			statusOk = false;
			Log.e("GetJSON1", "error: " + ioe.getMessage(), ioe);
		}
		//------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
			String str;   	       
			while (( str = inStream.readLine()) != null)
			{
				//TODO: We can verify success/failure here, just need to know what to expect from server!
				responseFromServer += str;
				Log.e("UploadFile","Server Response"+str);
			}
			inStream.close();
		}
		catch (IOException ioex){
			statusOk = false;
			Log.e("GetJSON1", "error: " + ioex.getMessage(), ioex);
		}
		return responseFromServer;
	}
	
	private String doJsonQuery2(double phoneNumberForQuery2, String sampleTitle){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes_josh.cgi");
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");  	 
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			// Set up a data output stream to write to the web
			dos = new DataOutputStream( conn.getOutputStream() );
			dos.writeBytes(twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumberForQuery2 + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					sampleTitle + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"get\"" + lineEnd + lineEnd +
					"Give me this JSON Object" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("GetJSON","JSON reqest sent");
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex)
		{
			statusOk = false;
			Log.e("GetJSON2", "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe)
		{
			statusOk = false;
			Log.e("GetJSON2", "error: " + ioe.getMessage(), ioe);
		}
		//------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
			String str;   	       
			while (( str = inStream.readLine()) != null)
			{
				//TODO: We can verify success/failure here, just need to know what to expect from server!
				responseFromServer += str;
				Log.e("UploadFile","Server Response"+str);
			}
			inStream.close();
		}
		catch (IOException ioex){
			statusOk = false;
			Log.e("GetJSON2", "error: " + ioex.getMessage(), ioex);
		}
		return responseFromServer;
	}
	
	private class SaveResults extends AsyncTask<String, Integer, Integer> {
		@Override
		protected void onPreExecute(){
			showDialog(ID_DIALOG_SAVE);
		}   	
		@Override
		protected Integer doInBackground(String... params) {
			try {
				FileOutputStream fos = new FileOutputStream(new File("/sdcard/" + fileName + ".json"));
				JSONObject tmpJo = new JSONObject();
				for(int i=0; i<resultsArr.length; i++){
					tmpJo.put("" + i, resultsArr[i]);
				}
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				osw.write(tmpJo.toString());
				return 1;
			}
			catch (Throwable e) {
				Log.e("SaveRes","exception thrown: " + e.toString());
				System.err.println("exception thrown: " + e.toString());
				statusOk = false;
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
		protected void onPostExecute(Integer value) {
			if(value == 1){
				dismissDialog(ID_DIALOG_SAVE);
			}
			if(value == -1){
				dismissDialog(ID_DIALOG_SAVE);
				// TODO: popup toast that says save failed.
			}
		}
	}	

	private class LoadResults extends AsyncTask<String, Integer, Integer> {
		@Override
		protected void onPreExecute(){
			showDialog(ID_DIALOG_LOAD);
		}	
		@Override
		protected Integer doInBackground(String... params) {
			String[] tmpStringArr = params[0].split("/sdcard/");
			//TODO: this may be unneeded, look at what happens when a normal (non-loaded) result is done.
			if(tmpStringArr.length > 1)
				tmpStringArr = tmpStringArr[1].split(".mmr");
			else
				tmpStringArr = tmpStringArr[0].split(".mmr");
			fileName = tmpStringArr[0];
			try {
				FileInputStream fis = new FileInputStream(new File(params[0]));
				ObjectInputStream ois =
					new ObjectInputStream(fis);
				resultsArr = (Object[])ois.readObject();
				fis.close();
				return 1;
			}
			catch (Throwable e) {
				System.err.println("exception thrown from LoadResults doInBackground");
				statusOk = false;
				return -1;
				// TODO: Pop up a toast or something
			}
		}    	
		@Override
		protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				dismissDialog(ID_DIALOG_LOAD);
			}
			if(values[0] == -1){
				dismissDialog(ID_DIALOG_LOAD);
				// TODO: popup toast that says save failed.
			}
		}
		@Override
		protected void onPostExecute(Integer value) {
			if(value == 1){
				resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, resultsArr));
				dismissDialog(ID_DIALOG_LOAD);
			}
			if(value == -1){
				dismissDialog(ID_DIALOG_LOAD);
				// TODO: popup toast that says save failed.
			}
		}
	}

	private class shareResults extends AsyncTask<String, Integer, Integer> {
		@Override
		protected void onPreExecute(){
			showDialog(ID_DIALOG_SHARE);
		}
		@Override
		protected Integer doInBackground(String... params) {
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
					statusOk = false;
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
					String tmpFileName = fileName;					
					// TODO: can check success/failure here, just need to examine what the server does on failure!
					doJsonUpload(tmpLineNumber, tmpFileName, tmpString);
					return 1;
				}
				catch (Throwable e) {
					Log.e("shareResults","exception thrown: " + e.toString());
					System.err.println("exception thrown: " + e.toString());
					statusOk = false;
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
		protected void onPostExecute(Integer value) {
			// TODO: Conclude progress dialogues etc...
			if(value == 1){
				dismissDialog(ID_DIALOG_SHARE);
			}
		}
	}	
	
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
			statusOk = false;
		}
	}
}
