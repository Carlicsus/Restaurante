package com.ordenaris

import grails.web.context.ServletContextHolder as SCH
import javax.servlet.ServletContext
 
public class Conf {
    
    private final static ServletContext servletContext = SCH.servletContext

    public static String findConfiguration( identifier ){
        return servletContext["Conf"]."${identifier}"
    }
}