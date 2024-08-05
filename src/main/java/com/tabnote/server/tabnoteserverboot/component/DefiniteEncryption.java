package com.tabnote.server.tabnoteserverboot.component;

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
                Thread.sleep(3000000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
