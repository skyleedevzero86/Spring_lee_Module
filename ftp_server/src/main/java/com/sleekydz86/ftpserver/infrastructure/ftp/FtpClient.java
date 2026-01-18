package com.sleekydz86.ftpserver.infrastructure.ftp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class FtpClient {
    
    private final FtpConnectionProperties properties;

    public void uploadFile(String remotePath, String fileName, byte[] fileContent) throws FtpException {
        executeWithFtpClient(ftp -> {
            try {
                connect(ftp);
                login(ftp);
                configureFtp(ftp);
                createDirectories(ftp, remotePath);
                uploadFile(ftp, remotePath, fileName, fileContent);
                log.info("FTP 파일 업로드 성공: {}/{}", remotePath, fileName);
            } catch (IOException | FtpException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean testConnection() {
        try {
            executeWithFtpClient(ftp -> {
                try {
                    connect(ftp);
                    login(ftp);
                } catch (IOException | FtpException e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        } catch (Exception e) {
            log.error("FTP 연결 테스트 실패", e);
            return false;
        }
    }

    private void executeWithFtpClient(Consumer<FTPClient> operation) throws FtpException {
        FTPClient ftp = null;
        try {
            ftp = createFtpClient();
            operation.accept(ftp);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof FtpException) {
                throw (FtpException) cause;
            }
            log.error("FTP 작업 실패", e);
            throw new FtpException("FTP 작업 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("FTP 작업 실패", e);
            throw new FtpException("FTP 작업 실패: " + e.getMessage(), e);
        } finally {
            disconnect(ftp);
        }
    }

    private FTPClient createFtpClient() {
        FTPClient ftp = new FTPClient();
        if (properties.isDebug()) {
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        }
        ftp.setControlEncoding("UTF-8");
        return ftp;
    }

    private void connect(FTPClient ftp) throws IOException, FtpException {
        ftp.connect(properties.getHost(), properties.getPort());
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new FtpException("FTP 서버 연결 실패: " + ftp.getReplyString());
        }
        ftp.setSoTimeout(properties.getConnectTimeout());
    }

    private void login(FTPClient ftp) throws IOException, FtpException {
        if (!ftp.login(properties.getUsername(), properties.getPassword())) {
            ftp.logout();
            ftp.disconnect();
            throw new FtpException("FTP 로그인 실패");
        }
    }

    private void configureFtp(FTPClient ftp) throws IOException {
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        if (properties.isPassiveModeEnabled()) {
            ftp.enterLocalPassiveMode();
        }
    }

    private void createDirectories(FTPClient ftp, String remotePath) throws IOException {
        if (remotePath == null || remotePath.isEmpty()) {
            return;
        }
        String[] directories = remotePath.split("/");
        StringBuilder currentPath = new StringBuilder();
        for (String directory : directories) {
            if (directory.isEmpty()) continue;
            currentPath.append("/").append(directory);
            String path = currentPath.toString();
            if (!ftp.changeWorkingDirectory(path)) {
                if (ftp.makeDirectory(path)) {
                    log.debug("디렉토리 생성: {}", path);
                }
            }
        }
    }

    private void uploadFile(FTPClient ftp, String remotePath, String fileName, byte[] fileContent) 
            throws IOException, FtpException {
        String fullPath = remotePath.isEmpty() ? fileName : remotePath + "/" + fileName;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContent)) {
            if (!ftp.storeFile(fullPath, inputStream)) {
                throw new FtpException("파일 업로드 실패: " + ftp.getReplyString());
            }
        }
    }

    private void disconnect(FTPClient ftp) {
        if (ftp != null && ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException e) {
                log.warn("FTP 연결 종료 중 오류 발생", e);
            }
        }
    }
}
