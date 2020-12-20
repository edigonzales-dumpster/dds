package ch.so.agi.dds;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
public class UploadController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static String FOLDER_PREFIX = "dds_";
    
    private static String SIDECAR_FILE_NAME = "transfer.xml";

    @Value("${app.bucketName}")
    private String bucketName;
    
    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("ping dds");
        return new ResponseEntity<String>("dds", HttpStatus.OK);
    }
    
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<?> uploadFile(@RequestHeader(required = false, value = "Authorization") String authorization, @RequestParam(name = "file", required = true) MultipartFile uploadFile) {
        
        // TODO: Try to connect to AWS Cognito in case there is no SES
        /*
        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        final String[] values = credentials.split(":", 2);
        */
        
        String fileName = uploadFile.getOriginalFilename();
        
        if (uploadFile.getSize() == 0 || fileName.trim().equalsIgnoreCase("") || fileName == null) {
            log.warn("No file was uploaded.");

//            Message msg = new Message(HttpStatus.NO_CONTENT.value(), "no file was uploaded", null, null);
//            return new ResponseEntity<Message>(msg, HttpStatus.NO_CONTENT);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        
        // Copy file in local folder.
        Path tmpDirectory;
        Path uploadFilePath;
        try {
            tmpDirectory = Files.createTempDirectory(FOLDER_PREFIX);
            uploadFilePath = Paths.get(tmpDirectory.toString(), fileName);
            byte[] bytes = uploadFile.getBytes();
            Files.write(uploadFilePath, bytes); 
            log.info(uploadFilePath.toFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        // Authorisierung
        try {
            extractZipFile(uploadFilePath.toFile(), tmpDirectory);

            File file = Paths.get(tmpDirectory.toFile().getAbsolutePath(), SIDECAR_FILE_NAME).toFile();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
            DocumentBuilder db = dbf.newDocumentBuilder();  
            Document doc = db.parse(file);  
            
            doc.getDocumentElement().normalize();  
            NodeList nodeList = doc.getElementsByTagName("Transfer"); 
            
            for (int itr = 0; itr < nodeList.getLength(); itr++) {  
                Node node = nodeList.item(itr);                  
                if (node.getNodeType() == Node.ELEMENT_NODE) {  
                    Element eElement = (Element) node;
                    String topic =  eElement.getElementsByTagName("Topic").item(0).getTextContent();
                    String dataset =  eElement.getElementsByTagName("Dataset").item(0).getTextContent();
                    
                    // TODO: isAuthorized?
                    log.info("TODO: isAuthorized?");
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (SAXException e) {
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);            
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);            
        }

        // Upload zip file to S3.
        String prefix = new File(uploadFilePath.toFile().getParent()).getName();
        String key = prefix + "/" + uploadFilePath.toFile().getName();

        S3Client s3 = S3Client.create();
        s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), uploadFilePath);
        s3.close();
        
        // Delete local files.
        try {
            boolean result = FileSystemUtils.deleteRecursively(tmpDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }        

//        Message msg = new Message(HttpStatus.CREATED.value(), null, "success", null);
//        return new ResponseEntity<Message>(msg, HttpStatus.CREATED);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private void extractZipFile(File zipFile, Path targetDir) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");
        URI uri = URI.create("jar:file://" + zipFile.getAbsolutePath());
        FileSystem zipFs = FileSystems.newFileSystem(uri, new HashMap<>());
        Path pathInZip = zipFs.getPath("/");
        Files.walkFileTree(pathInZip, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                Path relativePathInZip = pathInZip.relativize(filePath);                
                Path targetPath = targetDir.resolve(relativePathInZip.toString());
                log.debug("targetPath: " + targetPath);
                Files.createDirectories(targetPath.getParent());
                Files.copy(filePath, targetPath);
                
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
}
