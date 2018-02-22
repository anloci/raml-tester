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

import org.raml.v2.api.model.v08.security.SecuritySchemeRef;

import java.util.ArrayList;
import java.util.List;

class SecSchemeRef08 implements RamlSecSchemeRef {
    private final SecuritySchemeRef ref;

    SecSchemeRef08(SecuritySchemeRef ref) {
        this.ref = ref;
    }

    static List<RamlSecSchemeRef> of(List<SecuritySchemeRef> refs) {
        final List<RamlSecSchemeRef> res = new ArrayList<>();
        for (final SecuritySchemeRef r : refs) {
            res.add(new SecSchemeRef08(r));
        }
        return res;
    }

    @Override
    public RamlSecScheme securityScheme() {
        return ref == null ? null : new SecScheme08(ref.securityScheme());
    }
}
