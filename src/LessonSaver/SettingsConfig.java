package LessonSaver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
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

    //<editor-fold defaultstate="collapsed" desc=" IP, ports ">
  
    /**
     * порт прослушки маяков студентов
     */
    private int  PORT_UDP; 
    /**
     * порт вещания доски
     */
    public int  PORT_UDP_BOARD;
    /**
     * порт TCP соединения для получения картинки
     */
    private int  PORT_TCP_IMG;
    /**
     * порт TCP соединения для отправки команды
     */
    private int  PORT_TCP_COMMAND;
    /**
     * порт для отправки экрана преподавателя по UDP
     */
    public int  PORT_UDP_ScStr;
    //</editor-fold>    
    InputStream IS;
    /**
     * адресс компьютера
     */
    public InetAddress IP;
    /**
     * broadcast UDP
     */
    public InetAddress IP_UDP;  
  
    
    Document doc;
    
    public  SettingsConfig()
    {
        isValid=isLoadStyle();
    }
    
    /**
     * считывание данных из файла
     * @return 
     */
    private boolean isLoadStyle()
    { 
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.doc = builder.parse(new File("Settings.xml"));
            
            Element ip = (Element)doc.getElementsByTagName("IP").item(0); 
            this.IP=InetAddress.getByName(ip.getTextContent().trim());
            
            Element ip_udp = (Element)doc.getElementsByTagName("IP_UDP").item(0); 
            this.IP_UDP=InetAddress.getByName(ip_udp.getTextContent().trim());
            
            Element p_udp = (Element)doc.getElementsByTagName("PORT_UDP").item(0); 
            this.PORT_UDP=Integer.parseInt(p_udp.getTextContent());
            
            this.PORT_UDP_BOARD=this.PORT_UDP+1;            
            this.PORT_TCP_IMG=this.PORT_UDP+2;
            this.PORT_TCP_COMMAND=this.PORT_UDP+3;
            this.PORT_UDP_ScStr=this.PORT_UDP+4;
           
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
