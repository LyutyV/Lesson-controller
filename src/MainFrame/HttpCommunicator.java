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
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author incode3
 */
class HttpCommunicator
{
    private final String USER_AGENT = "Mozilla/5.0";

    public void setCombos(JComboBox comboGroups, JComboBox comboDates, LessonTableModel tableModel) throws MalformedURLException, IOException {
        //String url = "http://www.itstepdeskview.hol.es";
        URL obj = new URL(SingleDataHolder.getInstance().hostAdress);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "apideskviewer.getAllLessons={}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + SingleDataHolder.getInstance().hostAdress);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        JSONParser parser = new JSONParser();
        try {
            Object parsedResponse = parser.parse(in);

            JSONObject jsonParsedResponse = (JSONObject) parsedResponse;

            for (int i = 0; i < jsonParsedResponse.size(); i++) {
                String s = (String) jsonParsedResponse.get(String.valueOf(i));
                String [] splittedPath = s.split("/");
                DateFormat DF = new SimpleDateFormat("yyyyMMdd");
                Date d = DF.parse(splittedPath[1].replaceAll(".bin", ""));
                Lesson lesson = new Lesson(splittedPath[0], d, false);
                String group = splittedPath[0];
                String date = new SimpleDateFormat("dd.MM.yyyy").format(d);
                
                if(((DefaultComboBoxModel)comboGroups.getModel()).getIndexOf(group) == -1 ) {
                    comboGroups.addItem(group);
                }
                if(((DefaultComboBoxModel)comboDates.getModel()).getIndexOf(date) == -1 ) {
                    comboDates.addItem(date);
                }
                tableModel.addLesson(lesson);
            }
        } catch (Exception ex) {
        }
    }

    public boolean removeLessons(JSONObject jsObj) throws MalformedURLException, IOException
    {
            //String url = "http://itstepdeskview.hol.es/index.php";
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(SingleDataHolder.getInstance().hostAdress + "index.php");
            
            StringBody head = new StringBody(jsObj.toString(), ContentType.TEXT_PLAIN);

            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("apiDeskViewer.removeLesson", head);
 
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = client.execute(post, responseHandler);
            System.out.println("responseBody : " + response);
            if (response.equals(new String("\"success\"")))
                return true;
            else
                return false;

    }
    
    public boolean setPassword(String password, String group) throws IOException
    {
        //String url = "http://itstepdeskview.hol.es/index.php";
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(SingleDataHolder.getInstance().hostAdress + "index.php");
        
        String hashPassword = md5Custom(password);

        JSONObject jsObj = new JSONObject();
        jsObj.put("group", group);
        jsObj.put("newHash", hashPassword);

        StringBody head = new StringBody(jsObj.toString(), ContentType.TEXT_PLAIN);


        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("apiDeskViewer.updateGroupAccess", head);

        HttpEntity entity = builder.build();
        post.setEntity(entity);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response = client.execute(post, responseHandler);
        System.out.println("responseBody : " + response);
        if (response.equals(new String("\"success\"")))
            return true;
        else
            return false;
    }
    
    public static String md5Custom(String st) 
    {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];
        try 
        {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) 
        {
            System.err.printf("MD-5 error");
        }
 
        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 )
        {
            md5Hex = "0" + md5Hex;
        }
 
        return md5Hex;
    }
}