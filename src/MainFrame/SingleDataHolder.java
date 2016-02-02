/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

/**
 *
 * @author Slava
 */
public class SingleDataHolder {
    private static volatile SingleDataHolder instance;
    
    public int sampleRate = 16000;
    public int sampleSize = 16;
    public int channels = 1;
    public int encodedBlockSize = 15;
    public int decodedBlockSize = 640;
    public String hostAdress = "http://itstepdeskview.hol.es/";
    public boolean isProxyActivated = false;
    public String proxyIpAdress = "123.0.0.0";
    public int proxyPort = 12345;
    public String proxyLogin = "testLogin";
    public String proxyPassword = "testPassword";
    
    private SingleDataHolder() {
    }
    
    public static SingleDataHolder getInstance() {
        if (instance == null) {
            synchronized (SingleDataHolder.class) {
                if (instance == null) {
                    instance = new SingleDataHolder();
                }
            }
        }
        return instance;
    }
}
