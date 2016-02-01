/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Slava
 */
public class FramesProcessor {
    private ConcurrentLinkedQueue<Frame> frameQueue;
    private FrameSaver frameSaver;
    
    public FramesProcessor()
    {
        this.frameQueue = new ConcurrentLinkedQueue<Frame>();
        this.frameSaver = new FrameSaver();
        
        Thread t = new Thread(this.frameSaver);
        t.setDaemon(true);
        t.start();
    }
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
            this.audioData = audioData;
        }
    }
    
    class FrameSaver extends Thread
    {
        @Override 
        public void run(){
            
            this.writeHead();
            
            while (true) {
                
                if (FramesProcessor.this.frameQueue.size() > 0) {
                    
                    try {
                        //Достаем элемент из очереди
                        Frame f = FramesProcessor.this.frameQueue.peek();
                        //Преобразуем его в байты
                        byte [] frame = this.frameToBytes(f);
                        //Записываем байты в файл
                        FileOutputStream fos = null;
                        fos = new FileOutputStream("lesson.dat", true);
                        fos.write(frame);
                        fos.close();
                        //Если все ОК то вырезаем элемент из очереди
                        FramesProcessor.this.frameQueue.remove(f);
                        
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {}
                }
            }
        }
        private byte [] frameToBytes(Frame f) throws Exception
        {
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            DataOutputStream DOS = new DataOutputStream(BAOS);
            try {
//                DOS.writeChars(f.group);            //Запись имени группы (15 bytes)
//                DOS.writeLong(f.date.getTime());    //Запись даты в милисекундах
//                DOS.writeInt(f.sampleRate);         //Запись частоты дискретизации
//                DOS.writeByte(f.sampleSize);        //Запись размера фрагмента
//                DOS.writeByte(f.channels);          //Запись типа моно/стерео
//                DOS.writeInt(f.encodedBlockSize);   //Запись encodedBlockSize
//                DOS.writeInt(f.decodedBlockSize);   //Запись decodedBlockSize
                switch (f.picType)                  //Запись типа картинки
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
                BAOS.write(f.picData);              //Запись данных картинки
                DOS.writeInt(f.audioData.length);   //Запись размера данных звука
                BAOS.write(f.audioData);            //Запись данных звука
                
                DOS.close();
                
                return BAOS.toByteArray();
                
            } catch (IOException ex) {
                throw new Exception("Ошибка при создании массива байт из фрейма "
                        + ex.getMessage());
            }
        }
        
        private void writeHead()
        {
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            DataOutputStream DOS = new DataOutputStream(BAOS);
            try {
                DOS.write("abcdefghijklmnoabcdefghijklmno".getBytes(Charset.forName("UTF-8")));            //Запись имени группы (30 bytes)
                DOS.writeLong(new Date().getTime());    //Запись даты в милисекундах
                DOS.writeInt(16000);         //Запись частоты дискретизации
                DOS.writeByte(16);        //Запись размера фрагмента
                DOS.writeByte(1);          //Запись типа моно/стерео
                DOS.writeInt(15);   //Запись encodedBlockSize
                DOS.writeInt(640);   //Запись decodedBlockSize
                
                DOS.close();

                
                FileOutputStream fos = null;
                fos = new FileOutputStream("lesson.dat", true);
                fos.write(BAOS.toByteArray());
                fos.close();
            
            } catch (IOException ex) {
            }

            

        }
    }
    
    public void setFrame(String group, Date date, String picType, byte [] picData, byte [] audioData) {
        Frame f = new Frame(group, date, picType, picData, audioData);
        this.frameQueue.add(f);
    }

}
