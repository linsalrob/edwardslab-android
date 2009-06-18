package edwardslab.example;
//Proof of Concept by Josh Hoffman
//Written for Dr. Rob Edwards' Bioinformatics Lab
//San Diego State University
//June 18, 2009
//References: www.anddev.org
//Used for file upload code, and file creation code

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FileUploadPOC extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final EditText file = (EditText) findViewById(R.id.fileName);
        final EditText content = (EditText) findViewById(R.id.fileContent);
        final Button button = (Button)findViewById(R.id.confirmButton);
		final TextView tv = new TextView(this);
        final Toast toast = new Toast(this);
        
        button.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        		//Attempt to write the specified file
        		if(writeToFile(file.getText().toString(),content.getText().toString())){
        			tv.setText("File output succeeded!");
        		}
        		else{
        			tv.setText("File output failed!");
        		}
        	    toast.setView(tv);
        	    toast.setDuration(Toast.LENGTH_LONG);
        	    toast.show();
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
	        FileOutputStream fOut = openFileOutput(fileName + ".txt",
	                                 MODE_WORLD_READABLE);
	        OutputStreamWriter osw = new OutputStreamWriter(fOut); 
	
	        // Write the string to the file
	        osw.write(userInput);
	        /* ensure that everything is
	         * really written out and close */
	        osw.flush();
	        osw.close();
	        return true;
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
    		return false;
    	}
    }
    
    
}