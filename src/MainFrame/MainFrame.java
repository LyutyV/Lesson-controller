/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.json.simple.JSONObject;

/**
 *
 * @author incode3
 */
public class MainFrame extends JFrame {
    //Создаем основное меню
    private JMenuBar menuBar;
    //Создаем три подменю
    private JMenu menuProxy;
    private JCheckBoxMenuItem cbMenuItem;
    private JTextField proxyIpAdress, proxyPort, proxyLogin;
    private JPasswordField proxyPassword;
    
    private LessonTableModel lessonTableModel;
    private TableRowSorter<LessonTableModel> sorter;
    private JTable table;
    private JPanel mainPanel;
    private JLabel labelGroup, labelDate;
    private JButton buttonDelete, buttonPasswordsManager, buttonRefresh;
    private JComboBox comboGroup, comboDate;
    private mainFrameActionListener myActionListener;
    private HttpCommunicator communicator;

    class mainFrameActionListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            //Add combo filters
            if (e.getSource() == MainFrame.this.comboGroup || e.getSource() == MainFrame.this.comboDate)
            {
                String groupRegex = (comboGroup.getSelectedIndex() == 0) ? "" : comboGroup.getSelectedItem().toString() ;
                String dateRegex = (comboDate.getSelectedIndex() == 0) ? "" : comboDate.getSelectedItem().toString() ;
                List<RowFilter<Object,Object>> rfs = new ArrayList<RowFilter<Object,Object>>(2);
                rfs.add(RowFilter.regexFilter(groupRegex, 0));
                rfs.add(RowFilter.regexFilter(dateRegex, 1));

                RowFilter<Object,Object> af = RowFilter.andFilter(rfs);
                sorter.setRowFilter(af);
            }
            
            if (e.getSource() == MainFrame.this.buttonDelete)
            {
                TableModel model = table.getModel();
                JSONObject jsObj = new JSONObject();
                int j = 0;
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((boolean)model.getValueAt(i, 2) == true)
                    {
                        String group = (String)model.getValueAt(i, 0);
                        String date = (String)model.getValueAt(i, 1);
                        DateFormat DF = new SimpleDateFormat("dd.MM.yyyy");
                        Date d = null;
                        try {
                            d = DF.parse(date);
                        } catch (ParseException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        date = new SimpleDateFormat("yyyyMMdd").format(d);
                        jsObj.put(j++, group.concat("/").concat(date));
                    }
                }
                if (!jsObj.isEmpty())
                {
                    try {
                        if (communicator.removeLessons(jsObj))
                        {
                            JOptionPane.showOptionDialog(null, "Удаление прошло успешно", "Удаление лекций", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                        }
                        else
                        {
                            JOptionPane.showOptionDialog(null, "Возникла ошибка во время удаления лекции", "Удаление лекций", JOptionPane.PLAIN_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                        }
                        //Обновить таблицу
                        MainFrame.this.refreshScreen();
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            if (e.getSource() == MainFrame.this.buttonPasswordsManager)
            {
                int size = comboGroup.getModel().getSize();
                String [] groups = new String[size-1];
                for (int i = 1; i < size; i++) {
                    groups[i-1] = (String) comboGroup.getItemAt(i);
                }
                StudentsFrame sFrame = new StudentsFrame(groups, communicator);
            }
            
            if (e.getSource() == MainFrame.this.buttonRefresh)
            {
                MainFrame.this.refreshScreen();
            }
        }
    }

    public MainFrame() {
        //Create menu
        this.menuBar = new JMenuBar();
        this.menuProxy = new JMenu("Proxy");
        this.menuBar.add(menuProxy);
        this.cbMenuItem = new JCheckBoxMenuItem("Использовать proxy");
        this.cbMenuItem.setMnemonic(KeyEvent.VK_C);
        
        ActionListener aListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                AbstractButton aButton = (AbstractButton) event.getSource();
                boolean selected = aButton.getModel().isSelected();
                if (selected) {
                    SingleDataHolder.getInstance().isProxyActivated = true;
                    SingleDataHolder.getInstance().proxyIpAdress = MainFrame.this.proxyIpAdress.getText();
                    SingleDataHolder.getInstance().proxyPort = Integer.parseInt(MainFrame.this.proxyPort.getText());
                    SingleDataHolder.getInstance().proxyLogin = MainFrame.this.proxyLogin.getText();
                    SingleDataHolder.getInstance().proxyPassword = String.valueOf(MainFrame.this.proxyPassword.getPassword());
                } else {
                    SingleDataHolder.getInstance().isProxyActivated = false;
                }
            }
        };
        this.cbMenuItem.addActionListener(aListener);
        
        this.proxyIpAdress = new JTextField();
        this.proxyPort = new JTextField();
        this.proxyLogin = new JTextField();;
        this.proxyPassword = new JPasswordField();
        this.menuProxy.add(this.cbMenuItem);
        this.menuProxy.add(new JLabel("IP адрес"));
        this.menuProxy.add(this.proxyIpAdress);
        this.menuProxy.add(new JLabel("Номер порта"));
        this.menuProxy.add(this.proxyPort);
        this.menuProxy.add(new JLabel("Логин"));
        this.menuProxy.add(this.proxyLogin);
        this.menuProxy.add(new JLabel("Пароль"));
        this.menuProxy.add(this.proxyPassword);
        this.setJMenuBar(this.menuBar);
        
        communicator = new HttpCommunicator();
        myActionListener = new mainFrameActionListener();
        mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        
        JPanel P = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        mainPanel.add(P, BorderLayout.NORTH);

        //Create Group label
        labelGroup = new JLabel("Группы");
        P.add(labelGroup);
        //Create Group combo
        comboGroup = new JComboBox();
        comboGroup.addItem("Все");
        comboGroup.addActionListener(myActionListener);
        P.add(comboGroup);
        
        //Create Date label
        labelDate = new JLabel("Даты");
        P.add(labelDate);
        //Create Date combo
        comboDate = new JComboBox();
        comboDate.addItem("Все");
        comboDate.addActionListener(myActionListener);
        P.add(comboDate);
        
        //Create Delete button
        buttonDelete = new JButton("Удалить");
        buttonDelete.setEnabled(false);
        buttonDelete.addActionListener(myActionListener);
        P.add(buttonDelete);
        
        //this.checkConnection = new CheckConnection(buttonDelete);
        
        buttonPasswordsManager = new JButton("Менеджер паролей");
        buttonPasswordsManager.addActionListener(myActionListener);
        P.add(buttonPasswordsManager);
        
        try {
            Image img = ImageIO.read(getClass().getResource("resources/table_refresh.png"));
            this.buttonRefresh = new JButton(new ImageIcon(img));
            this.buttonRefresh.addActionListener(myActionListener);
            this.buttonRefresh.setToolTipText("Обновить таблицу");
            P.add(buttonRefresh);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //CreateLessonTableModel
        lessonTableModel = new LessonTableModel();

        try {
            communicator.setCombos(comboGroup, comboDate, lessonTableModel);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Проверьте соединение с интернет.");
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //CreateTable
        table = new JTable(lessonTableModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        
        JScrollPane scrollP = new JScrollPane(table);
        mainPanel.add(scrollP, BorderLayout.CENTER);
        //Create Table sorter
        sorter = new TableRowSorter<LessonTableModel>(lessonTableModel);
        table.setRowSorter(sorter);
        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                TableModel model = (TableModel)e.getSource();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((boolean)model.getValueAt(i, 2) == true)
                    {
                        buttonDelete.setEnabled(true);
                        return;
                    }
                }
                buttonDelete.setEnabled(false);
            }
        });
    
        //Standart block
        this.setSize(700, 400);
        this.setTitle("Менеджер лекций");
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        try {
            Image img = ImageIO.read(getClass().getResource("resources/appIcon.png"));
            this.setIconImage(img);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.setVisible(true);
    }
    
    private void refreshScreen() {
        //Clear combos and table model
        for (int i = 1; i < this.comboGroup.getItemCount(); i++) {
            this.comboGroup.removeItemAt(i);
        }
        for (int i = 1; i < this.comboDate.getItemCount(); i++) {
            this.comboDate.removeItemAt(i);
        }
        this.lessonTableModel.Clear();
        
        //Set fresh data
        try {
            communicator.setCombos(comboGroup, comboDate, lessonTableModel);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Проверьте соединение с интернет.");
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
