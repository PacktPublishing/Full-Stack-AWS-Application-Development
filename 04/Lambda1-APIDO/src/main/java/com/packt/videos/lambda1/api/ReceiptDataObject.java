package com.packt.videos.lambda1.api;

import java.io.Serializable;


public class ReceiptDataObject implements Serializable
{

	private String id;			// id is bucket/folder/file
//	@NotNull
	private String reason;
//	@NotNull
	private Double amount;
	private String description;
	
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}