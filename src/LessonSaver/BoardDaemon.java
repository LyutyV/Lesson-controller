/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Slava
 */
public class BoardDaemon implements Runnable {
    private int port;
    private ConcurrentLinkedQueue boardQueue;
    private DataInputStream DIS;
    private AtomicBoolean isRec;
    private MessageHolder previousTextData, previousGraphData;
    private AtomicString groupName;
    
    public BoardDaemon(ConcurrentLinkedQueue boardQueue,
            int port,
            AtomicBoolean isRec,
            AtomicString grName)
    {
        this.boardQueue = boardQueue;
        this.port = port;
        this.isRec = isRec;
        this.previousTextData = new MessageHolder();
        this.previousGraphData = new MessageHolder();
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
                    System.out.println( " ReceiverBoard_UDP receive() : udp" + se.getMessage());
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
                            this.boardQueue.add(MH);
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
            System.out.println( " ReceiverBoard_UDP SocketException #1 : udp" + se.getMessage());
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
            //Удаление странного Викиного лоудаша из начала имени группы
            String tmpGroupName = String.valueOf(name_gr);
            if (tmpGroupName.startsWith("_"))
                this.groupName.setValue(tmpGroupName.substring(1));
            else
                this.groupName.setValue(tmpGroupName);
            
            int numberPage = (int)localDIS.readByte();
            int type = (int)localDIS.readByte();
            MessageHolder MH = new MessageHolder();
            switch (type) {
                case 1:
                {
                    int syze = localDIS.readInt();
                    MH.messageType = "boardText";
                    MH.message = new byte[syze];
                    
                    //New algorithm
                    byte lineNumber = localDIS.readByte();
                    byte fontHeight = localDIS.readByte();
                    String message = localDIS.readUTF();
                    byte [] byteMessage = message.getBytes();
                    ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                    BAOS.write(lineNumber);
                    BAOS.write(fontHeight);
                    BAOS.write(byteMessage);
                    MH.message = new byte[BAOS.size()];
                    MH.message = BAOS.toByteArray();
                    //!!!!!!!!!!!!!!!!!!!!!!!!
                    
                    //Old algorithm
                    //localDIS.read(MH.message, 0, syze);

                    if (this.previousTextData.equals(MH))
                    {
                        return null;
                    }
                    else
                    {
                        this.previousTextData = MH;
                        return MH;
                    }
                }
                case 2:
                {
                    int syze = localDIS.readInt();
                    MH.messageType = "boardGraph";
                    MH.message = new byte[syze];
                    localDIS.read(MH.message, 0, syze);
                    
                    if (this.previousGraphData.equals(MH))
                    {
                        return null;
                    }
                    else
                    {
                        this.previousGraphData = MH;
                        return MH;
                    }
                }
                default:
                    throw new Exception("Не верный тип сообщения доски.");
            }
        } catch (IOException ex) {
            throw new Exception("Ошибка в BoardDaemon.");
        }
    }
}