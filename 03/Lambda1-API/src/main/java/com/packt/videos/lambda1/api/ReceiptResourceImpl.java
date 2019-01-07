package com.packt.videos.lambda1.api;

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

import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

@Path("/receipts")
public class ReceiptResourceImpl implements ReceiptResource
{
	private		static		Logger LOG = null;
	private		static		List<Parameter>		parameters = null;

	static
	{
		LOG = LoggerFactory.getLogger(Lambda1FunctionHandler.class);
		parameters = Lambda1FunctionHandler.getParameters();
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
		LOG.info("getById::id = " + id);
		return Response
				.ok()
				.build();
	}

	@Override
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createReceipt(@Valid ReceiptDataObject co)
	{
		LOG.info("createReceipt::data = " + co);
		return Response
				.ok()
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
		return Response
				.ok()
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
