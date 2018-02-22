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
package guru.nidi.ramltester.v08;

import guru.nidi.ramltester.HighlevelTestBase;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.SimpleReportAggregator;
import guru.nidi.ramltester.junit.ExpectedUsage;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.MediaType;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class FormTest extends HighlevelTestBase {
    private static final RamlDefinition form = RamlLoaders.fromClasspath(QueryParameterTest.class).load("form.raml");
    private static final SimpleReportAggregator aggregator = new SimpleReportAggregator();

    @ClassRule
    public static final ExpectedUsage expectedUsage = new ExpectedUsage(aggregator);

    @Test
    public void formTest() throws Exception {
        assertThat(
                test(aggregator, form,
                        post("/form").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("param", "a+b"),
                        response(200, "", null)),
                hasNoViolations());
    }

    @Test
    public void undefinedParam() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                form,
                post("/form").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("hula", "a+b"),
                response(200, "", null)),
                equalTo("Form parameter 'hula' on action(POST /form) mime-type('application/x-www-form-urlencoded') is not defined")
        );
    }

    @Test
    public void noContentType() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                form,
                post("/form").content("param=a+b"),
                response(200, "", null)),
                equalTo("No Content-Type header given")
        );
    }

    @Test
    public void formWithoutFormParameters() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                form,
                post("/form/parameterless").contentType(MediaType.APPLICATION_FORM_URLENCODED),
                response(200, "", null)),
                equalTo("No formParameters given on action(POST /form/parameterless) mime-type('application/x-www-form-urlencoded')")
        );
    }

    @Test
    public void formNoFormParameters() throws Exception {
        assertOneRequestViolationThat(test(aggregator,
                form,
                post("/form/noparameters").contentType(MediaType.APPLICATION_FORM_URLENCODED),
                response(200, "", null)),
                equalTo("No formParameters given on action(POST /form/noparameters) mime-type('application/x-www-form-urlencoded')")
        );
    }

    @Test
    public void formWithSchema() throws Exception {
        final RamlDefinition form = RamlLoaders
                .fromClasspath(getClass())
                .addSchemaValidator(new FormEncodedSchemaValidator())
                .load("form.raml");

        assertOneRequestViolationThat(test(aggregator,
                form,
                post("/form/schema").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("param", "a"),
                response(200, "", null)),
                equalTo("No schema allowed on action(POST /form/schema) mime-type('application/x-www-form-urlencoded')")
        );
    }

    @Test
    public void multipartForm() throws Exception {
        assertThat(
                test(aggregator, form,
                        fileUpload("/form/multi").file("file", new byte[]{1, 2, 3})
                                .contentType(MediaType.MULTIPART_FORM_DATA).param("param", "a +b"),
                        response(200, "", null)),
                hasNoViolations());
    }

    @Test
    public void noFormMimeType() throws Exception {
        assertThat(
                test(aggregator, form,
                        post("/noForm").contentType(MediaType.APPLICATION_JSON).content("\"hula\""),
                        jsonResponse(200)),
                hasNoViolations());
    }

}
