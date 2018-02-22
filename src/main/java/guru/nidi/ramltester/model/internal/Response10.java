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
package guru.nidi.ramltester.model.internal;

import org.raml.v2.api.model.v10.bodies.Response;

import java.util.ArrayList;
import java.util.List;

class Response10 implements RamlApiResponse {
    private final Response response;

    Response10(Response response) {
        this.response = response;
    }

    static List<RamlApiResponse> of(List<Response> responses) {
        final List<RamlApiResponse> res = new ArrayList<>();
        for (final Response r : responses) {
            res.add(new Response10(r));
        }
        return res;
    }

    @Override
    public String description() {
        return response.description() == null ? null : response.description().value();
    }

    @Override
    public String code() {
        return response.code().value();
    }

    @Override
    public List<RamlType> headers() {
        return Type10.of(response.headers());
    }

    @Override
    public List<RamlBody> body() {
        return Body10.of(response.body());
    }
}
