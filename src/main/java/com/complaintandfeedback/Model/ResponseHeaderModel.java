package com.complaintandfeedback.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseHeaderModel {
	// Private Variables
	private String ts;
	private String txn;
	private String respCode;
	private String statusMsg;
	private String err;
	private String errMsg;
	private String info;
	@JsonProperty(value = "responseHeader")
	private List<? extends Object> listCommonResponse;

	public ResponseHeaderModel() {

	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public String getTxn() {
		return txn;
	}

	public void setTxn(String txn) {
		this.txn = txn;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public List<? extends Object> getListCommonResponse() {
		return listCommonResponse;
	}

	public void setListCommonResponse(List<? extends Object> listCommonResponse) {
		this.listCommonResponse = listCommonResponse;
	}

}
