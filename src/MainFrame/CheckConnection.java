/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Slava
 */
public class CheckConnection extends Thread{
    private JButton button;
    private Thread t;
    public CheckConnection(JButton b)
    {
        this.button = b;
        this.setDaemon(true);
        
        this.t = new Thread(this);
        this.t.start();
    }

    @Override
    public void run() {
        while (true)
        {
            try
            {
                Thread.sleep(1000);
                if (isOnline())
                    this.button.setEnabled(true);
                else
                    this.button.setEnabled(false);
            }
            catch (Exception e)
            {
                System.out.println("Exseption in chechConnectionClass");
                this.button.setEnabled(false);
            }
        }
    }
    
    private boolean isOnline() throws MalformedURLException, IOException, Exception
    {
        String url = "http://www.itstepdeskview.hol.es";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "apideskviewer.checkStatus={}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + urlParameters);
//        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        JSONParser parser = new JSONParser();
        Object parsedResponse = parser.parse(in);

        JSONObject jsonParsedResponse = (JSONObject) parsedResponse;

        String s = (String) jsonParsedResponse.get("response");
        if (s.equals("online"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
