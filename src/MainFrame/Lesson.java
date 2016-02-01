/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainFrame;

import java.util.Date;

/**
 *
 * @author incode3
 */
public class Lesson {
    public String Group;
    public Date Date;
    public boolean isSelected;
    public Lesson(String group, Date date, boolean selected) {
        this.Group = group;
        this.Date = date;
        this.isSelected = selected;
    }
}