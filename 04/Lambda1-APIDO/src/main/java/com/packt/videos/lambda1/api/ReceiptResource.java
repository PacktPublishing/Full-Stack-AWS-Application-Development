package com.packt.videos.lambda1.api;

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


public interface ReceiptResource
{

	//
	// Implement @GET, @POST, @PUT and @DELETE as needed here.
	/*
	 * Returns nothing; just serves to keep the Lambda warm.
	 * <p>
	 * This method always returns immediately with an 200 OK status and no body.
	 * 
	 * @return	200 OK status with no body.
	 */
	@HEAD
	@Path("/ping")
	public Response ping(@QueryParam("additionalDelay") @DefaultValue("0") int additionalDelay);

	/*
	 * @OPTIONS responds to preflight requests from browsers, allowing the browser
	 * to make the actual call.
	 */
	
	// Covers GET, DELETE, PUT
	@OPTIONS
	@Path("{id}")
	public Response optionsId();

	// Covers POST and the query method
	@OPTIONS
	public Response options();

	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("/{id}")
	public Response deleteReceipt(@PathParam("id") String id);

	@GET
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getById(@PathParam("id") String id);

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
//	public Response createReceipt();
	public Response createReceipt(ReceiptDataObject rdo);

	@PUT
	@Path("/{id}")
	@Produces({MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_JSON})
	public Response updateReceipt(@PathParam("id") String id, ReceiptDataObject co);
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response query(
			@QueryParam("r") String reason,
			@QueryParam("amtLT") String amountGT,
			@QueryParam("amtGT") String amountLT,
			@QueryParam("dt") String dt);

}
