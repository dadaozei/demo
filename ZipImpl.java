package com.bd.framework.zip.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.bd.framework.constant.ExceptionCode;
import com.bd.framework.exception.ZipException;
import com.bd.framework.util.FileUtil;
import com.bd.framework.util.StreamUtil;
import com.bd.framework.zip.IZip;

/**
 * 
 * <p>
 * Title : zip解、压缩实现类
 * </p>
 * <p>
 * Description:
 * </p>
 */
public class ZipImpl implements IZip {

	public String compress(String data) throws ZipException {
		return null;
	}

	public String decompress(String data) throws ZipException {
		return null;
	}

	public File compressFile(File file, File zipFile) throws ZipException {

		// 输出文件
		if (zipFile == null) {
			zipFile=new File(file.getPath() + ".zip");
		}
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		}

		// 压缩流
		CheckedOutputStream cos = new CheckedOutputStream(fos, new CRC32());
		ZipOutputStream zos = new ZipOutputStream(cos);
		zos.setComment("SHANGHI-RONGDA-DEP");
		try {
			compress(file, zos, "");
		} catch (Exception e) {
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		} finally {
			StreamUtil.close(zos);
			StreamUtil.close(cos);
			StreamUtil.close(fos);
		}
		return zipFile;
	}

	private void compress(File file, ZipOutputStream out, String basedir) throws IOException {
		if (file.isDirectory()) {
			compressDirectory(file, out, basedir);
		} else {
			compressFile(file, out, basedir);
		}
	}

	private void compressDirectory(File dir, ZipOutputStream out, String basedir) throws IOException {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			compress(files[i], out, basedir + dir.getName() + "/");
		}
	}

	private void compressFile(File file, ZipOutputStream out, String basedir) throws IOException {

		// 输入文件（要压缩的文件）
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw e;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipEntry entry = new ZipEntry(basedir + file.getName());
		try {
			out.putNextEntry(entry);
		} catch (IOException e) {
			StreamUtil.close(bis);
			StreamUtil.close(fis);
			throw e;
		}

		// 压缩
		int count;
		byte data[] = new byte[8192];
		try {
			while ((count = bis.read(data, 0, 8192)) != -1) {
				out.write(data, 0, count);
			}
		} catch (IOException e) {
			StreamUtil.close(bis);
			StreamUtil.close(fis);
			throw e;
		}

		// 关闭流
		StreamUtil.close(bis);
		StreamUtil.close(fis);
	}

	public File decompressFile(File zipFile, File file) throws ZipException {

		// 输入的压缩文件
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		}
		ZipInputStream zis = new ZipInputStream(fis);

		// 实例化ZipFile，每一个zip压缩文件都可以表示为一个ZipFile
		ZipFile zf = null;
		try {
			zf = new ZipFile(zipFile);
		} catch (Exception e) {
			StreamUtil.close(zis);
			StreamUtil.close(fis);
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		}

		// 输出路径
		boolean isAutoPath;
		try {
			isAutoPath = false;
			if (file == null) {
				String path = zipFile.getPath().replaceAll(zipFile.getName(), "");
				file = new File(path);
				isAutoPath=true;
			}
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			StreamUtil.close(zis);
			StreamUtil.close(fis);
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		}

		// 开始解压
		ZipEntry zipEntry = null;
		try {
			while ((zipEntry = zis.getNextEntry()) != null) {

				// 压缩包中的文件
				String fileName = zipEntry.getName();
				File temp = new File(file.getPath() + "/" + fileName);
				if (!temp.getParentFile().exists()) {
					temp.getParentFile().mkdirs();
				}

				// 通过ZipFile的getInputStream方法拿到具体的ZipEntry的输入流
				InputStream is = null;
				try {
					is = zf.getInputStream(zipEntry);
				} catch (IOException e) {
					throw e;
				}

				// 输出文件
				OutputStream os = null;
				try {
					os = new FileOutputStream(temp);
				} catch (FileNotFoundException e) {
					StreamUtil.close(is);
					throw e;
				}
				
				// 解压此文件
				try {
					int len = 0;
					byte data[] = new byte[8192];
					while ((len = is.read(data, 0, 8192)) != -1) {
						os.write(data, 0, len);
					}
				} catch (IOException e) {
					throw e;
				}finally{
					StreamUtil.close(os);
					StreamUtil.close(is);
				}
			}
		} catch (Exception e) {
			throw new ZipException(ExceptionCode.ZIP_ERROR, e);
		} finally {
			StreamUtil.close(zis);
			StreamUtil.close(fis);
		}
		
		//返回
		if(isAutoPath){
			String shortName=zipFile.getName();
//			shortName=shortName.replaceAll(FileUtil.getExtendName(shortName), "");
			String path = zipFile.getPath().replaceAll(FileUtil.getExtendName(shortName), "");
			file = new File(path);
		}
		return file;
	}

	public static void main(String args[]) throws IOException {
		ZipImpl zip = new ZipImpl();
		File file = new File("/c:/rhip/update/SP1.0.00001");
		try {
			zip.compressFile(file, null);
		} catch (ZipException e) {
		}

		// ZipImpl zip = new ZipImpl();
//		file = new File("/c:/rhip/zip.zip");
//		try {
//			zip.decompressFile(file, new File("/c:/rhip/zip"));
//		} catch (ZipException e) {
//		}
	}
}
