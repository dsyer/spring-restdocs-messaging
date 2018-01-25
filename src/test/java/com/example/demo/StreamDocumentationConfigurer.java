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

package com.example.demo;

import java.util.Map;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.config.RestDocumentationConfigurer;

/**
 * @author Dave Syer
 *
 */
public class StreamDocumentationConfigurer extends
		RestDocumentationConfigurer<StreamSnippetConfigurer, StreamOperationPreprocessorsConfigurer, StreamDocumentationConfigurer> {

	private RestDocumentationContextProvider provider;
	private StreamSnippetConfigurer snippets = new StreamSnippetConfigurer(this);
	private StreamOperationPreprocessorsConfigurer preprocessors = new StreamOperationPreprocessorsConfigurer(this);

	public StreamDocumentationConfigurer(RestDocumentationContextProvider provider) {
		this.provider = provider;
	}

	@Override
	public StreamSnippetConfigurer snippets() {
		return snippets;
	}

	@Override
	public StreamOperationPreprocessorsConfigurer operationPreprocessors() {
		return preprocessors;
	}

	public RestDocumentationContext getContext() {
		return provider.beforeOperation();
	}
	
	public void apply(Map<String, Object> configuration) {
		super.apply(configuration, getContext());
	}

}
