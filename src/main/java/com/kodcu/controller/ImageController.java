package com.kodcu.controller;

import com.kodcu.other.Current;
import com.kodcu.other.IOHelper;
import com.kodcu.service.DirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Created by usta on 25.12.2014.
 */
@Controller
public class ImageController {

    private final Current current;
    private final DirectoryService directoryService;

    @Autowired
    public ImageController(final Current current, final DirectoryService directoryService) {
        this.current = current;
        this.directoryService = directoryService;
    }

    @RequestMapping(value = {"/**/{extension:(?:\\w|\\W)+\\.(?:jpg|bmp|gif|jpeg|png|webp|svg)$}"}, method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> images(HttpServletRequest request, HttpServletResponse response, @PathVariable("extension") String extension) {

        response.setDateHeader("Expires", System.currentTimeMillis() + Duration.ofSeconds(10).toMillis());

        Path imageFile;
        String uri = request.getRequestURI();

        if (uri.startsWith("/"))
            uri = uri.substring(1);

        if (current.currentPath().isPresent()) {
            imageFile = current.currentPath().map(Path::getParent).get().resolve(uri);
        } else {
            imageFile = directoryService.getWorkingDirectory().get().resolve(uri);
        }

        byte[] temp = IOHelper.readAllBytes(imageFile);

        // if not found, try again in working dir
        if (temp.length == 0) {
            imageFile = directoryService.getWorkingDirectory().get().resolve(uri);
            temp = IOHelper.readAllBytes(imageFile);
        }

        return new ResponseEntity<>(temp, HttpStatus.OK);
    }
}
