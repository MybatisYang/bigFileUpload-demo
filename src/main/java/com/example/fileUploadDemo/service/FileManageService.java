package com.example.fileUploadDemo.service;

import java.io.IOException;
import java.util.List;

public interface FileManageService {
    /**
     * 文件分割
     * @param fileName
     * @return
     */
    List<String> cutFile(String fileName);

    String merageFile(String cutFileName, int chunks)  throws IOException;
}
