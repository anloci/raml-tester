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
package guru.nidi.ramltester.restassured;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.model.RamlResponse;
import guru.nidi.ramltester.model.Values;

class RestAssuredRamlResponse extends RestAssuredRamlMessage implements RamlResponse {
    private final Response response;

    RestAssuredRamlResponse(Response response) {
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.statusCode();
    }

    @Override
    public Values getHeaderValues() {
        return headersToValues(response.getHeaders());
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public byte[] getContent() {
        return response.getBody().asByteArray();
    }
}