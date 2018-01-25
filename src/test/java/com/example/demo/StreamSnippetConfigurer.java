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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.config.SnippetConfigurer;

/**
 * @author Dave Syer
 *
 */
public class StreamSnippetConfigurer extends SnippetConfigurer<StreamDocumentationConfigurer, StreamSnippetConfigurer> {
	
	protected StreamSnippetConfigurer(StreamDocumentationConfigurer parent) {
		super(parent);
	}

	private static final Log logger = LogFactory.getLog(StreamSnippetConfigurer.class);

	@Override
	public void apply(Map<String, Object> configuration,
			RestDocumentationContext context) {
		super.apply(configuration, context);
		logger.info(configuration);
		logger.info(context.getOutputDirectory());
	}

}
