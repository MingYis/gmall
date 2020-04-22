package com.my.gmall.product.controller;

import com.my.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mmy
 * @create 2020-04-21 15:18
 */
@RestController
@RequestMapping("/admin/product")
public class FileController {

    @Value("${image.url}")
    private String imageUrl;

    //上传图片 （Spu）添加
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{

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

        //获取文件扩展名
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        //3.保存文件，并返回file_id将来可以用来访问获取之前保存的文件
        String path = storageClient1.upload_file1(
                file.getBytes(), extension, null);

        //group1/M00/00/00/wKjIgF6e3NmAKsW6AABhj__VRmw114.jpg
        System.out.println(imageUrl + path);
        return Result.ok(imageUrl + path);
    }
}
