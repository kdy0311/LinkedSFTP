import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

public class FTPUploader {
	private String serverIp;
	private int serverPort;
	private String user;
	private String password;

	public FTPUploader(String serverIp, int serverPort, String user, String password) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.user = user;
		this.password = password;
	}

	public boolean upload(String filePath, String fileLocation)
			throws SocketException, IOException, Exception {
		FileInputStream fis = null;
		FTPClient ftpClient = new FTPClient();

		try {
			ftpClient.setControlEncoding("EUC-KR");
			ftpClient.connect(serverIp, serverPort); // ftp 연결
			ftpClient.setControlEncoding("EUC-KR");
			int reply = ftpClient.getReplyCode(); // 응답코드받기

			if (!FTPReply.isPositiveCompletion(reply)) { // 응답이 false 라면 연결 해제
															// exception 발생
				ftpClient.disconnect();
				throw new Exception(serverIp + " FTP 서버 연결 실패");
			}

			ftpClient.setSoTimeout(1000 * 100); // timeout 설정
			ftpClient.login(user, password); // ftp 로그인
			try {
				ftpClient.makeDirectory(fileLocation);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ftpClient.changeWorkingDirectory(fileLocation);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE); // 파일타입설정
			ftpClient.enterLocalPassiveMode(); // active 모드 설정
			File file = new File(filePath);
			String fileName = file.getName();
			fis = new FileInputStream(file);

			return ftpClient.storeFile(fileName, fis); // 파일 업로드
		} finally {
			if (ftpClient.isConnected()) {
				ftpClient.disconnect();
			}
			if (fis != null) {
				fis.close();
			}
		}
	}

}