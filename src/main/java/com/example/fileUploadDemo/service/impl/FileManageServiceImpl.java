package com.example.fileUploadDemo.service.impl;

import com.example.fileUploadDemo.service.FileManageService;
import com.example.fileUploadDemo.util.CutFileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * @Description: fileManageService
 * @Author: yjs
 * @createTime: 2022年05月11日 09:58:32
 * @version: 1.0
 */
@Service
public class FileManageServiceImpl implements FileManageService {
    @Value("${save_addr}")
    private String saveAddr;

    @Value("${save_addr1}")
    private String saveAddr1;

    @Override
    public List<String> cutFile(String fileName) {
        //待分片文件在主机上的路径
        String filePath = saveAddr + fileName;

        File file = new File(filePath);
        //分片文件的大小（字节）
        Long byteSize = 52428800L;
        List<String> fileNames = new CutFileUtil().cutFileBySize(filePath, byteSize, saveAddr);
        return fileNames;
    }

    @Override
    public String merageFile(String cutFileName, int chunks) throws IOException {
        int indexOf = cutFileName.indexOf("-");
        String timeStream = cutFileName.substring(0, indexOf);
        //段数+文件名+后缀名
        String substring = cutFileName.substring(indexOf + 1, cutFileName.length());
        int indexOf1 = substring.indexOf("-");
        //文件名+后缀名
        String fileName = substring.substring(indexOf1+1, substring.length());
        File file = new File(saveAddr+fileName);
        if (file.exists()){
            file.delete();
            System.out.println("覆盖已经存在的文件");
        }
        BufferedOutputStream destOutputStream = new BufferedOutputStream(new FileOutputStream(saveAddr+fileName));
        for (int i = 1; i <= chunks ; i++) {
            //循环将每个分片的数据写入目标文件
            byte[] fileBuffer = new byte[1024];//文件读写缓存
            int readBytesLength = 0; //每次读取字节数
            File sourceFile = new File(saveAddr+timeStream+"-"+i+"-"+fileName);
            BufferedInputStream sourceInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            System.out.println("开始合并分段文件："+timeStream+"-"+i+"-"+fileName);
            while ((readBytesLength = sourceInputStream.read(fileBuffer))!=-1){
                destOutputStream.write(fileBuffer, 0 , readBytesLength);
            }
            sourceInputStream.close();
            System.out.println("合并分段文件完成："+timeStream+"-"+i+"-"+fileName);
            //分片合并后删除
            boolean delete = sourceFile.delete();
            if (delete){
                System.out.println(timeStream+"-"+i+"-"+fileName+"删除完成");
            }
        }
        destOutputStream.flush();
        destOutputStream.close();
        return fileName+"合并完成";
    }
}
