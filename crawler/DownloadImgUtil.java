package com.et.crawler;

import com.et.util.DateUtil;
import com.et.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  19:09
 * @Description: TODO 下载图片工具类
 * @Version: 1.0
 */
public class DownloadImgUtil {

    /**
     * 下载图片到本地
     * @param imageList 存放的是网页中获取到的图片地址
     * @param realPath 图片本地保存的路径
     * @return
     */
    public static Map<String,Object> download(List<String> imageList,String realPath){
        Map<String,Object> map = new HashMap<>();
        for (String link : imageList) {
            // 剔除小图标的抓取
            String s = link.substring(link.length()-4, link.length());
            if(s.contains("gif")) {
                continue;
            }
            try {
                // 每次下载间隔1秒 防止访问过快被封禁
                Thread.sleep(1000);
                // 执行请求
                CloseableHttpResponse response = HttpUtil.getResponse(link);
                if(response!=null) {
                    // 获取返回实体
                    HttpEntity entity = response.getEntity();
                    // 判断返回状态是否是200
                    if(response.getStatusLine().getStatusCode()==200) {
                        try {
                            // 将图片转为InputStream
                            InputStream is = entity.getContent();
                            // 文件类型 例如 image/jpg   image/png
                            String imageType = entity.getContentType().getValue();
                            String b = imageType.split("/")[1];

                            // common-io.FileUtils .copyToFile 直接将文件下载到本地指定目录
                            String uuid = UUID.randomUUID().toString();
                            // 图片在项目中的位置
                            String fileURI = DateUtil.currentDatePath()+"/"+uuid+"."+b;
                            // 图片保存的地址
                            FileUtils.copyToFile(is,new File(realPath+"/static/blogImages/"+fileURI));
                            // 封装图片信息返回map
                            map.put(link, PropertiesUtil.getValue("imageUrl")+fileURI);
                        } catch (UnsupportedOperationException | IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println("获取链接["+link+"]返回的状态是  "+response.getStatusLine().getStatusCode());
                    }
                }else{
                    System.out.println("请求 ["+link+"] 时 链接超时");
                }
                // 关闭资源
                try {
                    if(response!=null)
                        response.close();
                } catch (IOException e) {
                    System.out.println("释放资源发生错误 ");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("https://img2024.cnblogs.com/blog/2462804/202406/2462804-20240602140202065-601606452.png");
        Map<String, Object> map = download(list, "d:/");
        System.out.println(map);
    }
}
