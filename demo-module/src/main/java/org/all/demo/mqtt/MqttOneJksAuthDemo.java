package org.all.demo.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class MqttOneJksAuthDemo {
    // 修改为你的 MQTT 服务端地址（ssl:// 开头，端口8883）
    private static final String BROKER = "ssl://47.113.227.247:8883";
    private static final String CLIENT_ID = "java-client-" + System.currentTimeMillis();
    private static final String TOPIC = "test/topic";
    // 证书密码
    private static final String PASSWORD = "123456";
    // p12转jks默认别名是 1，固定即可
    private static final String KEY_ALIAS = "1";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();

            // 核心：仅加载 1 个 JKS，完成双向认证
            options.setSocketFactory(getSingleJksSslFactory());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            // 连接回调
            client.setCallback(new MqttCallback() {
                @Override public void connectionLost(Throwable cause) { System.out.println("断开连接"); }
                @Override public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("收到消息：" + new String(message.getPayload()));
                }
                @Override public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            // 连接、订阅、发消息
            client.connect(options);
            System.out.println("✅ 双向认证连接成功（仅用1个JKS）");
            client.subscribe(TOPIC);
            client.publish(TOPIC, new MqttMessage("Hello 单JKS双向认证".getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 核心方法：只用 1 个 client.jks 实现双向认证
    private static SSLSocketFactory getSingleJksSslFactory() throws Exception {
        // 1. 加载包含客户端私钥和证书链的 JKS 文件
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream("C:\\Users\\s2288\\Downloads\\emqx-certs\\client.jks")) {
            keyStore.load(fis, PASSWORD.toCharArray());
        }

        // 2. 初始化密钥管理器（客户端身份：私钥+证书）
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, PASSWORD.toCharArray());

        // ====================== 核心：从证书链中提取CA证书 ======================
        // 获取客户端证书链（包含：客户端证书 + CA根证书）
        Certificate[] certChain = keyStore.getCertificateChain(KEY_ALIAS);
        // 最后一个就是 CA根证书（证书链规则：根CA在最后）
        X509Certificate caCert = (X509Certificate) certChain[certChain.length - 1];


        // 3. 手动构建信任库，只信任提取的CA证书
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("mqtt-ca", caCert);

        // 4. 初始化信任管理器
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // 4. 构建SSL上下文
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext.getSocketFactory();
    }
}
