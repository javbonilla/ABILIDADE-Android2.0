package org.abilidadev2.network;
/*package org.abilidade.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.abilidade.model.Point;
import json.*;

public class Connection {
	private String serverRootUrl;

	public Connection setUrl(String urlRootAddress) {
		this.serverRootUrl = urlRootAddress;
		return this;
	}
	
	public String sendData(Point newPoint) throws Exception {
		String data = generateCreatePointDataURL(newPoint);
		URL serverUrl = new URL(serverRootUrl + "/api/create");
		return makeRequest(serverUrl,data);
	}
	
	public List<Point> getPointsFromOperation(int operation) throws Exception {
		List<Point> poinstToReturn = new ArrayList<Point>();
		String data = generateGetPointsDataURL(operation);
		URL serverUrl = new URL(serverRootUrl + "/api/get");
		JSONArray pointsDownloaded = new JSONArray(makeRequest(serverUrl,data));
		for (int i = 0; i < pointsDownloaded.length(); i++) {
			JSONObject point = new JSONObject(pointsDownloaded.getString(i));
			System.out.println(point);
			Point newPoint = new Point();
			newPoint.setAddress(point.getString("a"));
			newPoint.setDescription(point.getString("d"));
			newPoint.addPhoto(point.getString("p"));
			newPoint.setSubmitterMail(point.getString("m"));
			newPoint.setLat(Double.valueOf(point.get("lt").toString()));
			newPoint.setLon(Double.valueOf(point.get("lg").toString()));
			poinstToReturn.add(newPoint);
		}
		return poinstToReturn; 
	}
	
	public String makeRequest(URL serverUrl,String data)	throws Exception {
	        try {
	            URLConnection conn = serverUrl.openConnection();
	            conn.setDoOutput(true);
	            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
	            //write parameters
	            writer.write(data);
	            writer.flush();
	            // Get the response
	            StringBuffer answer = new StringBuffer();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                answer.append(line);
	            }
	            writer.close();
	            reader.close();
	            //Output the response
	            return answer.toString();
	            
	        } catch (MalformedURLException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
			return null;
	}

	private String generateCreatePointDataURL(Point newPoint) throws UnsupportedEncodingException {
		String data = URLEncoder.encode("a", "UTF-8") + "="
				+ URLEncoder.encode(newPoint.getAddress(), "UTF-8");
		data += "&" + URLEncoder.encode("d", "UTF-8") + "="
				+ URLEncoder.encode(newPoint.getDescription(), "UTF-8");
		data += "&" + URLEncoder.encode("p", "UTF-8") + "="
				+ URLEncoder.encode(newPoint.getPhoto()[0], "UTF-8");
		data += "&" + URLEncoder.encode("m", "UTF-8") + "="
				+ URLEncoder.encode(newPoint.getSubmitterMail(), "UTF-8");
		data += "&" + URLEncoder.encode("lt", "UTF-8") + "="
				+ URLEncoder.encode(String.valueOf(newPoint.getLat()), "UTF-8");
		data += "&" + URLEncoder.encode("lg", "UTF-8") + "="
				+ URLEncoder.encode(String.valueOf(newPoint.getLon()), "UTF-8");
		return data;
	}
	
	private String generateGetPointsDataURL(int operation) throws UnsupportedEncodingException {
		String data = URLEncoder.encode("o", "UTF-8") + "="+ URLEncoder.encode(String.valueOf(operation), "UTF-8");
		return data;
	}
}

*/