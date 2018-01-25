This project shows a proof of concept of how to use Spring RESTdocs with Spring Cloud Stream (the same techniques probably work equally well with Spring Integration).

The basic idea is that tyou get a `MessageDocumentationInterceptor` that inspects all the messages flowing. It is also a RESTdocs configurer, so it has methods that accept `Snippets`. The default behaviour is to output the message headers and payload for an "input" and/or an "output" channel which are selected by the user. E.g.

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureRestDocs
public class RestdocsTestApplicationTests {

	@Autowired
	@Qualifier(Processor.INPUT)
	private MessageChannel input;

	@Autowired
	private MessageDocumentationInterceptor messages;

	@Autowired
	private MessageCollector collector;

	@Test
	public void bar() throws Exception {
		messages.inputs(input);
		input.send(MessageBuilder.withPayload(new Foo("bar")).build());
		assertThat(collector.forChannel(output).poll(1, TimeUnit.SECONDS)).isNotNull();
	}

}
```

Results in an Asciidoctor snippet in `target/generated-snippets/bar-input/message.adoc`:

```
[source,options="nowrap"]
----
id: e85c0363-628d-d763-c69e-83861a42c3af
contentType: application/json
timestamp: 1516897094835

{"value":"bar"}
----
```

You can add other snippets (e.g. to document fields in the message payload) using the public method in `MessageDocumentationInterceptor` before you send or receive any messages.
