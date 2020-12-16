package ch.so.agi.dds;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("ping dds");
        return new ResponseEntity<String>("dds", HttpStatus.OK);
    }
    
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<?> uploadFile(@RequestHeader(required = false, value = "Authorization") String authorization, @RequestParam(name = "file", required = true) MultipartFile uploadFile) {
        
        // TODO: connect to aws cognito
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
        
        log.info(fileName);
        
//        Message msg = new Message(HttpStatus.CREATED.value(), null, "success", null);
//        return new ResponseEntity<Message>(msg, HttpStatus.CREATED);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    
}
