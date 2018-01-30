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
	private Map<MessageChannel, MessageChannelConfiguration> channels = new HashMap<>();
	private Map<MessageChannel, List<Message<?>>> messages = new LinkedHashMap<>();

	public MessageDocumentationInterceptor with(MessageDocumentationConfigurer provider) {
		this.provider = provider;
		return this;
	}

	public MessageDocumentationInterceptor collect(MessageChannel... channels) {
		for (MessageChannel channel : channels) {
			messages.put(channel, new ArrayList<>());
		}
		return this;
	}

	public void document(Snippet... snippets) {
		if (messages.isEmpty() || snippets.length == 0) {
			return;
		}
		MessageDelivery<?> request = null;
		MessageDelivery<?> response = null;
		for (MessageChannel channel : messages.keySet()) {
			if (!messages.get(channel).isEmpty()) {
				if (request == null) {
					request = new MessageDelivery<>(channel.toString(),
							messages.get(channel).iterator().next());
				}
				else {
					response = new MessageDelivery<>(channel.toString(),
							messages.get(channel).iterator().next());
				}
			}
		}
		if (request == null && response == null) {
			return;
		}
		if (response == null) {
			response = request;
		}
		MessageSnippetConfigurer configurer = provider.snippets();
		configurer.withDefaults(snippets);
		provider.beforeOperation(configuration); // sets up context
		RestDocumentationContext context = (RestDocumentationContext) configuration
				.get(CONTEXT_KEY);
		configurer.apply(configuration, context);
		provider.operationPreprocessors().apply(configuration, context);
		configuration.put("messages", messages);
		new RestDocumentationGenerator<>(context.getTestMethodName(), requestConverter,
				responseConverter, snippets).handle(request, response, configuration);
	}

	public MessageDocumentationInterceptor document(MessageChannel channel,
			Snippet... snippets) {
		return document(channel, channel.toString() + "-" + "message", snippets);
	}

	public MessageDocumentationInterceptor document(MessageChannel channel, String path,
			Snippet... snippets) {
		channels.put(channel, new MessageChannelConfiguration(path, snippets));
		return this;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		Snippet[] snippets;
		String path;
		if (messages.containsKey(channel)) {
			messages.get(channel).add(message);
		}
		if (channels.containsKey(channel)) {
			MessageChannelConfiguration config = channels.get(channel);
			snippets = config.getSnippets();
			path = config.getPath();
		}
		else {
			return message;
		}
		provider.beforeOperation(configuration); // sets up context
		MessageSnippetConfigurer configurer = provider.snippets();
		configurer.withDefaults(MessageDocumentation.message(path));
		RestDocumentationContext context = (RestDocumentationContext) configuration
				.get(CONTEXT_KEY);
		configurer.apply(configuration, context);
		provider.operationPreprocessors().apply(configuration, context);
		MessageDelivery<?> delivery = new MessageDelivery<>(channel.toString(), message);
		configuration.put("delivery", delivery);
		new RestDocumentationGenerator<>(context.getTestMethodName(), requestConverter,
				responseConverter, snippets).handle(delivery, delivery, configuration);
		return message;
	}

	private static class MessageChannelConfiguration {
		private Snippet[] snippets = new Snippet[0];
		private String path;

		public MessageChannelConfiguration(String path, Snippet... snippets) {
			this.path = path;
			this.snippets = snippets;
		}

		public Snippet[] getSnippets() {
			return this.snippets;
		}

		public String getPath() {
			return this.path;
		}

	}

	public void afterTest() {
		configuration.clear();
		channels.clear();
		messages.clear();
	}

}