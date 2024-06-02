package com.et.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  17:59
 * @Description: TODO http请求相关工具类
 * @Version: 1.0
 */
public class HttpUtil {

    /**
     * 发送请求 返回网页内容
     * @param url
     * @return
     */
    public static String send(String url){
        // 定义网页内容
        String webContent = "";
        // 1. 创建HttpClient对象 用来发送http请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 2. 创建HttpGet对象 用来发送get请求
        HttpGet httpGet = new HttpGet(url);
        // 3. 配置请求消息
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000) // 连接超时时间 单位毫秒
                .setSocketTimeout(1 * 1000 * 10) // 读取超时时间 单位毫秒
                .build();
        httpGet.setConfig(config);

        // 4. 创建response对象 用来接收返回的数据
        CloseableHttpResponse response = null;
        try {
            // 执行get请求 获取数据
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            System.out.println("请求 ["+url+"] 时发生了异常 "+e.getMessage());
            throw new RuntimeException(e);
        }
        // 5. 通过response获取网页实体
        if(response!=null){
            // 获取返回实体
            HttpEntity entity = response.getEntity();
            // 判断返回状态是否是200
            if(response.getStatusLine().getStatusCode()==200) {

                try {
                    // 获取网页中的内容
                    webContent = EntityUtils.toString(entity, "utf-8");
                } catch (ParseException | IOException e) {
                    System.out.println("解析链接["+url+"]返回的实体异常 "+e.getMessage());
                    e.printStackTrace();
                }
            }else{
                System.out.println("获取链接["+url+"]返回的状态是  "+response.getStatusLine().getStatusCode());
            }
        }else{
            System.out.println("请求 ["+url+"] 时 链接超时");
        }
        // 释放资源
        closeHttp(response,httpClient);
        return webContent;
    }

    /**
     * 发送请求 返回response对象
     * 注意： 资源需要自行关闭
     * @param url
     * @return
     */
    public static CloseableHttpResponse getResponse(String url){
        // 1. 创建HttpClient对象 用来发送http请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 2. 创建HttpGet对象 用来发送get请求
        HttpGet httpGet = new HttpGet(url);
        // 3. 配置请求消息
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000) // 连接超时时间 单位毫秒
                .setSocketTimeout(1 * 1000 * 10) // 读取超时时间 单位毫秒
                .build();
        httpGet.setConfig(config);

        // 4. 创建response对象 用来接收返回的数据
        CloseableHttpResponse response = null;
        try {
            // 执行get请求 获取数据
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            System.out.println("请求 ["+url+"] 时发生了异常 "+e.getMessage());
            throw new RuntimeException(e);
        }

        return response;
    }

    private static void closeHttp(CloseableHttpResponse response, CloseableHttpClient httpClient){
        try {
            if(response!=null)
                response.close();
            if(httpClient!=null)
                httpClient.close();
        } catch (IOException e) {
            System.out.println("释放资源发生错误 ");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String s = send("https://www.cnblogs.com/");
        System.out.println(s);
    }
}
