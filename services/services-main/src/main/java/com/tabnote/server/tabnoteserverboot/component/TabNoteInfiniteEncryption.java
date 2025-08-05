package com.tabnote.server.tabnoteserverboot.component;

import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;


@Component
public class TabNoteInfiniteEncryption {
    
    private static final Logger log = LoggerFactory.getLogger(TabNoteInfiniteEncryption.class);

    AccountMapper accountMapper;

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    private String lastPrivateKey = "";

    private String publicKey = "";
    private String privateKey = "";
    private String tokenInput = "";

    @PostConstruct
    public void doPostConstruct() {
        DefiniteEncryption de = new DefiniteEncryption(this);
        de.start();
    }

    public void newEncryption() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        this.lastPrivateKey = this.privateKey;

        this.publicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        this.privateKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        this.tokenInput = Base64.getEncoder().encodeToString(String.valueOf(System.currentTimeMillis()).getBytes());
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getTokenInput(){
        return tokenInput;
    }

    public String decrypt(String s){
        try {
            byte[] stringBytes = new byte[0];
            try{
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.privateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                stringBytes = cipher.doFinal(Base64.getDecoder().decode(s));
            }catch (BadPaddingException e){
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.lastPrivateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                stringBytes = cipher.doFinal(Base64.getDecoder().decode(s));
            }

            return new String(stringBytes);

        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }

    public String encryptionTokenGetId(String encryptionToken){
        try {
            byte[] tokenBytes = new byte[0];
            try{
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.privateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                tokenBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionToken));
            }catch (BadPaddingException e){
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.lastPrivateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                tokenBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionToken));
            }

            String token = new String(tokenBytes);
            return accountMapper.tokenCheckIn(token);

        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }

    public boolean encryptionTokenCheckIn(String id, String encryptionToken) {
        try {
            byte[] tokenBytes = new byte[0];
            try{
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.privateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                tokenBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionToken));
            }catch (BadPaddingException e){
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.lastPrivateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                tokenBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionToken));
            }

            String token = new String(tokenBytes);
            if (!token.isEmpty() && id.equals(accountMapper.tokenCheckIn(token))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public boolean encryptionPasswordCheckIn(String truePassword,String encryptionPassword) {
        try {
            byte[] passwordBytes = new byte[0];
            try{
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.privateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                passwordBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionPassword));
            }catch (BadPaddingException e){
                PrivateKey privateKey = KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.lastPrivateKey)));

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);

                passwordBytes = cipher.doFinal(Base64.getDecoder().decode(encryptionPassword));
            }

            String password = new String(passwordBytes);
            if (!password.isEmpty() && password.equals(truePassword)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public String proxyGetIp(HttpServletRequest request) {
        // 从头部获取真实的客户端 IP 地址
        String ip = request.getHeader("X-Forwarded-For");

        // 如果没有找到 X-Forwarded-For，可能是直接连接的客户端
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
