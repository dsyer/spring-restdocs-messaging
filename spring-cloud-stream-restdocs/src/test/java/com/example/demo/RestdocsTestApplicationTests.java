package com.example.demo;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.restdocs.message.MessageDocumentation;
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
	private MessageDocumentation messages;

	@Autowired
	private MessageCollector collector;
	
	@Test
	public void foo() throws Exception {
		input.send(MessageBuilder.withPayload(new Foo("foo")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
		messages.processor(input, output);
		messages.document(output);
		messages.document(input, PayloadDocumentation.requestFields(PayloadDocumentation
				.fieldWithPath("value").description("The value of the Foo")));
		String content = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/foo/input-message.adoc"),
				Charset.forName("UTF-8"));
		assertThat(content).contains("id:");
		assertThat(content).contains("contentType: application/json");
		assertThat(content).contains("timestamp:");
		assertThat(content).contains("\"value\":\"foo\"");
		String yaml = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/contracts/foo.yml"),
				Charset.forName("UTF-8"));
		assertThat(yaml).contains("input:");
		assertThat(yaml).contains("messageBody:");
		assertThat(yaml).contains("outputMessage:");
		assertThat(yaml).contains("body:");
	}

	@Test
	public void bar() throws Exception {
		input.send(MessageBuilder.withPayload(new Foo("bar")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
		messages.document(input);
		messages.document(output, "output-{step}");
		String content = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/bar/output-3.adoc"),
				Charset.forName("UTF-8"));
		assertThat(content).contains("\"value\":\"bar1\"");
	}

	@Test
	public void spam() throws Exception {
		output.send(MessageBuilder.withPayload(new Bar("spam")).build());
		messages.source(output);
		String yaml = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/contracts/spam.yml"),
				Charset.forName("UTF-8"));
		assertThat(yaml).doesNotContain("input:");
		assertThat(yaml).contains("outputMessage:");
		assertThat(yaml).contains("\"value\":\"spam\"");
	}

	@Test
	public void bucket() throws Exception {
		input.send(MessageBuilder.withPayload(new Foo("bucket")).build());
		messages.sink(input);
		String yaml = StreamUtils.copyToString(
				new FileInputStream("target/generated-snippets/contracts/bucket.yml"),
				Charset.forName("UTF-8"));
		assertThat(yaml).contains("input:");
		assertThat(yaml).doesNotContain("outputMessage:");
		assertThat(yaml).contains("\"value\":\"bucket\"");
	}
}

@SpringBootApplication
@EnableBinding(Processor.class)
class RestdocsTestApplication {

	@Autowired
	@Output(Processor.OUTPUT)
	private MessageChannel output;

	@StreamListener(Processor.INPUT)
	public void process(Foo foo) {
		List<Bar> bars = new ArrayList<>();
		if (foo.getValue().startsWith("bar")) {
			for (int i = 0; i < 2; i++) {
				bars.add(new Bar(foo.getValue() + i));
			}
		}
		else {
			bars.add(new Bar(foo.getValue()));
		}
		for (Bar bar : bars) {
			output.send(MessageBuilder.withPayload(bar).build());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(RestdocsTestApplication.class, args);
	}
}


class Foo {

	private String value;

	Foo() {
	}

	public String lowercase() {
		return value.toLowerCase();
	}

	public Foo(String value) {
		this.value = value;
	}

	public String uppercase() {
		return value.toUpperCase();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

class Bar {

	private String value;

	Bar() {
	}

	public Bar(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}