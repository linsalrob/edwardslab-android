package edwardslab.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FileUploadPOC extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final EditText file = (EditText) findViewById(R.id.fileName);
        final EditText content = (EditText) findViewById(R.id.fileContent);
        final Button button = (Button)findViewById(R.id.confirmButton);
    
        button.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        			writeToFile(file.getText().toString(),content.getText().toString());
        	}
        });
    }
    
    public void writeToFile(String fileName, String userInput){
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
    	}catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    	
    }
    
    
}