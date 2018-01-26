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

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * @author Dave Syer
 *
 */
public class MessageTestExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		new DocumentationHandler().afterTestMethod(testContext);
	}

	private static class DocumentationHandler {

		private void afterTestMethod(TestContext testContext) {
			MessageDocumentationInterceptor restDocumentation = findMessageDocumentationInterceptor(
					testContext);
			if (restDocumentation != null) {
				restDocumentation.afterTest();
			}
		}

		private MessageDocumentationInterceptor findMessageDocumentationInterceptor(
				TestContext testContext) {
			try {
				return testContext.getApplicationContext()
						.getBean(MessageDocumentationInterceptor.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				return null;
			}
		}

	}

}
