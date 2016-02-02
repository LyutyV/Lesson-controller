/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lessoncontroller;

import LessonSaver.UdpReceiver;
import MainFrame.MainFrame;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.swing.JOptionPane;

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
        checkIfRunning();
        
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
    
    private static void checkIfRunning() {
        try {
          //Bind to localhost adapter with a zero connection queue 
          ServerSocket socket = new ServerSocket(9999,0,InetAddress.getByAddress(new byte[] {127,0,0,1}));
        }
        catch (BindException e) {
          JOptionPane.showMessageDialog(null, "Приложение уже запущено");
          System.exit(1);
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(null, "Что-то пошло не так");
          e.printStackTrace();
          System.exit(2);
        }
    }
}