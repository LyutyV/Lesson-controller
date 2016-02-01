/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import LessonSaver.CustomTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author incode3
 */
public class StudentsFrame extends JFrame {
    private JLabel label;
    private JPanel panel;
    private CustomTextField textField;
    private JComboBox comboGroups;
    private JButton btnOK;
    private myActionListener actionListener;
    private HttpCommunicator communicator;
    
    class myActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == StudentsFrame.this.btnOK)
            {
                String group = (String) comboGroups.getSelectedItem();
                String password = textField.getText();
                try {
                    if (communicator.setPassword(password, group))
                    {
                        int result = JOptionPane.showOptionDialog(null, "Пароль задан успешно", "Установка доступа", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                        if (result == JOptionPane.OK_OPTION)
                        {
                            StudentsFrame.this.dispose();
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "Чтото пошло не так(");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(StudentsFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    //public StudentsFrame(String [] groups, String selectedGroup) {
    public StudentsFrame(String [] model, HttpCommunicator communicator) {
        this.communicator = communicator;
        panel = new JPanel(new BorderLayout());
        this.setContentPane(panel);
        
        this.actionListener = new myActionListener();
        
        JPanel P = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.add(P, BorderLayout.SOUTH);
        
        //Add label
        this.label = new JLabel("Введите новый пароль:");
        P.add(this.label);
        
        //Add textfield
        this.textField = new CustomTextField(15);
        this.textField.setPlaceholder("Введите пароль");
        P.add(this.textField);
        
        //Add combo
        this.comboGroups = new JComboBox(model);
        P.add(this.comboGroups);
        
        //AddButtons
        this.btnOK = new JButton("Сохранить");
        this.btnOK.addActionListener(this.actionListener);
        P.add(this.btnOK);
        
        //Standart block
        this.setSize(550, 80);
        this.setTitle("Менеджер паролей");
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        
        try {
            Image img = ImageIO.read(getClass().getResource("../MainFrame/resources/appIcon.png"));
            this.setIconImage(img);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        this.setVisible(true);
    }
    
}