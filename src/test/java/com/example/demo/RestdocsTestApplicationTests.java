package com.example.demo;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.restdocs.message.MessageDocumentationInterceptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs
public class RestdocsTestApplicationTests {

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
		messages.outputs(output);
	}

	@Test
	public void foo() throws Exception {
		messages.inputs(input, PayloadDocumentation.requestFields(PayloadDocumentation
				.fieldWithPath("value").description("The value of the Foo")));
		input.send(MessageBuilder.withPayload(new Foo("foo")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
		String content = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/foo-input/message.adoc"),
				Charset.forName("UTF-8"));
		assertThat(content).contains("id:");
		assertThat(content).contains("contentType: application/json");
		assertThat(content).contains("timestamp:");
		assertThat(content).contains("\"value\":\"foo\"");
	}

	@Test
	public void bar() throws Exception {
		messages.inputs(input);
		input.send(MessageBuilder.withPayload(new Foo("bar")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
		String content = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/bar-input/message.adoc"),
				Charset.forName("UTF-8"));
		assertThat(content).contains("\"value\":\"bar\"");
	}
}
