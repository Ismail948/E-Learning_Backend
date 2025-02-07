package com.elearning.response_request;

public class LoginResponse {
    private boolean success;
    private String token;
    private String username;

    public LoginResponse(boolean success, String token, String username) {
    	this.username=username;
        this.success = success;
        this.token = token;
    }

    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
