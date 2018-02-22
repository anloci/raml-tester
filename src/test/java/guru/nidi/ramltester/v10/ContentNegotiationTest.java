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
package guru.nidi.ramltester.v10;

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import org.junit.Test;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ContentNegotiationTest extends HighlevelTestBase {

    private static final RamlDefinition simple = RamlLoaders.fromClasspath(ContentNegotiationTest.class).load("simple.raml");

    @Test
    public void noAcceptHeaderIsOk() throws Exception {
        assertThat(test(simple, get("/"), jsonResponse(200)), hasNoViolations());
    }

    @Test
    public void emptyAcceptHeaderIsOk() throws Exception {
        assertThat(test(simple, get("/").header("Accept", " "), jsonResponse(200)), hasNoViolations());
    }

    @Test
    public void emptyBodyIsOk() throws Exception {
        assertThat(test(simple, get("/").accept("*/*"), jsonResponse(200)), hasNoViolations());
    }

    @Test
    public void wildcardAcceptHeaderIsOk() throws Exception {
        assertThat(test(simple, get("/mediaType").accept("*/*"), response(201, "", "text/xml;bla=blu")),
                hasNoViolations());
    }

    @Test
    public void responseMayHaveParametersThatAreNotInRamlNeitherInAccept() throws Exception {
        assertThat(test(simple, get("/mediaType").accept("text/xml"), response(201, "", "text/xml;bla=blu")),
                hasNoViolations());
    }

    @Test
    public void noResponseContentTypeIsOk() throws Exception {
        assertThat(test(simple, get("/mediaType").accept("bla/blu"), response(201, "", null)),
                hasNoViolations());
    }

    @Test
    public void simpleMatchingAcceptHeader() throws Exception {
        assertThat(test(simple, get("/schema").accept("application/json"), jsonResponse(200, "{\"s\":\"string\",\"i\":42}")), hasNoViolations());
    }

    @Test
    public void wildcardMatchingAcceptHeader() throws Exception {
        assertThat(test(simple, get("/schema").accept("application/*"), jsonResponse(200, "{\"s\":\"string\",\"i\":42}")), hasNoViolations());
        assertThat(test(simple, get("/schema").accept("*/*"), jsonResponse(200, "{\"s\":\"string\",\"i\":42}")), hasNoViolations());
    }

    @Test
    public void wildcardMatchingResponseHeader() throws Exception {
        assertThat(test(simple, get("/mediaType").accept("text/xml;a=b"), response(202, "", "text/xml;a=b")), hasNoViolations());
        assertThat(test(simple, get("/mediaType").accept("text/xml"), response(202, "", "text/xml")), hasNoViolations());
        assertThat(test(simple, get("/mediaType").accept("text/*"), response(202, "", "text/bla")), hasNoViolations());
        assertThat(test(simple, get("/mediaType").accept("*/*"), response(202, "", "bla/blu")), hasNoViolations());
    }

    @Test
    public void nonMatchingResponse() throws Exception {
        assertOneResponseViolationThat(
                test(simple, get("/schema").accept("application/bla", "x/y"), jsonResponse(200, "{\"s\":\"string\",\"i\":42}")),
                equalTo("Response Content-Type 'application/json' is not compatible with Accept header 'application/bla, x/y'"));
    }

    @Test
    public void responseWithNoMatchingRamlMimeType() throws Exception {
        assertOneResponseViolationThat(
                test(simple, get("/mediaType").accept("*/*"), response(201, "", "bla/blu")),
                equalTo("Media type 'bla/blu' is not defined on action(GET /mediaType) response(201)"));
    }

    @Test
    public void invalidResponseMimeType() throws Exception {
        assertOneResponseViolationThat(
                test(simple, get("/mediaType").header("Accept", "text/xml"), response(201, "", "text")),
                equalTo("Illegal media type 'text' in action(GET /mediaType) response(201): Does not contain '/'"));
    }

    @Test
    public void useQValue() throws Exception {
        assertThat(test(simple, get("/mediaType").accept("text/plain;q=.5", "text/xml"), response(201, "", "text/xml")),
                hasNoViolations());
        assertOneResponseViolationThat(
                test(simple, get("/mediaType").accept("text/plain;q=.5", "text/xml"), response(201, "", "text/plain")),
                equalTo("Given the Accept header 'text/plain;q=.5, text/xml', the response to action(GET /mediaType) response(201) should have media type 'text/xml', not 'text/plain'"));
    }

    @Test
    public void invalidQ() throws Exception {
        assertOneRequestViolationThat(
                test(simple, get("/mediaType").header("Accept", "text/xml;q=b,text/plain"), response(201, "", "text/plain")),
                equalTo("Illegal media type 'text/xml;q=b' in Accept header: Invalid quality value 'b': Should be a number between 0.0 and 1.0"));
    }

}
