package com.tieto.clawer;

import sun.security.ssl.SSLSocketFactoryImpl;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Clawer {
    public static void main(String[] args) {
        int poolSize = 10;
        boolean finished = true;
        ExecutorService executorService = Executors.newScheduledThreadPool(poolSize);
        List<Future> futures = new ArrayList<>();
        for (int count = 0; count < 10; count++) {
            futures.add(executorService.submit(new Test()));
        }
        while (finished) {
            finished = false;
            for (Future future : futures) {
                if (!future.isDone()) {
                    finished = true;
                }
            }
        }
        executorService.shutdownNow();
    }
}

class Test implements Runnable {
    public void run() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            long beginTime = System.currentTimeMillis();
            for (int count = 0; count < 1000; count++) {
                long startTime = System.currentTimeMillis();
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://h5cdn.cretech.cn/index.php?c=scene&a=counterset").openConnection();
                httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.connect();
                OutputStream outputStream = httpsURLConnection.getOutputStream();
                outputStream.write("sceneId=9010832&fieldId=7996662488".getBytes());
                outputStream.flush();
                outputStream.close();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                bufferedReader.close();
                httpsURLConnection.disconnect();
                System.out.println("count = " + count + ", spent: " + (System.currentTimeMillis() - startTime) + " milliseconds.");
            }
            System.out.println("Click 1000 times spent " + (System.currentTimeMillis() - beginTime) / 1000 + " sceonds");
        } catch (Exception e) {

        }
    }
}
