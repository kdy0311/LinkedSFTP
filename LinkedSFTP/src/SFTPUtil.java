import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPUtil {
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;

	// SFTP 서버연결
	public boolean init(String url, String user, int port, String password) {
		System.out.println(url);
		boolean result = true;
		// JSch 객체 생성
		JSch jsch = new JSch();
		try {
			// 세션객체 생성 ( user , host, port )
			session = jsch.getSession(user, url, port);
			// password 설정
			session.setPassword(password);
			// 세션관련 설정정보 설정
			java.util.Properties config = new java.util.Properties();
			// 호스트 정보 검사하지 않는다.
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			// 접속
			session.connect();
			// sftp 채널 접속
			channel = session.openChannel("sftp");
			channel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
			result = false;
		}
		channelSftp = (ChannelSftp) channel;
		return result;
	}

	/**
	 * 단일 파일을 업로드
	 * 
	 * @param dir
	 *            저장시킬 주소(서버)
	 * @param file
	 *            저장할 파일 경로
	 */
	public boolean upload(String dir, String filePath) {
		boolean result = true;
		FileInputStream in = null;
		try {
			File file = new File(filePath);
			String fileName = file.getName();
			System.out.println("fileName:>>" + fileName);
			// fileName = URLEncoder.encode(fileName,"EUC-KR");
			in = new FileInputStream(file);
			channelSftp.cd(dir);
			channelSftp.put(in, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 단일 파일 다운로드
	 * 
	 * @param dir
	 *            저장할 경로(서버)
	 * @param downloadFileName
	 *            다운로드할 파일
	 * @param path
	 *            저장될 공간
	 */
	public boolean download(String dir, String downloadFileName, String path) {
		long start = System.currentTimeMillis();
		boolean result = true;
		InputStream in = null;
		FileOutputStream out = null;
		try {
			channelSftp.cd(dir);
			in = channelSftp.get(downloadFileName);
		} catch (SftpException e) {
			result = false;
			e.printStackTrace();
		}
		try {
			out = new FileOutputStream(new File(path + "/" + downloadFileName));
			byte[] b = new byte[1024];
			int cnt = 0;
			while ((cnt=in.read(b)) != -1){
				out.write(b, 0, cnt);
			}
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
        System.out.println( "실행 시간 : " + ( end - start )/1000.0 );
		return result;
	}
	public boolean remove(String dir, String downloadFileName, String path) {
		boolean result = true;
		try {
			channelSftp.rm(downloadFileName);
		} catch (SftpException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	// 파일서버와 세션 종료
	public boolean disconnect() {
		channelSftp.quit();
		session.disconnect();
		
		return true;
	}
}
