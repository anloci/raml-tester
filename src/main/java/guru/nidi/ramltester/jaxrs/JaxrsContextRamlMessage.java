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
package guru.nidi.ramltester.jaxrs;

import guru.nidi.ramltester.model.Values;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

class JaxrsContextRamlMessage {
    Values headersOf(MultivaluedMap<String, ?> headers) {
        final Values values = new Values();
        for (final Map.Entry<String, ? extends List<?>> entry : headers.entrySet()) {
            values.addValues(entry.getKey(), entry.getValue());
        }
        return values;
    }
}
