package ch.so.agi.dds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<?> uploadFile(@RequestParam(name = "file", required = true) MultipartFile uploadFile) {
        String fileName = uploadFile.getOriginalFilename();
        
        if (uploadFile.getSize() == 0 || fileName.trim().equalsIgnoreCase("") || fileName == null) {
            log.warn("No file was uploaded.");

            //HttpHeaders headers = new HttpHeaders();
            //headers.add("Location", servletContext.getContextPath() + "/upload");
            //return new ResponseEntity<String>(headers, HttpStatus.FOUND);
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        }
        
        log.info(fileName);
        
        
        
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    
}
