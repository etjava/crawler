package com.et.crawler;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author: ETJAVA
 * @CreateTime: 2024-06-02  18:35
 * @Description: TODO 抓取CNBlog中的文章信息
 * @Version: 1.0
 */
public class CNBlog {

    // 首页地址
    private static final String URL = "https://www.cnblogs.com/";

    // 博客中图片存放的位置
    private static String realPath="C:\\Users\\etjav\\Desktop\\blog\\target\\blog";

    public static void main(String[] args) {
        parseHome();
    }
    /**
     * 抓取首页内容
     */
    public static void parseHome(){
        // 发送http请求 获取首页内容
        String homeContent = HttpUtil.send(URL);
        // 解析首页内容 获取指定连接
        List<String> linkList = parseHomeLink(homeContent);
        // 判断该连接是否抓取过 如果没有则进行抓取 同时放到缓存中 避免重复抓取
        for (String link : linkList) {
            if(CacheUtil.getCache(link)!=null) {
                System.out.println("已抓取当前链接博客信息 " + URL);
                continue;
            }else {
                // 放到缓存中 避免二次抓取
                CacheUtil.openCache(link);
                // 根据链接地址解析博客内容
                String blogContent = HttpUtil.send(link);
                // 解析博客内容获取有效数据
                parseContent(blogContent,link);
            }
        }
        // 关闭缓存
        CacheUtil.shutdown();
    }

    /**
     * 解析博客内容 获取有效数据 返回替换后的文章内容
     * @param blogContent
     */
    private static void parseContent(String blogContent,String url) {
        if(StringUtils.isEmpty(blogContent)){
            return ;
        }
        // 解析内容获取Document对象
        Document document = Jsoup.parse(blogContent);
        // 获取标题
        Elements titles = document.select("#cb_post_title_url span");
        // 如果标题没有获取到 则舍弃当前文章
        if(titles.size()==0) {
            System.out.println(url + " - 未获取到标题内容");
            return ;
        }
        // 获取博客标题
        String title = titles.get(0).text();
        // 获取博客内容  - 包含标签的文章内容
        Elements contents = document.select("#cnblogs_post_body");
        List<String> imgList = new ArrayList<>();
        // 如果内容为空则舍弃
        if(contents.size()==0) {
            System.out.println(url + " - 未获取到博客内容");
            return ;
        }
        // 获取带有html标签的文章内容
        String content = contents.get(0).html();
        // 移除博客内容中的emojo符号
        String content2 = EmojiParser.removeAllEmojis(content);
        // 查找文章中所有img标签 将图片保存到本地指定目录
        Elements images = contents.select("img");

        for(int i=0;i<images.size();i++) {
            String imgUrl = images.get(i).attr("src");
            imgList.add(imgUrl);
        }

        // 替换文章中的图片路径
        if(imgList.size()>0) {
            // 下载图片到本地 返回 原始地址-新地址 map集合   后边需要替换到文章中的img src属性中
            Map<String, Object> imageMap = DownloadImgUtil.download(imgList,realPath);
            // 替换文章中图片地址 返被修改好之后的文章内容
            content2 = replaceContentImages(imageMap,content2);

            // 保存数据库中
            System.out.println("文章标题："+title);
            System.out.println("文章内容："+content2);
        }
    }

    /**
     * 替换文章中原因的图片路径为本地图片路径
     * @param imageMap 图片路径集合 key原始路径  value本地路径
     * @param content2 需要被替换的文章内容
     * @return
     */
    private static String replaceContentImages(Map<String, Object> imageMap, String content2) {
        for(String url:imageMap.keySet()) {
            String newPath = (String) imageMap.get(url);// 根据原始地址获取新的地址
            content2 = content2.replace(url, newPath);
        }
        return content2;
    }

    /*
    * 解析首页内容 获取指定的连接
    * */
    private static List<String> parseHomeLink(String homeContent) {
        if (StringUtils.isEmpty(homeContent)) {
            return null;
        }
        List<String> list = new LinkedList<>();
        // 解析首页内容获取document对象
        Document document = Jsoup.parse(homeContent);
        // 获取指定的链接地址  因为存在子的a标签 因此这里直接使用clss定位到需要的链接标签中
        Elements links = document.select("#post_list .post-item .post-item-body .post-item-text .post-item-title");
        for (int i = 0; i < links.size(); i++) {
            Element link = links.get(i);
            String url = link.attr("href");
            list.add(url);
            //System.out.println("获取到的连接地址：" + url);
        }
        return list;
    }

}
