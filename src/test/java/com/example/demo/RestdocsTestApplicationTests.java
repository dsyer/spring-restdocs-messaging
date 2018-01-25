package com.example.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestdocsTestApplicationTests {

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Autowired
	@Qualifier(Processor.INPUT)
	private MessageChannel input;

	@Autowired
	@Qualifier(Processor.OUTPUT)
	private MessageChannel output;

	@Autowired
	@Qualifier("testInterceptor")
	private DocumentationInterceptor testInterceptor;

	@Autowired
	private MessageCollector collector;

	@Before
	public void setUp() {
		testInterceptor.setProvider(
				StreamDocumentation.documentationConfiguration(restDocumentation));
	}

	@Test
	public void contextLoads() throws Exception {
		input.send(MessageBuilder.withPayload(new Foo("foo")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
	}

	@TestConfiguration
	public static class ExtraConfiguration {

		@Bean
		@GlobalChannelInterceptor(patterns = "*")
		public ChannelInterceptor testInterceptor() {
			return new DocumentationInterceptor();
		}
	}

	private static class DocumentationInterceptor extends ChannelInterceptorAdapter {
		private StreamDocumentationConfigurer provider;
		private Map<String, Object> configuration = new HashMap<>();
		private RestDocumentationContext context;
		private MessageRequestConverter requestConverter = new MessageRequestConverter();
		private MessageResponseConverter responseConverter = new MessageResponseConverter();

		public void setProvider(StreamDocumentationConfigurer provider) {
			this.provider = provider;
			this.context = provider.getContext();
			this.configuration.put(RestDocumentationContext.class.getName(), this.context);
		}

		@Override
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			System.err.println(channel);
			System.err.println(message);
			provider.snippets().apply(configuration, context);
			provider.operationPreprocessors().apply(configuration, context);
			provider.womp(configuration);
			new RestDocumentationGenerator<>(channel.toString(), requestConverter,
					responseConverter).handle(
							new MessageDelivery(channel.toString(), message),
							new MessageDelivery(channel.toString(), message),
							configuration);
			;
			return message;
		}
	}
}
