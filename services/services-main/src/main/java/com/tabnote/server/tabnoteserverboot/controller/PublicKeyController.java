package com.tabnote.server.tabnoteserverboot.controller;

import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@CrossOrigin
@Controller
public class PublicKeyController {

    private static final Logger log = LoggerFactory.getLogger(PublicKeyController.class);

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    @GetMapping("public_key")
    public ResponseEntity<String> publicKey() {
        log.info("publicKey");
        return sendMes(tabNoteInfiniteEncryption.getPublicKey());
    }

    private ResponseEntity<String> sendMes(String s) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(s);
    }
}
