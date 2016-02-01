/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
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


/**
 *
 * @author Slava
 */
public class ServerBridge {
    private ConcurrentLinkedQueue<ServerBridge.Frame> frameQueue;
    private Bridge bridge;
    private InetAddress IA;
    class Frame {
        private String group;
        private Date date;
        private int sampleRate;
        private int sampleSize;
        private int channels;
        private int encodedBlockSize;
        private int decodedBlockSize;
        private String picType;
        private int picSize;
        private byte [] picData;
        private int audioSize;
        private byte [] audioData;
        
        public Frame(String group, Date date, String picType, byte [] picData, byte [] audioData) {
            this.group = group;
            this.date = date;
            this.sampleRate = 16000;
            this.sampleSize = 16;
            this.channels = 1;
            this.encodedBlockSize = 15;
            this.decodedBlockSize = 640;
            this.picType = picType;
            this.picData = picData;
            this.picSize = picData.length;
            this.audioData = audioData;
            this.audioSize = audioData.length;
        }
    }
    class Bridge implements Runnable
    {
        @Override
        public void run()
        {
            while (true)
            {
                try {
                    //Проверка наличия пакетов в очереди и наличия интернет соединения с сервером
                    while (ServerBridge.this.frameQueue.size() > 0)
                    {
                        System.out.println(ServerBridge.this.frameQueue.size());
                        Frame f = (Frame) ServerBridge.this.frameQueue.peek();
                        String response = this.alterSendFrame(f);
                        ServerBridge.this.frameQueue.remove(f);
//                        //<editor-fold defaultstate="collapsed" desc="comment">
//                        Object obj = JSONValue.parse(response);
//                        JSONObject jsonObj = (JSONObject) obj;
//                        long date = Long.parseLong((String) jsonObj.get("response"));
//
//                        ServerBridge.this.frameQueue.forEach((v)->{
//                            if (v.date.getTime() == date)
//                            {
//                                ServerBridge.this.frameQueue.remove(v);
//                            }
//                        });
//
//                        Thread t = new Thread(new MyRunnable(date));
//                        t.start();
//                        Если отправка удалась - удалить фрейм
//                        Если отправка не удалась - по новой
//</editor-fold>
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ServerBridge.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerBridge.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        private String alterSendFrame(Frame f) throws MalformedURLException, IOException, Exception
        {
            String url = "http://itstepdeskview.hol.es/index.php";
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            
            JSONObject headJSON = new JSONObject();
            ByteArrayOutputStream byteHead = new ByteArrayOutputStream();
            ByteArrayOutputStream byteBody = new ByteArrayOutputStream();
            alterFrameToBytes(f, headJSON, byteHead, byteBody);
            
            StringBody head = new StringBody(headJSON.toString(), ContentType.TEXT_PLAIN);
            // byteHead;
            byte [] byteArrHead = byteHead.toByteArray();
            // bytebody;
            byte [] byteArrBody = byteBody.toByteArray();
            
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("apiDeskViewer.setFrame", head);
            builder.addBinaryBody("byteHead", byteArrHead, ContentType.DEFAULT_BINARY, "byteHead.bin");
            builder.addBinaryBody("byteBody", byteArrBody, ContentType.DEFAULT_BINARY, "byteBody.bin");
 
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = client.execute(post, responseHandler);
            System.out.println("responseBody : " + response);
            return response;
        }
        
        private  byte[] longToByteArray(long value)
        {
            byte[] buffer = new byte[8];    //longs are 8 bytes I believe
            for (int i = 7; i >= 0; i--) {  //fill from the right
                buffer[i]= (byte)(value & 0x00000000000000ff); //get the bottom byte
                value=value >>> 8; //Shift the value right 8 bits
            }
            return buffer;
        }
        
        private void alterFrameToBytes(Frame f, JSONObject head, ByteArrayOutputStream headBAOS, ByteArrayOutputStream bodyBAOS)
        {
            try {
                head.put("name", create30BytesName(f.group));
                head.put("date", String.valueOf(f.date.getTime()));

                DataOutputStream DOS = new DataOutputStream(headBAOS);
                DOS.write(create30BytesName(f.group).getBytes(Charset.forName("UTF-8")));            //Запись имени группы (30 bytes)
                DOS.writeLong(new Date().getTime());    //Запись даты в милисекундах
                DOS.writeInt(16000);         //Запись частоты дискретизации
                DOS.writeByte(16);        //Запись размера фрагмента
                DOS.writeByte(1);          //Запись типа моно/стерео
                DOS.writeInt(15);   //Запись encodedBlockSize
                DOS.writeInt(640);   //Запись decodedBlockSize
                DOS.close();
                
                DOS = new DataOutputStream(bodyBAOS);
                switch (f.picType)                                                  //Запись типа картинки
                {
                    case "boardText":
                        DOS.writeByte(1);
                        break;
                    case "boardGraph":
                        DOS.writeByte(2);
                        break;
                    case "screenStream":
                        DOS.writeByte(3);
                        break;
                    case "empty":
                        DOS.writeByte(4);
                        break;
                }
                DOS.writeInt(f.picData.length);     //Запись размера данных картинки
                bodyBAOS.write(f.picData);              //Запись данных картинки
                DOS.writeInt(f.audioData.length);   //Запись размера данных звука
                bodyBAOS.write(f.audioData);            //Запись данных звука
                DOS.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private String create30BytesName(String n)
        {
            int diff = 30 - n.getBytes(Charset.forName("UTF-8")).length;
            String appendix = "";
            if (diff > 0)
            {
                for (int i = 0; i < diff; i++) {
                    appendix += " ";
                }
                return appendix + n;
            }
            return n;
        }
        
        class MyRunnable implements Runnable {
            private long date;
            
            public MyRunnable(long d) {
               this.date = d;
            }
            public void run() {
                ServerBridge.this.frameQueue.forEach((v)->{
                    if (v.date.getTime() == date)
                    {
                        ServerBridge.this.frameQueue.remove(v);
                    }
                });
            }
        }
    }
    
    public ServerBridge()
    {
        //Проверка наличия неотправленных пакетов путем определения наличия сериализованого бинарника
        if(new File("frameQueue.dat").exists()) //Если файл существует десериалезируем его и создаем из него очередь к отправке
        {
            try
            {
                FileInputStream FIS = new FileInputStream("frameQueue.dat");
                try
                {
                    ObjectInputStream OIS = new ObjectInputStream(FIS);
                    try {
                        this.frameQueue = (ConcurrentLinkedQueue<ServerBridge.Frame>) OIS.readObject();
                    } catch (ClassNotFoundException ex) {}
                    OIS.close();
                }
                catch (IOException ex) {}
            } 
            catch (FileNotFoundException ex) {}
        }
        else    //Если файл отсутствует то создаем новую очередь
        {
            this.frameQueue = new ConcurrentLinkedQueue<>();
        }

        
        this.bridge = new Bridge();
        Thread T = new Thread(this.bridge);
        T.setDaemon(true);
        T.start();
    }
    
    public void setFrame(String group, Date date, String picType, byte [] picData, byte [] audioData)
    {
        this.frameQueue.add(new Frame(group, date, picType, picData, audioData));
    }
}
