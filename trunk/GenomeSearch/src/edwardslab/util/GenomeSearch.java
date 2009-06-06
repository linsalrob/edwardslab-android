package edwardslab.util;
//References: www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
//Used for the entire code, minus target site modification.

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class GenomeSearch extends Activity {
    protected Hashtable genome = new Hashtable();
	protected Spinner s;
	protected AutoCompleteTextView autoComplete;
	TextView result;
	
	public String getJSONData(){
		/* Will be filled and displayed later. */
        String myString = null;
        try {
             /* Define the URL we want to load data from. */
             URL myURL = new URL(
          		       "http://bioseed.mcs.anl.gov/~redwards/FIG/rest_seed.cgi/genomes/complete/undef/Bacteria");
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
		 try{// Take the stringified JSON Hash of Hashes and put it into our Hash
	        	JSONObject HoH = new JSONObject(myString);
	        	JSONObject myObj = HoH.optJSONObject("result");
	        	Iterator iter= myObj.keys();
	        	ArrayAdapter myAA = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line);
	            //ArrayAdapter myAA = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
	        	while(iter.hasNext()){
	                //Parse myString and fill our hash from it, then connect it to our spinner
	        		//May need to explicitly call toString here, but I can't check at the moment... 
	        		String myKey = (String) iter.next();
	        		String myVal = (String) myObj.get(myKey);
	        		genome.put(myKey, myVal);
	                myAA.add(myVal);            
	        	}           
                //end test
	        	//setUpSpinner(myAA);
	        	setUpAuto(myAA);
	            //TODO: Make this exception meaningful.
	        } catch (Exception E){
	        	result.setText("Error parsing JSON data...");
	        }	
	}
	
	/*public void setUpSpinner(ArrayAdapter aa){
        s = (Spinner) findViewById(R.id.spinner);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(aa);
        // TODO: make sure that the spinner implementation above actually fills it with the genome list.
	}*/
	
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
	        final EditText edittext = (EditText) findViewById(R.id.entry);
		    final Button button = (Button)findViewById(R.id.ok);
	    //Connect Listeners to UI
	        button.setOnClickListener(new OnClickListener(){
	        	public void onClick(View v) {
	            	//TODO: Actually perform the search when the button is clicked.
	            }
	        });
	        edittext.setOnKeyListener(new OnKeyListener() {
	            public boolean onKey(View v, int keyCode, KeyEvent event) {
	                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	                  // Perform action on key press
	                	//TODO: I think this is wrong. User writes a PROTEIN name in the edittext, not a genome.
	                	//The genome choice should come from the spinbox selection.
	                	genome.get(edittext.getText().toString());
	                	result.setText(edittext.getText().toString());
	                  return true;
	                }
	                return false;
	            }
	        });
	        
	       /* 
	        * TODO: We need to figure out what kind of Listener is needed and
	        * how we need to pull information out of our autocomplete box.
	        * 
	        * autoComplete.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					
				}
	        	
	        });
	        autoComplete.setOnKeyListener(new OnKeyListener() {
	            public boolean onKey(View v, int keyCode, KeyEvent event) {
	                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	                  // Perform action on key press
	                	genome.get(edittext.getText().toString());
	                	result.setText(edittext.getText().toString());
	                  return true;
	                }
	                return false;
	            }
	        });  */
	//Populate User Interface Elements
	    //Pull JSON file from the seed, parse it, call setUpSpinner method
        parseJSON(getJSONData());
        //Old example of how to use text box
        //tv.setText(myString);
        //this.setContentView(tv);
    }    
}