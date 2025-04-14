package com.tabnote.server.tabnoteserverboot.component;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TagsListProcess {

    class TagCount {
        String tag;
        int count;
        public TagCount(String tag) {
            this.tag = tag;
            this.count = 1;
        }
    }

    public List<String> tagsListChoiceBestTradition(List<String> tagsList,int size){
       List<TagCount> tagCountList = new ArrayList<>();
        for(String tags : tagsList){
            String nsTags = tags.replaceAll(" ", "");
            String[] tagList = nsTags.split("#");
            for (int j = 0; j < tagList.length && j < 100; j++) {
                boolean found = false;
                for (TagCount tc:tagCountList){
                    if (tc.tag.equals(tagList[j])){
                        tc.count++;
                        found = true;
                        break;
                    }
                }
                if (!found){
                    tagCountList.add(new TagCount(tagList[j]));
                }
            }
        }
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int max = 0;
            String maxTag = "";
            for (TagCount tc:tagCountList){
                if (tc.count > max){
                    maxTag = tc.tag;
                    max = tc.count;
                }
            }
            tags.add(maxTag);
        }

        return tags;
    }


    public List<String> tagsListChoiceBestModern(List<String> tagsList, int size) {
        Map<String, Integer> tagCountMap = new HashMap<>();

        // 统计标签出现次数
        for (String tags : tagsList) {
            String nsTags = tags.replaceAll(" ", "");
            String[] tagList = nsTags.split("#");
            for (String tag : tagList) {
                // 仅处理非空标签
                if (!tag.isEmpty()) {
                    tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                }
            }
        }

        // 使用一个优先队列来排序计算
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>(
                (a, b) -> b.getValue() - a.getValue()
        );

        maxHeap.addAll(tagCountMap.entrySet());

        List<String> result = new ArrayList<>();
        // 获取指定数量的标签
        for (int i = 0; i < size && !maxHeap.isEmpty(); i++) {
            result.add(maxHeap.poll().getKey());
        }

        return result;
    }

}
