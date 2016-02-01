/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

/**
 *
 * @author Slava
 */
public class AtomicString {
    private String value;
    public synchronized void setValue(String value)
    {
        this.value = value;
    }
    public synchronized String getValue()
    {
        return this.value;
    }
}
