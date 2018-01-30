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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.restdocs.RestDocumentationContextProvider;

/**
 * @author Dave Syer
 *
 */
@Configuration
public class MessageRestDocsAutoConfiguration {

	@Bean
	@GlobalChannelInterceptor(patterns = "*")
	public MessageDocumentationInterceptor testMessageInterceptor() {
		return new MessageDocumentationInterceptor();
	}

	@Bean
	public MessageDocumentation messageDocumentation(
			MessageDocumentationInterceptor interceptor,
			RestDocumentationContextProvider restDocumentation) {
		return new MessageDocumentation(interceptor,
				new MessageDocumentationConfigurer(restDocumentation));
	}

}
