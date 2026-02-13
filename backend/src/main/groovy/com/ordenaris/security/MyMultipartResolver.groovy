package com.ordenaris.security

import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest

import javax.servlet.http.HttpServletRequest

class MaxFileUploadSizeResolver extends CommonsMultipartResolver {

    static final String FILE_SIZE_EXCEEDED_EXCEPTION = "demo.fileSizeExceeded"

    @Override
    MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        try {
            println request
            return super.resolveMultipart(request)
        } catch (MaxUploadSizeExceededException e) {

            request.setAttribute(FILE_SIZE_EXCEEDED_EXCEPTION, e)
            return new DefaultMultipartHttpServletRequest(request, new LinkedMultiValueMap<String, MultipartFile>(), new LinkedHashMap<String, String[]>(), new LinkedHashMap<String, String>())
        }
    }

}