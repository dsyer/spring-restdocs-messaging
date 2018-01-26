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

import java.util.Map;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.config.RestDocumentationConfigurer;

/**
 * @author Dave Syer
 *
 */
public class MessageDocumentationConfigurer extends
		RestDocumentationConfigurer<MessageSnippetConfigurer, MessageOperationPreprocessorsConfigurer, MessageDocumentationConfigurer> {

	private RestDocumentationContextProvider provider;
	private MessageSnippetConfigurer snippets = new MessageSnippetConfigurer(this);
	private MessageOperationPreprocessorsConfigurer preprocessors = new MessageOperationPreprocessorsConfigurer(this);

	public MessageDocumentationConfigurer(RestDocumentationContextProvider provider) {
		this.provider = provider;
	}

	@Override
	public MessageSnippetConfigurer snippets() {
		return snippets;
	}

	@Override
	public MessageOperationPreprocessorsConfigurer operationPreprocessors() {
		return preprocessors;
	}
	
	public void beforeOperation(Map<String, Object> configuration) {
		super.apply(configuration, provider.beforeOperation());
	}

}
