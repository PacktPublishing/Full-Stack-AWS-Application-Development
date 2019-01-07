package com.packt.videos.lambda1.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.util.StringUtils;

@Path("/receipts")
public class ReceiptResourceImpl implements ReceiptResource
{
	private		static		Logger 				LOG = null;
	private		static		List<Parameter>		parameters = null;
	
	// Added for DynamoDB
	private		static		AmazonDynamoDB 		client = null;
	private		static		DynamoDB			dynamo = null;
	private		static		Table				receiptInfoTable = null;
	
	static
	{
		try
		{
			LOG = LoggerFactory.getLogger(Lambda1FunctionHandler.class);
			parameters = Lambda1FunctionHandler.getParameters();

			// Get the DynamoDB client
			client = AmazonDynamoDBClientBuilder.standard()
					.withRegion(Regions.US_WEST_2)
					.build();

//			Use the code below if you want to run against DynamoDB local during development or testing.
//			client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//					new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
//					.build();
			
			dynamo = new DynamoDB(client);
			
			// Get a reference to the table we're using
			receiptInfoTable = dynamo.getTable("receiptInfo");
			
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	@HEAD
	@Path("/ping")
	public Response ping(@QueryParam("additionalDelay") @DefaultValue("0") int additionalDelay)
	{
		LOG.info("Entering ping::" + additionalDelay);
		
		try
		{
			Thread.sleep(additionalDelay);
		}
		catch ( Exception ex )
		{
			// Ignore errors in sleeping
		}

		LOG.info("Exiting ping::");
		return Response
				.ok()
				.build();
	}

	@Override
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{id}")
	public Response deleteReceipt(@PathParam("id") String id)
	{
		LOG.info("deleteReceipt::id = " + id);
		return Response
				.ok()
				.build();
	}

	@Override
	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getById(@PathParam("id") String id)
	{
		ReceiptDataObject ret = new ReceiptDataObject();
		
		LOG.info("getById::id = " + id);
		
		// Read the item from the table
		Item item = receiptInfoTable.getItem("id", id);
		
		if ( item != null )
		{
			ret = convert(item);
			LOG.info("Found: " + item.getString("id"));
		}
		else
			LOG.info("Not found");
		
		return Response
				.ok()
				.entity(ret)
				.header("Access-Control-Allow-Headers", "Content-Type,Accept,Authentication")
				.header("Access-Control-Allow-Origin", "*")
				.build();
	}

	private ReceiptDataObject convert(Item item)
	{
		ReceiptDataObject ret = new ReceiptDataObject();
		ret.setAmount(item.getDouble("amount"));
		ret.setDescription(item.getString("description"));
		ret.setReason(item.getString("reason"));
		ret.setId(item.getString("id"));
		
		return ret;
	}
	
	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
//	public Response createReceipt()
	public Response createReceipt(ReceiptDataObject rdo)
	{
//		ReceiptDataObject rdo = new ReceiptDataObject();
		try
		{
			LOG.info("createReceipt::data = " + rdo);
			
			// Create the Item to store in Dynamo
			Item item = new Item()
					.withPrimaryKey("id", rdo.getId())		// Id is bucket/folder/file
					.withString("reason", rdo.getReason())
					.withDouble("amount", rdo.getAmount())
					.withString("description", rdo.getDescription());
			
			// Write the item to the table 
			PutItemOutcome outcome = receiptInfoTable.putItem(item);
			
			LOG.info("Status: " + outcome.getPutItemResult().getSdkHttpMetadata().getHttpStatusCode());
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
			return Response
					.status(500)
					.header("error", ex.getMessage())
					.build();
		}
		return Response
				.status(201)
				.header("Access-Control-Allow-Headers", "Content-Type,Accept,Authentication")
				.header("Access-Control-Allow-Origin", "*")
				.build();
	}

	@Override
	@PUT
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response updateReceipt(@PathParam("id") String id, ReceiptDataObject co)
	{
		LOG.info("updateReceipt::id = " + id);
		return Response
				.ok()
				.build();
	}

	@Override
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response query(
			@QueryParam("r") String reason,
			@QueryParam("amtLT") String amountLT,
			@QueryParam("amtGT") String amountGT,
			@QueryParam("dt") String dt)
	{
		LOG.info("queryReceipt::r = " + reason + ", amtLT = " + amountLT +
				", amtGT = " + amountGT + ", dt = " + dt);

		String query = "";
		ValueMap vmap = new ValueMap();
		
		if ( ! StringUtils.isNullOrEmpty(reason))
		{
			query += "AND reason = :reason ";
			vmap = vmap.with(":reason", reason);
		}
		if ( ! StringUtils.isNullOrEmpty(amountLT))
		{
			query += "AND amount <= :amountLT ";
			vmap = vmap.with(":amountLT", amountLT);
		}
		if ( ! StringUtils.isNullOrEmpty(amountGT))
		{
			query += "AND amount >= :amountGT ";
			vmap = vmap.with(":amountGT", amountGT);
		}
		if ( ! StringUtils.isNullOrEmpty(dt))
		{
			query += "AND date = :date ";
			vmap = vmap.with(":date", dt);
		}
		if ( query.length() < 1 )
		{
			return Response.noContent().build();
		}
		query = query.substring(4);		// Strip starting AND

		QuerySpec spec = new QuerySpec()
			    .withFilterExpression(query)
			    .withValueMap(vmap);

		ItemCollection<QueryOutcome> items = receiptInfoTable.query(spec);

		ArrayList<ReceiptDataObject> dos = new ArrayList<>();
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
		    System.out.println(item.toJSONPretty());
		    dos.add(convert(item));
		}
			
		return Response
				.ok()
				.entity(dos)
				.build();
	}

	@Override
	@OPTIONS
	@Path("{id}")
	public Response optionsId() {
		return options();
	}

	@Override
	@OPTIONS
	public Response options() {
		LOG.info("Options::");
		return Response
				.ok()
				.header("Access-Control-Allow-Headers", "Content-Type,Accept,Authentication")
				.header("Access-Control-Allow-Origin", "*")
				.build();
	}

}
