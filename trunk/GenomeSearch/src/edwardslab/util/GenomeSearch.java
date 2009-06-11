package edwardslab.util;

//References: www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
//Used for the web access portion of code.
//Dr. Rob Edwards' SEEDgenomes.js
//Used for structure of the seed search code elements.
//http://www.helloandroid.com/node/243
//Used as an example for thread and progress dialog code.
//Example change to make Daniel hate svn again
//Second example change


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
import android.app.ProgressDialog;
import android.content.Context;
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
        }
        return myString;
	}
	
	public void parseJSON(String myString){
		//This is a special parse method, specific to how I want the auto-complete box to have its data built.
		//Note that I reverse the key-value pairs in this method, versus the straightforward method in JSONToHash!
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
	        	setUpAuto(myAA);
	            //TODO: Make this exception more meaningful? Or is it good enough?
	        } catch (JSONException e){
	        	result.setText("Error parsing JSON data in parseJSON. We should retry here, but currently don't!");
	        }	
	}
	
	public Hashtable JSONToHash(String myString){
		//This is a more general parse method (and perhaps I should reconsider the names), which we can hopefully re-use.
		Hashtable myHash = new Hashtable();
		try{// Take the stringified JSON Hash of Hashes and put it into our Hash
        	JSONObject HoH = new JSONObject(myString);
        	JSONObject myObj = HoH.optJSONObject("result");
        	Iterator iter= myObj.keys();
        	while(iter.hasNext()){
                //Parse myString and fill our hash from it, then connect it to our spinner
        		String myKey = (String) iter.next();
        		String myVal = (String) myObj.get(myKey);  
        		myHash.put(myKey, myVal);
        	}           
            //TODO: Make this exception more meaningful? Or is it good enough?
        } catch (Exception E){
        	result.setText("Error parsing JSON data in JSONToHash");
        }	
        return myHash;
	}
	
	public String genSearchResults(Hashtable h){
		String resString = "";
		if(h.isEmpty()){
			resString = "Empty hash";
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
	
	public void setUpAuto(ArrayAdapter aa){
		autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
		autoComplete.setAdapter(aa);
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
	        			pd = ProgressDialog.show(GenomeSearch.this, "Performing Search...", "Please wait (this may take a few moments)", true, false);
	        			Thread thread = new Thread(GenomeSearch.this);
	        			thread.start();
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
        parseJSON(getWebInfo(queryUrl));
    }

	@Override
	public void run() {
		//Create our results, turning links data into working links
		String searchUrl = baseUrl + "/search_genome/" + genome.get(autoComplete.getText().toString()).toString() + 
		"/" + edittext.getText().toString();
		myResultString = genSearchResults(JSONToHash(getWebInfo(searchUrl)));
		handler.sendEmptyMessage(0);
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