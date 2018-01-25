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
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;

public class MessageDocumentationInterceptor extends ChannelInterceptorAdapter {

	private MessageDocumentationConfigurer provider;
	private Map<String, Object> configuration = new HashMap<>();
	private RestDocumentationContext context;
	private MessageRequestConverter requestConverter = new MessageRequestConverter();
	private MessageResponseConverter responseConverter = new MessageResponseConverter();
	private Snippet[] inputs = new Snippet[0];
	private MessageChannel input;
	private MessageChannel output;
	private Snippet[] outputs = new Snippet[0];

	public MessageDocumentationInterceptor with(MessageDocumentationConfigurer provider) {
		this.provider = provider;
		this.configuration.put(RestDocumentationContext.class.getName(), this.context);
		return this;
	}

	public MessageDocumentationInterceptor inputs(MessageChannel input,
			Snippet... snippets) {
		this.context = null;
		this.input = input;
		this.inputs = snippets;
		return this;
	}

	public MessageDocumentationInterceptor outputs(MessageChannel output,
			Snippet... snippets) {
		this.context = null;
		this.output = output;
		this.outputs = snippets;
		return this;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		MessageSnippetConfigurer configurer = provider.snippets();
		Snippet[] snippets;
		configurer.withDefaults();
		if (channel == input) {
			snippets = inputs;
		}
		else if (channel == output) {
			snippets = outputs;
		}
		else {
			return message;
		}
		if (this.context == null) {
			this.context = provider.getContext();
		}
		configurer.withAdditionalDefaults(MessageDocumentation.message());
		configurer.apply(configuration, context);
		provider.operationPreprocessors().apply(configuration, context);
		provider.apply(configuration);
		MessageDelivery<?> delivery = new MessageDelivery<>(channel.toString(), message);
		configuration.put("delivery", delivery);
		new RestDocumentationGenerator<>(
				this.context.getTestMethodName() + "-" + channel.toString(),
				requestConverter, responseConverter, snippets).handle(delivery, delivery,
						configuration);
		return message;
	}
}