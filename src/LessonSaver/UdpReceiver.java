/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

/**
 *
 * @author Slava
 */
public class UdpReceiver implements Runnable {
    
    BoardDaemon boardDaemon; //Поток принимающий инфу доски (Демон)
    VideoDaemon videoDaemon; //Поток принимающий инфу видео (Демон)
    AudioDaemon audioDaemon; //Поток прослушивающий аудиовход (Демон)
    //Поток получающий блок данных и сохраняющий его на сервере (Демон)
    ConcurrentLinkedQueue boardQueue; //БайтПоток для сохранения доски
    ConcurrentLinkedQueue videoQueue;//БайтПоток для сохранения видео
    ConcurrentLinkedQueue audioQueue;//БайтПоток для сохранения звука
    ServerBridge bridge; //Очередь кадров для передачи на сервер
    
    
    private SettingsConfig portsParser;
    private AtomicBoolean isBoardRecord;//Условие записи звука при трансляции доски.
    private AtomicBoolean isVideoRecord;//Условие записи звука при трансляции видео.
    private byte [] previousBoardData;
    private AtomicString groupName;
    
    
    public UdpReceiver () {
        this.bridge = new ServerBridge();
        this.boardQueue = new ConcurrentLinkedQueue<MessageHolder>();
        this.videoQueue = new ConcurrentLinkedQueue<MessageHolder>();
        this.audioQueue = new ConcurrentLinkedQueue<Byte>();
        this.portsParser = new SettingsConfig();
        this.isBoardRecord = new AtomicBoolean(false);
        this.isVideoRecord = new AtomicBoolean(false);
        this.previousBoardData = null;
        this.groupName = new AtomicString();
        
        this.boardDaemon = new BoardDaemon(boardQueue,
                this.portsParser.PORT_UDP_BOARD,
                this.isBoardRecord, this.groupName);
        this.videoDaemon = new VideoDaemon(videoQueue,
                this.portsParser.PORT_UDP_ScStr,
                this.isVideoRecord, this.groupName);
        this.audioDaemon = new AudioDaemon(audioQueue, this.isBoardRecord, this.isVideoRecord);

        //Запуск потока ожидающего инфу от сокета доски
        Thread boardThread = new Thread(this.boardDaemon);
        boardThread.setDaemon(true);
        boardThread.start();
        
        //Запуск потока ожидающего инфу от сокета видео
        Thread videoThread = new Thread(this.videoDaemon);
        videoThread.setDaemon(true);
        videoThread.start();

        //Запуск потока ожидающего инфу от аудиовыхода
        Thread audioThread = new Thread(this.audioDaemon);
        audioThread.setDaemon(true);
        audioThread.start();
        //Запуск потока получающего блок данных и сохраняющего его на сервере
        
    }

    @Override
    public void run() {
        while (true)
        {
            if (this.isBoardRecord.get() || this.isVideoRecord.get())
            {
                try {
                    //получение байтов звука
                    ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
                    try {
                        int len = audioQueue.size();
                        for (int i = 0; i < len; i++)
                        {
                            BAOS.write((byte[])audioQueue.poll());
                        }
                    } catch (IOException ex) {
                    }
                    byte [] audioData = BAOS.toByteArray();

                    //Создание переменной для типа картинки
                    String picType = null;
                    //Создание контейнера для картинки
                    byte [] picData = null;
                    //получение байтов текстовой информации доски
                    if (!this.boardQueue.isEmpty())
                    {
                        MessageHolder boardMessageHolder = (MessageHolder)this.boardQueue.poll();
                        picType = boardMessageHolder.messageType;
                        picData = boardMessageHolder.message;
                        //Очистка очереди
                        boardQueue.clear();
                    }
                    //получение байтов видео
                    else if (!this.videoQueue.isEmpty())
                    {
                        MessageHolder videoMessageHolder = (MessageHolder)this.videoQueue.poll();
                        picType = videoMessageHolder.messageType;
                        picData = videoMessageHolder.message;
                        //Очистка очереди
                        videoQueue.clear();
                    }
                    else
                    {
                        picType = "empty";
                        picData = new byte[0];
                    }

//                    System.out.println("Group = " + groupName.getValue() +
//                            " type = " + picType +
//                            " pic = " + picData.length +
//                            " audio = " + audioData.length);
                    this.bridge.setFrame(groupName.getValue(), new Date(), picType, picData, audioData);

                    Thread.sleep(0);
                } catch (InterruptedException ex) {}
            }
//            else
//            {
                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException ex) {}
//            }
        }
    }
}