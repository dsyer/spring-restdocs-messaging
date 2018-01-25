/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.Message;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.operation.RequestCookie;

/**
 * @author Dave Syer
 *
 */
public class MessageOperationRequest implements OperationRequest {

	private Message<?> request;
	private String destination;

	public MessageOperationRequest(String destination, Message<?> request) {
		this.destination = destination;
		this.request = request;
	}

	@Override
	public byte[] getContent() {
		return getContentAsString().getBytes();
	}

	@Override
	public String getContentAsString() {
		return request.getPayload().toString();
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders headers = new HttpHeaders();
		for (String key : request.getHeaders().keySet()) {
			headers.set(key, request.getHeaders().toString());
		}
		return headers;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.GET;
	}

	@Override
	public Parameters getParameters() {
		return new Parameters();
	}

	@Override
	public Collection<OperationRequestPart> getParts() {
		return Collections.emptySet();
	}

	@Override
	public URI getUri() {
		try {
			return new URI("to://message/" + destination);
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException("Cannot create destination URI", e);
		}
	}

	@Override
	public Collection<RequestCookie> getCookies() {
		return Collections.emptySet();
	}

}
