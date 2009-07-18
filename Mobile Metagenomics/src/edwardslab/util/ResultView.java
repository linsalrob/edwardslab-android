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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ResultView extends Activity{

	private static final int SHARE_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	private static final int LOAD_ID = Menu.FIRST + 2;
	String fileName;
	int stringency;
	int level;
	Object[] keyArr;
	ListView resultListView;
	ArrayList<String> myList;
	int max;
	String url;
    private ProgressDialog pd;
    Thread setupInitialResult;
	Thread downloadRemainingResults;
    TextView mDisplay;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.resultview);
		resultListView = (ListView)findViewById(R.id.ResultsListView);
		mDisplay = (TextView)findViewById(R.id.display);
	    
		Bundle extras = getIntent().getExtras();
		if(extras.containsKey(MobileMetagenomics.LOAD_FILE_NAME)){
			new LoadResults().execute(extras.getString(MobileMetagenomics.LOAD_FILE_NAME));
		}
		else{
		fileName = extras.getString(MobileMetagenomics.FILE_NAME);
		level = extras.getInt(MobileMetagenomics.LEVEL);
		stringency = extras.getInt(MobileMetagenomics.STRINGENCY);
		new DownloadResults().execute("String");
		}
		
	}

    private void setupAsync(String resString){
    	Hashtable tmpHash = JSONToHash(resString);
    	url = (String) tmpHash.get("url");
    	max = Integer.parseInt((String) tmpHash.get("max"));
    	ArrayList<String> myList = new ArrayList<String>();
    	loadList(JSONToHash((makeWebRequest((String) url + 1))), myList);
    }

    
    public Hashtable<String,String> JSONToHash(String myString){
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
        }
        return myHash;
	}
    
    public void loadList(Hashtable<String,String> myHash, ArrayList<String> myList){
    	Object thisElem;
    	for (Enumeration<String> e = myHash.keys(); e.hasMoreElements();) {
    		thisElem = e.nextElement();
            myList.add(((String) thisElem) + " value: " + ((String) myHash.get(thisElem)));
        }
        keyArr = myList.toArray();
        Arrays.sort(keyArr);
    }
    
    public void addToList(Hashtable<String,String> myHash, ArrayList<String> myList){
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
    
    public String makeWebRequest(String s){
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
             myString = e.getMessage();
        }
        return myString;
	}

    private String doFileUpload(String ourFile, int level, int stringency){
	  	  final String existingFileName = "/sdcard/" + ourFile;   	  
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
  		    Log.e("UploadFile", "error: " + ex.getMessage(), ex);
    	  }
    	  catch (IOException ioe)
    	  {
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
    	       Log.e("UploadFile", "error: " + ioex.getMessage(), ioex);
    	  }
	      return responseFromServer;
    	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SHARE_ID, 0, R.string.share);
        menu.add(0, SAVE_ID, 0, R.string.save);
        menu.add(0, LOAD_ID, 0, R.string.load);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SHARE_ID:
            //doShare();
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
	
	
	private class SaveResults extends AsyncTask<String, Integer, Integer> {
		@Override
    	protected void onPreExecute(){
    		pd = ProgressDialog.show(ResultView.this, "Saving Results...", "Please wait (this may take a few moments)", true, false);
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
	             return -1;
	     }
		}   	
		@Override
        protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				pd.dismiss();
			}
			if(values[0] == -1){
				pd.dismiss();
				// TODO: popup toast that says save failed.
			}
        }
		@Override
        protected void onPostExecute(Integer value) {
			if(value == 1){
				pd.dismiss();
			}
			if(value == -1){
				pd.dismiss();
				// TODO: popup toast that says save failed.
			}
        }
	}	
	
	private class LoadResults extends AsyncTask<String, Integer, Integer> {
		@Override
    	protected void onPreExecute(){
    		pd = ProgressDialog.show(ResultView.this, "Loading Results...", "Please wait (this may take a few moments)", true, false);
    	}	
		@Override
		protected Integer doInBackground(String... params) {
			try {
		    	  FileInputStream fis = new FileInputStream(new File("/sdcard/" + params[0]));
		            ObjectInputStream ois =
		                new ObjectInputStream(fis);
		            keyArr = (Object[])ois.readObject();
		            fis.close();
		            return 1;
		    }
		    catch (Throwable e) {
		            System.err.println("exception thrown");
		            return -1;
		             // TODO: Pop up a toast or something
		    }
		}    	
		@Override
        protected void onProgressUpdate(Integer... values) {
			if(values[0] == 1){
				pd.dismiss();
			}
			if(values[0] == -1){
				pd.dismiss();
				// TODO: popup toast that says save failed.
			}
        }
		@Override
        protected void onPostExecute(Integer value) {
			if(value == 1){
	        	resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, keyArr));
				pd.dismiss();
			}
			if(value == -1){
				pd.dismiss();
				// TODO: popup toast that says save failed.
			}
        }
	}
	
	public void shareResults(){
		
	}
	
    private class DownloadResults extends AsyncTask<String, Integer, Integer> {
    	@Override
    	protected void onPreExecute(){
    		// TODO: SETUP PB
    		pd = ProgressDialog.show(ResultView.this, "Performing Annotation...", "Please wait (this may take a few moments)", true, false);
    	}
    	
		@Override
		protected Integer doInBackground(String... params) {
			// TODO: do meaningful work and update
			Integer status = 0;
			setupAsync(doFileUpload(fileName.toString(),
        			level,
        			stringency));
			status++;
			publishProgress(status);			
			//Do remaining blocks.
			for(int i=2; i<=max; i++){
            	addToList(JSONToHash((makeWebRequest((String) url + i))), myList);
            	status++;
            	publishProgress(status);
        	}
			return 1;
		}
    	
		@Override
        protected void onProgressUpdate(Integer... values) {
			// TODO: update PB
			if(values[0] == 1){
				pd.dismiss();
			}
			mDisplay.setText("Currently viewing pages 1 through " + values[0]);
        	resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, keyArr));
        }

		@Override
        protected void onPostExecute(Integer value) {
            // TODO: Conclude progress dialogues etc...
        }

    }
	
}