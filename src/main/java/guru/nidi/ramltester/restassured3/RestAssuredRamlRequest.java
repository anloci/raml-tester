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
package guru.nidi.ramltester.restassured3;

import guru.nidi.ramltester.core.RamlCheckerException;
import guru.nidi.ramltester.model.RamlRequest;
import guru.nidi.ramltester.model.Values;
import io.restassured.specification.FilterableRequestSpecification;

class RestAssuredRamlRequest extends RestAssuredRamlMessage implements RamlRequest {
    private final FilterableRequestSpecification requestSpec;

    RestAssuredRamlRequest(FilterableRequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    @Override
    public Values getHeaderValues() {
        return headersToValues(requestSpec.getHeaders());
    }

    @Override
    public String getContentType() {
        return requestSpec.getContentType();
    }

    @Override
    public byte[] getContent() {
        final Object body = requestSpec.getBody();
        if (body == null) {
            return null;
        }
        if (body instanceof String) {
            return ((String) body).getBytes();
        }
        if (body instanceof byte[]) {
            return (byte[]) body;
        }
        throw new RamlCheckerException("Cannot process body of type " + body.getClass());
    }

    @Override
    public String getRequestUrl(String baseUri, boolean includeServletPath) {
        return baseUri == null || baseUri.length() == 0
                ? requestSpec.getURI()
                : requestSpec.getURI().replace(requestSpec.getBaseUri(), baseUri);
    }

    @Override
    public String getMethod() {
        return requestSpec.getMethod();
    }

    @Override
    public Values getQueryValues() {
        return mapToValues(requestSpec.getQueryParams());
    }

    @Override
    public Values getFormValues() {
        return mapToValues(requestSpec.getFormParams());
    }
}
