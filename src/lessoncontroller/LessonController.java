/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lessoncontroller;

import LessonSaver.UdpReceiver;
import MainFrame.MainFrame;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author incode3
 */
public class LessonController {
    private static MainFrame frame;
    private static UdpReceiver receiver;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread mainFrameThread = new Thread(new Runnable()
        {
            @Override
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                frame = new MainFrame();
            }
        });
        
        receiver = new UdpReceiver();
        Thread udpReceiverThread = new Thread(receiver);
        
        try {
            udpReceiverThread.setDaemon(true);
            
            udpReceiverThread.start();
            mainFrameThread.start();
            
            mainFrameThread.join();
            udpReceiverThread.join();
        } catch (InterruptedException ex) {}

    }
}