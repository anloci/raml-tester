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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import guru.nidi.ramltester.core.*;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static guru.nidi.ramltester.util.TestUtils.map;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 *
 */
public class SchemaTest extends HighlevelTestBase {
    private final RamlDefinition simple = RamlLoaders.fromClasspath(getClass()).load("simple.raml");
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    @Test
    public void matchingJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(200, "{\"s\":\"str\",\"i\":42}"));
    }

    @Test
    public void multiNonMatchingJsonSchema() throws UnsupportedEncodingException {
        assertOneResponseViolationThat(simple, get("/schema"), jsonResponse(200, "{\"s\":{},\"i\":true}"), allOf(
                containsString("error: instance type (boolean) does not match any allowed primitive type (allowed: [\"integer\"])"),
                containsString("error: instance type (object) does not match any allowed primitive type (allowed: [\"string\"])")));
    }

    @Test
    public void matchingXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), response(208, "<api-request><input>str</input></api-request>", "text/xml"));
    }

    @Test
    public void matchingReferencedJsonSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), jsonResponse(204, "\"str\""));
        assertNoViolations(simple, get("/schema"), jsonResponse(205, "\"str\""));
    }

    @Test
    public void matchingReferencedXmlSchema() throws UnsupportedEncodingException {
        assertNoViolations(simple, get("/schema"), response(206, "<api-request><input>str</input></api-request>", "application/xml"));
        assertNoViolations(simple, get("/schema"), response(207, "<api-request><input>str</input></api-request>", "application/xml"));
    }

    @Test
    public void notMatchingJsonSchemaInline() throws Exception {
        final RamlReport report = test(simple, get("/schema"), jsonResponse(200, "{\"s\":{},\"i\":true}"));
        final RamlViolationMessage message = report.getResponseViolations().iterator().next();
        assertThat(message.getCause(), instanceOf(JsonSchemaViolationCause.class));
        final Map<?, ?> json = mapper.readValue(mapper.writeValueAsString(message.getCause()), Map.class);
        assertEquals(map("messages", Arrays.asList(
                map("message", "instance type (boolean) does not match any allowed primitive type (allowed: [\"integer\"])", "logLevel", "ERROR"),
                map("message", "instance type (object) does not match any allowed primitive type (allowed: [\"string\"])", "logLevel", "ERROR"))),
                json);
    }

    @Test
    public void notMatchingJsonSchemaInclude() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(201, "5"),
                startsWith("Body does not match schema for action(GET /schema) response(201) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Messages:\n"),
                instanceOf(JsonSchemaViolationCause.class)
        );
    }

    @Test
    public void notMatchingJsonSchemaReferenced() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(202, "5"),
                startsWith("Body does not match schema for action(GET /schema) response(202) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Messages:\n"),
                instanceOf(JsonSchemaViolationCause.class)
        );
    }

    @Test
    public void notMatchingXmlSchemaInline() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        final RamlReport report = test(simple, get("/schema"), response(208, "<api-request>str</api-request>", "text/xml"));
        final RamlViolationMessage message = report.getResponseViolations().iterator().next();
        assertThat(message.getCause(), instanceOf(XmlSchemaViolationCause.class));
        final String childMessage = "{'message':'cvc-complex-type.2.3: Element `api-request` cannot have character [children], because the type`s content type is element-only.','line':1,'column':31}";
        final String completeMessage = "{'message':'cvc-complex-type.2.4.b: The content of element `api-request` is not complete. One of `{input}` is expected.','line':1,'column':31}";
        assertThat(mapper.writeValueAsString(message.getCause()), either(
                equalTo(("{'messages':[" + childMessage + "," + completeMessage + "]}").replace("'", "\"").replace("`", "'"))).or(
                equalTo(("{'messages':[" + completeMessage + "," + childMessage + "]}").replace("'", "\"").replace("`", "'"))
        ));
    }

    @Test
    public void notMatchingXmlSchemaInclude() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                response(206, "5", "application/xml"),
                startsWith("Body does not match schema for action(GET /schema) response(206) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Messages:\n"),
                instanceOf(XmlSchemaViolationCause.class)
        );
    }

    @Test
    public void notMatchingXmlSchemaReferenced() throws Exception {
        assertResponseViolationsThat(
                simple,
                get("/schema"),
                response(207, "5", "application/xml"),
                startsWith("Body does not match schema for action(GET /schema) response(207) mime-type('application/xml')\n" +
                        "Content: 5\n" +
                        "Messages:\n"),
                instanceOf(XmlSchemaViolationCause.class)
        );
    }

    @Test
    public void noSchemaValidatorForMediaType() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                response(209, "5", "text/bla"),
                equalTo("No SchemaValidator found for media type 'text/bla' on action(GET /schema) response(209) mime-type('text/bla')")
        );
    }

    @Test
    public void invalidSchema() throws Exception {
        assertOneResponseViolationThat(
                simple,
                get("/schema"),
                jsonResponse(210, "5"),
                equalTo("Body does not match schema for action(GET /schema) response(210) mime-type('application/json')\n" +
                        "Content: 5\n" +
                        "Messages:\n- Schema invalid: Unrecognized token 'xxx': was expecting ('true', 'false' or 'null')\n" +
                        " at [Source: (guru.nidi.ramltester.core.NamedReader); line: 1, column: 7]")
        );
    }

}
