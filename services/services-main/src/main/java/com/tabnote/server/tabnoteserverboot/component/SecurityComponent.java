package com.tabnote.server.tabnoteserverboot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class SecurityComponent {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityComponent.class);

    private List<String> wjcList = new ArrayList<>();

    private List<HashMap<String,String>> problemList = new ArrayList<>();

    public SecurityComponent() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("wjc"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.equals("\n") && !line.isEmpty()) {
                        wjcList.add(line);
                }
            }
            bufferedReader.close();
            log.info("风控系统已部署，共有" + wjcList.size() + "条风控词，被添加");
        } catch (Exception e) {
            log.error("风控系统错误");
            log.error(e.getMessage());
        }
    }

    public boolean haveProblemWord(String s,String ip,String id,String type) {
        for (int i = 0; i < wjcList.size(); i++) {
            if (s.contains(wjcList.get(i))) {
                HashMap<String,String> map = new HashMap<>();
                map.put("ip",ip);
                map.put("id",id);
                map.put("type",type);
                map.put("data",s);
                log.info("ip:"+map.get("ip")+"id:"+map.get("id")+"type:"+map.get("type")+"data:"+map.get("data")+"\n**************************************************\n\n");
                problemList.add(map);
                return true;
            }
        }
        return false;
    }

    @Scheduled(cron = "1 0 0 * * *")
    public void dailyReader(){
        try {
            List<String> list = new ArrayList<String>();

            BufferedReader bufferedReader = new BufferedReader(new FileReader("wjc"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.equals("\n") && !line.isEmpty()) {
                    list.add(line);
                }
            }
            wjcList = list;
            bufferedReader.close();
            log.info("风控系统已重写，共有" + wjcList.size() + "个风控词");
        } catch (Exception e) {
            log.error("风控系统错误");
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "1 0 * * * *")
    public void writeAllProblemDataPreHour(){
        try {
            if (!problemList.isEmpty()) {
                File file = new File("problemData");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(file,true);
                for (int i = 0; i < problemList.size(); i++) {
                    HashMap<String,String> map = problemList.remove(0);
                    fileWriter.write("ip:"+map.get("ip")+"id:"+map.get("id")+"type:"+map.get("type")+"data:"+map.get("data")+"\n**************************************************\n\n");
                }
                fileWriter.flush();
                fileWriter.close();
            }
        }catch (Exception e){
            log.error("风控系统错误");
            log.error(e.getMessage());
        }

    }

}
