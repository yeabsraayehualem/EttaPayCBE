package com.sm.sdk.demo.utils;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class TLSConnectionUtil {

    /**
     * Loads a certificate from the Keystore and sets up an SSL context for a TLS connection.
     *
     * @param context  The Android context (used to access internal storage).
     * @param keystoreFileName The name of the Keystore file.
     * @param keystorePassword The password for the Keystore.
     * @param alias The alias of the certificate to be used.
     * @throws Exception If any error occurs during the process.
     */
    public static void setupTLSConnection(Context context, String keystoreFileName,
                                          String keystorePassword, String alias) throws Exception {
        // Step 1: Load the Keystore
        KeyStore keyStore = KeyStore.getInstance("BKS");
        File keystoreFile = new File(context.getFilesDir(), keystoreFileName);
        FileInputStream fis = new FileInputStream(keystoreFile);
        keyStore.load(fis, keystorePassword.toCharArray());
        fis.close();

        // Step 2: Retrieve the Certificate
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        if (certificate == null) {
            throw new Exception("Certificate with alias '" + alias + "' not found in the Keystore.");
        }

        // Step 3: Initialize the TrustManagerFactory with the Keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        // Step 4: Create an SSLContext with the TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

        // Step 5: Set the SSLContext for HttpsURLConnection
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        Log.d("TLSConnectionUtil", "TLS connection setup successfully with certificate alias: " + alias);
    }
}
