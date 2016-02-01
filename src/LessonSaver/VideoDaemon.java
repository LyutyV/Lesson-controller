/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Slava
 */
public class VideoDaemon  implements Runnable {
    private int port;
    private ConcurrentLinkedQueue videoQueue;
    private DataInputStream DIS;
    private AtomicBoolean isRec;
    private MessageHolder previousData;
    private AtomicString groupName;
    
    public VideoDaemon(ConcurrentLinkedQueue videoQueue,
            int port,
            AtomicBoolean isRec,
            AtomicString grName) {
        this.videoQueue = videoQueue;
        this.port = port;
        this.isRec = isRec;
        this.previousData = new MessageHolder();
        this.groupName = grName;
    }

    @Override
    public void run() {
        try
        {
            DatagramSocket DS = new DatagramSocket(this.port);           
            byte[] byte_info = new byte [32768];
            DatagramPacket info = new DatagramPacket (byte_info, 0, byte_info.length);
            boolean isReceive = false;
            while(true)
            {
                try
                {
                    DS.receive(info);
                    isReceive = true;
                }
                catch(Exception se)
                {
                    System.out.println( " ReceiverVideo_UDP receive() : udp" + se.getMessage());
                    isReceive = false;
                }
                if(isReceive)
                {
                    ByteArrayInputStream BAIS = new ByteArrayInputStream(info.getData());
                    this.DIS = new DataInputStream(BAIS);
                    int isRecord = (int)DIS.readByte();
                    if (isRecord == 1)
                    {
                        byte [] message = new byte [DIS.available()];
                        DIS.readFully(message);
                        
                        MessageHolder MH = isDataChanged(message);
                        if (MH != null)
                        {
                            this.videoQueue.add(MH);
                        }
                        
                        this.isRec.set(true);
                        BAIS.close();
                    }
                    else
                    {
                        this.isRec.set(false);
                    }
                }               
            }
        }
        catch(Exception se)
        {
            System.out.println( " ReceiverVideo_UDP SocketException #1 : udp" + se.getMessage());
        }
    }
    
    private MessageHolder isDataChanged(byte [] data) throws Exception
    {
        try {
            ByteArrayInputStream BAIS= new ByteArrayInputStream(data);
            DataInputStream localDIS = new DataInputStream(BAIS);
            int size = (int)localDIS.readByte();
            char [] name_gr = new char[size];
            for(int i=0; i < size; i++)
            {
                name_gr[i] = localDIS.readChar();
            }
            this.groupName.setValue(String.valueOf(name_gr));
            MessageHolder MH = new MessageHolder();
            
            int lengthByteArr = localDIS.readInt();
            MH.message = new byte[lengthByteArr];
            MH.messageType = "screenStream";
            localDIS.read(MH.message, 0, lengthByteArr);
            
            if (Arrays.equals(this.previousData.message, MH.message))
            {
                return null;
            }
            else
            {
                this.previousData = MH;
                return MH;
            }
        } catch (IOException ex) {
            throw new Exception("Ошибка в VideoDaemon.");
        }
    }
}