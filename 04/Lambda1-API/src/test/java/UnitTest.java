import org.testng.Assert;
import org.testng.annotations.Test;

//import org.junit.Test;

import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packt.videos.lambda1.api.Lambda1FunctionHandler;
import com.packt.videos.lambda1.api.ReceiptDataObject;

//import junit.framework.Assert;


@Test(groups = "{default}")
public class UnitTest {

	private			ObjectMapper mapper = new ObjectMapper();
	private 		Lambda1FunctionHandler handler = new Lambda1FunctionHandler();

    private Context createContext() {
        CommonTestContext ctx = new CommonTestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("TAP3FileAPI:dev");

        return ctx;
    }

	@Test(enabled = true)
	public void testPing() throws Exception {
		
    	System.out.println("Entering testPing");
    	
        Context ctx = createContext();
        AwsProxyRequest req = CommonTestContext.getRequest();
        req.setHttpMethod("HEAD");
        req.setPath("/receipts/ping");
//        req.getHeaders().put("Content-Type", "application/json");
        req.getHeaders().put("Accept", "application/json");

        AwsProxyResponse output = handler.handleRequest(req, ctx);
        
        Assert.assertEquals(output.getStatusCode(), 200);

	}

	@Test(enabled = true)
	public void testDelete() throws Exception {
		
    	System.out.println("Entering testDelete");
    	
        Context ctx = createContext();
        AwsProxyRequest req = CommonTestContext.getRequest();
        req.setHttpMethod("DELETE");
        req.setPath("/receipts/1234");
//        req.getHeaders().put("Content-Type", "application/json");
        req.getHeaders().put("Accept", "application/json");

        AwsProxyResponse output = handler.handleRequest(req, ctx);
        
        Assert.assertEquals(output.getStatusCode(), 200);

	}

	@Test(enabled = true)
	public void testCreate() throws Exception {
    	System.out.println("Entering testCreateFile");
    	
        Context ctx = createContext();
        AwsProxyRequest req = CommonTestContext.getRequest();
        req.setHttpMethod("POST");
        req.setPath("/receipts");
        req.getHeaders().put("Content-Type", "application/json");
        req.getHeaders().put("Accept", "application/json");

        ReceiptDataObject ro = new ReceiptDataObject();
        ro.setAmount(123.112);
        ro.setDescription("Test desc");
        ro.setReason("Test Reason");
        ro.setId("some-s3-file-key");
        
		req.setBody(mapper.writeValueAsString(ro));
		
        AwsProxyResponse output = handler.handleRequest(req, ctx);
        
        Assert.assertEquals(output.getStatusCode(), 201);

	}

	@Test(enabled = true)
	public void testPut() throws Exception {
    	System.out.println("Entering testUpdateFile");
    	
        Context ctx = createContext();
        AwsProxyRequest req = CommonTestContext.getRequest();
        req.setHttpMethod("PUT");
        req.setPath("/receipts/some-s3-file-key");
        req.getHeaders().put("Content-Type", "application/json");
        req.getHeaders().put("Accept", "application/json");

        ReceiptDataObject ro = new ReceiptDataObject();
        ro.setAmount(123.112);
        ro.setDescription("Test desc");
        ro.setReason("Test Reason");
        ro.setId("some-s3-file-key");
        
		req.setBody(mapper.writeValueAsString(ro));
		
        AwsProxyResponse output = handler.handleRequest(req, ctx);
        
        Assert.assertEquals(output.getStatusCode(), 200);

	}

}
