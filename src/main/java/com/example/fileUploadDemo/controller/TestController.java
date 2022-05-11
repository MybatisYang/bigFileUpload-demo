package com.example.fileUploadDemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.fileUploadDemo.service.FileManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: yjs
 * @createTime: 2022年05月11日 09:54:56
 * @version: 1.0
 */
@RestController
public class TestController {

    @Autowired
    FileManageService fileManageService;

    @Autowired
    RedisTemplate stringRedisTemplate;
    //private RedisTemplate<String, Object> redisTemplate;

    public void setRedisTemplate(RedisTemplate<String, Object> stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    /**
     * @param fileName 待分割的文件名 例：nginx.tar
     * @return  key
     */
    @GetMapping("/cutFile")
    @ResponseBody
    public String cutFile(String fileName){
        String key = String.valueOf(System.currentTimeMillis())+"-"+ fileName+"-key";
        stringRedisTemplate.boundValueOps(key).set("start");
        stringRedisTemplate.expire(key, 10, TimeUnit.MINUTES);

        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                List<String> fileNames = fileManageService.cutFile(fileName);
                if (CollectionUtils.isEmpty(fileNames)){
                    stringRedisTemplate.boundValueOps(key).set("failed");
                    stringRedisTemplate.expire(key, 1, TimeUnit.MINUTES);
                }

                if (!CollectionUtils.isEmpty(fileNames)){
                    stringRedisTemplate.boundValueOps(key).set(JSONObject.toJSONString(fileNames));
                    stringRedisTemplate.expire(key, 2, TimeUnit.MINUTES);
                }
            }
        });
        //返回key
        return key;
    }

    /**
     * @param cutFileName 任意一个分段文件名,例：1591604609899-1-redis.tar
     * @param chunks 分段总数
     * @return
     */
    @GetMapping("/merageFile")
    @ResponseBody
    public String merageFile(@RequestParam String cutFileName,
                             @RequestParam int chunks) throws IOException {
        return fileManageService.merageFile(cutFileName, chunks);
    }

}
