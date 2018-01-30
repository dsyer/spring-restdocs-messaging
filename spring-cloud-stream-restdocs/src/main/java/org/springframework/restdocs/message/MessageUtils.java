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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ObjectUtils;

/**
 * @author Dave Syer
 *
 */
public class MessageUtils {

	public static HttpHeaders fromMessage(MessageHeaders headers, HttpHeaders request) {
		HttpHeaders result = new HttpHeaders();
		for (String name : headers.keySet()) {
			Object value = headers.get(name);
			if (MessageHeaders.ID.equalsIgnoreCase(name)) {
				continue;
			}
			if (MessageHeaders.TIMESTAMP.equalsIgnoreCase(name)) {
				continue;
			}
			if (MessageHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
				name = HttpHeaders.CONTENT_TYPE;
			}
			name = name.toLowerCase();
			if (request.containsKey(name)) {
				if (name.startsWith("x-")) {
					if (!name.startsWith("x-forwarded")) {
						Collection<?> values = multi(value);
						for (Object object : values) {
							result.set(name, object.toString());
						}
					}
				}
			}
			else {
				Collection<?> values = multi(value);
				for (Object object : values) {
					result.set(name, object.toString());
				}
			}
		}
		return result;
	}

	private static Collection<?> multi(Object value) {
		if (value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			return collection;
		}
		else if (ObjectUtils.isArray(value)) {
			Object[] values = ObjectUtils.toObjectArray(value);
			return Arrays.asList(values);
		}
		return Arrays.asList(value);
	}

	public static MessageHeaders fromHttp(HttpHeaders headers) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (String name : headers.keySet()) {
			Collection<?> values = multi(headers.get(name));
			if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name)) {
				name = MessageHeaders.CONTENT_TYPE;
			}
			Object value = values == null ? null
					: (values.size() == 1 ? values.iterator().next() : values);
			map.put(name, value);
		}
		return new MessageHeaders(map);
	}

	public static String content(CompositeMessageConverterFactory factory, Message<?> request) {
		Object payload = request.getPayload();
		if (payload instanceof CharSequence) {
			return payload.toString();
		}
		else if (payload instanceof byte[]) {
			return new String((byte[]) payload);
		}
		HttpHeaders headers = MessageUtils.fromMessage(request.getHeaders(),
				new HttpHeaders());
		Object result = factory.getMessageConverterForType(headers.getContentType())
				.toMessage(payload, request.getHeaders()).getPayload();
		if (result != null) {
			payload = result;
		}
		if (payload instanceof CharSequence) {
			return payload.toString();
		}
		else if (payload instanceof byte[]) {
			return new String((byte[]) payload);
		}
		return payload.toString();
	}
}
