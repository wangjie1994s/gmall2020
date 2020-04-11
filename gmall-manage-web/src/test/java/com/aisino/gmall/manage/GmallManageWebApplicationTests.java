package com.aisino.gmall.manage;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GmallManageWebApplicationTests {
	/**
	 * fastdfs上传图片测试
	 * @throws Exception
	 */

	@Test
	public void contextLoads() throws Exception{

		//获取fastdfs的tracker路径
		String tracker = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();

		ClientGlobal.init(tracker);

		//新建一个trackerClient
		TrackerClient trackerClient = new TrackerClient();
		//获取到trackerServer
		TrackerServer trackerServer = trackerClient.getConnection();

		//通过trackerServer连接一个storageClient
		StorageClient StorageClient = new StorageClient(trackerServer, null);
		//利用StorageClient上传文件
		String[] uploadInfos = StorageClient.upload_file("/Users/wangjie/Downloads/95c273eda00e67c0.jpg", "jpg", null);

		String url = "http://192.168.2.170:8099";

		for (String uploadInfo : uploadInfos){
			url += "/" + uploadInfo;

		}
		System.out.println(url);

	}

}
