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

package org.springframework.restdocs.message;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.operation.RequestCookie;

/**
 * @author Dave Syer
 *
 */
class MessageOperation implements OperationRequest, OperationResponse {

	private Message<?> message;
	private String destination;
	private final CompositeMessageConverterFactory factory;

	public MessageOperation(String destination, Message<?> request) {
		this.destination = destination;
		this.message = request;
		this.factory = new CompositeMessageConverterFactory();
	}
	
	public String getDestination() {
		return this.destination;
	}
	
	public Message<?> getMessage() {
		return this.message;
	}

	@Override
	public HttpStatus getStatus() {
		return HttpStatus.OK;
	}

	@Override
	public byte[] getContent() {
		return getContentAsString().getBytes();
	}

	@Override
	public String getContentAsString() {
		return MessageUtils.content(this.factory, this.message);
	}

	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders headers = MessageUtils.fromMessage(message.getHeaders(),
				new HttpHeaders());
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
			return new URI("message://channel/" + destination);
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException("Cannot create destination URI", e);
		}
	}

	@Override
	public Collection<RequestCookie> getCookies() {
		return Collections.emptySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.destination == null) ? 0 : this.destination.hashCode());
		result = prime * result + ((this.message.getHeaders().getId() == null) ? 0 : this.message.getHeaders().getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageOperation other = (MessageOperation) obj;
		if (this.destination == null) {
			if (other.destination != null)
				return false;
		}
		else if (!this.destination.equals(other.destination))
			return false;
		if (this.message == null) {
			if (other.message != null)
				return false;
		}
		else if (!this.message.getHeaders().getId().equals(other.message.getHeaders().getId()))
			return false;
		return true;
	}

}
