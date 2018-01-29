package org.springframework.restdocs.message;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.StandardTemplateResourceResolver;
import org.springframework.restdocs.templates.TemplateEngine;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.templates.mustache.MustacheTemplateEngine;

/**
 * A {@link org.springframework.restdocs.snippet.Snippet} that documents the Spring Cloud
 * Contract Groovy YAML.
 *
 * @author Marcin Grzejszczak
 * @since 1.0.4
 */
public class ContractExchangeSnippet implements Snippet {

	private final Map<String, Object> attributes = new HashMap<>();

	private String snippetName;

	private String templateName;

	private static final String TEMPLATE_NAME = "contract-exchange";

	private static final String SNIPPET_NAME = "contracts";

	private static final Set<String> IGNORED_HEADERS = new HashSet<>(
			Arrays.asList(HttpHeaders.HOST, HttpHeaders.CONTENT_LENGTH, MessageHeaders.ID,
					MessageHeaders.TIMESTAMP));

	private static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

		@Override
		public String getId() {
			return "contracts";
		}

		@Override
		public String getFileExtension() {
			return "yml";
		}
	};

	/**
	 * Creates a new {@code ContractDslSnippet} with no additional attributes.
	 */
	protected ContractExchangeSnippet() {
		this(null);
	}

	/**
	 * Creates a new {@code ContractDslSnippet} with the given additional
	 * {@code attributes} that will be included in the model during template rendering.
	 *
	 * @param attributes The additional attributes
	 */
	protected ContractExchangeSnippet(Map<String, Object> attributes) {
		this(SNIPPET_NAME, TEMPLATE_NAME, attributes);
	}

	protected ContractExchangeSnippet(String snippetName, String templateName,
			Map<String, Object> attributes) {
		this.templateName = templateName;
		this.snippetName = snippetName;
		if (attributes != null) {
			this.attributes.putAll(attributes);
		}
	}

	@Override
	public void document(Operation operation) throws IOException {
		RestDocumentationContext context = (RestDocumentationContext) operation
				.getAttributes().get(RestDocumentationContext.class.getName());
		RestDocumentationContextPlaceholderResolverFactory placeholders = new RestDocumentationContextPlaceholderResolverFactory();
		WriterResolver writerResolver = new StandardWriterResolver(placeholders, "UTF-8",
				TEMPLATE_FORMAT);
		try (Writer writer = writerResolver.resolve(this.snippetName, operation.getName(),
				context)) {
			Map<String, Object> model = createModel(operation);
			model.putAll(this.attributes);
			TemplateEngine templateEngine = new MustacheTemplateEngine(
					new StandardTemplateResourceResolver(TEMPLATE_FORMAT));
			writer.append(
					templateEngine.compileTemplate(this.templateName).render(model));
		}
	}

	protected Map<String, Object> createModel(Operation operation) {
		return createModelForContract(operation);
	}

	private void insertResponseModel(Operation operation, Map<String, Object> model) {
		if (!(operation.getResponse() instanceof MessageOperation)) {
			return;
		}
		MessageOperation response = (MessageOperation) operation.getResponse();
		model.put("response_destination", response.getDestination());
		if (response.getContent().length > 0) {
			model.put("response_body", response.getContentAsString());
		}
		Map<String, Object> headers = new LinkedHashMap<>(
				MessageUtils.fromHttp(response.getHeaders()));
		filterHeaders(headers);
		model.put("response_headers_present", !headers.isEmpty());
		model.put("response_headers", headers.entrySet());
		@SuppressWarnings("unchecked")
		Set<String> jsonPaths = (Set<String>) operation.getAttributes()
				.get("contract.jsonPaths");
		model.put("response_json_paths_present",
				jsonPaths != null && !jsonPaths.isEmpty());
		model.put("response_json_paths", jsonPaths(jsonPaths));
	}

	private Set<JsonPaths> jsonPaths(Set<String> jsonPaths) {
		Set<JsonPaths> paths = new HashSet<>();
		if (jsonPaths == null) {
			return paths;
		}
		for (String s : jsonPaths) {
			paths.add(new JsonPaths(s));
		}
		return paths;
	}

	private void insertRequestModel(Operation operation, Map<String, Object> model) {
		if (!(operation.getResponse() instanceof MessageOperation)) {
			return;
		}
		MessageOperation request = (MessageOperation) operation.getRequest();
		model.put("request_destination", request.getDestination());
		if (request.getContent().length > 0) {
			model.put("request_body", request.getContentAsString());
		}
		Map<String, Object> headers = new LinkedHashMap<>(
				MessageUtils.fromHttp(request.getHeaders()));
		filterHeaders(headers);
		model.put("request_headers_present", !headers.isEmpty());
		model.put("request_headers", headers.entrySet());
	}

	private void filterHeaders(Map<String, Object> headers) {
		for (String header : IGNORED_HEADERS) {
			if (headers.containsKey(header)) {
				headers.remove(header);
			}
		}
	}

	private Map<String, Object> createModelForContract(Operation operation) {
		Map<String, Object> modelForContract = new HashMap<>();
		modelForContract.put("label", operation.getName());
		insertRequestModel(operation, modelForContract);
		insertResponseModel(operation, modelForContract);
		return modelForContract;
	}

}

class JsonPaths {
	private final String jsonPath;

	JsonPaths(String jsonPath) {
		this.jsonPath = jsonPath;
	}

	public String getJsonPath() {
		return this.jsonPath;
	}
}