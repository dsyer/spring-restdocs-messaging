package com.example.demo;

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
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.message.MessageDocumentation;
import org.springframework.restdocs.message.MessageDocumentationInterceptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
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
	private MessageDocumentationInterceptor messages;

	@Autowired
	private MessageCollector collector;

	@Before
	public void setUp() {
		messages.with(MessageDocumentation.documentationConfiguration(restDocumentation))
				.outputs(output);
	}

	@Test
	public void contextLoads() throws Exception {
		messages.inputs(input, PayloadDocumentation.requestFields(PayloadDocumentation
				.fieldWithPath("value").description("The value of the Foo")));
		input.send(MessageBuilder.withPayload(new Foo("foo")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
	}

	@TestConfiguration
	public static class ExtraConfiguration {

		@Bean
		@GlobalChannelInterceptor(patterns = "*")
		public ChannelInterceptor testInterceptor() {
			return new MessageDocumentationInterceptor();
		}
	}
}
