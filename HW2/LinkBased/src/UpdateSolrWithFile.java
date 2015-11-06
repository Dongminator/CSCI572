import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;


public class UpdateSolrWithFile {
	
	
	public static final String PREFIX_DONGLIN = "/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/THIS/test/";
	public static final String PREFIX_XUAN = "/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS/test/";
	
	
	/*
	 *  Solr ID: 
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/document/test/
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/document/test/00/ec/094757430094f707ecc36838c412ec20_.html
	 *  allMetadataGeo.txt 
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/THIS/test/
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/THIS/test/00/8a/0f06c22c095aa26886cd75afa5d9dc30_0001_yacht-peacock-bass-fishing-.geot
	 *  allMetadataHtmls.txt 
	 *  	/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS/test/
	 *  	/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS/test/00/8a/0f06c22c095aa26886cd75afa5d9dc30_0001_yacht-peacock-bass-fishing-.html
	 */
	public void updateGeo () {
		update("allMetadataGeo.txt", PREFIX_DONGLIN, 0);
	}

	public void updateTime () {
		update("allMetadataHtmls.txt", PREFIX_XUAN, 1);
	}
	
	
	public void update (String fileName, String splitBy, int option) {
		Charset charset = Charset.forName("UTF-8");
		Path path = FileSystems.getDefault().getPath("", fileName);
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
		    String line = null;
		    ArrayList<String> documentStrings = new ArrayList<>();
		    while ((line = reader.readLine()) != null) {
		    	if (line.startsWith("/Users/")) {
		    		processDocumentStrings(documentStrings, option);
		    		String docID = getDocId(line, splitBy, option);
		    		documentStrings = new ArrayList<>();
		    		documentStrings.add(docID);
		    	} else {
		    		if (line.length() > 12) {
		    			documentStrings.add(line);
		    		}
		    	}
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}

		
	}
	
	
	private void processDocumentStrings (ArrayList<String> inputStrings, int option) {
		// Gun_date: ctakes:DateAnnotation
		// Geographic_LATITUDE, Geographic_LONGITUDE, Geographic_NAME, Optional_LATITUDEx, Optional_LONGITUDEx, Optional_NAMEx
		
		if (inputStrings.size() > 0) {
			String docID = inputStrings.get(0);
			String paramsStart = "[{\"id\":\"" + docID + "\"";
			String paraMiddle = "";
			String paramsEnd = "}]";
			
			Date d = new Date();
			boolean dateSet = false;
			
			boolean updateSolr = false;
			
			for (int i = 1; i < inputStrings.size(); i++) {
				String thisLine = inputStrings.get(i);
				String first13 = thisLine.substring(0, 13);
				if (first13.equals("Optional_NAME") || first13.equals("Optional_LONG") || first13.equals("Optional_LATI") 
						|| first13.equals("Geographic_LA")  || first13.equals("Geographic_LO")  || first13.equals("Geographic_NA")
						|| first13.equals("ctakes:DateAn") ) {
					if (first13.equals("ctakes:DateAn")) {
						String[] split = thisLine.split("ctakes:DateAnnotation:");
						if (split.length > 0) {
							String dateValue = split[1].split(":")[0].trim();
							
							DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
							try {
								Date date = format.parse(dateValue);
								if (date.before(d)) {
									d = date;
									dateSet = true;
									updateSolr = true;
								}
								
							} catch (ParseException e) {
								// TODO Auto-generated catch block
//								e.printStackTrace();
							}
							
						}
					} else {
						String[] split = thisLine.split(":");
						if (split.length > 1) {
							String key = split[0].trim();
							String value = split[1].trim();
							paraMiddle += ", \"" + key + "\":{\"set\":\"" + value + "\"}";
							updateSolr = true;
						}
					}
				}
			}
			
			
			if (option == 1 && dateSet) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'");
				paraMiddle += ", \"Gun_date\":{\"set\":\"" + df.format(d) + "\"}";
			}
			
			String finalParams = paramsStart + paraMiddle + paramsEnd;
			
			if (updateSolr) {
				System.out.println(finalParams);
				HttpClient httpClient = HttpClientBuilder.create().build();
				
				HttpPost request = new HttpPost("http://localhost:8983/solr/update?commit=true");
				StringEntity params;
				try {
					
					params = new StringEntity(finalParams);
					request.addHeader("content-type", "application/json");
					
					request.setEntity(params);
			        HttpResponse response = httpClient.execute(request);
			        System.out.println(response.getStatusLine().getStatusCode());
			        System.out.println(response.getStatusLine().getReasonPhrase());
			        
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/*
	 *  Solr ID: 
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/document/test/
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/document/test/00/8a/0f06c22c095aa26886cd75afa5d9dc30_0001_yacht-peacock-bass-fishing-.html
	 *  allMetadataGeo.txt 
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/THIS/test/
	 *  	/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/THIS/test/00/8a/0f06c22c095aa26886cd75afa5d9dc30_0001_yacht-peacock-bass-fishing-.html.geot

	 *  allMetadataHtmls.txt 
	 *  	/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS/test/
	 *  	/Users/sx/Desktop/CSCI572/Assignments/Assignment2/THIS/test/00/8a/0f06c22c095aa26886cd75afa5d9dc30_0001_yacht-peacock-bass-fishing-.html
	 */
	public String getDocId (String line, String splitBy, int option) {
		String filePath = line.split(splitBy)[1];
		if (option == 0) {
			filePath = filePath.replace(".geot","");
		}
		return "/Users/Dongminator/Documents/Study/USC/CS/CSCI572/hw2/document/test/" + filePath;
	}
}
