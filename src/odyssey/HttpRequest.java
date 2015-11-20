/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jaam
 */
public class HttpRequest {

    public String getRequest(String pUrlRequest) {
        
        String returnValue = "";

        try {
            URL myURL = new URL(pUrlRequest);

            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            if (conn.getResponseCode() == 400) {
                System.out.println("!Bad Request!");
            } else {
                returnValue = br.readLine();
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Error 1");
            e.printStackTrace();
            returnValue = "-1";
        } catch (IOException e) {
            System.out.println("Error 2");
            e.printStackTrace();
            returnValue = "-1";
        }
        
        return returnValue;
    }

    public String postRequest(String urlRequest, ArrayList keyValues, ArrayList keyNames) {
        String returnValue = "";

        JSONObject json = new JSONObject();

        for (int i = 0; i < keyNames.size(); i++) {
            try {
                json.put((String) keyNames.get(i), keyValues.get(i));

            } catch (JSONException ex) {
                Logger.getLogger(HttpRequest.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            URL host = new URL(urlRequest);
            URLConnection connection = host.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            //connection.setConnectTimeout(5000);
            //connection.setReadTimeout(5000);

            OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
            output.write(json.toString());
            output.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            returnValue = input.readLine();

            // close i/o
            input.close();
            output.close();
        } catch (Exception e) {
            System.out.println("Error: Connecting to Odyssey Cloud");
            System.out.println(e);
            returnValue = "-1";
        }

        return returnValue;
    }
}
