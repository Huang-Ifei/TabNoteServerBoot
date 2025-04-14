package com.tabnote.server.tabnoteserverboot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class DefiniteEncryption extends Thread{

    TabNoteInfiniteEncryption tie;

    public DefiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tie = tie;
    }

    @Override
    public void run() {
        while(true){
            this.tie.newEncryption();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
