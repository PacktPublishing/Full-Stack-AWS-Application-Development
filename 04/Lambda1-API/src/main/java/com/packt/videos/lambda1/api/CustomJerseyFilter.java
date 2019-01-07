package com.packt.videos.lambda1.api;


import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Code taken from http://www.makeinjava.com/custom-logging-filter-using-jersey-containerrequestfilter-containerresponsefilter/
 */
@Provider
public class CustomJerseyFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Context
    private ResourceInfo resourceInfo;

    private static final Logger log = LoggerFactory.getLogger(CustomJerseyFilter.class);
    private static final ConcurrentHashMap<String,Long> hashMap = new ConcurrentHashMap<>();
    boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().
            getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    
    static
    {
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        //Note down the start request time...we will use to calculate the total
        //execution time

        log.debug("Entering in Resource : /{} ", requestContext.getUriInfo().getPath());
        log.debug("Method Name : {} ", resourceInfo.getResourceMethod().getName());
        log.debug("Class : {} ", resourceInfo.getResourceClass().getCanonicalName());
        log.debug("Annotations: {} ", resourceInfo.getResourceMethod().getAnnotations());
        
        if(isDebug) {
            hashMap.put("start-time", System.currentTimeMillis());
            logQueryParameters(requestContext);
            logMethodAnnotations();
            logRequestHeader(requestContext);

            //log entity stream...
            String entity = readEntityStream(requestContext);
            if (null != entity && entity.trim().length() > 0) {
                log.debug("Entity Stream : {}", entity);
            }
        }
    }

    private void logQueryParameters(ContainerRequestContext requestContext) {
        Iterator iterator = requestContext.getUriInfo().getPathParameters().keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            List obj = requestContext.getUriInfo().getPathParameters().get(name);
            String value = null;
            if(null != obj && obj.size() > 0) {
                value = (String) obj.get(0);
            }
            log.debug("Query Parameter Name: {}, Value :{}", name, value);
        }
    }

    private void logMethodAnnotations() {
        Annotation[] annotations = resourceInfo.getResourceMethod().getDeclaredAnnotations();
        if (annotations != null && annotations.length > 0) {
            log.debug("----Start Annotations of resource ----");
            for (Annotation annotation : annotations) {
                log.debug(annotation.toString());
            }
            log.debug("----End Annotations of resource----");
        }
    }

    private void logRequestHeader(ContainerRequestContext requestContext) {
        Iterator iterator;
        log.debug("----Start Header Section of request ----");
        log.debug("Method Type : {}", requestContext.getMethod());
        iterator = requestContext.getHeaders().keySet().iterator();
        while (iterator.hasNext()) {
            String headerName = (String) iterator.next();
            String headerValue = requestContext.getHeaderString(headerName);
            log.debug("Header Name: {}, Header Value :{} ",headerName, headerValue);
        }
        log.debug("----End Header Section of request ----");
    }

    private String readEntityStream(ContainerRequestContext requestContext)
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final InputStream inputStream = requestContext.getEntityStream();
        final StringBuilder builder = new StringBuilder();
        try
        {
            ReaderWriter.writeTo(inputStream, outStream);
            byte[] requestEntity = outStream.toByteArray();
            if (requestEntity.length == 0) {
                builder.append("");
            } else {
                builder.append(new String(requestEntity));
            }
            requestContext.setEntityStream(new ByteArrayInputStream(requestEntity) );
        } catch (IOException ex) {
            log.debug("----Exception occurred while reading entity stream :{}",ex.getMessage());
        }
        return builder.toString();
    }

//    protected void initSQL() throws InitializationException
//    {
//        if ( !SQLinitialized )
//        {
//            try {
//                BaseResourceImpl.initSQL();
//                SQLinitialized = true;
//            } catch (InitializationException e) {
//                log.error("Filter could not initialize SQL.", e);
//            }
//        }
//    }
//


    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        if(isDebug) {
            Long stTime = hashMap.get("start-time");
            if (null == stTime) {
                return;
            }
            long executionTime = System.currentTimeMillis() - stTime;
            log.debug("Total request execution time : {} milliseconds", executionTime);
            //clear the context on exit
        }
    }
}