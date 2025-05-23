package io.diligencevault.plugin.core.tasks;
import java.net.http.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class Httpclient {

    private HttpClient client;

    public Httpclient(Boolean isInsecure) {
        try {
            if (Boolean.TRUE.equals(isInsecure)) {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLParameters sslParams = new SSLParameters();
                // ❗ This disables the hostname check
                sslParams.setEndpointIdentificationAlgorithm(null);
                this.client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .sslContext(sslContext)
                    .version(HttpClient.Version.HTTP_1_1)
                    .sslParameters(sslParams)
                    .build();
            } else {
                // Secure client (default)
                this.client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient getClient() {
        return this.client;
    }

}


