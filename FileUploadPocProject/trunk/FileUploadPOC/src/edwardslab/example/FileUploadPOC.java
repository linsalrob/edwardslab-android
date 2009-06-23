package edwardslab.example;
//Proof of Concept by Josh Hoffman
//Written for Dr. Rob Edwards' Bioinformatics Lab
//San Diego State University
//June 18, 2009
//References: www.anddev.org
//Used for file upload code, and file creation code

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FileUploadPOC extends Activity implements Runnable {
    /** Called when the activity is first created. */
    private String createdFile;
	private ProgressDialog pd;
	private boolean useSdCard = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final EditText file = (EditText) findViewById(R.id.fileName);
        final EditText content = (EditText) findViewById(R.id.fileContent);
        final Button button1 = (Button)findViewById(R.id.confirmButton);
        final Button button2 = (Button)findViewById(R.id.uploadButton);
    	final InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
    	final Toast toast = new Toast(this);
    	final TextView tv = new TextView(this);
    	
        button1.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
    			inputManager.hideSoftInputFromWindow(content.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS); 
        		//Attempt to write the specified file
        		if(writeToFile(file.getText().toString(),content.getText().toString())){
        			tv.setText("File output succeeded!");
        			createdFile = file.getText().toString();
        		}
        		else{
        			tv.setText("File output failed!");
        		}
        	    toast.setView(tv);
        	    toast.setDuration(Toast.LENGTH_LONG);
        	    toast.show();
        	}
        });
        button2.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        		//Attempt to upload file
        		if(createdFile!=null){
        			pd = ProgressDialog.show(FileUploadPOC.this, "Performing Upload...", "Please wait (this shouldn't take long)", true, false);
        			Thread thread = new Thread(FileUploadPOC.this);
        			thread.start();
            		tv.setText("Attempted file upload...");
        		}
        		else{
        			tv.setText("Cannot perform upload, please create a file first!");
        		}
        	}
        });
    }
    
    public boolean writeToFile(String fileName, String userInput){
    	try { // catches IOException below
	        //final String TESTSTRING = new String("Hello Android");
	        
	        // ##### Write a file to the disk #####
	        /* We have to use the openFileOutput()-method
	         * the ActivityContext provides, to
	         * protect your file from others and
	         * This is done for security-reasons.
	         * We chose MODE_WORLD_READABLE, because
	         *  we have nothing to hide in our file */    
    		
    		if(useSdCard){
	    		File root = Environment.getExternalStorageDirectory();
	    	    if (root.canWrite()){
	    	        File myFile = new File(root, fileName);
	    	        FileWriter myWriter = new FileWriter(myFile);
	    	        BufferedWriter out = new BufferedWriter(myWriter);
	    	        out.write(userInput);
	    	        out.close();
	    	    }
    		}
    		else{
	    		// USE THIS FOR NORMAL FILE SYSTEM
		        FileOutputStream fOut = openFileOutput(fileName,
		                                 MODE_WORLD_READABLE);
		        OutputStreamWriter osw = new OutputStreamWriter(fOut); 
		
		        // Write the string to the file
		        osw.write(userInput);
		        // ensure that everything is
		        // really written out and close
		        osw.flush();
		        osw.close();
    		}
	        return true;
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
    		return false;
    	}
    }
    
    private void doFileUpload(){

  	  HttpURLConnection conn = null;
  	  DataOutputStream dos = null;
  	  DataInputStream inStream = null;
  	  String existingFileName;
  	 
  	  //TODO: change this to edwardslab.util in the production version!
  	  if(useSdCard){
  	  	  existingFileName = "/sdcard/" + createdFile;
  	  }
  	  else{
  		  existingFileName = "/data/data/edwardslab.example/files/" + createdFile;
  	  }
	  //String existingFileName = "/data/data/edwardslab.example/files/settings.dat";
  	  
  	  String lineEnd = "\r\n";
  	  String twoHyphens = "--";
  	  String boundary =  "*****";


  	  int bytesRead, bytesAvailable, bufferSize;

  	  byte[] buffer;

  	  int maxBufferSize = 1*1024*1024;

  	  String responseFromServer = "";

  	  String urlString = "http://edwards.sdsu.edu/~redwards/cgi-bin/josh_upload.cgi";
  	  //String urlString = "http://octopussy.sdsu.edu/uploader.php";
  	  //String urlString = "http://localhost/uploader.php";
  	  
  	  try
  	  {
  	   //------------------ CLIENT REQUEST

  	  Log.e("MediaPlayer","Inside second Method");

  	  FileInputStream fileInputStream = new FileInputStream(new File(existingFileName) );


  	  URL url = new URL(urlString);
  	   String test = "test";

  	   // Open a HTTP connection to the URL

  	   conn = (HttpURLConnection) url.openConnection();

  	   // Allow Inputs
  	   conn.setDoInput(true);

  	   // Allow Outputs
  	   conn.setDoOutput(true);

  	   // Don't use a cached copy.
  	   conn.setUseCaches(false);

  	   // Use a post method.
  	   conn.setRequestMethod("POST");

  	   conn.setRequestProperty("Connection", "Keep-Alive");
  	 
  	   conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

  	   dos = new DataOutputStream( conn.getOutputStream() );

  	   dos.writeBytes(twoHyphens + boundary + lineEnd);
  	   dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + existingFileName +"\"" + lineEnd);
  	   //dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + createdFile +"\"" + lineEnd);
  	   dos.writeBytes("Content-Type: text/plain" + lineEnd);
  	   dos.writeBytes(lineEnd);
  	   
  	   //dos.writeBytes("BEGIN-");
  	   Log.e("MediaPlayer","Headers are written");

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
  	   //dos.writeBytes("-END");
  	   // send multipart form data necesssary after file data...

  	   dos.writeBytes(lineEnd + lineEnd);
  	   dos.writeBytes(twoHyphens + boundary + lineEnd);
  	   dos.writeBytes("Content-Disposition: form-data; name=\"Upload\"");
  	   dos.writeBytes(lineEnd + lineEnd);
  	   dos.writeBytes("Upload" + lineEnd);
	   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
  	   // close streams
  	   Log.e("MediaPlayer","File is written");
  	   fileInputStream.close();
  	   Log.e("Not MediaPlayer","FIS is closed!");
  	   dos.flush();
  	   dos.close();


  	  }
  	  catch (MalformedURLException ex)
  	  {
		    Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
  	  }

  	  catch (IOException ioe)
  	  {
		    Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
  	  }


  	  //------------------ read the SERVER RESPONSE


  	  try {
  	        inStream = new DataInputStream ( conn.getInputStream() );
  	        String str;
  	       
  	        while (( str = inStream.readLine()) != null)
  	        {
  	        	//TODO: We can verify success/failure here, just need to know what to expect from server!
  	             Log.e("MediaPlayer","Server Response"+str);
  	        }
  	        inStream.close();

  	  }
  	  catch (IOException ioex){
  	       Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
  	  }

  	}
    
    @Override
	public void run() {
		//Try to upload file....
		doFileUpload();
		handler.sendEmptyMessage(0);
	}   
	
	private Handler handler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            pd.dismiss();
	        }
	    };
    
}