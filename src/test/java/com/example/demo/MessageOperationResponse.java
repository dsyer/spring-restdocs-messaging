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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.restdocs.operation.OperationResponse;

/**
 * @author Dave Syer
 *
 */
public class MessageOperationResponse implements OperationResponse {

	private Message<?> request;

	public MessageOperationResponse(Message<?> request) {
		this.request = request;
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.OK;
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
	public byte[] getContent() {
		return getContentAsString().getBytes();
	}

	@Override
	public String getContentAsString() {
		return request.getPayload().toString();
	}

}
