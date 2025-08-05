package com.tabnote.server.tabnoteserverboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class DefiniteEncryption extends Thread{

    private static final Logger log = LoggerFactory.getLogger(DefiniteEncryption.class);

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
                log.error(e.getMessage());
            }
        }
    }
}
