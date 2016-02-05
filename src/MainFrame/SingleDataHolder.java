/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import LessonSaver.SettingsConfig;

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
    public boolean isProxyActivated;
    public String proxyIpAdress;
    public int proxyPort;
    public String proxyLogin;
    public String proxyPassword;
    
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
    
    public void setSettings(SettingsConfig sConfig)
    {
        this.isProxyActivated = sConfig.IS_PROXY_ACTIVATE;
        this.proxyIpAdress = sConfig.PROXY_IP;
        this.proxyPort = sConfig.PROXY_PORT;
        this.proxyLogin = sConfig.PROXY_USERNAME;
        this.proxyPassword = sConfig.PROXY_PASSWORD;
    }
}
