/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LessonSaver;

import java.util.Arrays;

/**
 *
 * @author Slava
 */
public class MessageHolder {
    public byte [] message = new byte[0];
    public String messageType = "";
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MessageHolder)) {
            return false;
        }

        MessageHolder that = (MessageHolder) other;

        if (message.length != that.message.length)
            return false;

        if (message.length > 0 && that.message.length > 0)
        {
            byte [] test = new byte[message.length - 1];
            Arrays.copyOfRange(message, 1, message.length);
            byte [] test1 = new byte [that.message.length - 1];
            Arrays.copyOfRange(that.message, 1, that.message.length);

            return this.messageType.equals(that.messageType)
                    &&  Arrays.equals(
                            Arrays.copyOfRange(message, 1, message.length),
                            Arrays.copyOfRange(that.message, 1, that.message.length)
                    );
        }
        return false;
    }
}
