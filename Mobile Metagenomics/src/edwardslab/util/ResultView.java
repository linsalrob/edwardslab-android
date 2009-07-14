package edwardslab.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ResultView extends Activity{

	private static final int SHARE_ID = Menu.FIRST;
	private static final int SAVE_ID = Menu.FIRST + 1;
	String fileName;
	int stringency;
	int level;
	Object[] keyArr;
	Object[] valArr;
	ListView resultListView;
    private ProgressDialog pd;
    Thread setupInitialResult;
	Thread downloadRemainingResults;
    
	Handler initialThreadHandler = new Handler()
    {
            @Override public void handleMessage(Message msg)
            {
            	resultListView.setAdapter(new ArrayAdapter(ResultView.this, android.R.layout.simple_list_item_1, keyArr));
            	pd.dismiss();
                setupInitialResult.stop();
                downloadRemainingResults.start();
            }
    };

    Handler remainingThreadHandler = new Handler()
    {
            @Override public void handleMessage(Message msg)
            {
            	downloadRemainingResults.stop();
            }
    };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		resultListView = (ListView)findViewById(R.id.ResultListView);
	    setupInitialResult = new Thread(new Runnable()
	    {
	        // Setup the run() method that is called when the background thread
	        // is started.
	        public void run()
	        {
	        	setupAsync(doFileUpload(fileName.toString(),
	        			level,
	        			stringency));
	                // Send the handler message to the UI thread.
	                initialThreadHandler.sendEmptyMessage(0);

	        }
	    });
	    
	    downloadRemainingResults = new Thread(new Runnable()
	    {
	        // Setup the run() method that is called when the background thread
	        // is started.
	        public void run()
	        {
	        	/*
	        	for(int i=2; i<Integer.parseInt((String) tmpHash.get("max")); i++){
	            	loadList(JSONToHash((makeWebRequest((String) tmpHash.get("url") + i))), myList);
	        	}*/
	                // Send the handler message to the UI thread.
	                remainingThreadHandler.sendEmptyMessage(0);

	        }
	    });
	    
		Bundle extras = getIntent().getExtras();
		fileName = extras.getString(MobileMetagenomics.FILE_NAME);
		level = extras.getInt(MobileMetagenomics.LEVEL);
		stringency = extras.getInt(MobileMetagenomics.STRINGENCY);
		pd = ProgressDialog.show(ResultView.this, "Performing Annotation...", "Please wait (this may take a few moments)", true, false);
		setupInitialResult.start();
	}

    private void setupAsync(String resString){
    	Hashtable tmpHash = JSONToHash(resString);
    	ArrayList<String> myList = new ArrayList<String>();
    	loadList(JSONToHash((makeWebRequest((String) tmpHash.get("url") + 1))), myList);
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
        //Collections.sort(myList);
        /*valArr = myList.toArray();
        for(int i=0; i<keyArr.length; i++){
        	valArr[i]=(String)myHash.get(keyArr[i]);
        }*/
       // resultListView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, keyArr));
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
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SHARE_ID:
            //doShare();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
	}
	
}