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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.ModelCreationException;
import org.springframework.restdocs.snippet.TemplatedSnippet;

/**
 * @author Dave Syer
 *
 */
public class MessageSnippet extends TemplatedSnippet {

	public MessageSnippet() {
		this(new HashMap<>());
	}

	public MessageSnippet(Map<String, Object> attributes) {
		super("message", "message", attributes);
	}

	@Override
	protected Map<String, Object> createModel(Operation operation) {
		try {
			MediaType contentType = getContentType(operation);
			byte[] content = getContent(operation);
			Charset charset = extractCharset(contentType);
			String body = charset != null ? new String(content, charset)
					: new String(content);
			MessageDelivery<?> delivery =  (MessageDelivery<?>) operation.getAttributes().get("delivery");
			Map<String, Object> model = new HashMap<>();
			model.put("headers", getHeaders(delivery.getMessage().getHeaders()));
			model.put("payload", body);
			return model;
		}
		catch (IOException ex) {
			throw new ModelCreationException(ex);
		}
	}

	private List<Map<String, String>> getHeaders(MessageHeaders messageHeaders) {
		List<Map<String, String>> headers = new ArrayList<>();
		for (Entry<String, Object> header : messageHeaders.entrySet()) {
			headers.add(header(header.getKey(), header.getValue().toString()));
		}
		return headers;
	}

	protected MediaType getContentType(Operation operation) {
		return operation.getResponse().getHeaders().getContentType();
	}

	protected byte[] getContent(Operation operation) throws IOException {
		return operation.getResponse().getContent();
	}

	private Charset extractCharset(MediaType contentType) {
		if (contentType == null) {
			return null;
		}
		return contentType.getCharset();
	}

	private Map<String, String> header(String name, String value) {
		Map<String, String> header = new HashMap<>();
		header.put("name", name);
		header.put("value", value);
		return header;
	}

}
