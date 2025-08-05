package com.tabnote.server.tabnoteserverboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Cryptic {

    private static final Logger log = LoggerFactory.getLogger(Cryptic.class);
    private String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3zVfhSOLFTAsQ3Zqt5S9HlC7EwawA0MZZW9h9QAf/ledp1y/z4JBOjGYoa7gyGaNZdZ7qj/dPQYdtzD+gN1Wj42BKDzCheSzv3NASsmEe0VSDhgRRe+WW71I40KgxO1TR/1XBEElIgUIaRZ7G/twhvqZhHJ+doys1qSj5sF/Q7tj7d9fSPkmb468QgmyRwprXsxAHY/xl1N8uLJm5QFsaaQuwh1JLaXyZIUuxYc+7u4rzYKTMQvZEU7hq5w/cgQK2LLwKwrW71CHVsidjZTZmI2nlGiSN1KPeoQQ66uXS/brnpgFnx9UlcWEFPrbmZ/ChE/vMtkO+tr650IA50hgNQIDAQAB";
    private String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDfNV+FI4sVMCxDdmq3lL0eULsTBrADQxllb2H1AB/+V52nXL/PgkE6MZihruDIZo1l1nuqP909Bh23MP6A3VaPjYEoPMKF5LO/c0BKyYR7RVIOGBFF75ZbvUjjQqDE7VNH/VcEQSUiBQhpFnsb+3CG+pmEcn52jKzWpKPmwX9Du2Pt319I+SZvjrxCCbJHCmtezEAdj/GXU3y4smblAWxppC7CHUktpfJkhS7Fhz7u7ivNgpMxC9kRTuGrnD9yBArYsvArCtbvUIdWyJ2NlNmYjaeUaJI3Uo96hBDrq5dL9uuemAWfH1SVxYQU+tuZn8KET+8y2Q762vrnQgDnSGA1AgMBAAECggEAAoMuq2p/J5jzXcxMeqXj3t5vdoADJ2o+/IS5H1Sugz5PdioFruT8XHi+vluO+7yek1Q6VjjmX7tatXCnhgy1nprwRHRL1G/CvGV1feuc7yi/HB40ADzO1bYE6Rh9jEc+2/PEhA+G7ShSC4gbVQI3pmv37vhXBQooBsHiTJw5d2GaqV4QZH9sxOC8QNzzRM/bzLIjQUhIdWbJVtyVjWGCcUIN4z7YWBSN6B7RuAe79Hwt2LVGVCsBT/Fdu7LqzCQHK4AuohclsjYJlwqGXddUPJlCNPwdN7FpPpJXXU2oZ9aDtl4MnwGwmWddZDc7ANj+3sBMRUvJMwSUTUPNqNt1EQKBgQDkx7MjtfisgsIG4F+R+vdYtQDbVBAu9RVW6EjFl7KsrD4o6KHRvtuGyHQAH8pFqpRD/T7Gupm7Qf8Q+71r/WMC90ql/kZbeUKhv68EH3iBPXr7ZNM+GrmUSP+o5LsW3b0XBQISOw497x+W9awApnZ3V9l3lzT9Q8ZUKidqgPOeMQKBgQD5w/iYuyFONCjXLtifiz4Dn6dsajPMTFYr2+Ex/HHpodJ6TVhTzRR4Ud5OIpCNip0IpDaZQTpoQTcdYzMC9K5RhD7t2EiSv/pV3NY27NqwQtEmo3Yv9z84Sea1Rcj6NKhmkWuvqlwyIeLTiRB2vAehkYqPAAMNt5jaLTvn2ypNRQKBgQDerZWqo5s45kjOgqPjJeCM4hjEYo0h94Dex1bVpHLP6RLTpKKk0d1A0mk+GbM6ne6UQrFQox2xC2ql8DGOI+K0Z1isOtPmgx+c6kMCg1M6kEnc2WVXJJIPSAI4NPH5Lri26DP85KhXFiGsQNE7DMtwG/zajz2PaeFn2GPnIT5+cQKBgFKPG7poiL7P8PwICSTbovkRqgblKBAM36MJwGuEdabzjZ5NuLein3SSIziSplOTEQtNNJr9+6+AdxZotvDwLjrVyvNVvc98U+RT5h8rtbHztCzgdW2vfZ8+llsvIRrLkyqsQPtFBcqwdsjTkrScvK0EbdeM+nVrTcQ4lYezY009AoGASe+PkZ+4YKa+iwwi0favQBoTcTd0KQGGraela78lgj9ZKBv0aG7DLIAQ0f/mULD243gV6bX4LM1cIw3bc9vtVs+QXhUIe1XFE3+Gv8TjLuCLsDDQi98D8Sbx1S/RXoU4jZYez/aOxaBuDy3MahFAesPkIx7FxCZs0I4mR2dvY9s=";

    public String encrypt(String s){
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(this.publicKey)));

            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = encryptCipher.doFinal(s.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }catch (Exception e){
            log.error(e.getMessage());
            return "";
        }
    }
}
