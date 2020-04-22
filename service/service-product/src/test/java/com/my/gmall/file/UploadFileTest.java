package com.my.gmall.file;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

/**
 * @author mmy
 * @create 2020-04-21 11:11
 */
@RunWith(SpringRunner.class)
public class UploadFileTest {
    @Test
    public void uploadFile() throws Exception {
        //获取配置文件全路径，两种方式
        //第一种
//        ClassPathResource resource = new ClassPathResource("tracker.conf");
//        String allPath = resource.getClassLoader().getResource("tracker.conf").getPath();
        //第二种
        String allPath = ClassUtils.getDefaultClassLoader().getResource("tracker.conf").getPath();

        //初始化配置文件
        ClientGlobal.init(allPath); //IO流 不认识相对路径
        //1.连接Tracker跟踪器，获取存储节点的地址
        TrackerClient trackerClient = new TrackerClient();
        //Tracker跟踪器 服务器 返回的地址
        TrackerServer trackerServer = trackerClient.getConnection();

        //2.连接存储节点
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);

        //3.保存文件，并返回file_id将来可以用来访问获取之前保存的文件
        String path = storageClient1.upload_file1(
                "C:\\Users\\maingy\\Desktop\\tools\\图片文件\\12345.jpg",
                "jpg", null);
        //System.out.println(path);
        //http://192.168.200.128:8080/group1/M00/00/00/wKjIgF6e-2eAfUPoAAK-e9CbsaU255.png
        //group1/M00/00/00/wKjIgF6e3NmAKsW6AABhj__VRmw114.jpg

    }
}
