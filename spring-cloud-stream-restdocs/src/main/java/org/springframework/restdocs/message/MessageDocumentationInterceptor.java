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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

public class MessageDocumentationInterceptor extends ChannelInterceptorAdapter {

	private Map<MessageChannel, List<Message<?>>> messages = new LinkedHashMap<>();
	private boolean collectAll = true;

	public void collect(MessageChannel... channels) {
		for (MessageChannel channel : channels) {
			messages.put(channel, new ArrayList<>());
		}
		this.collectAll = false;
	}

	public Map<MessageChannel, List<Message<?>>> getMessages() {
		return this.messages;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (this.collectAll || messages.containsKey(channel)) {
			messages.computeIfAbsent(channel, c -> new ArrayList<>()).add(message);
		}
		return message;
	}

	public void afterTest() {
		messages.clear();
	}

}