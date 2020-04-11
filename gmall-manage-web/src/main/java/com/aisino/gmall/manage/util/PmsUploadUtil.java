package com.aisino.gmall.manage.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public class PmsUploadUtil {

    public static String uploadImage(MultipartFile multipartFile){

        String imgUrl = "http://192.168.2.170:8099";

        //上传服务器代码
        //获取fastdfs的tracker路径
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //新建一个trackerClient
        TrackerClient trackerClient = new TrackerClient();
        //获取到trackerServer
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //通过trackerServer连接一个storageClient
        StorageClient StorageClient = new StorageClient(trackerServer, null);
        //利用StorageClient上传文件
        try {
            //获得上传的二进制对象
            byte[] bytes = multipartFile.getBytes();

            //获得文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();
            //根据最后一个"."获取后缀名
            int i = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i + 1);

            //上传文件
            String[] uploadInfos = StorageClient.upload_file(bytes, extName, null);

            //循环遍历uploadInfos
            for (String uploadInfo : uploadInfos){
                //拼接imgUrl
                imgUrl += "/" + uploadInfo;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return  imgUrl;

    }
}
