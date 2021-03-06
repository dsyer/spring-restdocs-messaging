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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;

/**
 * @author Dave Syer
 *
 */
public class MessageDocumentation {
	
	private MessageDocumentationInterceptor interceptor;
	private static final String CONTEXT_KEY = RestDocumentationContext.class.getName();
	private MessageDocumentationConfigurer provider;
	private Map<String, Object> configuration = new HashMap<>();
	private MessageRequestConverter requestConverter = new MessageRequestConverter();
	private MessageResponseConverter responseConverter = new MessageResponseConverter();

	public MessageDocumentation(MessageDocumentationInterceptor interceptor, MessageDocumentationConfigurer provider) {
		this.interceptor = interceptor;
		this.provider = provider;
	}

	public void collect(MessageChannel... channels) {
		this.interceptor.collect(channels);
	}

	public void document(Snippet... snippets) {
		for (MessageChannel channel : messages().keySet()) {
			document(channel, snippets);
		}
	}

	public void document(MessageChannel channel, Snippet... snippets) {
		document(channel, channel.toString() + "-" + "message", snippets);
	}

	public void document(MessageChannel channel, String path, Snippet... snippets) {
		generate(channel, null, path, new MessageSnippet(path), snippets);
	}

	public void processor(MessageChannel input, MessageChannel output,
			Snippet... snippets) {
		generate(input, output, "messages", new MessageContractYmlSnippet(), snippets);
	}

	public void source(MessageChannel channel, Snippet... snippets) {
		generate(null, channel, "messages", new MessageContractYmlSnippet(), snippets);
	}

	public void sink(MessageChannel channel, Snippet... snippets) {
		generate(channel, null, "messages", new MessageContractYmlSnippet(), snippets);
	}
	
	private Map<MessageChannel, List<Message<?>>> messages() {
		return this.interceptor.getMessages();
	}

	private void generate(MessageChannel input, MessageChannel output, String path,
			Snippet defaultSnippet, Snippet... snippets) {
		Map<MessageChannel, List<Message<?>>> messages = messages();
		if (messages.isEmpty()) {
			return;
		}
		boolean single = true;
		if (output != null && input != null) {
			single = false;
		}
		MessageChannel channel = input != null ? input : output;
		for (Message<?> message : messages.get(channel)) {
			MessageDelivery<?> request = new MessageDelivery<>(channel.toString(),
					message);
			MessageDelivery<?> response;
			if (!single) {
				response = new MessageDelivery<>(output.toString(),
						messages.get(output).iterator().next());
			}
			else {
				response = request;
			}
			MessageSnippetConfigurer configurer = provider.snippets();
			configurer.withDefaults(defaultSnippet);
			provider.beforeOperation(configuration); // sets up context
			RestDocumentationContext context = (RestDocumentationContext) configuration
					.get(CONTEXT_KEY);
			configurer.apply(configuration, context);
			provider.operationPreprocessors().apply(configuration, context);
			configuration.put("messages", messages);
			configuration.put("delivery", request);
			if (single && output == null) {
				configuration.put("sink", true);
			}
			new RestDocumentationGenerator<>(context.getTestMethodName(),
					requestConverter, responseConverter, snippets).handle(request,
							response, configuration);
			configuration.remove("sink");
			if (!single) {
				return;
			}
		}
	}

	public void afterTest() {
		configuration.clear();
		interceptor.afterTest();
	}

	public static MessageDocumentationConfigurer documentationConfiguration(RestDocumentationContextProvider provider) {
		return new MessageDocumentationConfigurer(provider);
	}

	public static MessageSnippet message() {
		return new MessageSnippet();
	}

	public static MessageSnippet message(String path) {
		return new MessageSnippet(path);
	}

	public static MessageContractYmlSnippet contract() {
		return new MessageContractYmlSnippet();
	}

}
