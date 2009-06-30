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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

public class MobileMetagenomics extends Activity implements Runnable{
	static final int GONE = 0x00000008;
	static final int  VISIBLE = 0x00000000;
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
        		fileName.setVisibility(GONE);
        		stringencySpinner.setVisibility(GONE);
        		levelSpinner.setVisibility(GONE);
        		uploadButton.setVisibility(GONE);
        		browseButton.setVisibility(GONE);
    			inputManager.hideSoftInputFromWindow(fileName.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    			pd = ProgressDialog.show(MobileMetagenomics.this, "Performing Sequencing...", "Please wait (this may take a few moments)", true, false);
    			Thread thread = new Thread(MobileMetagenomics.this);
    			thread.start();
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

    private void getResults(String resString){
    	Hashtable tmpHash = JSONToHash(resString);
    	//for(int i=1; i<Integer.parseInt((String) tmpHash.get("max")); i++){
    		//displayResult(makeWebRequest((String) tmpHash.get("url") + i));
    	//}
		displayResult(makeWebRequest((String) tmpHash.get("url") + 1));
    }
    
    public void displayResult(String s){
    	Log.e("MobileMetagenomics", s);
    	//Hashtable resHash = JSONToHash(s);
    	displayHash(JSONToHash(s));
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
    
    public void displayHash(Hashtable<String,String> myHash){
    	ArrayList<String> myList = new ArrayList<String>();
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
    
//HERE'S WHAT YOU'RE LOOKIN FOR...
//http://bioseed.mcs.anl.gov/~redwards/FIG/RTMg_cellphone.cgi?annotate=1&stringency=1&file=13910&level=0&number=
//for(int i=1; i<max; i++)
    //number = i
//fill in LEVEL with int-ified level spinner
    //fill in stringency with stringency spinner
    //TODO: change this method based on whatever Rob's cgi form generates. We want to match that!
    private void doFileUpload(String ourFile, int level, int stringency){
	  	  final String existingFileName = "/sdcard/" + ourFile;   	  
    	  final String lineEnd = "\r\n";
    	  final String twoHyphens = "--";
    	  final String boundary =  "*****";
    	  final int maxBufferSize = 1*1024*1024;
    	  //final String urlString = "http://edwards.sdsu.edu/~redwards/cgi-bin/josh_upload.cgi";
    	  //final String urlString = "http://bioseed.mcs.anl.gov/~redwards/FIG/RTMg_josh.cgi";
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
    	        getResults(responseFromServer);
    	  }
    	  catch (IOException ioex){
    	       Log.e("UploadFile", "error: " + ioex.getMessage(), ioex);
    	  }
    	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		doFileUpload(fileName.getText().toString(),
				levelSpinner.getSelectedItemPosition(),
				stringencySpinner.getSelectedItemPosition());
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            resultListView.setAdapter(new ArrayAdapter(MobileMetagenomics.this, android.R.layout.simple_list_item_1, keyArr));
        	pd.dismiss();
        }
    };
}