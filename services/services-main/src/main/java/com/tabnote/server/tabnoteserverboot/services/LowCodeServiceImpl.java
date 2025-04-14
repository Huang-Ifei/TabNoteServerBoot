package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.LowCodeMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.services.inteface.LowCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LowCodeServiceImpl implements LowCode {
    VipMapper vipMapper;

    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

    LowCodeMapper lowCodeMapper;

    @Autowired
    public void setLowCodeMapper(LowCodeMapper lowCodeMapper) {
        this.lowCodeMapper = lowCodeMapper;
    }


    @Override
    @Transactional
    public JSONObject insertHuffmanLCID(String usr_id, String token,String language, String environment, String save) {
        JSONObject obj = new JSONObject();
        StringBuilder filePath = new StringBuilder();
        filePath.append("low_code/");
        int cq = 0;
        if (language.equals("Java") && environment.equals("JDK17")) {
            filePath.append("Huffman-Java-JDK17");
            cq = 1500000;
        } else if (language.equals("Java") && environment.equals("JDK8")) {
            filePath.append("Huffman-Java-JDK8");
            cq = 1800000;
        } else if ((language.equals("C") || language.equals("C++")) && (environment.equals("Visual Studio"))) {
            filePath.append("Huffman-C");
            cq = 2000000;
        }
        if (save.equals("save")) {
            filePath.append("-save");
            cq += 10000;
        }else{
            filePath.append("-remove");
        }
        filePath.append(".zip");
        try {
            String lc = lowCodeMapper.isDone(usr_id,"huffman",filePath.toString());
            if (lc!=null){
                obj.put("id", lc);
                obj.put("response", "success");
                return obj;
            }else{
                String vip_id = vipMapper.selectVipIdByUserId(usr_id);
                int have = vipMapper.selectQuotaByVipId(vip_id);
                if (have > cq) {
                    System.out.println(usr_id+"扣款"+cq);
                    vipMapper.useQuota(cq,vip_id);
                } else {
                    obj.put("response", "quota_low");
                    return obj;
                }
                UUID uuid = UUID.randomUUID();
                lowCodeMapper.insertLowCode(uuid.toString(),usr_id,"huffman",filePath.toString());
                obj.put("id", uuid.toString());
                obj.put("response", "success");
            }
        } catch (Exception ex) {
            throw ex;
        }
        return obj;
    }

    @Override
    public Resource getFile(String lc_id){
        String path = lowCodeMapper.getLowCode(lc_id);
        Resource resource = new FileSystemResource(path);
        return resource;
    }
}
