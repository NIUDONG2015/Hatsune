package com.pang.hatsune.dehtml;

import android.provider.DocumentsContract;

import com.pang.hatsune.acache.ACache;
import com.pang.hatsune.data.DATA;
import com.pang.hatsune.http.HttpResquestPang;
import com.pang.hatsune.info.EchoHotInfo;
import com.pang.hatsune.info.Fragment2ChannelHorizontalInfo;
import com.pang.hatsune.info.gsonfactory.Fragment4CelebrityStartinfo;
import com.pang.hatsune.utils.StringFilter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pang on 2016/7/27.
 */
public class DeHtml {
    private volatile static DeHtml instance;//volatile  轻量级同步锁
    private ACache aCache;//缓存网络请求的字符串

    protected DeHtml() {
    }

    public static DeHtml getInstance() {//仿imageLoader的单例模式

        if (instance == null) {
            synchronized (DeHtml.class) {
                if (instance == null) {
                    instance = new DeHtml();
                }
            }
        }
        return instance;
    }


    /**
     * 获取频道页面的viewpager图片<br/>使用解析html的框架
     *
     * @param jsonString
     * @return
     */
    public HashMap<String, String> getChannelViewPagerImage(String jsonString) {
        HashMap<String, String> list = new HashMap<String, String>();
        Document doc = Jsoup.parse(jsonString);//解析html 文本代码
        Elements div = doc.select(".chn-left_content");
        Iterator<Element> it = div.iterator();
//        System.out.println("hhtjim0:"+jsonString);
//        System.out.println("hhtjim0:"+div.select("h4").text());

        int i = 0;
        while (it.hasNext()) {
            Element divChild = it.next();
            String title = divChild.select("h4").text();
            String url = divChild.attr("style");

            Pattern p = Pattern.compile("\\(([^)]+?)\\-\\d+\\)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(url);
            while (m.find()) {//while循环操作
                url = m.group(1);
            }
//            System.out.println("hhtjim:"+title);
//            System.out.println("hhtjim:"+url);
            list.put(title, url);
            i++;
        }

        return list;
    }

    /**
     * 获取频道页面的 频道分类 数据 无图片
     *
     * @param jsonString
     * @return
     */
    public ArrayList<String> getChannelClassname(String jsonString) {
        ArrayList<String> list = new ArrayList<String>();

        Pattern p = Pattern.compile("style='color:#999'\\s*?href=\"[^\"]+?\">(.*?)</a>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(jsonString);
//        System.out.println("hhtjim:--======start");
        while (m.find()) {//while循环操作
            String t = m.group(1).trim();
//            System.out.println(t);
            list.add(t);
        }
//            System.out.println("hhtjim:"+title);
//            System.out.println("hhtjim:"+url);
        return list;
    }


    /**
     * 使用正则表达式解析获取频道分类的最新，热门 的频道数据
     *
     * @return
     */
    public ArrayList<Fragment2ChannelHorizontalInfo> getHotAndNewData(String htmlString) {
        ArrayList<Fragment2ChannelHorizontalInfo> list = new ArrayList<Fragment2ChannelHorizontalInfo>();
        String regx = "<a href=\"[^\"]+?(\\d+)\">[\\s\\S]+?\\(([^\\)]+?)(?:-\\d+)?\\)[\\s\\S]+?<h4>([^</h4>]+?)</h4>[\\s\\S]+?</a>";
//        System.out.println("hhtjim7878："+htmlString);

        Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlString);
//        System.out.println("====---===:"+matcher.find()+":"+htmlString);
//        matcher.reset();
        while (matcher.find()) {
            Fragment2ChannelHorizontalInfo info = new Fragment2ChannelHorizontalInfo();
            info.setId(matcher.group(1));
            info.setUrl(matcher.group(2));
            info.setName(matcher.group(3));
            list.add(info);
//            System.out.println(matcher.group(1));
//            System.out.println(matcher.group(2));
//            System.out.println(matcher.group(3));
//            System.out.println("-------");
        }
        return list;
    }


    /**
     * 使用正则表达式解析获取Echo页面的本周，今日的热门数据//
     *
     * @return
     */
    public EchoHotInfo getEchoHotData(String htmlString) {
        EchoHotInfo info = new EchoHotInfo();
        String regx = "";
        Pattern hotDataPattern = null;
        Matcher hotDataMatcher = null;

//        String regx = "<a href=\"[^\"]+?(\\d+)\">[\\s\\S]+?\\(([^\\)]+?)(?:-\\d+)?\\)[\\s\\S]+?<h4>([^</h4>]+?)</h4>[\\s\\S]+?</a>";


//		regx = "data-sid=\"([\\d,]+)\">月榜</i>";
        regx = "data-sid=\"([\\d,]+)\">\\s*周榜\\s*</i>";
        hotDataPattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        hotDataMatcher = hotDataPattern.matcher(htmlString);

        String weekIdArr[] = null;//周榜数组
        while (hotDataMatcher.find()) {
            weekIdArr = hotDataMatcher.group(1).split(",");
        }


//        String hotIdArr[] = null;//热门榜单数组
//        regx = "热门榜单\\s*<i class=\"play-all js-mp-play-one\" data-sid=\"([\\d,]+)\">";
//        hotDataPattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
//        hotDataMatcher = hotDataPattern.matcher(htmlString);
//        while (hotDataMatcher.find()) {
//            hotIdArr = hotDataMatcher.group(1).split(",");
//        }


//        regx = "<a href=\"/sound/(\\d+)\">[\\s\\S]+?<img src=\"([^\"\\?]+)(?:\\?[^\">]+)?\">[\\s\\S]+?<h4>([^>]+?)</h4>\\s+<h5>([^>]+?)</h5>";//去掉图片裁剪尾巴
        regx = "<a href=\"/sound/(\\d+)\">[\\s\\S]{1,300}<img src=\"([^\"]+)\">[\\s\\S]+?<h4>([^>]+?)</h4>\\s+<h5>([^>]+?)</h5>";//去掉图片裁剪尾巴
        hotDataPattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        hotDataMatcher = hotDataPattern.matcher(htmlString);
        List<EchoHotInfo.DayHotListBean> dayList = new ArrayList<EchoHotInfo.DayHotListBean>();
        List<EchoHotInfo.WeekHotListBean> weekList = new ArrayList<EchoHotInfo.WeekHotListBean>();
        w1:
        while (hotDataMatcher.find()) {
            EchoHotInfo.WeekHotListBean week = new EchoHotInfo.WeekHotListBean();
//            EchoHotInfo.DayHotListBean day = new EchoHotInfo.DayHotListBean();
//            w2:
//            for (String id : hotIdArr) {
//                if (id.equals(hotDataMatcher.group(1))) {
//                    day.setId(Integer.valueOf(hotDataMatcher.group(1)));
//                    day.setChannel(hotDataMatcher.group(4));
//                    day.setPic(StringFilter.getInstance().replaceWH(hotDataMatcher.group(2)));
//                    day.setTitle(hotDataMatcher.group(3));
//                    dayList.add(day);
//                    continue w1;
//                }
//            }


            for (String id : weekIdArr) {
                if (id.equals(hotDataMatcher.group(1))) {
                    week.setId(Integer.valueOf(hotDataMatcher.group(1)));
                    week.setUsername(hotDataMatcher.group(4));
                    week.setPic(StringFilter.getInstance().replaceWH(hotDataMatcher.group(2)));
                    week.setTitle(hotDataMatcher.group(3));
                    weekList.add(week);
                    continue w1;
                }
            }

//            info.set
//            System.out.println(m.group(1)); //id
//            System.out.println(m.group(2));//pic
//            System.out.println(m.group(3));//sound name
//            System.out.println(m.group(4));//channel or username
        }

        info.setWeekHotList(weekList);


        /**
         * 正则匹配今日热门的数据
         */
        regx = "<a href=\"/sound/(\\d+)\">(?:<img src=\"([^\"]+)\"></a></div>\\s*<a class=\"song-title\" href=\"/sound/\\1\">)?([^<]+)</a>";
        hotDataPattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        hotDataMatcher = hotDataPattern.matcher(htmlString);
        while (hotDataMatcher.find()) {
            EchoHotInfo.DayHotListBean day = new EchoHotInfo.DayHotListBean();
            day.setId(Integer.valueOf(hotDataMatcher.group(1)));
            String pic = hotDataMatcher.group(2);
            if (pic != null) {
                day.setPic(StringFilter.getInstance().replaceWH(hotDataMatcher.group(2)));
            }
            day.setTitle(hotDataMatcher.group(3));
            dayList.add(day);

//            System.out.println("-----------");
//            System.out.println(hotDataMatcher.group(1)); //id
//            System.out.println(hotDataMatcher.group(2)); //pic
//            System.out.println(hotDataMatcher.group(3));//name
        }

        info.setDayHotList(dayList);
        return info;
    }

    /**
     * 初音群星
     *
     * @param htmlString
     * @return
     */
    public ArrayList<Fragment4CelebrityStartinfo> getCelebrityStarts(String htmlString) {
        ArrayList<Fragment4CelebrityStartinfo> list = new ArrayList<Fragment4CelebrityStartinfo>();

        Pattern pattern = Pattern
                .compile("<img src=\"([^\"]+)\"></a>\\s+<a class=\"name\" href=\"/user/\\d+\">([^<]+)</a>\\s+<h5>([^<]+)</h5>");
        Matcher m = pattern.matcher(htmlString);

        while (m.find()) {
//            System.out.println(m.group(1));// image
//            System.out.println(m.group(2));// 明星
//            System.out.println(m.group(3));// desc

            Fragment4CelebrityStartinfo info = new Fragment4CelebrityStartinfo();
            info.setName(StringFilter.getInstance().replaceWH(m.group(2), 100));
            info.setPic(m.group(1));
            info.setDescOrChannel(m.group(3));
            list.add(info);
        }
//        System.out.println(list.size());
//        System.out.println("-----------------");
        return list;
    }


    /**
     * 精选MV
     *
     * @param htmlString
     * @return
     */
    public ArrayList<Fragment4CelebrityStartinfo> getCelebrityMvs(String htmlString) {
        ArrayList<Fragment4CelebrityStartinfo> list = new ArrayList<Fragment4CelebrityStartinfo>();

        Pattern pattern = Pattern
                .compile("<a class=\"pic\" href=\"/mv/(\\d+)\">\\s+<img src=\"([^\"]+)\">[\\s\\S]*?</a>\\s*<a class=\"mv-title\" href=\"/mv/\\1\">([^<]+)</a>\\s*<a class=\"mv-channel\" href=\"/user/\\d+\">([^<]+)</a>");

        Matcher m = pattern.matcher(htmlString);

        while (m.find()) {
//			System.out.println(m.group(1));// mv ID
//			System.out.println(m.group(2));// image
//			System.out.println(m.group(3));// name
//			System.out.println(m.group(4));// 频道名称


            Fragment4CelebrityStartinfo info = new Fragment4CelebrityStartinfo();
            info.setMvIdOrStartId(m.group(1));
            info.setPic(m.group(2));
            info.setDescOrChannel(m.group(4));
            info.setName(m.group(3));
            list.add(info);
        }
//        System.out.println(list.size());
//        System.out.println("-----------------");
        return list;
    }


    /**
     * 初音推荐 明星
     *
     * @param htmlString
     * @return
     */
    public ArrayList<Fragment4CelebrityStartinfo> getCelebrityRecommendStarts(String htmlString) {
        ArrayList<Fragment4CelebrityStartinfo> list = new ArrayList<Fragment4CelebrityStartinfo>();

        Pattern pattern = Pattern
                .compile("<img src=\"([^\"]+)\">\\s*</a>\\s*<div class=\"hgroup\">\\s*<h4><a href=\"/user/\\d+\">([^<]+)</a></h4>\\s*<span>([^<]+)</span>");
        Matcher m = pattern.matcher(htmlString);

        while (m.find()) {
//            System.out.println(m.group(1));// image
//            System.out.println(m.group(2));// 明星名字
//            System.out.println(m.group(3));// desc

            Fragment4CelebrityStartinfo info = new Fragment4CelebrityStartinfo();
            info.setPic(m.group(1));
            info.setName(m.group(2));
            info.setDescOrChannel(m.group(3));
            list.add(info);
        }

//        System.out.println(list.size());
//        System.out.println("-----------------");

        return list;
    }


}
