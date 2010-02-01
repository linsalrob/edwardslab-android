package edwardslab.util;

/*References: http://www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351.html
Used for the web access portion of code.
*/

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MgUtilFunc {

	/**
	 * @author jhoffman
	 * @param s	A String containing the web address to connect to.
	 * @return String	Results of the web request
	 * Sends a query to the web.
	 */
	public static String makeWebRequest(String s){
		Log.e("makeWebRequest","Performing " + s);
			/* Will be filled and displayed later. */
			String webResultString = null;
			try {
				/* Define the URL we want to load data from. */
				URL urlToOpen = new URL(s);
				/* Open a connection to that URL. */
				URLConnection ucon = urlToOpen.openConnection();
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
				webResultString = new String(baf.toByteArray());
			} catch (Exception e) {
				/* On any Error we want to display it. */
				//webResultString = e.getMessage();
			}
			return webResultString;
	}

	/**
	 * @author jhoffman
	 * @param jsonString	A json formatted String to convert into a Hash.
	 * @return Hashtable	The hash version of the json String.
	 * Converts a json formatted hash into a java Hashtable.
	 */
	public static Hashtable<String,String> JSONToHash(String jsonString){
			System.out.println("JSONToHash reached, input is: " + jsonString);
			//This is a more general parse method (and perhaps I should reconsider the names), which we can hopefully re-use.
			Hashtable<String,String> resultHash = new Hashtable<String,String>();
			try{// Take the stringified JSON Hash of Hashes and put it into our Hash
				JSONObject convObj = new JSONObject(jsonString);       	
				//convObj is null when an unknown search item was entered in second text box
				//If true, return empty hash table
				if(convObj != null) {
					Iterator<String> iter= convObj.keys();
					String currKey;
					String currVal;
					while(iter.hasNext()){
						//Parse jsonString and fill our hash from it, then connect it to our spinner
						currKey = (String) iter.next();
						currVal = convObj.get(currKey).toString();  
						resultHash.put(currKey, currVal);
					}
				}
			} catch (Exception E){
				Log.e("MobileMetagenomics", "JSON to Hash failed: " + E);
			}
			return resultHash;
	}

	/**
	 * @author jhoffman
	 * @param ourFile		The file to annotate
	 * @param level			The level, or mode, to annotate
	 * @param stringency	How strict the annotation should be
	 * @param kmer			Length of the 'words' in the annotation
	 * @param maxGap		Maximum distance between matches
	 * @return String		The server output - a json object with a website url and # of results portions contained there
	 * Uploads a file with all annotation parameters, returning a json String with information on getting results from the server.
	 */
	public static String doFileUpload(String ourFile, int level, int stringency, int kmer, int maxGap){
			final String existingFileName = ourFile;   	  
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
						+ "Content-Disposition: form-data; name=\"kmer\"" + lineEnd + lineEnd
						+ kmer + lineEnd
						+ twoHyphens + boundary + lineEnd
						+ "Content-Disposition: form-data; name=\"maxGap\"" + lineEnd + lineEnd
						+ maxGap + lineEnd
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

	/**
	 * @author jhoffman
	 * @param phoneNumber	The phone number to tag results with
	 * @param fileName		The filename to tag results with
	 * @param jsonObject	The json Object to upload to the server
	 * @return	String		A String containing the results of the web upload
	 * Uploads a json result to the results storage server.
	 */
	public static String doJsonUpload(String phoneNumber, String fileName, String jsonObject){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes_josh.cgi");
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
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumber + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					fileName + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd			
					+ jsonObject +	lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"put\"" + lineEnd + lineEnd +
					"Save this JSON Object" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("UploadFile","JSON is written");
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

	/**
	 * @author jhoffman
	 * @param phoneNumberForQuery	Phone number which desired results are tagged with.
	 * @param sampleNumber			Sample number which desired results are tagged with.
	 * @return	String				A String from the server containing a json hash of results.
	 * Downloads saved annotation results from the results storage server.
	 */
	public static String doJsonQuery1(String phoneNumberForQuery, int sampleNumber){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes.cgi");
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
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumberForQuery + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					sampleNumber + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"get\"" + lineEnd + lineEnd +
					"jsonObject" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("GetJSON","JSON reqest sent");
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex)
		{
			Log.e("GetJSON1", "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe)
		{
			Log.e("GetJSON1", "error: " + ioe.getMessage(), ioe);
		}
		//------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream ( conn.getInputStream() );
			String str;   	       
			while (( str = inStream.readLine()) != null)
			{
				//TODO: We can verify success/failure here, just need to know what to expect from server!
				responseFromServer += str;
				Log.e("GetJSON1","Server Response"+str);
			}
			inStream.close();
		}
		catch (IOException ioex){
			System.out.println("Upload failed.");
			Log.e("GetJSON1", "error: " + ioex.getMessage(), ioex);
		}
		System.out.println("Upload finished. Response is: " + responseFromServer);
		return responseFromServer;
	}
	/*
	public static String doJsonQuery2(String phoneNumberForQuery2, String sampleTitle){
		final String lineEnd = "\r\n";
		final String twoHyphens = "--";
		final String boundary =  "---------------------------2916890032591";
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String responseFromServer = "";
		try
		{
			//------------------ CLIENT REQUEST
			URL url = new URL("http://edwards.sdsu.edu/cgi-bin/cell_phone_metagenomes.cgi");
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
					"Content-Disposition: form-data; name=\"phoneNumber\"" + lineEnd + lineEnd +
					phoneNumberForQuery2 + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"count\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"title\"" + lineEnd + lineEnd +
					sampleTitle + lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"jsonObject\"" + lineEnd + lineEnd +
					lineEnd +
					twoHyphens + boundary + lineEnd +
					"Content-Disposition: form-data; name=\"get\"" + lineEnd + lineEnd +
					"jsonObject" + lineEnd +
					twoHyphens + boundary + twoHyphens + lineEnd);
			Log.e("GetJSON","JSON reqest sent");
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex)
		{
			statusOk = false;
			Log.e("GetJSON2", "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe)
		{
			statusOk = false;
			Log.e("GetJSON2", "error: " + ioe.getMessage(), ioe);
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
			statusOk = false;
			Log.e("GetJSON2", "error: " + ioex.getMessage(), ioex);
		}
		return responseFromServer;
	}*/
}
