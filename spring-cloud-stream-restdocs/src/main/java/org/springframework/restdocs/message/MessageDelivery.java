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
import java.util.List;

import org.springframework.messaging.Message;

/**
 * @author Dave Syer
 *
 */
class MessageDelivery<T> {

	private final String destination;
	private final List<Message<T>> messages = new ArrayList<>();

	public MessageDelivery(String destination, Message<T> message) {
		this.destination = destination;
		this.messages.add(message);
	}

	public String getDestination() {
		return this.destination;
	}

	public Message<T> getMessage() {
		return this.messages.isEmpty() ? null : this.messages.iterator().next();
	}

	public List<Message<T>> getMessages() {
		return this.messages;
	}

	@Override
	public String toString() {
		return "MessageDelivery [destination=" + this.destination + ", messages="
				+ this.messages + "]";
	}

}
