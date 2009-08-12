package edwardslab.util;

//References: http://www.glenmccl.com/tip_030.htm for serializable code.

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ResultView extends Activity{

	private static final int SHARE_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	private static final int LOAD_ID = Menu.FIRST + 2;
	private static boolean statusOk = true;
	static final int  ID_DIALOG_ANNOTATE = 0;
	static final int  ID_DIALOG_LOAD=1;
	static final int  ID_DIALOG_SAVE=2;
	static final int  ID_DIALOG_SHARE=3;
	String fileName;
	String shareMode;
	int stringency = -1;
	int level = -1;
	Object[] keyArr;
	ListView resultListView;
	ArrayList<String> myList;
	int max;
	int downloadIterationValue;
	String url;
    private ProgressDialog pd;
    Thread setupInitialResult;
	Thread downloadRemainingResults;
    TextView mDisplay;
    ProgressBar mBar = null;
	int PROGRESS_MODIFIER;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
        requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.resultview);
        setProgressBarVisibility(false);
        mBar = (ProgressBar) findViewById(R.id.placeholder);
        setSecondaryProgress(0);
		resultListView = (ListView)findViewById(R.id.ResultsListView);
		mDisplay = (TextView)findViewById(R.id.display);
        mBar.setVisibility(ProgressBar.GONE);
		
		Bundle extras = getIntent().getExtras();
		if(extras.containsKey(MobileMetagenomics.LOAD_FILE_NAME)){
			new LoadResults().execute(extras.getString(MobileMetagenomics.LOAD_FILE_NAME));
		}
		else{
		fileName = extras.getString(MobileMetagenomics.FILE_NAME);
		level = extras.getInt(MobileMetagenomics.LEVEL);
		stringency = extras.getInt(MobileMetagenomics.STRINGENCY);
		
			if((fileName == null) || (level == -1) || (stringency == -1) || (fileName.equals(""))){
				 Toast.makeText(this, "Invalid parameters, please try again.", Toast.LENGTH_LONG).show();
				 finish();
			}
			else{
				new DownloadResults().execute("String");
			}	
		}
	}

	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt("max", max);
		savedInstanceState.putString("url", url);
	}
	
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
	}
	
    private void setupAsync(String resString){
    	if(statusOk){
	    	Hashtable tmpHash = JSONToHash(resString);
	    	if(tmpHash != null){
		    	url = (String) tmpHash.get("url");
		    	String tmpMax = (String) tmpHash.get("max");
	    		if(url != null && tmpMax != null){
	    	    	max = Integer.parseInt(tmpMax);
	    			PROGRESS_MODIFIER = 10000 / max;
	    	        mBar.setMax(max);
	    	    	ArrayList<String> myList = new ArrayList<String>();
	    	    	loadList(JSONToHash((makeWebRequest((String) url + 1))), myList);
	    		}
	    		else{
	    			statusOk = false;
	    		}
	    	}
	    	else{
	    		statusOk = false;
	    	}
    	}
    }

    
    public Hashtable<String,String> JSONToHash(String myString){
    	if(statusOk){
			//This is a more general parse method (and perhaps I should reconsider the names), which we can hopefully re-use.
			Hashtable<String,String> myHash = new Hashtable<String,String>();
			try{// Take the stringified JSON Hash of Hashes and put it into our Hash
	        	JSONObject myObj = new JSONObject(myString);       	
	        	//myObj is null when an unknown search item was entered in second text box
	        	//If true, return empty hash table
	        	if(myObj != null) {
	        		Iterator<String> iter= myObj.keys();
	        		String myKey;
	        		String myVal;
	        		while(iter.hasNext()){
	        			//Parse myString and fill our hash from it, then connect it to our spinner
	        			myKey = (String) iter.next();
	        			myVal = myObj.get(myKey).toString();  
	        			myHash.put(myKey, myVal);
	        		}
	        	}
	        } catch (Exception E){
	        	Log.e("MobileMetagenomics", "JSON to Hash failed: " + E);
	        	statusOk = false;
	        }
	        return myHash;
    	}
    	else{
    		// Pop up a toast or something.
    		return null;
    	}
	}
    
    public void loadList(Hashtable<String,String> myHash, ArrayList<String> myList){
    	if(statusOk){
	    	Object thisElem;
	    	for (Enumeration<String> e = myHash.keys(); e.hasMoreElements();) {
	    		thisElem = e.nextElement();
	            myList.add(((String) thisElem) + " value: " + ((String) myHash.get(thisElem)));
	        }
	        keyArr = myList.toArray();
	        Arrays.sort(keyArr);
    	}
    }
    
    public void addToList(Hashtable<String,String> myHash, ArrayList<String> myList){
    	if(statusOk){
	    	Object thisElem;
	    	Object[] tmp = new Object[keyArr.length + myHash.size()];
	    	int i=keyArr.length;
	    	for(int j=0; j<keyArr.length; j++){
	    		tmp[j]=keyArr[j];
	    	}
	    	int q = 4;
	    	for (Enumeration<String> e = myHash.keys(); e.hasMoreElements();) {
	    		thisElem = e.nextElement();
	            tmp[i++] = ((String) thisElem) + " value: " + ((String) myHash.get(thisElem));
	        }
	        keyArr = tmp;
	        Arrays.sort(keyArr);
    	}
    }
    
    public String makeWebRequest(String s){
    	if(statusOk){
		/* Will be filled and displayed later. */
        String myString = null;
        try {
             /* Define the URL we want to load data from. */
             URL myURL = new URL(s);
             /* Open a connection to that URL. */
             URLConnection ucon = myURL.openConnection();

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
             myString = new String(baf.toByteArray());
        } catch (Exception e) {
             /* On any Error we want to display it. */
        	statusOk = false;
             myString = e.getMessage();
        }
        return myString;
    	}
    	else{
    		//pop up a toast or something
    		return null;
    	}
	}

    private String doFileUpload(String ourFile, int level, int stringency){
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
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SHARE_ID, 0, R.string.share).setIcon(android.R.drawable.ic_menu_share);
        menu.add(0, SAVE_ID, 0, R.string.save).setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, LOAD_ID, 0, R.string.load).setIcon(android.R.drawable.ic_menu_set_as);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SHARE_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(ResultView.this);
			builder.setMessage("Share as plain text or Android-viewable format?")
			       .setCancelable(false)
			       .setPositiveButton(".txt", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                shareMode = "txt";
			                new shareResults().execute("String");
			           }
			       })
			       .setNegativeButton(".mmr", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                shareMode = "mmr";
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
	
	private class SaveResults extends AsyncTask<String, Integer, Integer> {
		@Override
    	protected void onPreExecute(){
			showDialog(ID_DIALOG_SAVE);
			}   	
		@Override
		protected Integer doInBackground(String... params) {
			try {
		    	  FileOutputStream fos = new FileOutputStream(new File("/sdcard/" + fileName + ".mmr"));
	             ObjectOutputStream oos =
	                 new ObjectOutputStream(fos);
	             oos.writeObject(keyArr);
	             oos.flush();
	             fos.close();
	    	     return 1;
	     }
	     catch (Throwable e) {
	             System.err.println("exception thrown");
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
			try {
		    	  FileInputStream fis = new FileInputStream(new File(params[0]));
		            ObjectInputStream ois =
		                new ObjectInputStream(fis);
		            keyArr = (Object[])ois.readObject();
		            fis.close();
		            return 1;
		    }
		    catch (Throwable e) {
		            System.err.println("exception thrown");
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
	        	resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, keyArr));
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
			try {
		    	  FileOutputStream fos = new FileOutputStream(saveFile);
	             ObjectOutputStream oos =
	                 new ObjectOutputStream(fos);
	             oos.writeObject(keyArr);
	             oos.flush();
	             fos.close();
	             publishProgress(1);
	     }
	     catch (Throwable e) {
	             System.err.println("exception thrown");
	             statusOk = false;
	             return -1;
	     }}
			else{
				writeFileOut();
				saveFile = new File("/sdcard/" + fileName + ".txt");
			}
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_TEXT, "Attached are the results of the Mobile Metagenomics app on " + fileName);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Annotation Results: " + fileName);
			emailIntent.setType("message/rfc822");
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(saveFile));
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
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
        }
	}	
	
    private class DownloadResults extends AsyncTask<String, Integer, Integer> {
    	@Override
    	protected void onPreExecute(){
			showDialog(ID_DIALOG_ANNOTATE);
    	}
    	
		@Override
		protected Integer doInBackground(String... params) {
			Integer status = 0;
			setupAsync(doFileUpload(fileName.toString(),
        			level,
        			stringency));
			if(statusOk){
				status++;
				publishProgress(status);			
				//Do remaining blocks.
				for(int i=2; i<=max; i++){
	            	addToList(JSONToHash((makeWebRequest((String) url + i))), myList);
	            	status++;
	            	downloadIterationValue = i;
	            	publishProgress(status);
	        	}
			}
			else{
				dismissDialog(ID_DIALOG_ANNOTATE);
				//publish an error!
				return(-1);
			}
			return 1;
		}
    	
		@Override
        protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				dismissDialog(ID_DIALOG_ANNOTATE);
			}
			mBar.setProgress(values[0]);
            setProgress(PROGRESS_MODIFIER * mBar.getProgress());
            setTitle("Downloading segments: " + values[0] + "/" + max);
        	 resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, keyArr));
        }

		@Override
        protected void onPostExecute(Integer value) {
			if(value == 1){
				setProgress(10000);
			}
			else if(value == -1){
	    		Toast.makeText(ResultView.this, "Error: Server down or incorrect filetype", Toast.LENGTH_LONG).show();
	    		finish();
			}
        }

    }
    
    public void writeFileOut(){
    	try{
    	//DataOutputStream dos = new DataOutputStream(
    					//new BufferedOutputStream(
    		 OutputStreamWriter osw = new OutputStreamWriter(	new FileOutputStream(
    					new File("/sdcard/" + fileName + ".txt")));
        for(int i=0; i<keyArr.length; i++){
        	osw.write((String) keyArr[i] + "\n\r");
        	//osw.write();
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
	