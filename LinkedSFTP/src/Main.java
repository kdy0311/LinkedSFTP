import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author kdy
 * 2021-03-04
 * 농공산단, 하이옥스에서 한 달 동안 수집한 데이터 압축 파일을 사업개발실로 옮김
 * 농공산단->운영서버->사업개발실
 * 하이옥스->운영서버->사업개발실
 * 
 * 옮긴 압축 파일은 삭제되어 사업개발실 서버에서 보관
 *
 */
public class Main {
	
	private final static Logger LOG = Logger.getGlobal();
	
    public static void main(String[] args) throws Exception {
    	// 로그 설정 start
		Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }
        LOG.setLevel(Level.INFO);
        Handler handler = new FileHandler("/home/Oadr/message.log", true);
        CustomLogFormatter formatter = new CustomLogFormatter();
        handler.setFormatter(formatter);
        LOG.addHandler(handler);
        // 로그 설정 end
        // 변수 설정 start
    	String ngUrl = "URL";				// 농공산단 URL
		String ngUser = "ID";				// 농공산단 계정
		int ngPort = 1122;					// 농공산단 port
		String ngPw = "PW";					// 농공산단 비밀번호
        String ngPath = "DIRECTORY";		// 농공산단 디렉토리(서버)
        
        String hiUrl = "URL";				// 하이옥스 URL
		String hiUser = "ID";				// 하이옥스 계정
		int hiPort = 22;					// 하이옥스 port
		String hiPw = "PW";					// 하이옥스 비밀번호
        String hiPath = "DIRECTORY";		// 하이옥스 디렉토리(서버)
        
        String bdUrl = "URL";				// 사업개발실 URL
        String bdUser = "ID";				// 사업개발실 계정
        int bdPort = 2121;					// 사업개발실 port
        String bdPw = "PW";					// 사업개발실 비밀번호
        String ngServerPath = "DIRECTORY";	// 사업개발실 디렉토리(서버)
        String hiServerPath = "DIRECTORY";	// 사업개발실 디렉토리(서버)
       
        Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM");
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1);
		String dateStr = sdf.format(cal.getTime());
		String fileName = dateStr+".zip";
		//String fileName = "2019_02.zip";			// 파일이름
        String localPath = System.getProperty("user.dir");
        //String localPath = "C:/_work/SFTP_TEST";	// 전송 파일 위치(로컬)
        // 변수 설정 end
        
        SFTPUtil sftpUtil = new SFTPUtil();
        FTPUploader ftpUploader = new FTPUploader(bdUrl, bdPort, bdUser, bdPw);
        
        downloadFile(ngUrl, ngUser, ngPort, ngPw, ngPath, fileName, localPath, "_ng", ngServerPath, sftpUtil, ftpUploader);
        downloadFile(hiUrl, hiUser, hiPort, hiPw, hiPath, fileName, localPath, "_hi", hiServerPath, sftpUtil, ftpUploader);
        
    }
    public static void downloadFile(String url, String user, int port, String pw, 
    		String path, String fileName, String localPath, String id, String serverPath,
    		SFTPUtil sftpUtil, FTPUploader ftpUploader){
    	StringBuffer newFileName = null;
		File file = null;
		File fileToMove = null;
		boolean isMoved;
    	if(sftpUtil.init(url, user, port, pw)){
        	LOG.info("::>> "+id+" 연결 성공");
        }else{
        	LOG.info("::>> "+id+" 연결 실패");
        	while(!sftpUtil.init(url, user, port, pw));
        }
        if(sftpUtil.download(path, fileName, localPath)){
        	LOG.info("::>> "+id+" 데이터 다운로드 완료");
        	file = new File(localPath+"/"+fileName);
        	newFileName = new StringBuffer(fileName);
            fileToMove = new File(localPath+"/"+newFileName.insert(fileName.indexOf("."), id));
            isMoved = file .renameTo(fileToMove);
            System.out.println(isMoved);
	        if(sftpUtil.remove(path, fileName, localPath)){
	    		LOG.info("::>> "+id+" 데이터 삭제 완료");
	    	}else{
	    		LOG.info("::>> "+id+" 데이터 삭제 실패");
	    	}
        }else{
        	LOG.info("::>> "+id+" 데이터 다운로드 실패");
        }
        if(sftpUtil.disconnect()){
        	LOG.info("::>> "+id+" 연결 종료");
        }
        try {
			if(ftpUploader.upload(localPath+"/"+newFileName, serverPath)){
				LOG.info("::>> 사업개발실 데이터 전송 완료");
				deleteFile(localPath+"/"+newFileName);
			}else{
				LOG.info("::>> 사업개발실 데이터 전송 실패");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private static void deleteFile(String filePath) {
		File file = new File(filePath);
		if( file.exists() ){ 
			System.out.println(file);
			if(file.delete()){
				LOG.info("::>> 운영서버 파일삭제 성공");
				System.out.println("운영서버 파일삭제 성공"); 
			}else{
				LOG.info("::>> 운영서버 파일삭제 실패");
				System.out.println("운영서버 파일삭제 실패"); 
			} 
		}else{ 
			LOG.info("::>> 운영서버 파일 없음");
			System.out.println("운영서버 파일 없음"); 
		}
		
	}
}


