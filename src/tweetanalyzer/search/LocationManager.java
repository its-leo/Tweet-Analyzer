/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetanalyzer.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tweetanalyzer.store.Location;

/**
 *
 * @author HENSEL
 */
public class LocationManager {

    private static final String KEY = "AIzaSyC5g057ME9N12CyMEx0M3kl_syXuynpgm8";

    public static void search(Location location) throws ParseException {
        String json = "";

        InputStream inputStream = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://maps.googleapis.com/maps/api/geocode/json?address=" + location.getName().replaceAll(" ", "_") + "&sensor=true&key=" + KEY);
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
        } catch (Exception e) {
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder sbuild = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sbuild.append(line);
            }
            inputStream.close();
            json = sbuild.toString();
            System.out.println(json);
        } catch (Exception e) {
        }

        //now parse
        JSONParser parser = new JSONParser();
        if (!json.isEmpty()) {
            Object obj = parser.parse(json);
            JSONObject jb = (JSONObject) obj;

            //now read
            JSONArray jsonObject1 = (JSONArray) jb.get("results");
            if (!jsonObject1.isEmpty()) {
                JSONObject jsonObject2 = (JSONObject) jsonObject1.get(0);
                JSONObject jsonObject3 = (JSONObject) jsonObject2.get("geometry");
                JSONObject jsonObject4 = (JSONObject) jsonObject3.get("location");
                location.setLatitude(Double.parseDouble("" + jsonObject4.get("lat")));
                location.setLongitude(Double.parseDouble("" + jsonObject4.get("lng")));

                JSONArray jsonObject6 = (JSONArray) jsonObject2.get("address_components");
                JSONObject jsonObject7 = (JSONObject) jsonObject6.get(0);
                location.setName((String) jsonObject7.get("long_name"));

                JSONArray jsonObject8 = (JSONArray) jsonObject7.get("types");
                location.setType((String) jsonObject8.get(0));
            }
        }
    }
}
