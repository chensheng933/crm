package com.huike.clues.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.huike.clues.service.ISysFileService;
import com.huike.common.config.MinioConfig;
import com.huike.common.core.domain.AjaxResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SysFileServiceImpl implements ISysFileService{

	@Autowired
	MinioConfig minioConfig;

	/**
	 * 文件上传至Minio
	 */
	@Override
	public AjaxResult upload(MultipartFile file) {
		InputStream inputStream = null;
		//创建Minio的连接对象
		MinioClient minioClient = getClient();
		String bucketName = minioConfig.getBucketName();
		try {
			inputStream = file.getInputStream();
			//判断文件存储的桶是否存在
			boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
			if (!found) {
				//如果桶不存在则创建通
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
			}
			//操作文件
			String fileName = file.getOriginalFilename();
			String objectName = new SimpleDateFormat("yyyy/MM/dd/").format(new Date()) + UUID.randomUUID().toString().replaceAll("-", "")
					+ fileName.substring(fileName.lastIndexOf("."));
			//文件上传
			//由于使用的是SpringBoot与之进行集成 上传的时候拿到的是MultipartFile 需要通过输入输出流的方式进行添加
			PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
					.bucket(bucketName)
					.contentType(file.getContentType())
					.stream(file.getInputStream(),file.getSize(),-1).build();
			minioClient.putObject(objectArgs);
			//封装访问的url给前端
			AjaxResult ajax = AjaxResult.success();
			ajax.put("fileName", "/"+bucketName+"/"+objectName);
			//url需要进行截取
			ajax.put("url", minioConfig.getEndpoint()+":"+ minioConfig.getPort()+"/"+ minioConfig.getBucketName()+"/"+fileName);
			return ajax;
		}catch(Exception e){
			e.printStackTrace();
			return AjaxResult.error("上传失败");
		}finally {
			//防止内存泄漏
			if (inputStream != null) {
				try {
					inputStream.close(); // 关闭流
				} catch (IOException e) {
					log.debug("inputStream close IOException:" + e.getMessage());
				}
			}
		}
	}


	/**
	 * 获取Minio连接
	 * @return
	 */
	private MinioClient getClient(){
		MinioClient minioClient =
				MinioClient.builder()
						.endpoint("http://"+minioConfig.getEndpoint()+":"+ minioConfig.getPort())
						.credentials(minioConfig.getAccessKey(),minioConfig.getSecretKey())
						.build();
		return minioClient;
	}
}
