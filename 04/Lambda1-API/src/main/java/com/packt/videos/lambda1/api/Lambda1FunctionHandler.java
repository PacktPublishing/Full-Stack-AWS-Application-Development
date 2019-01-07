package com.packt.videos.lambda1.api;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.internal.model.CognitoAuthorizerClaims;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.jersey.factory.AwsProxyServletContextFactory;
import com.amazonaws.serverless.proxy.jersey.factory.AwsProxyServletRequestFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DescribeParametersResult;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Lambda1FunctionHandler {
	private		static		Logger LOG = null;
	private		static		AWSSimpleSystemsManagement ssm = null;
	private		static		List<Parameter>		parameters = null;

	protected	ResourceConfig 	jerseyApplication = null;
    protected	JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> 
    							handler = null;
    protected	ObjectMapper 	mapper = new ObjectMapper();

	static
	{
		try
		{
			LOG = LoggerFactory.getLogger(Lambda1FunctionHandler.class);
			ssm = AWSSimpleSystemsManagementClientBuilder.defaultClient();
			getParameters();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Lambda1FunctionHandler()
	{
		try
		{
			jerseyApplication = new ResourceConfig()
					.packages("com.packt.videos.lambda1.api,com.fasterxml.jackson.jaxrs.json")
					.register(new AbstractBinder() {
						
						@Override
						protected void configure() {
							bindFactory(AwsProxyServletRequestFactory.class)
								.to(HttpServletRequest.class)
								.in(RequestScoped.class);
							bindFactory(AwsProxyServletContextFactory.class)
								.to(ServletContext.class)
								.in(RequestScoped.class);
						}
					});
			handler = JerseyLambdaContainerHandler.getAwsProxyHandler(jerseyApplication);
		}
		catch ( Exception ex )
		{
			LOG.info("Caught Exception in BaseLambdaFunctionHandler: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
    public AwsProxyResponse handleRequest(AwsProxyRequest awsProxyRequest, Context context) 
    {
    	try
    	{
	    	// Handle identiy
	    	String ident = null;
	    	
	    	Map<String, String> headers = awsProxyRequest.getHeaders();
	    	LOG.info("Headers:");
	    	String authentication = null;
	    	if ( headers != null )
	    	{
		    	for ( String key : headers.keySet())
		    	{
		    		LOG.info(key + ": " + headers.get(key));
		    		if ( key.equalsIgnoreCase("authentication"))
		    			authentication = headers.get(key).toString();
		    	}

	    	}	    	
	    	
	    	LOG.info("Authentication: " + authentication);
//	    	if ( authentication != null )
//	    	{
//	    		String [] jwt = JWTUtils.decoded(authentication);
//	    		LOG.info("JWT[0]: " + jwt[0]);
//	    		LOG.info("JWT[1]: " + jwt[1]);
//	    		try
//	    		{
//	    			jwtPayload = mapper.readValue(jwt[1], JWTPayload.class);
//	    			ident = jwtPayload.getSub();
//	    		}
//	    		catch ( Exception ex )
//	    		{
//	    			LOG.error("Exception parsing jwtPayload: " + ex.getMessage());
//	    			ex.printStackTrace();
//	    		}
//	    	}
	    	
	    	if ( ident == null && awsProxyRequest.getRequestContext() != null && 
	    			awsProxyRequest.getRequestContext().getIdentity() != null &&
	    			awsProxyRequest.getRequestContext().getIdentity().getCognitoIdentityId() != null )
	    	{
	    		LOG.info("Cog Id: " + awsProxyRequest.getRequestContext().getIdentity().getCognitoIdentityId());
	    	}
	    	
	    	if ( awsProxyRequest.getRequestContext() != null && 
	    			awsProxyRequest.getRequestContext().getIdentity() != null  )
	    	{
	    		LOG.info("Other id: " + awsProxyRequest.getRequestContext().getIdentity().getUser());
	    	}
	    	
	    	String subject = "anonymous";
	    	
//	    	if ( awsProxyRequest.getRequestContext() != null && awsProxyRequest.getRequestContext().getAuthorizer() != null )
//	    	{
//		    	CognitoAuthorizerClaims claims = awsProxyRequest.getRequestContext().getAuthorizer().getClaims();
//		    	subject = awsProxyRequest.getRequestContext().getAuthorizer().getClaims().getSubject();
//	    	}	
	    	
	    	LOG.debug("handleRequest::Using Identity Subject: " + ident);
	    	
	    	LOG.info("handleRequest::Request::method = " + awsProxyRequest.getHttpMethod());
	    	LOG.info("handleRequest::Request::path = " + awsProxyRequest.getPath());
	    	LOG.info("handleRequest::Request::qs = " + awsProxyRequest.getQueryString());
	    	LOG.info("handleRequest::Request::resource = " + awsProxyRequest.getResource());
	    	LOG.info("handleRequest::Request::pathParams = " + awsProxyRequest.getPathParameters());
	    	LOG.info("handleRequest::Request::body = " + awsProxyRequest.getBody());
	    	LOG.info("handleRequest::Calling handler");

	    	AwsProxyResponse resp = handler.proxy(awsProxyRequest, context);
	        
	    	LOG.info("handleRequest::Response::statusCode = " + resp.getStatusCode());
	        LOG.info("handleRequest::Response::headers = " + resp.getHeaders());
	        LOG.info("handleRequest::Response::body = " + resp.getBody());
	        
	        return resp;
    	}
    	catch ( Exception ex )
    	{
    		LOG.info("handleRequest::caught exception: " + ex.getMessage());
    		ex.printStackTrace();
    		return new AwsProxyResponse(501);
    	}
    }

	public static List<Parameter> getParameters()
	{
		if ( parameters == null )
		{
			try
			{
				// Let's get the set of parameters
				DescribeParametersRequest dpReq = new DescribeParametersRequest();
				DescribeParametersResult dpRes = ssm.describeParameters(dpReq);
				
				GetParametersRequest gpReq = new GetParametersRequest();
	
				// Add all of the known parameters to the set that we'll get the values of.
				if ( dpRes.getParameters().isEmpty() )
					return parameters;
				
				dpRes.getParameters().stream().forEach(p -> gpReq.getNames().add(p.getName()));
				gpReq.setWithDecryption(true);
				GetParametersResult gpRes = ssm.getParameters(gpReq);
				parameters = gpRes.getParameters();
				parameters.stream().forEach(p -> LOG.debug("Loaded Parameter: " + p.getName()));
			}
			catch (Throwable t)
			{
				LOG.error("Caught error in getParameters: ", t);
			}
		}
		
		return parameters;
	}
}
