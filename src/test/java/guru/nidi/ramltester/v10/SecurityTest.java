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
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.RamlViolationException;
import org.junit.Test;

import static guru.nidi.ramltester.junit.RamlMatchers.hasNoViolations;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class SecurityTest extends HighlevelTestBase {
    private static final RamlLoaders base = RamlLoaders.fromClasspath(SecurityTest.class);
    private static final RamlDefinition
            global = base.load("global-security.raml"),
            local = base.load("local-security.raml");

    @Test
    public void allowSecurityElementsInGlobalSecured() throws Exception {
        assertThat(test(global, get("/sec2?access_token=bla").header("Authorization2", "blu"), response(401, "", null)),
                hasNoViolations());
    }

    @Test
    public void allowSecurityElementsInLocalGlobalSecured() throws Exception {
        assertThat(test(global, get("/sec12").header("AuthorizationOpt", "blu"), response(200, "", null)),
                hasNoViolations());
    }

    @Test
    public void dontAllowMixSecuritySchemas() throws Exception {
        assertRequestViolationsThat(test(
                global,
                get("/sec12").header("AuthorizationOpt", "1").header("Authorization2", "2"),
                response(200, "", null)),
                either(is(equalTo("Header 'AuthorizationOpt' on action(GET /sec12) is not defined")))
                        .or(is(equalTo("Header 'Authorization2' on action(GET /sec12) is not defined")))
        );
    }

    @Test
    public void dontEliminateUniqueSecurityScheme() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/uniqueSec").header("AuthorizationOpt", "blu"),
                response(200, "", null)),
                equalTo("Header 'AuthorizationReq' on action(GET /uniqueSec) is required but not found"));
    }

    @Test
    public void showAmbiguousSecurityResolutionWithNull() throws Exception {
        final RamlReport report = test(
                local,
                get("/optSec").header("AuthorizationOpt", "blu"),
                response(200, "", null));
        assertViolationsThat(report.getRequestViolations(),
                equalTo("Assuming security scheme 'null': Header 'AuthorizationOpt' on action(GET /optSec) is not defined"),
                equalTo("Assuming security scheme 'x-other': Header 'AuthorizationReq' on action(GET /optSec) is required but not found"));
    }

    @Test
    public void showAmbiguousSecurityResolution() throws Exception {
        final RamlReport report = test(
                local,
                get("/doubleSec").header("AuthorizationOpt", "blu"),
                response(200, "", null));
        assertViolationsThat(report.getRequestViolations(),
                equalTo("Assuming security scheme 'OAuth 2.0': Header 'AuthorizationOpt' on action(GET /doubleSec) is not defined"),
                equalTo("Assuming security scheme 'x-other': Header 'AuthorizationReq' on action(GET /doubleSec) is required but not found"));
    }

    @Test
    public void showOnlyBestSecurityResolution() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/doubleSec?access_token=a").header("Authorization2", "blu").header("AuthorizationReq", "s"),
                response(200, "", null)),
                equalTo("Assuming security scheme 'OAuth 2.0': Header 'AuthorizationReq' on action(GET /doubleSec) is not defined"));
    }

    @Test
    public void allowSecurityElementsInLocalSecured() throws Exception {
        assertThat(test(local, get("/sec?access_token=bla").header("Authorization2", "blu"), response(401, "", null)),
                hasNoViolations());
    }

    @Test
    public void dontAllowSecurityHeaderInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/unsec").header("Authorization2", "blu"),
                response(200, "", null)),
                equalTo("Header 'Authorization2' on action(GET /unsec) is not defined"));
    }

    @Test
    public void dontAllowSecurityQueryInUnsecured() throws Exception {
        assertOneRequestViolationThat(test(
                local,
                get("/unsec?access_token=bla"),
                response(200, "", null)),
                equalTo("Query parameter 'access_token' on action(GET /unsec) is not defined"));
    }

    @Test
    public void allowSecurityWithoutDescribedBy() throws Exception {
        assertThat(test(global, get("/undesc"), response(200, "", null)),
                hasNoViolations());
    }

    @Test
    public void dontAllowWrongSecurityType() {
        try {
            base.load("security-wrong-type.raml");
            fail();
        } catch (RamlViolationException e) {
            assertOneViolationThat(e.getReport().getValidationViolations(),
                    equalTo("Exception during RAML check: Invalid element wrong. -- security-wrong-type.raml [line=8, col=11]"));
        }
    }

    @Test
    public void undefinedSchema() throws Exception {
        try {
            base.load("undefined-security.raml");
            fail();
        } catch (RamlViolationException e) {
            assertViolationsThat(e.getReport().getValidationViolations(),
                    equalTo("Exception during RAML check: Invalid reference 'b' -- undefined-security.raml [line=6, col=13]"),
                    equalTo("Exception during RAML check: Invalid reference 'c' -- undefined-security.raml [line=9, col=15]"),
                    equalTo("Exception during RAML check: Invalid reference 'd' -- undefined-security.raml [line=11, col=17]"));
        }
    }

}
