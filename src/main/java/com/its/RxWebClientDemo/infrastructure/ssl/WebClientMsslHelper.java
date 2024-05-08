package com.its.RxWebClientDemo.infrastructure.ssl;

//import com.sun.org.apache.xml.internal.security.utils.Base64;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.binary.Base64;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import static com.its.RxWebClientDemo.infrastructure.DownstreamConstants.*;


@Component
@Slf4j
@Profile({"dev"})
public class WebClientMsslHelper implements WebClientSslHelper {

    @Override
    public SslContext getSslContext() {
        SslContext sslContext = null;
        try {
            // Get keystore
            final KeyStore keyStore = getKeyStore();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, JAVA_KEYSTORE_CRED_PROPERTY.toCharArray());

            // Get Truststore
            final KeyStore trustStore = getTrustStore();
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            sslContext = SslContextBuilder
                            .forClient()
                            .trustManager(trustManagerFactory)
                            .keyManager(keyManagerFactory)
                            .build();
        } catch(IOException | KeyStoreException | NoSuchAlgorithmException |
                UnrecoverableKeyException e) {
            log.error("Unable to initialize SslContext : error message : {}, exception : {}", e.getMessage(), e);
        }
        catch (Exception ex) {
            log.error("Unable to initialize SslContext : error message : {}, exception : {}", ex.getMessage(), ex);
        }
        return sslContext;
    }

    private KeyStore getKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String strKeyStore = System.getenv(JAVA_KEYSTORE_PROPERTY);
            if (ObjectUtils.isEmpty(strKeyStore)) {
                throw new IOException("Environment variable KEYSTORE is empty.");
            }
            String strKeyStorePassword = System.getenv(JAVA_KEYSTORE_CRED_PROPERTY);
            if (ObjectUtils.isEmpty(strKeyStore)) {
                throw new IOException("Environment variable KEYSTORE_PASSWORD is empty.");
            }
            //InputStream keyStoreInput = new ByteArrayInputStream(Base64.decode(strKeyStore));
            InputStream keyStoreInput = new ByteArrayInputStream(strKeyStore.getBytes());
            //keyStore.load(keyStoreInput, new String(Base64.decode(strKeyStorePassword), Charset.forName("UTF-8")).toCharArray());
            keyStore.load(keyStoreInput, new String(strKeyStorePassword.getBytes(), Charset.forName("UTF-8")).toCharArray());
            return keyStore;
        } catch (Exception ex) {
            log.error("TLS connection error occurred while loading keyStore. Error message : {} ", ex.getMessage());
            return null;
        }
    }

    private KeyStore getTrustStore() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String strTrustStore = System.getenv(JAVA_TRUSTSTORE_PROPERTY);
            if (ObjectUtils.isEmpty(strTrustStore)) {
                throw new IOException("Environment variable TRUSTSTORE is empty.");
            }
            //InputStream trustStoreInput = new ByteArrayInputStream(Base64.decode(strTrustStore));
            InputStream trustStoreInput = new ByteArrayInputStream(strTrustStore.getBytes());
            String strTrustStorePassword = System.getenv(JAVA_TRUSTSTORE_CRED_PROPERTY);
            if (ObjectUtils.isEmpty(strTrustStorePassword)) {
                throw new IOException("Environment variable TRUSTSTORE_PASSWORD is empty.");
            }
            //trustStore.load(trustStoreInput, new String(Base64.decode(strTrustStorePassword), Charset.forName("UTF-8")).toCharArray());
            trustStore.load(trustStoreInput, new String(strTrustStorePassword.getBytes(), Charset.forName("UTF-8")).toCharArray());
            return trustStore;
        } catch (Exception ex) {
            log.error("TLS connection error occurred while loading trustStore. Error message : " + ex.getMessage());
            return null;
        }
    }

    private TrustManagerFactory getTrustManagerFactory(KeyStore keyStore) {
        TrustManagerFactory trustManagerFactory ;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
        } catch (Exception e) {
            log.error("TLS connection error occurred while instantiating TrustManagerFactory. " +
                    "Error message : {} " + e.getMessage());
            return null;
        }
        return trustManagerFactory;
    }
}
