package com.human.found;

    import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
    import org.jasypt.iv.NoIvGenerator;

public class JasyptTest {
    public static void main(String[] args) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("wpqkfehofk"); // 암호화 키
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setIvGenerator(new NoIvGenerator());

        String target = "gpeyfguzsyiiwjei"; // 여기에 실제 MariaDB 비밀번호 입력
        String encrypted = encryptor.encrypt(target);
        
        System.out.println("새로운 암호화 값: ENC(" + encrypted + ")");
    }
}

