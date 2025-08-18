package com.hollywood.sweetspot.places;

import org.springframework.stereotype.Component;
import java.util.*;

/** 우리 카테고리 → 카카오 그룹코드 + 보조 키워드 */
@Component
public class CategoryMapper {
    public static record KakaoQuery(String groupCode, List<String> keywordHints) {}
    private static final Map<String, KakaoQuery> MAP = new HashMap<>();
    static {
        MAP.put("cafe", new KakaoQuery("CE7", List.of()));
        MAP.put("korean",   new KakaoQuery("FD6", List.of("한식")));
        MAP.put("japanese", new KakaoQuery("FD6", List.of("일식","초밥","스시")));
        MAP.put("chinese",  new KakaoQuery("FD6", List.of("중식","중국요리")));
        MAP.put("western",  new KakaoQuery("FD6", List.of("양식","이탈리안","파스타")));
        MAP.put("dessert",  new KakaoQuery("FD6", List.of("디저트","빙수","케이크")));
        MAP.put("bar_pub",  new KakaoQuery("FD6", List.of("바","펍","와인바","맥주")));
        MAP.put("bakery",   new KakaoQuery("FD6", List.of("베이커리","빵집")));
        MAP.put("brunch",   new KakaoQuery("FD6", List.of("브런치")));
        MAP.put("bbq",      new KakaoQuery("FD6", List.of("고기","구이","삼겹살","바베큐")));
        MAP.put("seafood",  new KakaoQuery("FD6", List.of("해산물","회")));
        MAP.put("buffet",   new KakaoQuery("FD6", List.of("뷔페","부페")));
        MAP.put("fastfood", new KakaoQuery("FD6", List.of("패스트푸드","버거","치킨")));
        MAP.put("attraction", new KakaoQuery("AT4", List.of("명소","전시","박물관")));
    }
    public KakaoQuery map(String ourCategory) {
        if (ourCategory == null) return null;
        return MAP.get(ourCategory);
    }
}