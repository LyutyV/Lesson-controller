package LessonSaver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Viky_Pa
 */
public class SettingsConfig
{
    public boolean isValid;

    private int PORT_UDP; 
    public int PORT_UDP_BOARD;
    public int PORT_UDP_ScStr;
    public boolean IS_PROXY_ACTIVATE;
    public String PROXY_IP;
    public int PROXY_PORT;
    public String PROXY_USERNAME;
    public String PROXY_PASSWORD;

    /**
     * адресс компьютера
     */
    Document doc;
    
    public  SettingsConfig()
    {
        isValid=isLoadStyle();
    }
    
    private boolean isLoadStyle()
    { 
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.doc = builder.parse(new File("Settings.xml"));
                        
            Element p_udp = (Element)doc.getElementsByTagName("PORT_UDP").item(0);
            this.PORT_UDP=Integer.parseInt(p_udp.getTextContent());
            
            this.PORT_UDP_BOARD=this.PORT_UDP+1;            
            this.PORT_UDP_ScStr=this.PORT_UDP+4;
            
            Element element_proxy_activate = (Element)doc.getElementsByTagName("PROXY_ACTIVATE").item(0);
            this.IS_PROXY_ACTIVATE = Boolean.parseBoolean(element_proxy_activate.getTextContent());
            
            Element element_proxy_ip = (Element)doc.getElementsByTagName("PROXY_IP").item(0);
            this.PROXY_IP = element_proxy_ip.getTextContent();

            Element element_proxy_port = (Element)doc.getElementsByTagName("PROXY_PORT").item(0);
            this.PROXY_PORT = Integer.parseInt(element_proxy_port.getTextContent());

            Element element_proxy_username = (Element)doc.getElementsByTagName("PROXY_USERNAME").item(0);
            this.PROXY_USERNAME = element_proxy_username.getTextContent();

            Element element_proxy_passwotd = (Element)doc.getElementsByTagName("PROXY_PASSWORD").item(0);
            this.PROXY_PASSWORD = element_proxy_passwotd.getTextContent();
            
            return true;
        }
        catch (ParserConfigurationException ex)
        {
            Logger.getLogger(SettingsConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SAXException ex)
        {
            Logger.getLogger(SettingsConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(SettingsConfig.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
}
