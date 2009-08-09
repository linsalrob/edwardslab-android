package edwardslab.util;
//Written by Josh Hoffman and Daniel Cuevas
//Dr. Rob Edwards' Lab, San Diego State University
//
//References: www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
//Used for the web access portion of code.
//Dr. Rob Edwards' SEEDgenomes.js
//Used for structure of the seed search code elements.
//http://www.helloandroid.com/node/243
//Used as an example for thread and progress dialog code.

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



public class GenomeSearch extends Activity implements Runnable {
    protected Hashtable genome = new Hashtable();
	protected Spinner s;
	protected AutoCompleteTextView autoComplete;
	final private String baseUrl = "http://bioseed.mcs.anl.gov/~redwards/FIG/rest_seed.cgi";
	final private String queryUrl = "http://bioseed.mcs.anl.gov/~redwards/FIG/rest_seed.cgi/genomes/complete/undef/Bacteria";
	TextView result;
	EditText edittext;
	private ProgressDialog pd;
	private String myResultString;
	private boolean statusOk = true;
    String getWebInfoErr = "Failed to connect to the SEED...";
    String parseJsonErr = "We're sorry, there was an error parsing your results...";
    String genSearchResultsErr = "Search did not generate any results...";
    String appFailedWith;
	
	public String getWebInfo(String s){
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
             statusOk = false;
             //displayErrorToast(getWebInfoErr);
             appFailedWith = getWebInfoErr;
        }
        return myString;
	}
	
	public ArrayAdapter parseJson(String myString){
		//This is a special parse method, specific to how I want the auto-complete box to have its data built.
		//Note that I reverse the key-value pairs in this method, versus the straightforward method in JSONToHash!
		if(statusOk){
		 try{// Take the stringified JSON Hash of Hashes and put it into our Hash
	        	JSONObject HoH = new JSONObject(myString);
	        	JSONObject myObj = HoH.optJSONObject("result");
	        	Iterator iter= myObj.keys();
	        	ArrayAdapter myAA = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line);
	        	while(iter.hasNext()){
	                //Parse myString and fill our hash from it, then connect it to our spinner
	        		String myVal = (String) iter.next();
	        		String myKey = (String) myObj.get(myVal);
	        		genome.put(myKey, myVal);
	                myAA.add(myKey);            
	        	}           
	        	return(myAA);
	            //TODO: Make this exception more meaningful? Or is it good enough?
	        } catch (JSONException e){
	        	//Error parsing JSON data in parseJSON. We should retry here, but currently don't!
	             //displayErrorToast(parseJsonErr);
	        	statusOk = false;
	        	appFailedWith = parseJsonErr;
	        	return null;
	        }
		}
		return null;
	}
	
	public Hashtable jsonToHash(String myString){
		//This is a more general parse method (and perhaps I should reconsider the names), which we can hopefully re-use.
		if(statusOk){
		Hashtable myHash = new Hashtable();
		try{// Take the stringified JSON Hash of Hashes and put it into our Hash
        	JSONObject HoH = new JSONObject(myString);
        	JSONObject myObj = HoH.optJSONObject("result");
        	
        	//myObj is null when an unknown search item was entered in second text box
        	//If true, return empty hash table
        	if(myObj != null) {
        		Iterator iter= myObj.keys();
        		while(iter.hasNext()){
        			//Parse myString and fill our hash from it, then connect it to our spinner
        			String myKey = (String) iter.next();
        			String myVal = (String) myObj.get(myKey);  
        			myHash.put(myKey, myVal);
        		}
        	}
            //TODO: Make this exception more meaningful? Or is it good enough?
        } catch (Exception E){
        	//result.setText("Error parsing JSON data in JSONToHash");
        	statusOk = false;
            //displayErrorToast(parseJsonErr);
        	appFailedWith = parseJsonErr;
        }	
        return myHash;}
		else return null;
	}
	
	public String genSearchResults(Hashtable h){
		if(statusOk){
		String resString = "";
		if(h.isEmpty()){
			statusOk = false;
			//displayErrorToast(genSearchResultsErr);
			appFailedWith = genSearchResultsErr;
			resString = "No search results, please try again.";
		}
		else{
			Enumeration myEnum = h.keys();
			while(myEnum.hasMoreElements()){
				String tmpKey = myEnum.nextElement().toString();
				resString = resString + "<a href=\"http://seed-viewer.theseed.org/linkin.cgi?id=" + tmpKey + "\">" + tmpKey + "</a>"
					+ ": " + h.get(tmpKey) + "<br><br>";
			}
		}
		return resString;
		}
		else return null;
	}
	
	public void setUpAuto(ArrayAdapter aa){
		if(statusOk){
		autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
		autoComplete.setAdapter(aa);
		}
	}
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    //Set up User Interface Elements and Event Listeners
    	//Define user interface from xml
	    	super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        //final TextView result = (TextView)  this.findViewById(R.id.result);
	        result = (TextView)  this.findViewById(R.id.result);
	        edittext = (EditText) findViewById(R.id.entry);
	    	final InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
		    final Button button = (Button)findViewById(R.id.ok);
	    //Connect Listeners to UI
	        button.setOnClickListener(new OnClickListener(){
	        	public void onClick(View v) {     		
	        		//This if check ensures we don't crash if the user hasn't entered any text.
	        		if(autoComplete.length() > 0 && edittext.length() > 0){
	        			inputManager.hideSoftInputFromWindow(edittext.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS); 
	        			if(isConnectionOk()){
	        				pd = ProgressDialog.show(GenomeSearch.this, "Performing Search...", "Please wait (this may take a few moments)", true, false);
		        			Thread thread = new Thread(GenomeSearch.this);
		        			thread.start();
	        	        }
	        	        else{
	        	        	noConnectionDialog();
	        	        }	        			
	        		}
	        	}
	        });
	        
	        result.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
        			inputManager.hideSoftInputFromWindow(edittext.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
	        	}
	        });
	        
	        //Clear Button
	        final Button clrButton = (Button) findViewById(R.id.clear);
	        
	        clrButton.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
	        		autoComplete.setText("");
	        		edittext.setText("");
	        		result.setText("");
	        	}
	        });
	//Populate User Interface Elements
	    //Pull JSON file from the seed, parse it, call setUpSpinner method
	        if(isConnectionOk()){
	        	setUpAuto(parseJson(getWebInfo(queryUrl)));
	        }
	        else{
	        	noConnectionDialog();
	        }
    }

	@Override
	public void run() {
		//Create our results, turning links data into working links
		String tmpString = autoComplete.getText().toString();
		if(genome.containsKey(tmpString)){
			String searchUrl = baseUrl + "/search_genome/" + genome.get(tmpString).toString() + 
			"/" + edittext.getText().toString();
			myResultString = genSearchResults(jsonToHash(getWebInfo(searchUrl)));
		}
		else
			myResultString = "Invalid search term, please try again.";
		handler.sendEmptyMessage(0);
	}   
	
	private void noConnectionDialog(){
		if(!isConnectionOk()){
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("No internet connection...")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   	noConnectionDialog();
	    		        	dialog.cancel();
	    	           }
	    	       })
	    	       .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                finish();
	    	           }
	    	       });
	    	AlertDialog alert = builder.create();
	    	alert.show();
		}
	}
	
	private void displayErrorToast(String s){
        Toast toast = new Toast(this);
        toast.setText(s);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
	}
	
	private boolean isConnectionOk(){
		 ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		    if (cm.getNetworkInfo(cm.TYPE_MOBILE).isConnected() || 
		         cm.getNetworkInfo(cm.TYPE_WIFI).isConnected()){
		    	return true;
		    }
		    else{
		       return false;
		    }
	}
	
	private Handler handler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            pd.dismiss();
	    		result.setText(Html.fromHtml(myResultString));
	    		result.setMovementMethod(LinkMovementMethod.getInstance());
	        }
	    };
}