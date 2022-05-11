package com.example.fileUploadDemo.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: CutFileUtil
 * @Author: yjs
 * @createTime: 2022年05月11日 09:59:29
 * @version: 1.0
 */
public class CutFileUtil {
    /**
     * @param filePath 文件所在主机的路径 例：/home/gyt/nginx.tar
     * @param byteSize 拆分文件字节大小
     * @param saveAddr 拆分后的文件保存目录  /homt/gyt/
     * @return
     */
    public List<String> cutFileBySize(String filePath, Long byteSize, String saveAddr){
        List<String> fileNames = new ArrayList<>();
        File file = new File(filePath);
        //计算总共段数
        int count = (int) Math.ceil(file.length()/(double)byteSize);
        int countLen = (count +"").length();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,1, TimeUnit.SECONDS,new ArrayBlockingQueue<>(count * 2));
        //时间戳
        String timeStamp = String.valueOf(System.currentTimeMillis());

        for (int i = 0; i < count; i++) {
            //分段文件名
            String fileName = timeStamp + "-" + leftPad((i+1) +"", countLen, '0') + "-" +file.getName();
            threadPoolExecutor.execute(new SplitRunnable(byteSize.intValue(), fileName, file, i*byteSize, saveAddr));
            fileNames.add(fileName);
        }
        threadPoolExecutor.shutdown();
        while (true){
            if (threadPoolExecutor.isTerminated()){
                return fileNames;
            }
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static String leftPad(String str, int length, char ch){
        if (str.length() >= length){
            return str;
        }
        char[] chs = new char[length];
        Arrays.fill(chs, ch);
        char[] src = str.toCharArray();
        System.arraycopy(src, 0, chs, length - src.length, src.length);
        return new String(chs);
    }

    private class SplitRunnable implements Runnable{
        int byteSize;
        String fileName;
        File originFile;
        Long startPos;
        String currentWorkDir;

        public SplitRunnable(int byteSize, String fileName, File originFile, Long startPos, String currentWorkDir) {
            this.byteSize = byteSize;
            this.fileName = fileName;
            this.originFile = originFile;
            this.startPos = startPos;
            this.currentWorkDir = currentWorkDir;
        }

        public void run(){
            RandomAccessFile randomAccessFile = null;
            OutputStream outputStream = null;
            try {
                randomAccessFile = new RandomAccessFile(originFile, "r");
                byte[] b = new byte[byteSize];
                randomAccessFile.seek(startPos); //移动指针到每“段”开头
                int s = randomAccessFile.read(b);
                outputStream = new FileOutputStream(currentWorkDir+fileName);
                outputStream.write(b, 0 , s);
                outputStream.flush();
                b= null;
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if (outputStream !=null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (randomAccessFile !=null){
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
