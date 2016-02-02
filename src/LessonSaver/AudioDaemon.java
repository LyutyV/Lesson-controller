/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import MainFrame.SingleDataHolder;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import org.xiph.speex.SpeexEncoder;

/**
 *
 * @author Slava
 */
public class AudioDaemon  extends Thread
{
    private ConcurrentLinkedQueue audioQueue;
    private int sample_rate = SingleDataHolder.getInstance().sampleRate;
    private int sample_size = SingleDataHolder.getInstance().sampleSize;
    private int channels = SingleDataHolder.getInstance().channels;
    AudioFormat format;
    TargetDataLine audioLine;
    byte[] tmpBuf;
    private AtomicBoolean isVRec, isBRec;
    
    AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        return new AudioFormat(sample_rate, sample_size, channels, true, false);
    }
    
    public AudioDaemon(ConcurrentLinkedQueue audioQueue, AtomicBoolean isBoardRec, AtomicBoolean isVideoRec)
    {
        this.audioQueue = audioQueue;
        this.isVRec = isVideoRec;
        this.isBRec = isBoardRec;
    } 
    
    @Override 
    public void run()
    {
        while (true)
        {
            if (this.isBRec.get() || this.isVRec.get())
            {
                format = getAudioFormat();
        
                DataLine.Info dataLineInfo = 
                        new DataLine.Info(TargetDataLine.class, format);
                try
                {
                    audioLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                    audioLine.open(format);
                }
                catch(LineUnavailableException lUav)
                {

                }
                
                int bytesRead = 0;

                audioLine.start();
                SpeexEncoder encoder = new SpeexEncoder();
                encoder.init(1, 1, sample_rate, channels);
                int raw_block_size = encoder.getFrameSize() * channels
                    * (sample_size / 8);
                tmpBuf = new byte[raw_block_size*2];
                try
                {
                    while(this.isBRec.get() || this.isVRec.get())
                    {
                        bytesRead = audioLine.read(tmpBuf, 0, raw_block_size);
                        if (bytesRead > 0)
                        {
                            if (!encoder.processData(tmpBuf, 0, raw_block_size)) 
                                {
                                    System.err.println("Could not encode data!");
                                    break;
                                }
                            int encoded = encoder.getProcessedData(tmpBuf, 0);

//                            System.out.println(encoded
//                                + " bytes resulted as a result of encoding " + bytesRead
//                                + " raw bytes.");
                            byte[] encoded_data = new byte[encoded];
                            System.arraycopy(tmpBuf, 0, encoded_data, 0, encoded);

                            this.audioQueue.add(encoded_data);
                        }
                    }

                    if(audioLine != null)
                    {
                        audioLine.close();
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}