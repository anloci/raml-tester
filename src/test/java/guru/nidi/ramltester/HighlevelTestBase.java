/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester;

import guru.nidi.loader.Loader;
import guru.nidi.ramltester.core.*;
import guru.nidi.ramltester.spring.SpringMockRamlRequest;
import guru.nidi.ramltester.spring.SpringMockRamlResponse;
import guru.nidi.ramltester.util.MediaType;
import guru.nidi.ramltester.util.Message;
import org.hamcrest.Matcher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;

import static guru.nidi.ramltester.junit.RamlMatchers.requestChecks;
import static guru.nidi.ramltester.junit.RamlMatchers.responseChecks;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HighlevelTestBase {
    protected MockHttpServletResponse response(int code, String body, String contentType) throws UnsupportedEncodingException {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(code);
        response.setContentType(contentType);
        response.getWriter().print(body);
        return response;
    }

    protected MockHttpServletResponse jsonResponse(int code, String json) throws UnsupportedEncodingException {
        return response(code, json, "application/json");
    }

    protected MockHttpServletResponse jsonResponse(int code) throws UnsupportedEncodingException {
        return response(code, "", "application/json");
    }

    protected void assertOneRequestViolationThat(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response, Matcher<String> matcher) {
        assertOneRequestViolationThat(test(raml, request, response), matcher);
    }

    protected void assertOneRequestViolationThat(RamlReport report, Matcher<String> matcher) {
        assertThat(report, responseChecks());
        assertOneViolationThat(report.getRequestViolations(), matcher);
    }

    @SafeVarargs
    protected final void assertRequestViolationsThat(RamlReport report, Matcher<String>... matcher) {
        assertThat(report, responseChecks());
        assertViolationsThat(report.getRequestViolations(), matcher);
    }

    protected void assertOneResponseViolationThat(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response, Matcher<String> matcher) {
        assertOneResponseViolationThat(test(raml, request, response), matcher);
    }

    protected void assertOneResponseViolationThat(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response, Matcher<String> messageMatcher, Matcher<Object> messageObjectMatcher) {
        assertOneResponseViolationThat(test(raml, request, response), messageMatcher, messageObjectMatcher);
    }

    protected void assertOneResponseViolationThat(RamlReport report, Matcher<String> matcher) {
        assertThat(report, requestChecks());
        assertOneViolationThat(report.getResponseViolations(), matcher);
    }

    protected void assertOneResponseViolationThat(RamlReport report, Matcher<String> messageMatcher, Matcher<Object> messageObjectMatcher) {
        assertThat(report, requestChecks());
        assertOneViolationThat(report.getResponseViolations(), messageMatcher, messageObjectMatcher);
    }

    protected void assertResponseViolationsThat(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response, Matcher<String> matcher) {
        assertResponseViolationsThat(test(raml, request, response), matcher);
    }

    protected void assertResponseViolationsThat(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response, Matcher<String> messageMatcher, Matcher<Object> messageObjectMatcher) {
        assertResponseViolationsThat(test(raml, request, response), messageMatcher, messageObjectMatcher);
    }

    protected void assertResponseViolationsThat(RamlReport report, Matcher<String> matcher) {
        assertThat(report, requestChecks());
        assertViolationsThat(report.getResponseViolations(), matcher);
    }

    protected void assertResponseViolationsThat(RamlReport report, Matcher<String> messageMatcher, Matcher<Object> messageObjectMatcher) {
        assertThat(report, requestChecks());
        for (final RamlViolationMessage message : report.getResponseViolations()) {
            assertThat(message.getMessage(), messageMatcher);
            assertThat(message.getCause(), messageObjectMatcher);
        }
    }

    protected RamlReport test(RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response) {
        return test(raml, request.buildRequest(new MockServletContext()), response);
    }

    protected RamlReport test(ReportAggregator aggregator, RamlDefinition raml, MockHttpServletRequestBuilder request, MockHttpServletResponse response) {
        return aggregator.addReport(test(raml, request.buildRequest(new MockServletContext()), response));
    }

    protected RamlReport test(RamlDefinition raml, MockHttpServletRequest request, MockHttpServletResponse response) {
        return raml.assumingBaseUri("http://nidi.guru/raml/v1").testAgainst(
                new SpringMockRamlRequest(request),
                new SpringMockRamlResponse(response));
    }

    protected void assertOneViolationThat(RamlViolations violations, Matcher<String> matcher) {
        assertThat("Expected exactly one violation", violations.size(), equalTo(1));
        assertThat(violations.iterator().next().getMessage(), matcher);
    }

    protected void assertOneViolationThat(RamlViolations violations, Matcher<String> messageMatcher, Matcher<Object> messageObjectMatcher) {
        assertThat("Expected exactly one violation", violations.size(), equalTo(1));
        final RamlViolationMessage message = violations.iterator().next();
        assertThat(message.getMessage(), messageMatcher);
        assertThat(message.getCause(), messageObjectMatcher);
    }

    protected Matcher<String> containsInOrder(String... parts) {
        return stringContainsInOrder(Arrays.asList(parts));
    }

    @SafeVarargs
    protected final void assertViolationsThat(RamlViolations violations, Matcher<String>... matchers) {
        assertEquals(matchers.length, violations.size());
        final Iterator<RamlViolationMessage> it = violations.iterator();
        for (final Matcher<String> matcher : matchers) {
            assertThat(it.next().getMessage(), matcher);
        }
    }

    public static class DefaultOkSchemaValidator implements SchemaValidator {
        @Override
        public boolean supports(MediaType mediaType) {
            return mediaType.isCompatibleWith(MediaType.valueOf("application/default"));
        }

        @Override
        public SchemaValidator withLoader(Loader loader) {
            return this;
        }

        @Override
        public void validate(Reader content, Reader schema, RamlViolations violations, Message message) {
            violations.add(message.withParam("ok"));
        }
    }

    public static class FormEncodedSchemaValidator implements SchemaValidator {
        @Override
        public boolean supports(MediaType mediaType) {
            return mediaType.isCompatibleWith(MediaType.FORM_URL_ENCODED);
        }

        @Override
        public SchemaValidator withLoader(Loader loader) {
            return this;
        }

        @Override
        public void validate(Reader content, Reader schema, RamlViolations violations, Message message) {
        }
    }
}
