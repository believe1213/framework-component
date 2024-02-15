package com.minister.framework.boot;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * TestBoot
 *
 * @author QIUCHANGQING620
 * @date 2020-02-18 13:44
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestBootApplication.class)
public class TestBoot {

    @Resource(name = "lazyJasyptStringEncryptor")
    private StringEncryptor stringEncryptor1;

    @Resource(name = "customizedStringEncryptor")
    private StringEncryptor stringEncryptor2;

    @Resource
    private EncryptablePropertyResolver encryptablePropertyResolver;

    @Value("${jasypt.encryptor.password}")
    private String password;

    @Test
    public void stringEncryptor() {
        // ===== lazyJasyptStringEncryptor =====
        String decValue = "testDecValue";
        String encValue = "testEncValue";
        String enc1 = stringEncryptor1.encrypt(decValue);
        String dec1 = stringEncryptor1.decrypt(enc1);
        Assert.assertEquals(decValue, dec1);

//        System.out.println(stringEncryptor1.encrypt(decValue));
//        System.out.println(stringEncryptor1.decrypt(encValue));

        String rec = encryptablePropertyResolver.resolvePropertyValue("ENC(" + enc1 + ")");
        Assert.assertEquals(decValue, rec);

        // ===== customizedStringEncryptor =====
        String enc2 = stringEncryptor2.encrypt(decValue);
        String dec2 = stringEncryptor2.decrypt(enc2);
        Assert.assertEquals(decValue, dec2);

//        System.out.println(stringEncryptor2.encrypt(decValue));
//        System.out.println(stringEncryptor2.decrypt(encValue));

        // ===== oldVersion lazyJasyptStringEncryptor =====
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        // 算法
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations(1000);
        config.setPoolSize(1);
        config.setProviderName(null);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        String enc3 = encryptor.encrypt(decValue);
        String dec3 = encryptor.decrypt(enc3);
        Assert.assertEquals(decValue, dec3);

//        System.out.println(encryptor.encrypt(decValue));
//        System.out.println(encryptor.decrypt(encValue));
    }

}
