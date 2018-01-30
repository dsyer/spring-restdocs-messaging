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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;

public class MessageDocumentationInterceptor extends ChannelInterceptorAdapter {

	private static final String CONTEXT_KEY = RestDocumentationContext.class.getName();
	private MessageDocumentationConfigurer provider;
	private Map<String, Object> configuration = new HashMap<>();
	private MessageRequestConverter requestConverter = new MessageRequestConverter();
	private MessageResponseConverter responseConverter = new MessageResponseConverter();
	private Map<MessageChannel, List<Message<?>>> messages = new LinkedHashMap<>();
	private boolean collectAll = true;

	public MessageDocumentationInterceptor with(MessageDocumentationConfigurer provider) {
		this.provider = provider;
		return this;
	}

	public MessageDocumentationInterceptor collect(MessageChannel... channels) {
		for (MessageChannel channel : channels) {
			messages.put(channel, new ArrayList<>());
		}
		this.collectAll = false;
		return this;
	}

	public void document(Snippet... snippets) {
		for (MessageChannel channel : messages.keySet()) {
			document(channel, snippets);
		}
	}

	public void document(MessageChannel channel, Snippet... snippets) {
		document(channel, channel.toString() + "-" + "message", snippets);
	}

	public void document(MessageChannel channel, String path, Snippet... snippets) {
		document(channel, null, path, snippets);
	}

	public void document(MessageChannel input, MessageChannel output,
			Snippet... snippets) {
		document(input, output, "messages", snippets);
	}

	public void document(MessageChannel input, MessageChannel output, String path,
			Snippet... snippets) {
		if (messages.isEmpty()) {
			return;
		}
		boolean single = true;
		if (output != null) {
			single = false;
		}
		for (Message<?> message : messages.get(input)) {
			MessageDelivery<?> request = new MessageDelivery<>(input.toString(), message);
			MessageDelivery<?> response;
			if (!single) {
				response = new MessageDelivery<>(output.toString(),
						messages.get(output).iterator().next());
			}
			else {
				response = request;
			}
			MessageSnippetConfigurer configurer = provider.snippets();
			if (!single) {
				configurer.withDefaults(new MessageContractYmlSnippet());
			}
			else {
				configurer.withDefaults(new MessageSnippet(path));
			}
			provider.beforeOperation(configuration); // sets up context
			RestDocumentationContext context = (RestDocumentationContext) configuration
					.get(CONTEXT_KEY);
			configurer.apply(configuration, context);
			provider.operationPreprocessors().apply(configuration, context);
			configuration.put("messages", messages);
			MessageDelivery<?> delivery = new MessageDelivery<>(input.toString(),
					message);
			configuration.put("delivery", delivery);
			new RestDocumentationGenerator<>(context.getTestMethodName(),
					requestConverter, responseConverter, snippets).handle(request,
							response, configuration);
			if (!single) {
				return;
			}
		}
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (this.collectAll || messages.containsKey(channel)) {
			messages.computeIfAbsent(channel, c -> new ArrayList<>()).add(message);
		}
		return message;
	}

	public void afterTest() {
		configuration.clear();
		messages.clear();
	}

}