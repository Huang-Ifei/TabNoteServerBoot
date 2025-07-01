package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.component.TagsListProcess;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.ClassMapper;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.models.TabNoteForList;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.redis.LikeCount;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import com.tabnote.server.tabnoteserverboot.services.inteface.TabNoteServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TabNoteServiceImpl implements TabNoteServiceInterface {

    TabNoteMapper tabNoteMapper;
    ClassMapper classMapper;
    AccountMapper accountMapper;
    FileServiceInterface fileService;
    LikeCount likeCount;
    VipMapper vipMapper;
    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }
    @Autowired
    public void setTabNoteMapper(TabNoteMapper tabNoteMapper) {
        this.tabNoteMapper = tabNoteMapper;
    }
    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
    @Autowired
    public void setFileService(FileServiceInterface fileService) {
        this.fileService = fileService;
    }
    @Autowired
    public void setLikeCount(LikeCount likeCount) {
        this.likeCount = likeCount;
    }

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tabNoteInfiniteEncryption = tie;
    }

    TagsListProcess tagsListProcess;
    @Autowired
    public void setTagsListProcess(TagsListProcess tagsListProcess) {
        this.tagsListProcess = tagsListProcess;
    }

    QuotaDeductionPublisher quotaDeductionPublisher;
    @Autowired
    public void setQuotaDeductionPublisher(QuotaDeductionPublisher quotaDeductionPublisher) {
        this.quotaDeductionPublisher = quotaDeductionPublisher;
    }

    //获取分类
    @Override
    public JSONObject getClasses() {
        JSONObject json = new JSONObject();
        try {
            List<HashMap<String, String>> classes = classMapper.getClasses();
            json.putArray("classes");
            for (HashMap<String, String> map : classes) {
                json.getJSONArray("classes").add(map.get("class_name").toString());
            }
            json.put("response", "success");
        } catch (Exception e) {
            json.put("response", "failed");
            e.printStackTrace();
        }
        return json;
    }

    //获取推荐标签
    @Override
    public JSONObject tagsRecommended(String id) {
        JSONObject json = new JSONObject();
        HashSet<String> tags = new HashSet<>();
        HashSet<String> userSetTags = new HashSet<>();
        try {
            String lastReadTabNoteId = tabNoteMapper.selectLastTabNoteId(id);
            Map<String, Object> uat = tabNoteMapper.selectUTD(lastReadTabNoteId);
            Timestamp lastReadTabNoteDateTime = (Timestamp) uat.get("date_time");
            //连读推荐（最近浏览的作者的下一篇文章）
            String lastReadTabNoteUserId = (String) uat.get("usr_id");
            String untnn = tabNoteMapper.selectUserNextTNN(lastReadTabNoteUserId, lastReadTabNoteDateTime);
            if (untnn != null) {
                userSetTags.add(untnn);
            }
            //连读推荐（最近浏览的所有标签的下一篇文章）
            String lastReadTabNoteTags = (String) uat.get("tags");
            lastReadTabNoteTags = lastReadTabNoteTags.replaceAll(" ", "");
            String[] lrtnts = lastReadTabNoteTags.split("#");
            for (int i = 0; i < lrtnts.length && i < 100; i++) {
                String tagFindNext = tabNoteMapper.selectTagNextTNN(lrtnts[i], lastReadTabNoteDateTime);
                if (tagFindNext != null) {
                    userSetTags.add(tagFindNext);
                }
            }
        } catch (NullPointerException e) {
            System.out.println("推荐系统无法查询上次读取的内容，用户id为：" + id);
        } catch (Exception e) {
            json.put("response", "failed");
            e.printStackTrace();
        }
        //最近访问的15个贴文的标签进行统计选出最高的3个
        try {
            List<String> ults = tabNoteMapper.selectUserLastTags(id);
            List<String> userTags = tagsListProcess.tagsListChoiceBestModern(ults, 3);
            tags.addAll(userTags);
        } catch (NullPointerException e) {
            System.out.println("推荐系统无法查询最后读取15条的内容，用户id为：" + id);
        } catch (Exception e) {
            json.put("response", "failed");
            e.printStackTrace();
        }
        //最新的30个贴文的标签进行统计选出最高的5个
        try {
            List<String> alts = tabNoteMapper.selectAllLastTags();
            List<String> allTags = tagsListProcess.tagsListChoiceBestModern(alts, 5);
            tags.addAll(allTags);
        } catch (NullPointerException e) {
            System.out.println("推荐系统无法查询最近30个贴文");
        } catch (Exception e) {
            json.put("response", "failed");
            e.printStackTrace();
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(userSetTags);
        jsonArray.addAll(tags);
        json.put("tags", jsonArray);
        json.put("response", "success");

        return json;
    }

    //获取所有的贴文数量并计算页数
    @Override
    public JSONObject getPageCount() {
        JSONObject returnJSON = new JSONObject();
        try {
            int count = tabNoteMapper.getTabNotePages();
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pagesCount", count);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    //不设关键词获取某一页
    @Transactional
    @Override
    public JSONObject getPageTabNotes(int page) {
        long startTime1 = System.currentTimeMillis();

        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.getTabNote(start);
            returnJSON.putArray("list");

            long startTime2 = System.currentTimeMillis();

            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            System.out.println("****JSON build time(ms)：" + (System.currentTimeMillis() - startTime2));
            int count = tabNoteMapper.getTabNotePages();
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pages", count);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
            throw e;
        }
        System.out.println("****get tab note time(ms)：" + (System.currentTimeMillis() - startTime1));
        return returnJSON;
    }

    //点赞
    @Transactional(noRollbackFor = {DuplicateKeyException.class})
    @Override
    public JSONObject likeTabNote(String tabNoteId, String id, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id, token)) {
                likeCount.likeTabNote(tabNoteId, id);
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (DuplicateKeyException e) {
            returnJSON.put("response", "click");
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            e.printStackTrace();
            throw e;
        }
        return returnJSON;
    }

    //获取某个
    @Transactional
    @Override
    public JSONObject getTabNote(String tabNoteId, String id, String token) {
        JSONObject returnJSON = new JSONObject();

        try {

            if (!id.isEmpty() && !token.isEmpty() && tabNoteInfiniteEncryption.encryptionTokenCheckIn(id, token)) {
                returnJSON.put("is_liked", tabNoteMapper.isLiked(tabNoteId, id));
                try {
                    tabNoteMapper.clickNote(tabNoteId, id);
                    tabNoteMapper.clickThis(tabNoteId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                returnJSON.put("is_liked", 0);
            }
            TabNote tabNote = tabNoteMapper.getTabNoteById(tabNoteId);
            returnJSON.put("usr_id", tabNote.getUsr_id());
            returnJSON.put("usr_name", accountMapper.getNameById(tabNote.getUsr_id()));
            returnJSON.put("ip_address", tabNote.getIp_address());
            returnJSON.put("class_name", tabNote.getClass_name());
            returnJSON.put("tab_note_name", tabNote.getTab_note_name());
            returnJSON.put("tags", tabNote.getTags());
            returnJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteId));
            returnJSON.put("click", tabNote.getClick());
            returnJSON.put("tab_note", tabNote.getTab_note());
            returnJSON.put("file", tabNote.getFile());
            returnJSON.put("imgs", tabNote.getImages());
            returnJSON.put("display", tabNote.getDisplay());
            returnJSON.put("date_time", tabNote.getDate_time());

            returnJSON.put("response", "success");
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            throw e;
        }

        return returnJSON;
    }

    @Transactional
    @Override
    public JSONObject insertTabNote(String token, String usr_id, String ip_address, String class_name, String tab_note_name, String tags, String tab_note, String base64FileString, JSONArray imgs, int display) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
                RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
                if (rankAndQuota == null || rankAndQuota.passAFAPP()) {
                    String date_time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String tab_note_id = usr_id.hashCode() + "" + System.currentTimeMillis();

                    if (base64FileString.isEmpty() && imgs.isEmpty()) {
                        tabNoteMapper.insertTabNote(tab_note_id, usr_id, ip_address, class_name, tab_note_name, tags, tab_note, date_time, display);
                        returnJSON.put("response", "success");
                    } else {
                        //兼容旧版
                        String fileName = "";
                        JSONObject imgJson = new JSONObject();
                        imgJson.putArray("images");
                        try {
                            if (!imgs.isEmpty()) {
                                for (int i = 0; i < imgs.size(); i++) {
                                    imgJson.getJSONArray("images").add(fileService.insertImgWithOutIdCheck(imgs.getString(i)));
                                }
                            }
                        } catch (Exception e) {
                            returnJSON.put("response", "img_insert_failed");
                            return returnJSON;
                        }

                        //附件保存
                        try {
                            if (!base64FileString.isEmpty()) {
                                fileName = String.valueOf(fileService.insertFileWithOutIdCheck(base64FileString));
                            }
                        } catch (Exception e) {
                            returnJSON.put("response", "img_insert_failed");
                            return returnJSON;
                        }
                        tabNoteMapper.insertTabNoteWithFile(tab_note_id, usr_id, ip_address, class_name, tab_note_name, tags, tab_note, date_time, fileName, imgJson.toString(), display);
                        returnJSON.put("response", "success");
                    }
                } else {
                    returnJSON.put("response", "No AFA++");
                }
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
            throw e;
        }

        return returnJSON;
    }

    @Override
    public JSONObject deleteTabNote(String tabNoteId) {
        JSONObject returnJSON = new JSONObject();

        try {
            tabNoteMapper.deleteTabNote(tabNoteId);
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    @Override
    public JSONObject updateTabNote(String tab_note_id, String ip_address, String tab_note_name, String tags, String tab_note, String date_time) {
        JSONObject returnJSON = new JSONObject();

        try {
            tabNoteMapper.updateTabNote(tab_note_id, ip_address, tab_note_name, tags, tab_note, date_time);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }


    @Override
    @Transactional
    public JSONObject searchTabNote(String key, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNote(key, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNotePages(key);
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
            throw e;
        }
        return returnJSON;
    }

    @Override
    @Transactional
    public JSONObject searchTabNoteWithCls(String className, String key, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteWithCls(className, key, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteWithClsPages(className, key);
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
            throw e;
        }
        return returnJSON;
    }


    @Override
    @Transactional
    public JSONObject searchTabNoteById(String id, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteById(id, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteByIdPages(id);
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    @Override
    @Transactional
    public JSONObject searchTabNoteByClass(String className, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteByClass(className, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteByClassPages(className);
            if (count % 20 == 0) {
                count = count / 20;
            } else {
                count = count / 20 + 1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }
}
