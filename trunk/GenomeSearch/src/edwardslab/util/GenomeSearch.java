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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class GenomeSearch extends Activity {
    protected Hashtable genome = new Hashtable();
	protected Spinner s;
	
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
	            ArrayAdapter myAA = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
	        	while(iter.hasNext()){
	                //Parse myString and fill our hash from it, then connect it to our spinner
	        		//May need to explicitly call toString here, but I can't check at the moment... 
	        		String myKey = (String) iter.next();
	        		String myVal = (String) myObj.get(myKey);
	        		genome.put(myKey, myVal);
	                myAA.add(myVal);
	        	}
	        	setUpSpinner(myAA);
	            //TODO: Make this exception meaningful.
	        } catch (Exception E){}	
	}
	
	public void setUpSpinner(ArrayAdapter aa){
        Spinner s = (Spinner) findViewById(R.id.spinner);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(aa);
        // TODO: make sure that the spinner implementation above actually fills it with the genome list.

        
        //Old example of spinner:
        //Spinner s = (Spinner) findViewById(R.id.spinner);
        //ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.genomeList, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //s.setAdapter(adapter);
	}
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    //Set up User Interface Elements and Event Listeners
    	//Define user interface from xml
	    	super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        final TextView result = (TextView)  this.findViewById(R.id.result);       
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
	//Populate User Interface Elements
	    //Pull JSON file from the seed, parse it, call setUpSpinner method
        parseJSON(getJSONData());
        //Old example of how to use text box
        /* Show the String on the GUI. */
        //tv.setText(myString);
        //this.setContentView(tv);
        //TODO: parse myString and create a hash from it
        //TODO: use this hash to query the seed for the user's search
        
        //TODO: Make this exception meaningful.
        
        /* Previous JSON code */
//        try{JSONObject myObj = new JSONObject("Hello");} catch (Exception E){}
        
        /* Create an array filled with genome names */
        /* First attempt to add first genome name into the list */
        //String[] mStrings = myString.split("[\"]");
        //String[] genomeList = new String[1];
        //genomeList[0] = mStrings[6];
        
     // Set up spinner with array of genomes
     // TODO: fill the arrays.xml file with the actual genome list.
        Spinner s = (Spinner) findViewById(R.id.spinner);
        
        /* Previous adapter code */
//      ArrayAdapter adapter = ArrayAdapter.createFromResource(
//      this, R.array.genomeList, android.R.layout.simple_spinner_item);
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        //		this, android.R.layout.simple_spinner_item, genomeList );
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //s.setAdapter(adapter);

        
     // Create an anonymous implementation of OnClickListener
        OnClickListener okButtonListener = new OnClickListener() {
            public void onClick(View v) {
            	//TODO: Actually perform the search when the button is clicked.
            }
        };       
     // Capture our button from layout
      //  Button button = (Button)findViewById(R.id.ok);        
     // Register the onClick listener with the implementation above
        button.setOnClickListener(okButtonListener);
     // Set up result box to display editText input
     // TODO: Make this actually perform a search.
       // final TextView result = (TextView)  this.findViewById(R.id.result);       
       // final EditText edittext = (EditText) findViewById(R.id.entry);
        edittext.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  // Perform action on key press
                	// TODO: Change this to take the edittext, perform search, and put result in
                	// the result box
                	result.setText(edittext.getText().toString());
                  return true;
                }
                return false;
            }
        });  
//>>>>>>> .r5
    }    
}